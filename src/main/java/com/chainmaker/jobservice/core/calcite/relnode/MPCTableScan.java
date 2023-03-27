package com.chainmaker.jobservice.core.calcite.relnode;

import com.chainmaker.jobservice.core.calcite.cost.MPCCost;
import com.google.common.collect.ImmutableList;
import org.apache.calcite.adapter.enumerable.*;
import org.apache.calcite.adapter.java.JavaTypeFactory;
import org.apache.calcite.interpreter.Row;
import org.apache.calcite.linq4j.Enumerable;
import org.apache.calcite.linq4j.Queryable;
import org.apache.calcite.linq4j.function.Function1;
import org.apache.calcite.linq4j.tree.*;
import org.apache.calcite.plan.*;
import org.apache.calcite.rel.RelCollationTraitDef;
import org.apache.calcite.rel.RelNode;
import org.apache.calcite.rel.core.TableScan;
import org.apache.calcite.rel.metadata.RelMetadataQuery;
import org.apache.calcite.rel.type.RelDataType;
import org.apache.calcite.rel.type.RelDataTypeField;
import org.apache.calcite.schema.*;
import org.apache.calcite.sql.SqlExplainLevel;
import org.apache.calcite.util.BuiltInMethod;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import static java.util.Objects.requireNonNull;
import static org.apache.calcite.linq4j.tree.Types.toClass;

/**
 * MPCTableScan类，仿照EnumerableTableScan类设计，具有EnumerableConvention物理属性
 * 查询优化时会将LogicalTableScan转换为MPCTableScan
 */
public class MPCTableScan extends TableScan implements EnumerableRel {
    private final Class elementType;
    private RelOptCost cost;
    private double rowCount;

    public MPCTableScan(RelOptCluster cluster, RelTraitSet traitSet,
                        RelOptTable table, Class elementType, double rowCount) {
        super(cluster, traitSet, table);
        this.elementType = elementType;
        this.rowCount = rowCount;
    }

    public static MPCTableScan create(RelOptCluster cluster, RelOptTable relOptTable, double rowCount) {
        final Table table = relOptTable.unwrap(Table.class);
        Class elementType = EnumerableTableScan.deduceElementType(table);
        final RelTraitSet traitSet =
                cluster.traitSetOf(EnumerableConvention.INSTANCE).replaceIfs(RelCollationTraitDef.INSTANCE, () -> {
                    if (table != null) {
                        return table.getStatistic().getCollations();
                    }
                    return ImmutableList.of();
                });
        return new MPCTableScan(cluster, traitSet, relOptTable, elementType, rowCount);
    }

    @Override
    public double estimateRowCount(RelMetadataQuery mq) {
        return rowCount;
    }

    @Override
    public RelOptCost computeSelfCost(RelOptPlanner planner, RelMetadataQuery mq) {
        double dRows = rowCount;
        double dCpu = dRows;
        double dIo = dRows;

        cost = planner.getCostFactory().makeCost(dRows, dCpu, dIo);
        return cost;
    }

    /** 以下内容均是为了该类能正常工作所完成的，与项目本身没有多少关系，不用在意其意义 **/

    private Expression getExpression(PhysType physType) {
        final Expression expression = table.getExpression(Queryable.class);
        if (expression == null) {
            throw new IllegalStateException(
                    "Unable to implement " + RelOptUtil.toString(this, SqlExplainLevel.ALL_ATTRIBUTES)
                            + ": " + table + ".getExpression(Queryable.class) returned null");
        }
        final Expression expression2 = toEnumerable(expression);
        assert Types.isAssignableFrom(Enumerable.class, expression2.getType());
        return toRows(physType, expression2);
    }

    private static Expression toEnumerable(Expression expression) {
        final Type type = expression.getType();
        if (Types.isArray(type)) {
            if (requireNonNull(toClass(type).getComponentType()).isPrimitive()) {
                expression =
                        Expressions.call(BuiltInMethod.AS_LIST.method, expression);
            }
            return Expressions.call(BuiltInMethod.AS_ENUMERABLE.method, expression);
        } else if (Types.isAssignableFrom(Iterable.class, type)
                && !Types.isAssignableFrom(Enumerable.class, type)) {
            return Expressions.call(BuiltInMethod.AS_ENUMERABLE2.method,
                    expression);
        } else if (Types.isAssignableFrom(Queryable.class, type)) {
            // Queryable extends Enumerable, but it's too "clever", so we call
            // Queryable.asEnumerable so that operations such as take(int) will be
            // evaluated directly.
            return Expressions.call(expression,
                    BuiltInMethod.QUERYABLE_AS_ENUMERABLE.method);
        }
        return expression;
    }

    private Expression toRows(PhysType physType, Expression expression) {
        if (physType.getFormat() == JavaRowFormat.SCALAR
                && Object[].class.isAssignableFrom(elementType)
                && getRowType().getFieldCount() == 1
                && (table.unwrap(ScannableTable.class) != null
                || table.unwrap(FilterableTable.class) != null
                || table.unwrap(ProjectableFilterableTable.class) != null)) {
            return Expressions.call(BuiltInMethod.SLICE0.method, expression);
        }
        JavaRowFormat oldFormat = format();
        if (physType.getFormat() == oldFormat && !hasCollectionField(getRowType())) {
            return expression;
        }
        final ParameterExpression row_ =
                Expressions.parameter(elementType, "row");
        final int fieldCount = table.getRowType().getFieldCount();
        List<Expression> expressionList = new ArrayList<>(fieldCount);
        for (int i = 0; i < fieldCount; i++) {
            expressionList.add(fieldExpression(row_, i, physType, oldFormat));
        }
        return Expressions.call(expression,
                BuiltInMethod.SELECT.method,
                Expressions.lambda(Function1.class, physType.record(expressionList),
                        row_));
    }

    private Expression fieldExpression(ParameterExpression row_, int i,
                                       PhysType physType, JavaRowFormat format) {
        final Expression e =
                format.field(row_, i, null, physType.getJavaFieldType(i));
        final RelDataType relFieldType =
                physType.getRowType().getFieldList().get(i).getType();
        switch (relFieldType.getSqlTypeName()) {
            case ARRAY:
            case MULTISET:
                final RelDataType fieldType = requireNonNull(relFieldType.getComponentType(),
                        () -> "relFieldType.getComponentType() for " + relFieldType);
                if (fieldType.isStruct()) {
                    // We can't represent a multiset or array as a List<Employee>, because
                    // the consumer does not know the element type.
                    // The standard element type is List.
                    // We need to convert to a List<List>.
                    final JavaTypeFactory typeFactory =
                            (JavaTypeFactory) getCluster().getTypeFactory();
                    final PhysType elementPhysType = PhysTypeImpl.of(
                            typeFactory, fieldType, JavaRowFormat.CUSTOM);
                    final MethodCallExpression e2 =
                            Expressions.call(BuiltInMethod.AS_ENUMERABLE2.method, e);
                    final Expression e3 = elementPhysType.convertTo(e2, JavaRowFormat.LIST);
                    return Expressions.call(e3, BuiltInMethod.ENUMERABLE_TO_LIST.method);
                } else {
                    return e;
                }
            default:
                return e;
        }
    }

    private JavaRowFormat format() {
        int fieldCount = getRowType().getFieldCount();
        if (fieldCount == 0) {
            return JavaRowFormat.LIST;
        }
        if (Object[].class.isAssignableFrom(elementType)) {
            return fieldCount == 1 ? JavaRowFormat.SCALAR : JavaRowFormat.ARRAY;
        }
        if (Row.class.isAssignableFrom(elementType)) {
            return JavaRowFormat.ROW;
        }
        if (fieldCount == 1 && (Object.class == elementType
                || Primitive.is(elementType)
                || Number.class.isAssignableFrom(elementType)
                || String.class == elementType)) {
            return JavaRowFormat.SCALAR;
        }
        return JavaRowFormat.CUSTOM;
    }

    private static boolean hasCollectionField(RelDataType rowType) {
        for (RelDataTypeField field : rowType.getFieldList()) {
            switch (field.getType().getSqlTypeName()) {
                case ARRAY:
                case MULTISET:
                    return true;
                default:
                    break;
            }
        }
        return false;
    }

    @Override public RelNode copy(RelTraitSet traitSet, List<RelNode> inputs) {
        return new EnumerableTableScan(getCluster(), traitSet, table, elementType);
    }

    @Override
    public Result implement(EnumerableRelImplementor implementor, Prefer prefer) {
        final PhysType physType =
                PhysTypeImpl.of(
                        implementor.getTypeFactory(),
                        getRowType(),
                        format());
        final Expression expression = getExpression(physType);
        return implementor.result(physType, Blocks.toBlock(expression));
    }
}