package com.app.billing.instabillz.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.widget.ImageView;

import androidx.annotation.Nullable;

import org.apache.commons.lang3.StringUtils;

import java.io.InputStream;

public class ImageLoader {

    private static final String[] SUPPORTED_EXT = {
            "png", "jpg", "jpeg", "webp"
    };

    public static void loadBrandLogo(Context context, ImageView imageView) {

        String filePath = getAppLogoPath(context);

        if (filePath != null) {
            loadImage(context, imageView, filePath);
        }
    }

    private static @Nullable String getAppLogoPath(Context context) {
        SharedPrefHelper sharedPrefHelper = new SharedPrefHelper(context);
        String brand = sharedPrefHelper.getAppName();
        if (StringUtils.isBlank(brand)) {
            brand = "default";
        }

        String filePath = findImageFile(context, brand);

        if (filePath == null) {
            // fallback to default folder
            filePath = findImageFile(context, "default");
        }
        return filePath;
    }

    private static String findImageFile(Context context, String folder) {

        for (String ext : SUPPORTED_EXT) {
            String candidate = folder + "/client_logo." + ext;
            try {
                InputStream is = context.getAssets().open(candidate);
                if (is != null) {
                    is.close();
                    return candidate; // Found valid file
                }
            } catch (Exception ignored) {
            }
        }
        return null;
    }

    private static void loadImage(Context context, ImageView imageView, String filePath) {
        try {
            InputStream is = context.getAssets().open(filePath);
            Bitmap bitmap = BitmapFactory.decodeStream(is);
            imageView.setImageBitmap(bitmap);
            is.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static Bitmap getBrandLogoBitmap(Context context) {

        String filePath = getAppLogoPath(context);
        if (filePath != null) {
            try {
                InputStream is = context.getAssets().open(filePath);
                Bitmap bmp = BitmapFactory.decodeStream(is);
                is.close();
                return bmp;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }

}
