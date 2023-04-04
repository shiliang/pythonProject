package com.chainmaker.jobservice.core.calcite.cost;

import org.apache.calcite.rel.metadata.*;
import org.apache.calcite.util.BuiltInMethod;

import static org.apache.calcite.rel.metadata.BuiltInMetadata.*;

/**
 * 根据Expression估算系数，用于替换默认的RelMdSelectivity
 */
public class MPCRelMdSelectivity extends RelMdSelectivity {

    public MetadataDef<Selectivity> getDef() {
        return BuiltInMetadata.Selectivity.DEF;
    }

    public static final RelMetadataProvider SOURCE =
            ReflectiveRelMetadataProvider.reflectiveSource(BuiltInMethod.SELECTIVITY.method, new MPCRelMdSelectivity());

}
