package com.app.billing.instabillz.activity;

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
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.app.billing.instabillz.R;
import com.app.billing.instabillz.adapter.CategoryViewAdapter;
import com.app.billing.instabillz.constants.AppConstants;
import com.app.billing.instabillz.listener.BillingClickListener;
import com.app.billing.instabillz.model.CategoryModel;
import com.app.billing.instabillz.model.ProductModel;
import com.app.billing.instabillz.repository.InstaFirebaseRepository;
import com.app.billing.instabillz.utils.SingleTon;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class CategoryActivity extends AppCompatActivity implements View.OnClickListener {

    TextView back, download;
    RecyclerView recyclerView;
    FloatingActionButton add_fab;

    Context context;
    Activity activity;

    List<CategoryModel> categoryModelList;
    CategoryViewAdapter adapter;
    BillingClickListener listener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_category);
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

        context = CategoryActivity.this;
        activity = CategoryActivity.this;
        recyclerView = (RecyclerView) findViewById(R.id.category_recyclerView);
        back = (TextView) findViewById(R.id.category_back);
        back.setOnClickListener(this);

        add_fab = (FloatingActionButton) findViewById(R.id.category_add_fab);
        add_fab.setOnClickListener(this);

        recyclerView.setLayoutManager(new LinearLayoutManager(context));

        listener = new BillingClickListener() {
            @Override
            public void click(int index, String type) {
                if ("DELETE".equalsIgnoreCase(type)) {
                    categoryDeleteConfirmationPopUp(index);
                }
            }
        };
        categoryModelList = new ArrayList<>();
        adapter = new CategoryViewAdapter(context,categoryModelList,listener);
        recyclerView.setLayoutManager(new LinearLayoutManager(context));
        recyclerView.setAdapter(adapter);

        loadCategoryList();
    }

    private void categoryDeleteConfirmationPopUp(int index) {
        // Create and configure the AlertDialog
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Confirmation");
        builder.setMessage("Do you want to Delete the Category ["+categoryModelList.get(index).getName()+"]");
        builder.setCancelable(true);

        // Set positive button
        builder.setPositiveButton("Yes", (dialog, which) -> {
            dialog.dismiss();
            try {
                Toast.makeText(context, "Refreshing.!", Toast.LENGTH_LONG).show();
                deleteCategoryItem(index);
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

    private void loadCategoryList() {
        Toast.makeText(context, "Loading.!", Toast.LENGTH_SHORT).show();
        InstaFirebaseRepository.getInstance().getAllDetails(AppConstants.APP_NAME + AppConstants.CATEGORIES_COLLECTION, "name", Query.Direction.ASCENDING, new InstaFirebaseRepository.OnFirebaseWriteListener() {
            @Override
            public void onSuccess(Object data) {
                categoryModelList.clear();
                QuerySnapshot documentSnapshotList = (QuerySnapshot) data;
                for (DocumentSnapshot doc : documentSnapshotList) {
                    categoryModelList.add(doc.toObject(CategoryModel.class));
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
    private void deleteCategoryItem(int index){
        Toast.makeText(context, "Loading.!", Toast.LENGTH_SHORT).show();
        InstaFirebaseRepository.getInstance().deleteData(AppConstants.APP_NAME + AppConstants.CATEGORIES_COLLECTION, categoryModelList.get(index).getId(), new InstaFirebaseRepository.OnFirebaseWriteListener() {
            @Override
            public void onSuccess(Object data) {
                Toast.makeText(context, "Category Removed", Toast.LENGTH_LONG).show();
                categoryModelList.remove(index);
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
        if (view.getId() == R.id.category_back) {
            finish();
        } else if (view.getId() == R.id.category_add_fab) {
            CategoryAddDialog();
        }
    }

    private void CategoryAddDialog() {
        Dialog dialog = new Dialog(this);
        View sheetView = getLayoutInflater().inflate(R.layout.dialog_category_create, null);
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

        EditText name = sheetView.findViewById(R.id.category_create_name);
        Button submit = sheetView.findViewById(R.id.category_create_submit);
        TextView close = sheetView.findViewById(R.id.category_create_close);

        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SingleTon.hideKeyboard(context,activity);
                dialog.dismiss();
            }
        });

        submit.setOnClickListener(b -> {
            CategoryModel newCategoryModel = new CategoryModel();
            newCategoryModel.setId(SingleTon.generateCategoryDocument());
            newCategoryModel.setName(name.getText().toString().toUpperCase());

            InstaFirebaseRepository.getInstance().addDataBase(AppConstants.APP_NAME + AppConstants.CATEGORIES_COLLECTION, newCategoryModel.getId(), newCategoryModel, new InstaFirebaseRepository.OnFirebaseWriteListener() {
                @Override
                public void onSuccess(Object orderId) {
                    Toast.makeText(context, "Categories Updated", Toast.LENGTH_LONG).show();
                    loadCategoryList();
                }

                @Override
                public void onFailure(Exception e) {
                    e.printStackTrace();
                    Toast.makeText(context, "Firebase Internal Server Error.!", Toast.LENGTH_LONG).show();
                }
            });

            dialog.dismiss();
        });

        dialog.show();

    }
}