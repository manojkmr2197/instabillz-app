package com.app.billing.instabillz.listener;

public interface FirestoreCallback<T> {
    void onCallback(T result);
}
