package com.chainmaker.jobservice.core.calcite.cost;

import org.apache.calcite.rel.metadata.RelMdSelectivity;

/**
 * 根据Expression估算系数，用于替换默认的RelMdSelectivity
 */
public class MPCRelMdSelectivity extends RelMdSelectivity {

}
