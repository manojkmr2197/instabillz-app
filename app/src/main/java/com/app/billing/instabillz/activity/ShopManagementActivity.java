package com.app.billing.instabillz.activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
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
import com.app.billing.instabillz.adapter.ShopMgmtViewAdapter;
import com.app.billing.instabillz.constants.AppConstants;
import com.app.billing.instabillz.listener.BillingClickListener;
import com.app.billing.instabillz.model.EmployeeModel;
import com.app.billing.instabillz.model.ShopsModel;
import com.app.billing.instabillz.repository.InstaFirebaseRepository;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

public class ShopManagementActivity extends AppCompatActivity implements View.OnClickListener {

    TextView back, download;
    RecyclerView recyclerView;
    FloatingActionButton add_fab;

    Context context;
    Activity activity;

    ShopMgmtViewAdapter adapter;
    List<ShopsModel> shopsModelList=new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shop_management);
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
        context = ShopManagementActivity.this;
        activity = ShopManagementActivity.this;
        back = (TextView) findViewById(R.id.shop_mgmt_back);
        back.setOnClickListener(this);

        recyclerView = (RecyclerView) findViewById(R.id.shop_mgmt_recyclerView);
        add_fab = (FloatingActionButton) findViewById(R.id.shop_mgmt_add_fab);
        add_fab.setOnClickListener(this);

        adapter = new ShopMgmtViewAdapter(context, shopsModelList, new BillingClickListener() {
            @Override
            public void click(int index, String type) {
                if("EDIT".equalsIgnoreCase(type)){
                    //edit option
                    Intent intent = new Intent(context, ShopProfileActivity.class);
                    intent.putExtra("shop_id", shopsModelList.get(index).getId());
                    startActivity(intent);
                    overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                }else if("DELETE".equalsIgnoreCase(type)){
                    deleteConfirmation(index);
                }
            }
        });
        recyclerView.setLayoutManager(new LinearLayoutManager(context));
        recyclerView.setAdapter(adapter);
        loadShopList();

    }

    private void loadShopList() {
        Toast.makeText(context, "Loading.!", Toast.LENGTH_SHORT).show();
        InstaFirebaseRepository.getInstance().getAllDetails(AppConstants.SHOP_COLLECTION, "shopName", Query.Direction.ASCENDING, new InstaFirebaseRepository.OnFirebaseWriteListener() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onSuccess(Object data) {
                shopsModelList.clear();
                QuerySnapshot documentSnapshotList = (QuerySnapshot) data;
                for (DocumentSnapshot doc : documentSnapshotList) {
                    shopsModelList.add(doc.toObject(ShopsModel.class));
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

    private void deleteConfirmation(int index) {
        // Create and configure the AlertDialog
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Confirmation");
        builder.setMessage("Do you want to Delete the Shop # "+shopsModelList.get(index).getId());
        builder.setCancelable(true);

        // Set positive button
        builder.setPositiveButton("Yes", (dialog, which) -> {
            dialog.dismiss();
            try {
                //deleteShopItem(index);
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

    private void deleteShopItem(int index) {
        Toast.makeText(context, "Loading...", Toast.LENGTH_SHORT).show();
        InstaFirebaseRepository.getInstance().deleteData(AppConstants.SHOP_COLLECTION, shopsModelList.get(index).getId(), new InstaFirebaseRepository.OnFirebaseWriteListener() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onSuccess(Object data) {
                shopsModelList.remove(index);
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onFailure(Exception e) {
                e.printStackTrace();
                Toast.makeText(context, "Firebase Internal Server Error.!", Toast.LENGTH_LONG).show();
            }
        });

    }


    @Override
    public void onClick(View view) {
        if(view.getId() == R.id.shop_mgmt_back){
            finish();
        }else if(view.getId() == R.id.shop_mgmt_add_fab){
            Intent intent = new Intent(context, OnboardingActivity.class);
            context.startActivity(intent);
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        }
    }
}