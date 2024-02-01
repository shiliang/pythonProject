package com.chainmaker.backendservice.model.dto;

public class PayTaxInfo {
    private String id;

    private String company_id;

    private String name;

    private int year;

    private String level;

    private String amount;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCompany_id() {
        return company_id;
    }

    public void setCompany_id(String company_id) {
        this.company_id = company_id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    @Override
    public String toString() {
        return "PayTaxPO{" +
                "id='" + id + '\'' +
                ", company_id='" + company_id + '\'' +
                ", name='" + name + '\'' +
                ", year=" + year +
                ", level='" + level + '\'' +
                ", amount=" + amount +
                '}';
    }
}
