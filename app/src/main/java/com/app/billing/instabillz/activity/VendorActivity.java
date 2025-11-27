package com.app.billing.instabillz.activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.app.billing.instabillz.R;
import com.app.billing.instabillz.adapter.VendorViewAdapter;
import com.app.billing.instabillz.constants.AppConstants;
import com.app.billing.instabillz.listener.BillingClickListener;
import com.app.billing.instabillz.model.EmployeeModel;
import com.app.billing.instabillz.model.VendorModel;
import com.app.billing.instabillz.repository.InstaFirebaseRepository;
import com.app.billing.instabillz.utils.SharedPrefHelper;
import com.app.billing.instabillz.utils.SingleTon;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class VendorActivity extends AppCompatActivity {

    Context context;
    Activity activity;

    TextView back;
    RecyclerView recyclerView;
    FloatingActionButton add_fab;

    VendorViewAdapter adapter;
    List<VendorModel> vendorModelList;

    SharedPrefHelper sharedPrefHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vendor);
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

        context = VendorActivity.this;
        activity = VendorActivity.this;
        sharedPrefHelper = new SharedPrefHelper(context);

        back = findViewById(R.id.vendor_back);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        add_fab = (FloatingActionButton) findViewById(R.id.vendor_add_fab);
        add_fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createVendor(null);
            }
        });

        BillingClickListener clickListener = new BillingClickListener() {
            @Override
            public void click(int index, String type) {
               if(type.equalsIgnoreCase("EDIT")){
                   createVendor(vendorModelList.get(index));
               }else if(type.equalsIgnoreCase("DELETE")){
                   deleteConfirmationPopUp(vendorModelList.get(index));
               }
            }
        };
        vendorModelList= new ArrayList<>();
        recyclerView = (RecyclerView) findViewById(R.id.vendor_recyclerView);
        adapter = new VendorViewAdapter(context,vendorModelList,clickListener);
        recyclerView.setLayoutManager(new LinearLayoutManager(context));
        recyclerView.setAdapter(adapter);

        loadVendorList();

    }

    private void createVendor(VendorModel vendorModel) {
        Dialog dialog = new Dialog(this);
        View sheetView = getLayoutInflater().inflate(R.layout.dialog_vendor_create, null);
        dialog.setContentView(sheetView);
        dialog.setCanceledOnTouchOutside(false);

        // Transparent background for rounded corners
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

            // Set dialog position to TOP
            Window window = dialog.getWindow();
            WindowManager.LayoutParams params = window.getAttributes();
            params.gravity = Gravity.TOP;  // 👈 This makes it appear at the top
            params.y = 60; // optional: push it slightly down (in dp)
            window.setAttributes(params);

            // Optional: match width to parent
            window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

            // Optional animation (slide from top)
            window.getAttributes().windowAnimations = R.style.TopDialogAnimation;
        }

        EditText name = (EditText) dialog.findViewById(R.id.etVendorName);
        EditText phone = (EditText) dialog.findViewById(R.id.etVendorPhone);
        EditText remarks = (EditText) dialog.findViewById(R.id.etVendorRemarks);
        Button submit = (Button) dialog.findViewById(R.id.btVendorSubmit);
        TextView close = (TextView) dialog.findViewById(R.id.tvVendorClose);

        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SingleTon.hideKeyboard(context, activity);
                dialog.dismiss();
            }
        });

        if (vendorModel != null) {
            name.setText(vendorModel.getName());
            phone.setText(vendorModel.getPhone());
            remarks.setText(vendorModel.getRemarks());
        }


        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (name.getText().toString().isEmpty()) {
                    name.setError("Enter Vendor name");
                    name.requestFocus();
                    return;
                }
                String phoneStr = phone.getText().toString();
                if (phoneStr.isEmpty() || phoneStr.length() != 10 || !phoneStr.matches("\\d{10}")) {
                    phone.setError("Enter valid 10-digit phone number");
                    phone.requestFocus();
                    return;
                }

                VendorModel newVendorModel = vendorModel;

                if (newVendorModel == null) {
                    newVendorModel = new VendorModel();
                    newVendorModel.setId(SingleTon.generateVendorDocument());
                }
                newVendorModel.setName(name.getText().toString().toUpperCase());
                newVendorModel.setPhone(phoneStr);
                newVendorModel.setRemarks(remarks.getText().toString());

                InstaFirebaseRepository.getInstance().addDataBase(sharedPrefHelper.getAppName() + AppConstants.VENDOR_COLLECTION, newVendorModel.getId(), newVendorModel, new InstaFirebaseRepository.OnFirebaseWriteListener() {
                    @Override
                    public void onSuccess(Object data) {
                        dialog.dismiss();
                        Toast.makeText(context, "New Vendor Added", Toast.LENGTH_LONG).show();
                        loadVendorList();
                    }

                    @Override
                    public void onFailure(Exception e) {
                        e.printStackTrace();
                        Toast.makeText(context, "Firebase Internal Server Error.!", Toast.LENGTH_LONG).show();
                    }
                });
            }
        });

        dialog.show();

    }

    private void deleteConfirmationPopUp(VendorModel vendorModel) {
        // Create and configure the AlertDialog
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Confirmation");
        builder.setMessage("Do you want to Delete the Vendor [" + vendorModel.getName() + "]");
        builder.setCancelable(true);

        // Set positive button
        builder.setPositiveButton("Yes", (dialog, which) -> {
            dialog.dismiss();
            try {
                deleteDBItem(vendorModel.getId());
            } catch (Exception e) {
                Toast.makeText(context, "Firebase Internal Server Error.!", Toast.LENGTH_LONG).show();
            }
        });

        // Set negative button
        builder.setNegativeButton("No", (dialog, which) -> {
            dialog.dismiss();
        });

        // Create and show the dialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();

    }

    private void deleteDBItem(String id) {
        Toast.makeText(context, "Loading.!", Toast.LENGTH_SHORT).show();
        InstaFirebaseRepository.getInstance().deleteData(sharedPrefHelper.getAppName() + AppConstants.VENDOR_COLLECTION, id, new InstaFirebaseRepository.OnFirebaseWriteListener() {
            @Override
            public void onSuccess(Object data) {
                Toast.makeText(context, "Employee Removed", Toast.LENGTH_LONG).show();
                loadVendorList();
            }

            @Override
            public void onFailure(Exception e) {
                e.printStackTrace();
                Toast.makeText(context, "Firebase Internal Server Error.!", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void loadVendorList() {
        Toast.makeText(context, "Loading.!", Toast.LENGTH_SHORT).show();
        InstaFirebaseRepository.getInstance().getAllDetails(sharedPrefHelper.getAppName() + AppConstants.VENDOR_COLLECTION, "name", Query.Direction.ASCENDING, new InstaFirebaseRepository.OnFirebaseWriteListener() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onSuccess(Object data) {
                vendorModelList.clear();
                QuerySnapshot documentSnapshotList = (QuerySnapshot) data;
                for (DocumentSnapshot doc : documentSnapshotList) {
                    vendorModelList.add(doc.toObject(VendorModel.class));
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onFailure(Exception e) {
                e.printStackTrace();
                Toast.makeText(context, "Firebase Internal Server Error.!", Toast.LENGTH_LONG).show();
            }
        });
    }

}