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

import java.text.SimpleDateFormat;
import java.util.Date;
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

            // Heavy operations moved to background
            Bitmap logo = BitmapFactory.decodeResource(context.getResources(), R.drawable.client_logo);
            String dateStr = new SimpleDateFormat("dd/MM/yyyy hh:mm a", Locale.getDefault()).format(new Date());
            int lineWidth = 48;
            String separator = new String(new char[lineWidth]).replace('\0', '-');
            String star_separator = new String(new char[lineWidth]).replace('\0', '*');

            StringBuilder productLines = new StringBuilder();
            for (ProductModel p : billData.getProductModelList()) {
                String name = (p.getName().length() > 20 ? p.getName().substring(0, 20) : p.getName());
                String qty = " x " + p.getQty();
                String price = "Rs." + String.format("%.2f", (p.getQty() * p.getPrice()));
                productLines.append(String.format("[L]%-24s %8s %12s\n", name, qty, price));
            }

            StringBuilder receipt = new StringBuilder();
            receipt.append("[C]<img>").append(PrinterTextParserImg.bitmapToHexadecimalString(printer, logo)).append("</img>\n");
            receipt.append("[C]<b>"+printerDataModel.getShopName().toUpperCase()+"</b>\n\n");
            receipt.append("[C]<font name='b'>+"+printerDataModel.getHeader1()+"</font>\n");
            receipt.append("[C]<font name='b'>+"+printerDataModel.getHeader2()+"</font>\n");
            receipt.append("[C]<font name='b'>+"+printerDataModel.getHeader3()+"</font>\n");
            receipt.append("[R]Date: ").append(dateStr.toUpperCase()).append("\n\n");
            receipt.append("[L]<u><b>Token No: ").append(billData.getToken()).append("</b></u>\n");
            receipt.append("[L]<b>Bill by: </b>").append(billData.getEmployeeName().toUpperCase()).append("\n");
            receipt.append("[L]").append(separator).append("\n");
            receipt.append(String.format("[L]%-24s %8s %12s\n", "PRODUCT", "QTY", "PRICE"));
            receipt.append("[L]").append(separator).append("\n");
            receipt.append(productLines.toString());
            receipt.append("[L]").append(separator).append("\n");

            if (billData.getParcelCost() > 0) {
                receipt.append("[C]<b>Bill Amount:[R]").append("Rs.").append(String.format("%.2f", billData.getTotalCost())).append("  </b>\n");
                receipt.append("[C]<b>Parcel Charges:[R]").append(String.format("%.1f", billData.getParcelCost())).append("%  </b>\n");
            }

            receipt.append("[C]<b><font size='big'>Total:[R]").append("<u>Rs.").append(String.format("%.2f", billData.getSellingCost())).append("</u></font></b>  \n\n");
            receipt.append("[L]Payment: ").append(billData.getPaymentMode()).append("\n");
            receipt.append("[L]").append(star_separator).append("\n");
            receipt.append("[C]"+printerDataModel.getFooter1()+"\n");
            receipt.append("[C]"+printerDataModel.getFooter2()+"\n");
            receipt.append("[L]").append(star_separator).append("\n");
            receipt.append("[C]"+printerDataModel.getFooter3()+"\n");
            receipt.append("[L]").append(star_separator).append("\n");

            printer.printFormattedTextAndCut(receipt.toString());

            //printingDialog.dismiss();
            Toast.makeText(context, "Printing Success!", Toast.LENGTH_SHORT).show();

        } catch (Exception e) {

            //printingDialog.dismiss();
            Toast.makeText(context, "Printing Failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }

    }


}
