package com.app.billing.instabillz.utils;

import com.google.firebase.firestore.FirebaseFirestoreException;

import java.util.ArrayList;
import java.util.List;

public class AutoIndexHelper {

    private static final List<String> pendingIndexUrls = new ArrayList<>();

    public static void collectIndexError(Exception e) {
        if (e instanceof FirebaseFirestoreException) {
            FirebaseFirestoreException fex = (FirebaseFirestoreException) e;
            if(fex.getCode() == FirebaseFirestoreException.Code.FAILED_PRECONDITION){

                String url = extractIndexUrl(fex.getMessage());
                if (url != null && !pendingIndexUrls.contains(url)) {
                    pendingIndexUrls.add(url);
                }
            }
        }
    }

    private static String extractIndexUrl(String message) {
        if (message == null) return null;

        System.out.println("INDEX----->"+message);
        int start = message.indexOf("https://");
        int end = message.length()-1;

        if (start != -1 && end != -1)
            return message.substring(start, end);

        return null;
    }

    public static List<String> getPendingIndexUrls() {
        return pendingIndexUrls;
    }

    public static void clear() {
        pendingIndexUrls.clear();
    }
}



