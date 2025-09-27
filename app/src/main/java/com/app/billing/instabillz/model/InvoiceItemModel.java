package com.app.billing.instabillz.model;

public class InvoiceItemModel {

    String name;
    String code;
    Integer units;
    Double unitPrice;
    Double totalPrice;
    Double sellingItemPrice;
    ProductModel productModel;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public Integer getUnits() {
        return units;
    }

    public void setUnits(Integer units) {
        this.units = units;
    }

    public Double getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(Double unitPrice) {
        this.unitPrice = unitPrice;
    }

    public Double getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(Double totalPrice) {
        this.totalPrice = totalPrice;
    }

    public Double getSellingItemPrice() {
        return sellingItemPrice;
    }

    public void setSellingItemPrice(Double sellingItemPrice) {
        this.sellingItemPrice = sellingItemPrice;
    }

    public ProductModel getProductModel() {
        return productModel;
    }

    public void setProductModel(ProductModel productModel) {
        this.productModel = productModel;
    }
}
