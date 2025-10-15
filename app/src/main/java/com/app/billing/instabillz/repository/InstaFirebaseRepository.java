package com.app.billing.instabillz.repository;

import com.app.billing.instabillz.constants.AppConstants;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.Map;

public class InstaFirebaseRepository {
    private static InstaFirebaseRepository instance;
    private final FirebaseFirestore db;

    // Private constructor -> prevents direct instantiation
    private InstaFirebaseRepository() {
        db = FirebaseFirestore.getInstance();
    }

    // Singleton instance
    public static synchronized InstaFirebaseRepository getInstance() {
        if (instance == null) {
            instance = new InstaFirebaseRepository();
        }
        return instance;
    }

    // Save method
    public void addDataBase(String collectionName,String docId, Object saveObject, OnFirebaseWriteListener listener) {

        db.collection(collectionName)
                .document(docId)
                .set(saveObject)
                .addOnSuccessListener(documentReference -> {
                    if (listener != null) {
                        listener.onSuccess(documentReference);
                    }
                })
                .addOnFailureListener(e -> {
                    if (listener != null) {
                        listener.onFailure(e);
                    }
                });
    }


    public void getAllDetails(String collectionName,String orderByColumn,Query.Direction direction, OnFirebaseWriteListener listener) {

        db.collection(collectionName)
                .orderBy(orderByColumn, direction)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (listener != null) {
                        listener.onSuccess(queryDocumentSnapshots);
                    }
                })
                .addOnFailureListener(e -> {
                    if (listener != null) {
                        listener.onFailure(e);
                    }
                });
    }

    public void deleteData(String collectionName, String docId, OnFirebaseWriteListener listener) {
        db.collection(collectionName)
                .document(docId)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    if (listener != null) {
                        listener.onSuccess(null);
                    }
                })
                .addOnFailureListener(e -> {
                    if (listener != null) {
                        listener.onFailure(e);
                    }
                });
    }

    public void getDetailsByDocumentId(String collectionName, String documentId, OnFirebaseWriteListener listener) {
        db.collection(collectionName)
                .document(documentId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (listener != null) {
                        listener.onSuccess(documentSnapshot);
                    }
                })
                .addOnFailureListener(e -> {
                    if (listener != null) {
                        listener.onFailure(e);
                    }
                });
    }

    public void getDocumentsByField(String collectionName, String fieldName, String fieldValue, OnFirebaseWriteListener listener) {
        db.collection(collectionName)
                .whereEqualTo(fieldName, fieldValue)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        if (listener != null) {
                            listener.onSuccess(queryDocumentSnapshots);
                        }
                    } else {
                        if (listener != null) {
                            listener.onFailure(new Exception("No documents found for " + fieldName + " = " + fieldValue));
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    if (listener != null) {
                        listener.onFailure(e);
                    }
                });
    }


    public void updateData(String collection, String docId, Map<String, Object> data, OnFirebaseWriteListener listener) {
        db.collection(collection)
                .document(docId)
                .update(data)
                .addOnSuccessListener(aVoid -> {
                    if (listener != null) listener.onSuccess(null);
                })
                .addOnFailureListener(e -> {
                    if (listener != null) listener.onFailure(e);
                });
    }

    public void userLogin( String phone, String passcode,OnFirebaseWriteListener listener) {
        db.collection(AppConstants.APP_NAME + AppConstants.EMPLOYEE_COLLECTION)
                .whereEqualTo("phone", phone)
                .whereEqualTo("passcode", passcode)
                .get()
                .addOnSuccessListener(querySnapshot  -> {
                    if (listener != null) {
                        listener.onSuccess(querySnapshot);
                    }
                })
                .addOnFailureListener(e -> {
                    if (listener != null) {
                        listener.onFailure(e);
                    }
                });
    }

    public void getLatestToken(OnFirebaseWriteListener listener) {
        db.collection(AppConstants.APP_NAME + AppConstants.SALES_COLLECTION)
                .orderBy("billingDate", Query.Direction.DESCENDING)
                .limit(1) // 🔹 only take the latest one
                .get()
                .addOnSuccessListener(querySnapshot  -> {
                    if (listener != null) {
                        listener.onSuccess(querySnapshot);
                    }
                })
                .addOnFailureListener(e -> {
                    if (listener != null) {
                        listener.onFailure(e);
                    }
                });
    }


    // Callback interface
    public interface OnFirebaseWriteListener {
        void onSuccess(Object data);
        void onFailure(Exception e);
    }
}

