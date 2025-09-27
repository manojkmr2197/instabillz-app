package com.app.billing.instabillz.model;

import java.util.List;

public class InvoiceModel {

    Long billingDate;
    Double totalCost;
    Double sellingCost;
    Double discount;
    String employeeName;
    String employeePhone;
    String paymentMode;
    String upiPaymentStatus;
    Boolean isPrint;
    List<ProductModel> productModelList;

    public Long getBillingDate() {
        return billingDate;
    }

    public void setBillingDate(Long billingDate) {
        this.billingDate = billingDate;
    }

    public Double getTotalCost() {
        return totalCost;
    }

    public void setTotalCost(Double totalCost) {
        this.totalCost = totalCost;
    }

    public Double getSellingCost() {
        return sellingCost;
    }

    public void setSellingCost(Double sellingCost) {
        this.sellingCost = sellingCost;
    }

    public Double getDiscount() {
        return discount;
    }

    public void setDiscount(Double discount) {
        this.discount = discount;
    }

    public String getEmployeeName() {
        return employeeName;
    }

    public void setEmployeeName(String employeeName) {
        this.employeeName = employeeName;
    }

    public String getEmployeePhone() {
        return employeePhone;
    }

    public void setEmployeePhone(String employeePhone) {
        this.employeePhone = employeePhone;
    }

    public String getPaymentMode() {
        return paymentMode;
    }

    public void setPaymentMode(String paymentMode) {
        this.paymentMode = paymentMode;
    }

    public String getUpiPaymentStatus() {
        return upiPaymentStatus;
    }

    public void setUpiPaymentStatus(String upiPaymentStatus) {
        this.upiPaymentStatus = upiPaymentStatus;
    }

    public Boolean getPrint() {
        return isPrint;
    }

    public void setPrint(Boolean print) {
        isPrint = print;
    }

    public List<ProductModel> getProductModelList() {
        return productModelList;
    }

    public void setProductModelList(List<ProductModel> productModelList) {
        this.productModelList = productModelList;
    }
}
