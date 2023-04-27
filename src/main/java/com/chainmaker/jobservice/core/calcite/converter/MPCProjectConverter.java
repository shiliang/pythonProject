package com.chainmaker.jobservice.core.calcite.converter;

import com.chainmaker.jobservice.core.calcite.relnode.MPCProject;
import org.apache.calcite.adapter.enumerable.EnumerableConvention;
import org.apache.calcite.adapter.enumerable.EnumerableProject;
import org.apache.calcite.plan.Convention;
import org.apache.calcite.rel.RelNode;
import org.apache.calcite.rel.convert.ConverterRule;
import org.apache.calcite.rel.core.Project;
import org.apache.calcite.rel.logical.LogicalFilter;
import org.apache.calcite.rel.logical.LogicalProject;

/**
 * Project的转换规则
 * 将LogicProject（逻辑节点）转换成MPCProject（带物理属性的节点）
 * 用于后续的查询优化
 */
public class MPCProjectConverter extends ConverterRule {

    public static final MPCProjectConverter INSTANCE = new MPCProjectConverter(
            LogicalProject.class,
            Convention.NONE,
            EnumerableConvention.INSTANCE,
            "MPCProject"
    );

    public MPCProjectConverter(Class<LogicalProject> logicalProjectClass, Convention none, EnumerableConvention instance, String description) {
        super(logicalProjectClass, none, instance, description);
    }

    @Override
    public RelNode convert(RelNode relNode) {

        final Project project = (Project) relNode;
        return MPCProject.create(
                convert(project.getInput(),
                        project.getInput().getTraitSet()
                                .replace(EnumerableConvention.INSTANCE)),
                project.getProjects(),
                project.getRowType());
    }
}
