package com.app.billing.instabillz.model;


import java.util.Date;
import java.util.List;

public class ShopsModel {

    // ---------- SHOP INFORMATION ----------
    private String id;
    private String shopName;
    private String phoneNumber;
    private String address;
    private String outletType;
    private String billingType;

    // ---------- LOGO ----------
    private String logo;      // firebase download url

    // ---------- SUBSCRIPTION ----------
    private boolean active;
    private String subscriptionDate; // yyyy-MM-dd

    // ---------- PRINTER CONFIG ----------
    private String printerName;
    private String header1;
    private String header2;
    private String header3;
    private String footer1;
    private String footer2;
    private String footer3;

    private List<String> indexUrls;
    private Date onboardingDate;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getShopName() {
        return shopName;
    }

    public void setShopName(String shopName) {
        this.shopName = shopName;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getLogo() {
        return logo;
    }

    public void setLogo(String logo) {
        this.logo = logo;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public String getSubscriptionDate() {
        return subscriptionDate;
    }

    public void setSubscriptionDate(String subscriptionDate) {
        this.subscriptionDate = subscriptionDate;
    }

    public String getPrinterName() {
        return printerName;
    }

    public void setPrinterName(String printerName) {
        this.printerName = printerName;
    }

    public String getHeader1() {
        return header1;
    }

    public void setHeader1(String header1) {
        this.header1 = header1;
    }

    public String getHeader2() {
        return header2;
    }

    public void setHeader2(String header2) {
        this.header2 = header2;
    }

    public String getHeader3() {
        return header3;
    }

    public void setHeader3(String header3) {
        this.header3 = header3;
    }

    public String getFooter1() {
        return footer1;
    }

    public void setFooter1(String footer1) {
        this.footer1 = footer1;
    }

    public String getFooter2() {
        return footer2;
    }

    public void setFooter2(String footer2) {
        this.footer2 = footer2;
    }

    public String getFooter3() {
        return footer3;
    }

    public void setFooter3(String footer3) {
        this.footer3 = footer3;
    }

    public List<String> getIndexUrls() {
        return indexUrls;
    }

    public void setIndexUrls(List<String> indexUrls) {
        this.indexUrls = indexUrls;
    }

    public Date getOnboardingDate() {
        return onboardingDate;
    }

    public void setOnboardingDate(Date onboardingDate) {
        this.onboardingDate = onboardingDate;
    }

    public String getOutletType() {
        return outletType;
    }

    public void setOutletType(String outletType) {
        this.outletType = outletType;
    }

    public String getBillingType() {
        return billingType;
    }

    public void setBillingType(String billingType) {
        this.billingType = billingType;
    }
}

