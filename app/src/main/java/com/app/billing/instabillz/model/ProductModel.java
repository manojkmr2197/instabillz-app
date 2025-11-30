package com.app.billing.instabillz.model;

public class ProductModel {

    private String id;
    private String name;
    private Double price;
    private int qty;
    private String categoryId;
    private String categoryName;
    private String code;

    public ProductModel() {
    }

    public ProductModel(String name, Double price) {
        this.name = name;
        this.price = price;
    }

    public ProductModel(String name, Double price,String categoryName) {
        this.name = name;
        this.price = price;
        this.categoryName = categoryName;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public String getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(String categoryId) {
        this.categoryId = categoryId;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public int getQty() {
        return qty;
    }

    public void setQty(int qty) {
        this.qty = qty;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }
}
