package com.app.billing.instabillz.listener;

import com.app.billing.instabillz.model.ProductApiResponse;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

public interface ApiService {

    @GET("api/v0/product/{barcode}.json")
    Call<ProductApiResponse> getProduct(
            @Path("barcode") String barcode
    );
}

