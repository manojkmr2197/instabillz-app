package com.app.billing.instabillz.model;

public class ScanCartModel {
    String name;
    String barcode;
    int qty;
    double price;

    public ScanCartModel(String name, String barcode, double price) {
        this.name = name;
        this.barcode = barcode;
        this.price = price;
        this.qty = 1;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getBarcode() {
        return barcode;
    }

    public void setBarcode(String barcode) {
        this.barcode = barcode;
    }

    public int getQty() {
        return qty;
    }

    public void setQty(int qty) {
        this.qty = qty;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }
}
