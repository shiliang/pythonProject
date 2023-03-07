package com.chainmaker.jobservice.api.model.bo.graph;

public class DagNode {

    /**
     * 节点ID
     */
    private int id;
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

    public String getFindLabel() {
        return findLabel;
    }

    public void setFindLabel(String findLabel) {
        this.findLabel = findLabel;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getShape() {
        return shape;
    }

    public String getImage() {
        return image;
    }

    public void setShape(String shape) {
        this.shape = shape;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public int getId() {
        return id;
    }

    public String getLabel() {
        return label;
    }

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
