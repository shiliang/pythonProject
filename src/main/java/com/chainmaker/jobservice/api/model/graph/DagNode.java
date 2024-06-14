package com.chainmaker.jobservice.api.model.graph;


import lombok.Data;

@Data
public class DagNode {

    /**
     * 节点ID
     */
    private String id;
    /**
     * 任务名称
     */
    private String label;
    /**
     * image
     */
    private String shape;
    /**
     * 图片名称
     */
    private String image;
    /**
     * 关联任务
     */
    private String findLabel;

    @Override
    public String toString() {
        return "DagNode{" +
                "id=" + id +
                ", label='" + label + '\'' +
                ", shape='" + shape + '\'' +
                ", image='" + image + '\'' +
                ", findLabel='" + findLabel + '\'' +
                '}';
    }
}
