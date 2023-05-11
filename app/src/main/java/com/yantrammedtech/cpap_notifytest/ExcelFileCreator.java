package com.yantrammedtech.cpap_notifytest;

import android.content.Context;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.yantrammedtech.cpap_notifytest.room.model.NotifyData;
import com.yantrammedtech.cpap_notifytest.room.repo.RepoNotifyData;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class ExcelFileCreator {
    private static final String TAG = "ExcelFileCreator";
    private Context context;
    private String fileName;
    private FileCreationListener listener;

    public interface FileCreationListener {
        void onFileCreated(boolean result, String fileName);
    }

    public ExcelFileCreator(Context context, String fileName, FileCreationListener listener) {
        this.context = context;
        this.fileName = fileName;
        this.listener = listener;
    }

    public void createExcelFile() {
        // Create a new workbook
        Workbook workbook = new XSSFWorkbook();

        // Create a new sheet
        Sheet sheet = workbook.createSheet("Sheet1");

        List<NotifyData> notifyData = getAllData();
        Row headerRow = sheet.createRow(0);
        Cell cell0 = headerRow.createCell(0);
        Cell cell1 = headerRow.createCell(0);
        Cell cell2 = headerRow.createCell(0);
        Cell cell3 = headerRow.createCell(0);
        Cell cell4 = headerRow.createCell(0);
        Cell cell5 = headerRow.createCell(0);
        Cell cell6 = headerRow.createCell(0);
        Cell cell7 = headerRow.createCell(0);

        cell0.setCellValue("id");
        cell1.setCellValue("characteristic");
        cell2.setCellValue("x");
        cell3.setCellValue("y");
        cell4.setCellValue("x1");
        cell5.setCellValue("y1");
        cell6.setCellValue("x2");
        cell7.setCellValue("y2");

        cell0.setCellValue("id");

        int i = 1;
        if (notifyData != null && notifyData.size() > 0) {
            for (NotifyData data : notifyData) {
                Row dataRow = sheet.createRow(i);
                Cell cellI = dataRow.createCell(0);
                Cell cellII = dataRow.createCell(1);
                Cell cellIII = dataRow.createCell(2);
                Cell cellIV = dataRow.createCell(3);
                Cell cellV = dataRow.createCell(4);
                Cell cellVI = dataRow.createCell(5);
                Cell cellVII = dataRow.createCell(6);
                Cell cellVIII = dataRow.createCell(7);

                cellI.setCellValue(data.getId());
                cellII.setCellValue(data.getCharacteristic());
                cellIII.setCellValue(data.getX());
                cellIV.setCellValue(data.getY());
                cellV.setCellValue(data.getX1());
                cellVI.setCellValue(data.getY1());
                cellVII.setCellValue(data.getX2());
                cellVIII.setCellValue(data.getY2());

                i++;
            }
        } else {
            Log.d(TAG, "createExcelFile: NO DATA FOUND");
        }

        // Save the workbook to a file
        try {
            File file = new File(context.getExternalFilesDir(Environment.DIRECTORY_DCIM), fileName + ".xlsx");
            FileOutputStream outputStream = new FileOutputStream(file);
            workbook.write(outputStream);
            outputStream.close();
            Log.d("ExcelFileCreator", "Excel file created successfully");
            listener.onFileCreated(true, fileName + ".xlsx");
        } catch (IOException e) {
            e.printStackTrace();
            Log.d(TAG, "createExcelFile: " + e.getMessage());
            listener.onFileCreated(false, "no File");
        }
    }

    private List<NotifyData> getAllData() {
        RepoNotifyData repoNotifyData = new RepoNotifyData(context);
        try {
            return repoNotifyData.getStaticData();
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
            return null;
        }
    }
}

