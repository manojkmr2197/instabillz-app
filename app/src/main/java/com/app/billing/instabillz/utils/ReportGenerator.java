package com.app.billing.instabillz.utils;

import com.app.billing.instabillz.model.ProductModel;
import com.app.billing.instabillz.model.StockModel;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileOutputStream;
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
            cell1.setCellValue(entry.getQuantity()+" " + entry.getUnit());
            cell1.setCellStyle(wrapStyle);
        }

    }


}
