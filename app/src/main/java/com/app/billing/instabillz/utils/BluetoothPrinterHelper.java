package com.app.billing.instabillz.utils;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.widget.Toast;

import com.app.billing.instabillz.R;
import com.app.billing.instabillz.model.InvoiceModel;
import com.app.billing.instabillz.model.ProductModel;
import com.app.billing.instabillz.model.ShopsModel;
import com.dantsu.escposprinter.EscPosPrinter;
import com.dantsu.escposprinter.connection.bluetooth.BluetoothConnection;
import com.dantsu.escposprinter.connection.bluetooth.BluetoothPrintersConnections;
import com.dantsu.escposprinter.textparser.PrinterTextParserImg;

import org.apache.commons.lang3.StringUtils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class BluetoothPrinterHelper {

    private Context context;
    private Activity activity;

    public BluetoothPrinterHelper(Context context, Activity activity) {
        this.context = context;
        this.activity = activity;
    }

    public void printSmallFontReceipt(InvoiceModel billData, ShopsModel printerDataModel) {

        try {
            BluetoothConnection printerConnection = BluetoothPrintersConnections.selectFirstPaired();

            if (printerConnection == null) {
                Toast.makeText(context, "Printer not available. Please restart the printer.!", Toast.LENGTH_SHORT).show();
                //printingDialog.dismiss();
                return;
            }

            EscPosPrinter printer = new EscPosPrinter(printerConnection, 203, 72f, 48);

            Bitmap logo = ImageLoader.getBrandLogoBitmap(context);
            String dateStr = new SimpleDateFormat("dd/MM/yyyy hh:mm a", Locale.getDefault()).format(new Date());

            int lineWidth = 48;
            String separator = new String(new char[lineWidth]).replace('\0', '-');
            String starSeparator = new String(new char[lineWidth]).replace('\0', '*');

            // ---- COLUMN WIDTHS ---- //
            // PRODUCT | UNIT | QTY | TOTAL  → total 48 chars
            int COL_PRODUCT = 20;
            int COL_UNIT = 8;
            int COL_QTY = 6;
            int COL_TOTAL = 10;


            // ---- BUILD PRODUCT TABLE ---- //
            StringBuilder productLines = new StringBuilder();

            for (ProductModel p : billData.getProductModelList()) {

                String product = p.getName().toUpperCase();
                String unit = "₹" + String.format("%.2f", p.getPrice());
                String qty = String.valueOf(p.getQty());
                String total = "₹" + String.format("%.2f", p.getPrice() * p.getQty());

                List<String> lines = wrapText(product, COL_PRODUCT);

                // First line → full row
                productLines.append(
                        "[L]"
                                + padRight(lines.get(0), COL_PRODUCT)
                                + padLeft(unit, COL_UNIT)
                                + padLeft(qty, COL_QTY)
                                + padLeft(total, COL_TOTAL)
                                + "\n"
                );

                // Next lines (only product name)
                for (int i = 1; i < lines.size(); i++) {
                    productLines.append(
                            "[L]"
                                    + padRight(lines.get(i), COL_PRODUCT)
                                    + padLeft("", COL_UNIT)
                                    + padLeft("", COL_QTY)
                                    + padLeft("", COL_TOTAL)
                                    + "\n"
                    );
                }
            }

            // ---- FINAL RECEIPT ---- //
            StringBuilder receipt = new StringBuilder();

            receipt.append("[C]<img>")
                    .append(PrinterTextParserImg.bitmapToHexadecimalString(printer, logo))
                    .append("</img>\n");

            receipt.append("[C]<b>").append(printerDataModel.getShopName().toUpperCase()).append("</b>\n");

            if (StringUtils.isNotBlank(printerDataModel.getHeader1()))
                receipt.append("[C]").append(printerDataModel.getHeader1()).append("\n");
            if (StringUtils.isNotBlank(printerDataModel.getHeader2()))
                receipt.append("[C]").append(printerDataModel.getHeader2()).append("\n");
            if (StringUtils.isNotBlank(printerDataModel.getHeader3()))
                receipt.append("[C]").append(printerDataModel.getHeader3()).append("\n");

            receipt.append("[R]DATE: ").append(dateStr.toUpperCase()).append("\n");
            receipt.append("[L]").append(separator).append("\n");

            // ---- TABLE HEADER ---- //
            receipt.append(
                    "[L]"
                            + padRight("PRODUCT", COL_PRODUCT)
                            + padLeft("UNIT", COL_UNIT)
                            + padLeft("QTY", COL_QTY)
                            + padLeft("TOTAL", COL_TOTAL)
                            + "\n"
            );
            receipt.append("[L]").append(separator).append("\n");

            // ---- PRODUCT ROWS ---- //
            receipt.append(productLines.toString());
            receipt.append("[L]").append(separator).append("\n");

            // ---- TOTALS ---- //
            if (billData.getParcelCost() > 0) {
                receipt.append("[L]Parcel Charges: ₹").append(billData.getParcelCost()).append("\n");
            }

            receipt.append("[C]<b>Total: ₹").append(String.format("%.2f", billData.getSellingCost())).append("</b>\n");
            receipt.append("[L]Payment: ").append(billData.getPaymentMode()).append("\n\n");

            receipt.append("[L]").append(starSeparator).append("\n");
            if (StringUtils.isNotBlank(printerDataModel.getFooter1()))
                receipt.append("[C]").append(printerDataModel.getFooter1()).append("\n");
            if (StringUtils.isNotBlank(printerDataModel.getFooter2()))
                receipt.append("[C]").append(printerDataModel.getFooter2()).append("\n");
            if (StringUtils.isNotBlank(printerDataModel.getFooter3()))
                receipt.append("[C]").append(printerDataModel.getFooter3()).append("\n");

            receipt.append("[C]").append("*Software - Instabillz [9585905176]*").append("\n");

            receipt.append("[L]").append(starSeparator).append("\n");

            // ---- PRINT ---- //
            printer.printFormattedTextAndCut(receipt.toString());

            //printingDialog.dismiss();
            Toast.makeText(context, "Printing Success!", Toast.LENGTH_SHORT).show();

        } catch (Exception e) {

            //printingDialog.dismiss();
            Toast.makeText(context, "Printing Failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }

    }

    // ---- FORMAT HELPERS ---- //
    String padRight(String s, int n) {
        if (s.length() > n) return s.substring(0, n);
        return String.format("%-" + n + "s", s);
    }

    String padLeft(String s, int n) {
        if (s.length() > n) return s.substring(0, n);
        return String.format("%" + n + "s", s);
    }

    // ---- MULTILINE WRAP FOR PRODUCT NAME ---- //
    List<String> wrapText(String text, int width) {
        List<String> lines = new ArrayList<>();
        while (text.length() > width) {
            lines.add(text.substring(0, width));
            text = text.substring(width);
        }
        lines.add(text);
        return lines;
    }


}
