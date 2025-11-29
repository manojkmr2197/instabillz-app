package com.app.billing.instabillz.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.app.billing.instabillz.R;
import com.app.billing.instabillz.adapter.UrlAdapter;
import com.app.billing.instabillz.constants.AppConstants;
import com.app.billing.instabillz.model.ShopsModel;
import com.app.billing.instabillz.repository.InstaFirebaseRepository;
import com.app.billing.instabillz.utils.SharedPrefHelper;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.WriteBatch;

import org.apache.commons.lang3.StringUtils;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ShopProfileActivity extends AppCompatActivity implements View.OnClickListener {

    Context context;
    Activity activity;

    TextView back;

    SharedPrefHelper sharedPrefHelper;

    EditText name, phone, address, printer_name, header1, header2, header3, footer1, footer2, footer3;
    Spinner outletTypeSpinner, billingTypeSpinner;
    RecyclerView indexLinkRecyclerView;
    Button preApproved, btnPickDate, submit;
    TextView txtSelectedDate;
    RadioGroup shopStatusRadioGroup;
    RadioButton shopActive, shopInactive;
    LinearLayout admin_ll, subs_date_ll;
    CardView preApprovalCardView;

    List<String> urlList = new ArrayList<>();
    UrlAdapter adapter;

    ArrayAdapter<CharSequence> billingTypeAdapter, outletTypeAdapter;

    private AlertDialog loaderDialog;

    ShopsModel shopsModel = null;

    FirebaseFirestore db;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shop_profile);
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

        context = ShopProfileActivity.this;
        activity = ShopProfileActivity.this;
        sharedPrefHelper = new SharedPrefHelper(context);
        db = FirebaseFirestore.getInstance();

        back = findViewById(R.id.shop_profile_back);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        name = (EditText) findViewById(R.id.shop_profile_name);
        phone = (EditText) findViewById(R.id.shop_profile_phone);
        address = (EditText) findViewById(R.id.shop_profile_address);
        printer_name = (EditText) findViewById(R.id.shop_profile_printer_name);
        header1 = (EditText) findViewById(R.id.shop_profile_header1);
        header2 = (EditText) findViewById(R.id.shop_profile_header2);
        header3 = (EditText) findViewById(R.id.shop_profile_header3);
        footer1 = (EditText) findViewById(R.id.shop_profile_footer1);
        footer2 = (EditText) findViewById(R.id.shop_profile_footer2);
        footer3 = (EditText) findViewById(R.id.shop_profile_footer3);

        outletTypeSpinner = (Spinner) findViewById(R.id.shop_profile_outlet_type_spinner);
        outletTypeAdapter = ArrayAdapter.createFromResource(
                this,
                R.array.spinner_outlet_type,
                android.R.layout.simple_spinner_item
        );
        outletTypeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        outletTypeSpinner.setAdapter(outletTypeAdapter);

        billingTypeSpinner = (Spinner) findViewById(R.id.shop_profile_billing_type_spinner);
        billingTypeAdapter = ArrayAdapter.createFromResource(
                this,
                R.array.spinner_billing_type,
                android.R.layout.simple_spinner_item
        );
        billingTypeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        billingTypeSpinner.setAdapter(billingTypeAdapter);

        indexLinkRecyclerView = (RecyclerView) findViewById(R.id.shop_profile_index_recycler_view);
        indexLinkRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        adapter = new UrlAdapter(this, urlList);
        indexLinkRecyclerView.setAdapter(adapter);

        preApprovalCardView = (CardView) findViewById(R.id.shop_profile_pre_approval_ll);
        preApproved = (Button) findViewById(R.id.shop_profile_pre_approved_btn);
        preApproved.setOnClickListener(this);
        btnPickDate = (Button) findViewById(R.id.shop_profile_pick_date_bt);
        btnPickDate.setOnClickListener(this);

        txtSelectedDate = (TextView) findViewById(R.id.shop_profile_selected_date);

        shopStatusRadioGroup = (RadioGroup) findViewById(R.id.shop_profile_shop_status);
        shopActive = (RadioButton) findViewById(R.id.shop_profile_active_rb);
        shopInactive = (RadioButton) findViewById(R.id.shop_profile_inactive_rb);

        shopStatusRadioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.shop_profile_active_rb) {
                shopsModel.setActive(true);     // active selected
                subs_date_ll.setVisibility(View.VISIBLE);
            } else if (checkedId == R.id.shop_profile_inactive_rb) {
                shopsModel.setActive(false);    // inactive selected
                subs_date_ll.setVisibility(View.GONE);
            }
        });

        admin_ll = (LinearLayout) findViewById(R.id.shop_profile_admin_ll);
        subs_date_ll = (LinearLayout) findViewById(R.id.shop_profile_subs_date_ll);

        submit = (Button) findViewById(R.id.shop_profile_submit);
        submit.setOnClickListener(this);

        String loginPhone = sharedPrefHelper.getSystemUserPhone();
        if (loginPhone.equals("9585905176")) {
            admin_ll.setVisibility(View.VISIBLE);   // show admin menu
        } else {
            admin_ll.setVisibility(View.GONE); // hide admin menu
        }

        if (StringUtils.isNotBlank(getIntent().getStringExtra("shop_id"))) {
            loadShopDetails(getIntent().getStringExtra("shop_id"));
        } else {
            Toast.makeText(context, "Requested Shop ID not found.", Toast.LENGTH_SHORT).show();
            finish();
        }

    }

    private void loadShopDetails(String shopId) {
        showLoader();
        InstaFirebaseRepository.getInstance().getDetailsByDocumentId(AppConstants.SHOP_COLLECTION, shopId, new InstaFirebaseRepository.OnFirebaseWriteListener() {
            @Override
            public void onSuccess(Object data) {
                hideLoader();
                DocumentSnapshot doc = (DocumentSnapshot) data;
                if (doc.exists()) {
                    shopsModel = doc.toObject(ShopsModel.class);

                    if (shopsModel != null) {
                        name.setText(shopsModel.getShopName());
                        phone.setText(shopsModel.getPhoneNumber());
                        address.setText(shopsModel.getAddress());

                        printer_name.setText(shopsModel.getPrinterName());
                        header1.setText(shopsModel.getHeader1());
                        header2.setText(shopsModel.getHeader2());
                        header3.setText(shopsModel.getHeader3());
                        footer1.setText(shopsModel.getFooter1());
                        footer2.setText(shopsModel.getFooter2());
                        footer3.setText(shopsModel.getFooter3());

                        String loginPhone = sharedPrefHelper.getSystemUserPhone();
                        if (loginPhone.equals("9585905176")) {
                            if (shopsModel.getIndexUrls() != null && !shopsModel.getIndexUrls().isEmpty()) {
                                urlList.clear();
                                urlList.addAll(shopsModel.getIndexUrls());
                                adapter.notifyDataSetChanged();
                                preApprovalCardView.setVisibility(View.VISIBLE);
                            } else {
                                preApprovalCardView.setVisibility(View.GONE);
                            }

                            outletTypeSpinner.setSelection(outletTypeAdapter.getPosition(shopsModel.getOutletType()));
                            billingTypeSpinner.setSelection(billingTypeAdapter.getPosition(shopsModel.getBillingType()));

                            if (shopsModel.isActive()) {
                                shopActive.setChecked(true);
                                subs_date_ll.setVisibility(View.VISIBLE);
                            } else {
                                shopInactive.setChecked(true);
                                subs_date_ll.setVisibility(View.GONE);
                            }
                            txtSelectedDate.setText(shopsModel.getSubscriptionDate());
                        }

                    } else {
                        Toast.makeText(context, "No Shop Information Found.!", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(context, "No Shop Information Found.!", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Exception e) {
                hideLoader();
                e.printStackTrace();
                Toast.makeText(context, "Firebase Internal Server Error.!", Toast.LENGTH_LONG).show();
            }
        });


    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.shop_profile_submit) {
            boolean status = validateAndUpdate();
            if (!status) {
                Toast.makeText(context,"Please fill all mandatory fields",Toast.LENGTH_SHORT).show();
            }
        } else if (view.getId() == R.id.shop_profile_pre_approved_btn) {
            shopsModel.setIndexUrls(new ArrayList<>());
            clearCollection(shopsModel.getId()+ AppConstants.EXPENSE_COLLECTION);
            clearCollection(shopsModel.getId()+ AppConstants.ATTENDANCE_COLLECTION);
            clearCollection(shopsModel.getId()+ AppConstants.SALES_COLLECTION);
            updateShopDetails(shopsModel);
        } else if (view.getId() == R.id.shop_profile_pick_date_bt) {
            chooseDate();
        }
    }


    public void clearCollection(String collectionPath) {
        db.collection(collectionPath)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    WriteBatch batch = db.batch();

                    for (DocumentSnapshot document : queryDocumentSnapshots) {
                        batch.delete(document.getReference());
                    }

                    batch.commit()
                            .addOnSuccessListener(aVoid -> {
                                System.out.println("Firestore All documents deleted!");
                            })
                            .addOnFailureListener(e -> {
                                System.out.println("Firestore Error deleting documents"+ e);
                            });
                })
                .addOnFailureListener(e -> {
                    System.out.println("Firestore Error getting documents"+ e);
                });
    }


    private void chooseDate() {

        final Calendar calendar = Calendar.getInstance();

        // If string is NOT empty — parse it
        if (shopsModel.getSubscriptionDate() != null && !shopsModel.getSubscriptionDate().isEmpty()) {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                Date date = sdf.parse(shopsModel.getSubscriptionDate());
                calendar.setTime(date);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog dialog = new DatePickerDialog(context,
                (view, y, m, d) -> {

                    // Format to yyyy-MM-dd
                    String formatted = String.format(Locale.getDefault(), "%04d-%02d-%02d", y, (m + 1), d);

                    // Store the string
                    shopsModel.setSubscriptionDate(formatted);

                    // Show in TextView or EditText
                    txtSelectedDate.setText(formatted);
                }, year, month, day);

        dialog.show();

    }

    private void updateShopDetails(ShopsModel shopsModel) {
        InstaFirebaseRepository.getInstance().addDataBase(AppConstants.SHOP_COLLECTION, shopsModel.getId(), shopsModel, new InstaFirebaseRepository.OnFirebaseWriteListener() {
            @Override
            public void onSuccess(Object orderId) {
                Toast.makeText(context, "Shop Updated.!", Toast.LENGTH_LONG).show();
            }

            @Override
            public void onFailure(Exception e) {
                e.printStackTrace();
                Toast.makeText(context, "Firebase Internal Server Error.!", Toast.LENGTH_LONG).show();
            }
        });
    }

    private boolean validateAndUpdate() {
        if (name.getText().toString().trim().isEmpty()) {
            name.setError("Enter shop name");
            name.requestFocus();
            return false;
        }else{
            shopsModel.setShopName(name.getText().toString());
        }

        if (phone.getText().toString().trim().isEmpty()) {
            phone.setError("Enter shop Phone number");
            phone.requestFocus();
            return false;
        }else{
            shopsModel.setPhoneNumber(phone.getText().toString());
        }

        if (address.getText().toString().trim().isEmpty()) {
            address.setError("Enter shop address");
            address.requestFocus();
            return false;
        }else{
            shopsModel.setAddress(address.getText().toString());
        }

        if (printer_name.getText().toString().trim().isEmpty()) {
            printer_name.setError("Enter Printer Name");
            printer_name.requestFocus();
            return false;
        }else{
            shopsModel.setPrinterName(printer_name.getText().toString());
        }

        String loginPhone = sharedPrefHelper.getSystemUserPhone();
        if (loginPhone.equals("9585905176")) {
            shopsModel.setOutletType(outletTypeSpinner.getSelectedItem().toString());
            shopsModel.setBillingType(billingTypeSpinner.getSelectedItem().toString());
        }

        updateShopDetails(shopsModel);
        return true;
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
}