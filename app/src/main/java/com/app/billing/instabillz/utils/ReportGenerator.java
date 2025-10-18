package com.app.billing.instabillz.utils;

import com.app.billing.instabillz.model.InvoiceModel;
import com.app.billing.instabillz.model.ProductModel;
import com.app.billing.instabillz.model.StockModel;

import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class ReportGenerator {

    public void createProductExcelReport(List<ProductModel> productModelList, File file) throws Exception {

        Workbook workbook = new XSSFWorkbook();

        prepareProductSheet(workbook, productModelList);

        // Write the output to a file
        FileOutputStream fileOut = new FileOutputStream(file.getAbsolutePath());
        workbook.write(fileOut);
        fileOut.close();
        workbook.close();

    }

    private void prepareProductSheet(Workbook workbook, List<ProductModel> productList) {

        CellStyle wrapStyle = workbook.createCellStyle();
        wrapStyle.setWrapText(true);
        Sheet sheet = workbook.createSheet("Product Details");
        Row headerRow = sheet.createRow(0);
        int cellIndex = 0;

        String[] headers = {"Name", "Price"};

        for (String key : headers) {
            Cell cell = headerRow.createCell(cellIndex++);
            cell.setCellValue(key);
            cell.setCellStyle(wrapStyle);
        }

        int rowCount = 0;
        for (ProductModel entry : productList) {
            rowCount = rowCount + 1;
            Row row = sheet.createRow(rowCount);
            Cell cell0 = row.createCell(0);
            cell0.setCellValue(entry.getName());
            cell0.setCellStyle(wrapStyle);
            Cell cell1 = row.createCell(1);
            cell1.setCellValue("Rs. " + entry.getPrice());
            cell1.setCellStyle(wrapStyle);
        }

    }

    public void createStockExcelReport(List<StockModel> stocks, File file) throws Exception {

        Workbook workbook = new XSSFWorkbook();

        prepareStockSheet(workbook, stocks);

        // Write the output to a file
        FileOutputStream fileOut = new FileOutputStream(file.getAbsolutePath());
        workbook.write(fileOut);
        fileOut.close();
        workbook.close();

    }

    private void prepareStockSheet(Workbook workbook, List<StockModel> productList) {

        CellStyle wrapStyle = workbook.createCellStyle();
        wrapStyle.setWrapText(true);
        Sheet sheet = workbook.createSheet("Stock Details");
        Row headerRow = sheet.createRow(0);
        int cellIndex = 0;

        String[] headers = {"Name", "Quantity"};

        for (String key : headers) {
            Cell cell = headerRow.createCell(cellIndex++);
            cell.setCellValue(key);
            cell.setCellStyle(wrapStyle);
        }

        int rowCount = 0;
        for (StockModel entry : productList) {
            rowCount = rowCount + 1;
            Row row = sheet.createRow(rowCount);
            Cell cell0 = row.createCell(0);
            cell0.setCellValue(entry.getName());
            cell0.setCellStyle(wrapStyle);
            Cell cell1 = row.createCell(1);
            cell1.setCellValue(entry.getQuantity() + " " + entry.getUnit());
            cell1.setCellStyle(wrapStyle);
        }

    }

    private void prepareInvoiceReportSheet(Workbook workbook, List<InvoiceModel> invoiceModelList) {

        // ✅ Create sheet
        Sheet sheet = workbook.createSheet("Invoice Report");

        // ✅ Create styles
        CellStyle headerStyle = workbook.createCellStyle();
        Font headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerStyle.setFont(headerFont);
        headerStyle.setAlignment(HorizontalAlignment.CENTER);
        headerStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        headerStyle.setFillForegroundColor(IndexedColors.LIGHT_CORNFLOWER_BLUE.getIndex());
        headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        headerStyle.setBorderBottom(BorderStyle.THIN);
        headerStyle.setBorderTop(BorderStyle.THIN);
        headerStyle.setBorderLeft(BorderStyle.THIN);
        headerStyle.setBorderRight(BorderStyle.THIN);

        CellStyle dataStyle = workbook.createCellStyle();
        dataStyle.setWrapText(true);
        dataStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        dataStyle.setBorderBottom(BorderStyle.THIN);
        dataStyle.setBorderTop(BorderStyle.THIN);
        dataStyle.setBorderLeft(BorderStyle.THIN);
        dataStyle.setBorderRight(BorderStyle.THIN);

        // ✅ Header Row
        String[] headers = {
                "Billing Date",
                "Token No",
                "Total Cost",
                "Parcel Cost",
                "Selling Cost",
                "Payment Mode",
                "Products Count"
        };

        Row headerRow = sheet.createRow(0);
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
            sheet.setColumnWidth(i, 5000);
        }

        // ✅ Fill invoice data
        int rowCount = 1;
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");

        for (InvoiceModel invoice : invoiceModelList) {
            Row row = sheet.createRow(rowCount++);

            // Convert epoch -> readable date (IST)
            String formattedDate = "";
            try {
                Instant instant = Instant.ofEpochSecond(invoice.getBillingDate());
                ZonedDateTime istTime = instant.atZone(ZoneId.of("Asia/Kolkata"));
                formattedDate = istTime.format(formatter);
            } catch (Exception e) {
                formattedDate = "N/A";
            }

            int productCount = (invoice.getProductModelList() != null)
                    ? invoice.getProductModelList().size() : 0;

            int cellIndex = 0;
            row.createCell(cellIndex++).setCellValue(formattedDate);
            row.createCell(cellIndex++).setCellValue(invoice.getToken() != null ? invoice.getToken() : 0);
            row.createCell(cellIndex++).setCellValue(invoice.getTotalCost() != null ? invoice.getTotalCost() : 0);
            row.createCell(cellIndex++).setCellValue(invoice.getParcelCost() != null ? invoice.getParcelCost() : 0);
            row.createCell(cellIndex++).setCellValue(invoice.getSellingCost() != null ? invoice.getSellingCost() : 0);
            row.createCell(cellIndex++).setCellValue(invoice.getPaymentMode() != null ? invoice.getPaymentMode() : "");
            row.createCell(cellIndex++).setCellValue(productCount);

            // Apply styles
            for (int i = 0; i < headers.length; i++) {
                row.getCell(i).setCellStyle(dataStyle);
            }
        }

        // ✅ Auto-size columns for better look
//        for (int i = 0; i < headers.length; i++) {
//            sheet.autoSizeColumn(i);
//        }
        for (int i = 0; i < headers.length; i++) {
            int width = headers[i].length() * 400;
            sheet.setColumnWidth(i, Math.min(width, 10000));
        }


        // ✅ Add footer / summary row
        int summaryRowIndex = rowCount + 1;
        Row summaryRow = sheet.createRow(summaryRowIndex);
        Cell summaryCell = summaryRow.createCell(0);
        summaryCell.setCellValue("Total Invoices: " + invoiceModelList.size());
        summaryCell.setCellStyle(headerStyle);
    }


    public void createInvoiceExcelReport(List<InvoiceModel> invoiceModelList, File file) throws IOException {
        Workbook workbook = new XSSFWorkbook();

        prepareInvoiceReportSheet(workbook, invoiceModelList);

        // Write the output to a file
        FileOutputStream fileOut = new FileOutputStream(file.getAbsolutePath());
        workbook.write(fileOut);
        fileOut.close();
        workbook.close();
    }
}
