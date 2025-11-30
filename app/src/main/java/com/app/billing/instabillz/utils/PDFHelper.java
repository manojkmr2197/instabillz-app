package com.app.billing.instabillz.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.pdf.PdfDocument;
import android.os.Environment;

import com.app.billing.instabillz.model.InvoiceModel;
import com.app.billing.instabillz.model.ProductModel;
import com.app.billing.instabillz.model.ShopsModel;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class PDFHelper {

    private Context context;

    public PDFHelper(Context context) {
        this.context = context;
    }

    public String createPdfAndShare(InvoiceModel billData, ShopsModel printerDetails, String billerName) {
        int pageWidth = 576; // For 3-inch printer @203 DPI
        int marginTop = 20;
        int y = marginTop;

        Paint paint = new Paint();
        paint.setColor(Color.BLACK);
        paint.setTextSize(22f);
        paint.setTypeface(Typeface.create(Typeface.MONOSPACE, Typeface.BOLD));

        // Estimate height
        int lineHeight = 30;
        int logoHeight = 150;
        int estimatedLines = 0;

        estimatedLines += 6; // Header lines
        estimatedLines += 4; // Address and footer
        estimatedLines += billData.getProductModelList().size(); // Items
        if (billData.getParcelCost() > 0) estimatedLines += 2;
        estimatedLines += 10; // separators, thank you, etc.

        int pageHeight = marginTop + logoHeight + (estimatedLines * lineHeight) + 100;

        // Create PDF
        PdfDocument pdfDocument = new PdfDocument();
        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 1).create();
        PdfDocument.Page page = pdfDocument.startPage(pageInfo);
        Canvas canvas = page.getCanvas();

        // Draw Logo
        Bitmap logo = ImageLoader.getBrandLogoBitmap(context);
        Bitmap scaledLogo = Bitmap.createScaledBitmap(logo, 150, 150, false);
        canvas.drawBitmap(scaledLogo, (pageWidth - scaledLogo.getWidth()) / 2f, y, paint);
        y += logoHeight + 20;

        // Header
        paint.setTextAlign(Paint.Align.CENTER);

        // Shop Name
        canvas.drawText(printerDetails.getShopName().toUpperCase(), pageWidth / 2f, y, paint);
        y += 30;

        // Header1
        if (printerDetails.getHeader1() != null && !printerDetails.getHeader1().isEmpty()) {
            paint.setTextSize(18f);
            canvas.drawText(printerDetails.getHeader1(), pageWidth / 2f, y, paint);
            y += 30;
        }

        // Header2
        if (printerDetails.getHeader2() != null && !printerDetails.getHeader2().isEmpty()) {
            paint.setTextSize(18f);
            canvas.drawText(printerDetails.getHeader2(), pageWidth / 2f, y, paint);
            y += 30;
        }

        // Header3
        if (printerDetails.getHeader3() != null && !printerDetails.getHeader3().isEmpty()) {
            paint.setTextSize(18f);
            canvas.drawText(printerDetails.getHeader3(), pageWidth / 2f, y, paint);
            y += 30;
        }

        // Add extra gap after all headers
        y += 10;


        // Date
        paint.setTextSize(16f);
        paint.setTextAlign(Paint.Align.RIGHT);
        String dateStr = new SimpleDateFormat("dd/MM/yyyy hh:mm a", Locale.getDefault()).format(new Date());
        canvas.drawText("Date: " + dateStr.toUpperCase(), pageWidth - 20, y, paint);
        y += 40;

        // Order details
        paint.setTextAlign(Paint.Align.LEFT);
        paint.setTypeface(Typeface.create(Typeface.MONOSPACE, Typeface.BOLD));
        canvas.drawText("Token No: " + billData.getToken(), 10, y, paint);
        y += 30;
        canvas.drawText("Bill by: " + billerName.toUpperCase(), 10, y, paint);
        y += 30;

        // Separator

        int starWidth = (int) paint.measureText("*");
        int starCount = pageWidth / starWidth;
        String starLine = new String(new char[starCount]).replace('\0', '*');
        paint.setTextAlign(Paint.Align.LEFT);

        int dashWidth = (int) paint.measureText("-");
        int dashCount = pageWidth / dashWidth;

        // Build the line
        String dashLine = new String(new char[dashCount]).replace('\0', '-');
        canvas.drawText(dashLine, 0, y, paint);  // start at X=0
        //canvas.drawText(dashLine, pageWidth / 2f, y, paint);
        y += 20;

        float colProductX = 10;          // Product name start
        float colUnitX    = 340;         // Unit price
        float colQtyX     = 430;         // Quantity
        float colTotalX   = 560;         // Total amount (right edge)

        // ---------------------
        //   TABLE HEADER
        // ---------------------
        paint.setTextSize(16f);

        // PRODUCT
        paint.setTextAlign(Paint.Align.LEFT);
        canvas.drawText("PRODUCT", colProductX, y, paint);

        // UNIT
        paint.setTextAlign(Paint.Align.CENTER);
        canvas.drawText("UNIT", colUnitX, y, paint);

        // QTY
        canvas.drawText("QTY", colQtyX, y, paint);

        // TOTAL
        paint.setTextAlign(Paint.Align.RIGHT);
        canvas.drawText("TOTAL", colTotalX, y, paint);

        y += 20;

        // Separator
        paint.setTextAlign(Paint.Align.LEFT);
        canvas.drawText(dashLine, 0, y, paint);
        y += 20;


        int maxNameWidth = 260; // Max width for product name area
        float linePrdHeight = 28f;

        for (ProductModel p : billData.getProductModelList()) {

            String productName = p.getName().trim();
            ArrayList<String> lines = new ArrayList<>();

            int start = 0;
            int len = productName.length();

            // Wrap product name based on PIXEL width
            while (start < len) {
                int count = paint.breakText(productName, start, len, true, maxNameWidth, null);
                lines.add(productName.substring(start, start + count));
                start += count;
            }

            // Draw product lines
            for (String line : lines) {
                paint.setTextAlign(Paint.Align.LEFT);
                canvas.drawText(line, colProductX, y, paint);
                y += linePrdHeight;
            }

            // Align other columns to FIRST line
            y -= (linePrdHeight * lines.size());

            paint.setTextAlign(Paint.Align.CENTER);
            canvas.drawText("₹" + String.format("%.2f", p.getPrice()), colUnitX, y, paint);

            canvas.drawText(String.valueOf(p.getQty()), colQtyX, y, paint);

            paint.setTextAlign(Paint.Align.RIGHT);
            canvas.drawText("₹" + String.format("%.2f", (p.getPrice()*p.getQty())), colTotalX, y, paint);

            // Go to next row
            y += (linePrdHeight + 25);

        }


        paint.setTextAlign(Paint.Align.LEFT);
        canvas.drawText(dashLine, 0, y, paint);
        y += 25;

        // Totals
        paint.setTextAlign(Paint.Align.CENTER);

        if (billData.getParcelCost() > 0) {
            canvas.drawText("Parcel Charge: ₹." + String.format("%.1f", billData.getParcelCost()), pageWidth / 2f, y, paint);
            y += 30;
            double sellingWithCourier = billData.getSellingCost();
            paint.setTextSize(22f);
            canvas.drawText("Total: ₹." + String.format("%.2f", sellingWithCourier), pageWidth / 2f, y, paint);
            y += 40;
        } else {
            paint.setTextSize(22f);
            canvas.drawText("Total: ₹." + String.format("%.2f", billData.getSellingCost()), pageWidth / 2f, y, paint);
            y += 40;
        }

        // Footer
        paint.setTextAlign(Paint.Align.LEFT);
        paint.setTextSize(16f);
        canvas.drawText("Payment: " + billData.getPaymentMode(), 10, y, paint);
        y += 30;

        // Address
        paint.setTextAlign(Paint.Align.LEFT);
        canvas.drawText(starLine, 0, y, paint);
        y += 30;
        paint.setTextAlign(Paint.Align.CENTER);

        // FOOTER 1
        if (printerDetails.getFooter1() != null && !printerDetails.getFooter1().isEmpty()) {
            canvas.drawText(printerDetails.getFooter1(), pageWidth / 2f, y, paint);
            y += 25;
        }

        // FOOTER 2
        if (printerDetails.getFooter2() != null && !printerDetails.getFooter2().isEmpty()) {
            canvas.drawText(printerDetails.getFooter2(), pageWidth / 2f, y, paint);
            y += 25;
        }

        // FOOTER 3
        if (printerDetails.getFooter3() != null && !printerDetails.getFooter3().isEmpty()) {
            canvas.drawText(printerDetails.getFooter3(), pageWidth / 2f, y, paint);
            y += 25;
        }


        canvas.drawText("*Software - Instabillz [9585905176]*", pageWidth / 2f, y, paint);
        y += 25;


        // Separator line
        paint.setTextAlign(Paint.Align.LEFT);
        canvas.drawText(starLine, 0, y, paint);


        pdfDocument.finishPage(page);

        return SavePDFFileAndShare(billData, pdfDocument);
    }


    private String SavePDFFileAndShare(InvoiceModel billData, PdfDocument pdfDocument) {
        // Save PDF
        String fileName = "invoice-" + billData.getBillingDate() + "-" +billData.getToken()+"-"+ OffsetDateTime.now().toEpochSecond() + ".pdf";
        File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), fileName);
        try {
            pdfDocument.writeTo(new FileOutputStream(file));
        } catch (IOException e) {
            e.printStackTrace();
        }
        pdfDocument.close();
        return fileName;
    }

}
