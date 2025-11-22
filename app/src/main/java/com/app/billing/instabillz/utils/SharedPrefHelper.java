package com.app.billing.instabillz.utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.app.billing.instabillz.constants.SharedConstants;
import com.app.billing.instabillz.model.EmployeeModel;
import com.app.billing.instabillz.model.ShopsModel;
import com.google.gson.Gson;

public class SharedPrefHelper {
    private SharedPreferences sharedPreferences;

    public SharedPrefHelper(Context context) {
        this.sharedPreferences = context.getSharedPreferences(SharedConstants.INSTABILLZ, Context.MODE_PRIVATE);
    }

    public String getSystemUserPhone() {
        return sharedPreferences.getString(SharedConstants.INSTABILLZ_SYSTEM_PHONE, null);
    }

    public String getSystemUserName() {
        return sharedPreferences.getString(SharedConstants.INSTABILLZ_SYSTEM_NAME, null);
    }

    public String getSystemUserRole() {
        return sharedPreferences.getString(SharedConstants.INSTABILLZ_SYSTEM_ROLE, null);
    }

    public void setSystemUserDetails(EmployeeModel model) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(SharedConstants.INSTABILLZ_SYSTEM_PHONE, model.getPhone());
        editor.putString(SharedConstants.INSTABILLZ_SYSTEM_ROLE, model.getRole());
        editor.putString(SharedConstants.INSTABILLZ_SYSTEM_NAME, model.getName());
        editor.apply();
        editor.commit();
    }

    public String getAppName() {
        return sharedPreferences.getString(SharedConstants.INSTABILLZ_APP_NAME, null);
    }

    public void setAppName(String appName) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(SharedConstants.INSTABILLZ_APP_NAME, appName);
        editor.apply();
        editor.commit();
    }

    public void clearAllDetails() {
        sharedPreferences.edit().clear().apply();
    }


    public void setPrinterDetails(ShopsModel shopsModel) {
        SharedPreferences.Editor editor = sharedPreferences.edit();

        Gson gson = new Gson();
        String json = gson.toJson(shopsModel); // ✅ Convert model to JSON string
        editor.putString(SharedConstants.INSTABILLZ_PRINTER_DETAILS, json);

        editor.apply(); // ✅ commit() not needed after apply()
    }

    public ShopsModel getPrinterDetails() {
        Gson gson = new Gson();
        String json = sharedPreferences.getString(SharedConstants.INSTABILLZ_PRINTER_DETAILS, null);

        if (json != null) {
            return gson.fromJson(json, ShopsModel.class); // ✅ Convert JSON to model
        } else {
            return null;
        }
    }


}
