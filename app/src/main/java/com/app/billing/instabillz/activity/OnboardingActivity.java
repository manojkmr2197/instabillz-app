package com.app.billing.instabillz.activity;

import static java.lang.Thread.sleep;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.app.billing.instabillz.R;
import com.app.billing.instabillz.constants.AppConstants;
import com.app.billing.instabillz.model.EmployeeModel;
import com.app.billing.instabillz.model.ShopsModel;
import com.app.billing.instabillz.repository.InstaFirebaseRepository;
import com.app.billing.instabillz.utils.AutoIndexHelper;
import com.app.billing.instabillz.utils.FileUtils;
import com.app.billing.instabillz.utils.SingleTon;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class OnboardingActivity extends AppCompatActivity {

    EditText edtShopName, edtPhone, edtAddress;
    ImageView imgPreview;
    TextView txtFileName, txtSelectedDate;

    Uri selectedImageUri = null;

    private final int PICK_IMAGE = 100;

    Context context;
    Activity activity;

    private AlertDialog loaderDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_onboarding);
        if (AppCompatDelegate.getDefaultNightMode() != AppCompatDelegate.MODE_NIGHT_NO) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }
        if (Build.VERSION.SDK_INT >= 21) {
            Window window = this.getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.setStatusBarColor(getResources().getColor(R.color.status_bar_color, getTheme()));
            window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }

        context = OnboardingActivity.this;
        activity = OnboardingActivity.this;

        edtShopName = findViewById(R.id.edtShopName);
        edtPhone = findViewById(R.id.edtPhone);
        edtAddress = findViewById(R.id.edtAddress);

        imgPreview = findViewById(R.id.imgPreview);
        txtFileName = findViewById(R.id.txtFileName);

        txtSelectedDate = findViewById(R.id.txtSelectedDate);

        findViewById(R.id.btnChooseLogo).setOnClickListener(v -> openImagePicker());
        findViewById(R.id.btnPickDate).setOnClickListener(v -> openDatePicker());
        findViewById(R.id.btnSubmit).setOnClickListener(v -> submitForm());
    }


    // ----------------- IMAGE PICKER -------------------
    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, PICK_IMAGE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE && resultCode == RESULT_OK) {
            selectedImageUri = data.getData();
            imgPreview.setImageURI(selectedImageUri);

            File file = new File(getPathFromURI(selectedImageUri));
            txtFileName.setText(file.getName());
        }
    }

    private String getPathFromURI(Uri uri) {
        return FileUtils.getPath(this, uri);  // Use your file util
    }


    // ---------------- DATE PICKER --------------------
    private void openDatePicker() {
        Calendar c = Calendar.getInstance();

        DatePickerDialog dialog = new DatePickerDialog(this,
                (view, year, month, dayOfMonth) -> {
                    String selected = dayOfMonth + "/" + (month + 1) + "/" + year;
                    txtSelectedDate.setText(selected);
                },
                c.get(Calendar.YEAR),
                c.get(Calendar.MONTH),
                c.get(Calendar.DAY_OF_MONTH));

        dialog.getDatePicker().setMinDate(System.currentTimeMillis());
        dialog.show();
    }


    // ---------------- SUBMIT FORM --------------------
    private void submitForm() {

        //if (selectedImageUri != null) {
        if (validateFields()) uploadImageToFirebase();
        //} else {
        //Toast.makeText(this, "Please select logo image", Toast.LENGTH_SHORT).show();
        //}

    }

    private void uploadImageToFirebase() {

        String fileName = "shop_logos/" + System.currentTimeMillis() + ".jpg";
        createOnboardingJson(fileName);
//        FirebaseStorage storage = FirebaseStorage.getInstance();
//        StorageReference storageRef = storage.getReference().child(fileName);
//
//        UploadTask uploadTask = storageRef.putFile(selectedImageUri);
//
//        uploadTask
//                .addOnSuccessListener(taskSnapshot ->
//                        storageRef.getDownloadUrl().addOnSuccessListener(uri -> {
//
//                            String imageUrl = uri.toString();
//                            createOnboardingJson(imageUrl);
//
//                        }))
//                .addOnFailureListener(e -> {
//                    Toast.makeText(this, "Upload failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
//                });
    }

    private void createOnboardingJson(String imageUrl) {

        try {
            ShopsModel shopsModel = createOnboardingModel(imageUrl);
            InstaFirebaseRepository.getInstance().getDetailsByDocumentId(AppConstants.SHOP_COLLECTION, shopsModel.getId(), new InstaFirebaseRepository.OnFirebaseWriteListener() {
                @Override
                public void onSuccess(Object data) {
                    DocumentSnapshot doc = (DocumentSnapshot) data;
                    if (doc.exists()) {

                        // Shop already exists
                        Toast.makeText(context,
                                "Shop ID already exists! Choose another name.",
                                Toast.LENGTH_LONG).show();
                    } else {

                        // ID not found -> proceed to add data
                        addNewShop(shopsModel.getId(), shopsModel);
                    }
                }

                @Override
                public void onFailure(Exception e) {

                }
            });

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void addNewShop(String id, ShopsModel shopsModel) {
        // You can send this JSON to backend or Firebase realtime DB
        InstaFirebaseRepository.getInstance().addDataBase(AppConstants.SHOP_COLLECTION, id, shopsModel, new InstaFirebaseRepository.OnFirebaseWriteListener() {
            @Override
            public void onSuccess(Object orderId) {
                Toast.makeText(context, "🧾 New Shop Added — ID : " + id, Toast.LENGTH_LONG).show();
                createAdminUser(shopsModel.getId());
            }

            @Override
            public void onFailure(Exception e) {
                e.printStackTrace();
                Toast.makeText(context, "Firebase Internal Server Error.!", Toast.LENGTH_LONG).show();
            }
        });
    }

    private ShopsModel createOnboardingModel(String imageUrl) {

        ShopsModel model = new ShopsModel();

        // ---------- SHOP INFO ----------
        model.setId(generateIdFromShopName(edtShopName.getText().toString().trim()));
        model.setShopName(edtShopName.getText().toString().trim());
        model.setPhoneNumber(edtPhone.getText().toString().trim());
        model.setAddress(edtAddress.getText().toString().trim());
        model.setOnboardingDate(new Date());

        // ---------- LOGO ----------
        model.setLogo(imageUrl);

        // ---------- SUBSCRIPTION ----------
        String formattedDate = convertToIsoFormat(txtSelectedDate.getText().toString());
        model.setSubscriptionDate(formattedDate);

        // ---------- PRINTER CONFIG ----------
        model.setPrinterName(((EditText) findViewById(R.id.edtPrinterName)).getText().toString());
        model.setHeader1(((EditText) findViewById(R.id.edtHeader1)).getText().toString());
        model.setHeader2(((EditText) findViewById(R.id.edtHeader2)).getText().toString());
        model.setHeader3(((EditText) findViewById(R.id.edtHeader3)).getText().toString());
        model.setFooter1(((EditText) findViewById(R.id.edtFooter1)).getText().toString());
        model.setFooter2(((EditText) findViewById(R.id.edtFooter2)).getText().toString());
        model.setFooter3(((EditText) findViewById(R.id.edtFooter3)).getText().toString());

        return model;
    }


    private String convertToIsoFormat(String inputDate) {

        try {
            if (StringUtils.isBlank(inputDate))
                return "";
            SimpleDateFormat sdfInput = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            SimpleDateFormat sdfOutput = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

            return sdfOutput.format(sdfInput.parse(inputDate));

        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    private String generateIdFromShopName(String shopName) {
        if (shopName == null) return "";

        // 1. Convert to lowercase
        String slug = shopName.toLowerCase().trim();

        // 2. Replace all non-alphanumeric characters with space
        slug = slug.replaceAll("[^a-z0-9 ]", "");

        // 3. Replace multiple spaces with single space
        slug = slug.replaceAll("\\s+", " ");

        // 4. Replace space with hyphen
        slug = slug.replace(" ", "-");

        return slug;
    }

    private boolean validateFields() {

        // ---- SHOP INFO ----
        if (edtShopName.getText().toString().trim().isEmpty()) {
            edtShopName.setError("Enter shop name");
            edtShopName.requestFocus();
            return false;
        }

        if (edtPhone.getText().toString().trim().isEmpty()) {
            edtPhone.setError("Enter phone number");
            edtPhone.requestFocus();
            return false;
        }

        if (edtPhone.getText().toString().trim().length() < 10) {
            edtPhone.setError("Enter valid phone number");
            edtPhone.requestFocus();
            return false;
        }

        if (edtAddress.getText().toString().trim().isEmpty()) {
            edtAddress.setError("Enter address area");
            edtAddress.requestFocus();
            return false;
        }

        // ---- LOGO ----
//        if (selectedImageUri == null) {
//            Toast.makeText(this, "Please choose a logo", Toast.LENGTH_SHORT).show();
//            return false;
//        }

        // ---- SUBSCRIPTION ----
        if (txtSelectedDate.getText().toString().trim().isEmpty()) {
            Toast.makeText(this, "Select subscription date", Toast.LENGTH_SHORT).show();
            return false;
        }

        // ---- PRINTER CONFIG ----
        EditText edtPrinterName = findViewById(R.id.edtPrinterName);

        if (edtPrinterName.getText().toString().trim().isEmpty()) {
            edtPrinterName.setError("Enter printer name");
            edtPrinterName.requestFocus();
            return false;
        }

        // ---- EMPLOYEE DETAILS ----
        EditText edtEmpName = findViewById(R.id.edtEmpName);
        EditText edtEmpPhone = findViewById(R.id.edtEmpPhone);
        EditText edtEmpPasscode = findViewById(R.id.edtEmpPasscode);

// Name
        if (edtEmpName.getText().toString().trim().isEmpty()) {
            edtEmpName.setError("Enter employee name");
            edtEmpName.requestFocus();
            return false;
        }

// Phone
        if (edtEmpPhone.getText().toString().trim().isEmpty()) {
            edtEmpPhone.setError("Enter employee phone");
            edtEmpPhone.requestFocus();
            return false;
        }

        if (edtEmpPhone.getText().toString().length() < 10) {
            edtEmpPhone.setError("Enter valid phone number");
            edtEmpPhone.requestFocus();
            return false;
        }

// Passcode
        String pass = edtEmpPasscode.getText().toString().trim();

        if (pass.isEmpty()) {
            edtEmpPasscode.setError("Enter passcode");
            edtEmpPasscode.requestFocus();
            return false;
        }

        if (!pass.matches("\\d{4}")) {  // must be exactly 4 digits
            edtEmpPasscode.setError("Passcode must be 4 digits");
            edtEmpPasscode.requestFocus();
            return false;
        }


        // Optional: allow empty headers/footers
        // But if you want mandatory, add checks

        return true; // ALL PASSED
    }

    private void createAdminUser(String id) {
        EmployeeModel newEmployeeModel = new EmployeeModel();
        newEmployeeModel.setId(SingleTon.generateEmployeeDetailDocument());
        newEmployeeModel.setName(((EditText) findViewById(R.id.edtEmpName)).getText().toString().trim());
        newEmployeeModel.setPhone(((EditText) findViewById(R.id.edtEmpPhone)).getText().toString().trim());
        newEmployeeModel.setPasscode(((EditText) findViewById(R.id.edtEmpPasscode)).getText().toString().trim());
        newEmployeeModel.setRole("ADMIN");
        newEmployeeModel.setActive(true);

        InstaFirebaseRepository.getInstance().addDataBase(id + AppConstants.EMPLOYEE_COLLECTION, newEmployeeModel.getId(), newEmployeeModel, new InstaFirebaseRepository.OnFirebaseWriteListener() {
            @Override
            public void onSuccess(Object data) {
                Toast.makeText(context, "Admin Employee Added", Toast.LENGTH_LONG).show();
                try {
                    creatingIndex(id);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Exception e) {
                e.printStackTrace();
                Toast.makeText(context, "Firebase Internal Server Error.!", Toast.LENGTH_LONG).show();
            }
        });

    }

    private void creatingIndex(String id) throws InterruptedException {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        showLoader();
        // Step 1: Create dummy documents
        insertDummyDoc(db, id + AppConstants.EXPENSE_COLLECTION);
        insertDummyDoc(db, id + AppConstants.ATTENDANCE_COLLECTION);
        insertDummyDoc(db, id + AppConstants.SALES_COLLECTION);

        sleep(2000);

        runIndexTestQueries(id);

        new Handler(Looper.getMainLooper()).postDelayed(() -> {

            List<String> urls = AutoIndexHelper.getPendingIndexUrls();
            hideLoader();
            if (!urls.isEmpty()) {

                Toast.makeText(this,
                        "Finishing Onboarding.!",
                        Toast.LENGTH_LONG).show();

                storeIndexUrls(id, urls);
                AutoIndexHelper.clear();
                // Step 4: Delete dummy docs
                deleteAllDummyDocs(id);

            }

        }, 3000);

    }


    private void storeIndexUrls(String shopId, List<String> urls) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection(AppConstants.SHOP_COLLECTION)
                .document(shopId)
                .update("indexUrls", urls)
                .addOnSuccessListener(aVoid -> {
                    showSuccessPopup(shopId);
                })
                .addOnFailureListener(e -> {
                    e.printStackTrace();
                    Toast.makeText(this, "Failed to update index URLs", Toast.LENGTH_SHORT).show();
                });

    }

    private void showSuccessPopup(String shopId) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = getLayoutInflater().inflate(R.layout.dialog_onboarding_success, null);

        TextView tvMsg = view.findViewById(R.id.tvSuccessMsg);
        TextView tvShopId = view.findViewById(R.id.tvShopId);
        Button btnOk = view.findViewById(R.id.btnOkay);

        tvMsg.setText("🎉 Onboarding Successful!");
        tvShopId.setText("Your Merchant ID: " + shopId);

        builder.setView(view);
        AlertDialog dialog = builder.create();
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        btnOk.setOnClickListener(v -> {
            dialog.dismiss();
            finish();
        });

        dialog.show();
    }


    private void showLoader() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(false);
        builder.setView(R.layout.loader_layout); // Your custom loader layout with ProgressBar
        loaderDialog = builder.create();
        loaderDialog.show();
    }

    private void hideLoader() {
        if (loaderDialog != null && loaderDialog.isShowing()) {
            loaderDialog.dismiss();
        }
    }

    private void runIndexTestQueries(String id) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String today = sdf.format(new Date());

        // 1) Expense Query
        db.collection(id + AppConstants.EXPENSE_COLLECTION)
                .whereEqualTo("type", "")
                .whereGreaterThanOrEqualTo("date", today)
                .orderBy("date", Query.Direction.DESCENDING)
                .get().addOnFailureListener(AutoIndexHelper::collectIndexError);

        // 2) Attendance Query
        db.collection(id + AppConstants.ATTENDANCE_COLLECTION)
                .whereEqualTo("employeeName", "")
                .whereGreaterThanOrEqualTo("date", today)
                .orderBy("date", Query.Direction.DESCENDING)
                .get().addOnFailureListener(AutoIndexHelper::collectIndexError);

        // 3) Sales Query
        long startEpoch = System.currentTimeMillis() / 1000;

        db.collection(id + AppConstants.SALES_COLLECTION)
                .whereEqualTo("employeeName", "")
                .whereGreaterThanOrEqualTo("billingDate", startEpoch)
                .orderBy("billingDate", Query.Direction.DESCENDING)
                .get().addOnFailureListener(AutoIndexHelper::collectIndexError);
    }


    private void insertDummyDoc(FirebaseFirestore db, String collectionPath) {
        Map<String, Object> dummy = new HashMap<>();
        dummy.put("dummy", true);

        db.collection(collectionPath)
                .document("_dummy_")
                .set(dummy);
    }

    private void deleteAllDummyDocs(String id) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection(id + AppConstants.EXPENSE_COLLECTION).document("_dummy_").delete();
        db.collection(id + AppConstants.ATTENDANCE_COLLECTION).document("_dummy_").delete();
        db.collection(id + AppConstants.SALES_COLLECTION).document("_dummy_").delete();
    }


}
