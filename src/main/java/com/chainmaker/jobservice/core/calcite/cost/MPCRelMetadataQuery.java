package com.chainmaker.jobservice.core.calcite.cost;

import org.apache.calcite.rel.RelNode;
import org.apache.calcite.rel.metadata.BuiltInMetadata;
import org.apache.calcite.rel.metadata.JaninoRelMetadataProvider;
import org.apache.calcite.rel.metadata.RelMetadataQuery;

/**
 * 用于在优化过程中提供相关元数据
 */
public class MPCRelMetadataQuery extends RelMetadataQuery {

    public MPCRelMetadataQuery() {
        super();
    }

    public static MPCRelMetadataQuery instance() {
        THREAD_PROVIDERS.set(JaninoRelMetadataProvider.of(MPCRelMetaDataProvider.relMetaDataProvider));
        return new MPCRelMetadataQuery();
    }

    public static final MPCRelMetadataQuery INSTANCE = instance();
}
