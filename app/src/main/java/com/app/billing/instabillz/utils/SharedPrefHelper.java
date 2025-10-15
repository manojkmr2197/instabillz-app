package com.app.billing.instabillz.utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.app.billing.instabillz.constants.SharedConstants;
import com.app.billing.instabillz.model.EmployeeModel;

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

    public void clearAllDetails() {
        sharedPreferences.edit().clear().apply();
    }
}
