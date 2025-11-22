package com.app.billing.instabillz.activity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Size;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.OptIn;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.app.billing.instabillz.R;
import com.app.billing.instabillz.adapter.ScanCartAdapter;
import com.app.billing.instabillz.model.ScanCartModel;
import com.google.android.material.card.MaterialCardView;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.mlkit.vision.barcode.BarcodeScanner;
import com.google.mlkit.vision.barcode.BarcodeScanning;
import com.google.mlkit.vision.barcode.common.Barcode;
import com.google.mlkit.vision.common.InputImage;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class ScanActivity extends AppCompatActivity {
    PreviewView previewView;
    List<ScanCartModel> cartList = new ArrayList<>();
    ScanCartAdapter cartAdapter;
    TextView tvTotalAmount;

    Button btnBill, btnPrint;
    RecyclerView rvCart;

    private boolean isProcessingScan = false;

    private Executor executor = Executors.newSingleThreadExecutor();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan);
        previewView = findViewById(R.id.previewView);
        cartAdapter = null;
        rvCart = findViewById(R.id.rvCart);
        rvCart.setLayoutManager(new LinearLayoutManager(this));
        rvCart.setAdapter(cartAdapter);
        tvTotalAmount = findViewById(R.id.tvTotalAmount);

        btnBill = (Button) findViewById(R.id.btnBill);
        btnPrint = (Button) findViewById(R.id.btnPrint);

        btnBill.setOnClickListener(v -> {
            Toast.makeText(this, "Bill generated!", Toast.LENGTH_SHORT).show();
        });

        btnPrint.setOnClickListener(v -> {
            Toast.makeText(this, "Printing...", Toast.LENGTH_SHORT).show();
        });


        askCameraPermission();
    }

    private void askCameraPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED) {
            startCamera();
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA}, 101);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 101 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            startCamera();
        }
    }

    private void startCamera() {
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture =
                ProcessCameraProvider.getInstance(this);

        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                bindCamera(cameraProvider);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, ContextCompat.getMainExecutor(this));
    }

    private void bindCamera(ProcessCameraProvider cameraProvider) {

        Preview preview = new Preview.Builder().build();
        preview.setSurfaceProvider(previewView.getSurfaceProvider());

        ImageAnalysis imageAnalysis = new ImageAnalysis.Builder()
                .setTargetResolution(new Size(1280, 720))
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build();

        imageAnalysis.setAnalyzer(executor, imageProxy -> {
            processImage(imageProxy);
        });

        CameraSelector selector = CameraSelector.DEFAULT_BACK_CAMERA;

        cameraProvider.bindToLifecycle(this, selector, preview, imageAnalysis);
    }

    @OptIn(markerClass = androidx.camera.core.ExperimentalGetImage.class)
    private void processImage(ImageProxy imageProxy) {
        InputImage image = InputImage.fromMediaImage(
                imageProxy.getImage(),
                imageProxy.getImageInfo().getRotationDegrees()
        );

        BarcodeScanner scanner = BarcodeScanning.getClient();

        scanner.process(image)
                .addOnSuccessListener(barcodes -> {
                    if (isProcessingScan) return;
                    for (Barcode barcode : barcodes) {
                        String code = barcode.getRawValue();
                        if (code != null) {
                            isProcessingScan = true;
                            runOnUiThread(() -> showProductDetails(code));
                        }
                    }
                })
                .addOnCompleteListener(task -> imageProxy.close());
    }

    private void fetchFromAPI(String barcode) {
        new Thread(() -> {
            try {
                String url = "https://world.openfoodfacts.org/api/v0/product/" + barcode + ".json";

                HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
                conn.setRequestMethod("GET");

                InputStream input = conn.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(input));

                StringBuilder result = new StringBuilder();
                String line;

                while ((line = reader.readLine()) != null) {
                    result.append(line);
                }

                JSONObject json = new JSONObject(result.toString());

                if (json.getInt("status") == 1) {
                    JSONObject product = json.getJSONObject("product");

                    String name = product.optString("product_name", "");
                    String brand = product.optString("brands", "");
                    String category = product.optString("categories", "");

                    runOnUiThread(() -> {
                        showAddProductDialog(barcode, name, brand, category);
                    });

                } else {
                    runOnUiThread(() -> {
                        showAddProductDialog(barcode, null, null, null);
                    });
                }

            } catch (Exception e) {
                runOnUiThread(() -> showAddProductDialog(barcode, null, null, null));
            }
        }).start();
    }

    private void unlockScannerAfterDelay() {
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            isProcessingScan = false;   // 🔥 unlock scanner
        }, 1500);
    }


    // Simulated Local Product DB (no backend)
    private void showProductDetails(String barcode) {

        // Dummy product mapping
        HashMap<String, String[]> map = new HashMap<>();
        map.put("8901234567890", new String[]{"Good Day Biscuit", "₹10"});
        map.put("9801112223334", new String[]{"Colgate Paste 100g", "₹45"});
        map.put("7867564534234", new String[]{"Maggi Noodles", "₹15"});

        String[] product = map.get(barcode);

        if (product != null) {
            addToCart(barcode, product[0], Double.parseDouble(product[1]));
        } else {
            fetchFromAPI(barcode);
        }
    }

    private void addToCart(String barcode, String name, double price) {

        boolean found = false;

        for (ScanCartModel item : cartList) {
            if (item.getBarcode().equals(barcode)) {
                item.setQty(item.getQty()+1);
                found = true;
                break;
            }
        }

        if (!found) {
            cartList.add(new ScanCartModel(name, barcode, price));
        }

        cartAdapter.notifyDataSetChanged();
        unlockScannerAfterDelay();
        updateTotal();
    }

    private void updateTotal() {
        double total = 0;

        for (ScanCartModel item : cartList) {
            total += item.getQty() * item.getPrice();
        }

        tvTotalAmount.setText("Total: ₹" + total);
    }



    private void showAddProductDialog(String barcode, String name, String brand, String category) {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = getLayoutInflater().inflate(R.layout.scan_product_add_dialog, null);

        EditText etName = view.findViewById(R.id.etName);
        //EditText etBrand = view.findViewById(R.id.etBrand);
        EditText etCategory = view.findViewById(R.id.etCategory);
        EditText etPrice = view.findViewById(R.id.etPrice);

        // Prefill from API if available
        if (name != null) etName.setText(name);
        //if (brand != null) etBrand.setText(brand);
        if (category != null) etCategory.setText(category);

        builder.setView(view);
        builder.setTitle("Add New Product");

        builder.setPositiveButton("Save", (dialog, which) -> {

            String n = etName.getText().toString().trim();
            //String b = etBrand.getText().toString().trim();
            String c = etCategory.getText().toString().trim();
            String p = etPrice.getText().toString().trim();

            addToCart(barcode, n, Double.parseDouble(p));
            unlockScannerAfterDelay();
            Toast.makeText(this, "Product saved successfully!", Toast.LENGTH_SHORT).show();
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> {unlockScannerAfterDelay();});

        builder.create().show();
    }

}