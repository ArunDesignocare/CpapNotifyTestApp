package com.yantrammedtech.cpap_notifytest.Share;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import com.yantrammedtech.cpap_notifytest.ExcelFileCreator;
import com.yantrammedtech.cpap_notifytest.room.model.BatteryData;
import com.yantrammedtech.cpap_notifytest.room.model.EepromData;
import com.yantrammedtech.cpap_notifytest.room.model.EepromStatus;
import com.yantrammedtech.cpap_notifytest.room.model.NotifyData;
import com.yantrammedtech.cpap_notifytest.room.repo.RepoBattery;
import com.yantrammedtech.cpap_notifytest.room.repo.RepoEepromData;
import com.yantrammedtech.cpap_notifytest.room.repo.RepoEepromStatus;
import com.yantrammedtech.cpap_notifytest.room.repo.RepoNotifyData;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class ExcelFileCreator2 {
    private static final String TAG = "ExcelFileCreator2";
    private Context context;
    private String fileName;
    private ExcelFileCreator.FileCreationListener listener;

    public interface FileCreationListener {
        void onFileCreated(boolean result, String fileName);
    }

    public ExcelFileCreator2(Context context, String fileName, ExcelFileCreator.FileCreationListener listener) {
        this.context = context;
        this.fileName = fileName;
        this.listener = listener;
    }

    public void createExcelFile() {
        // Create a new workbook
        Workbook workbook = new XSSFWorkbook();

        // Create a new sheet
        Sheet sheet = workbook.createSheet("Sheet1");
        enterBatteryData(sheet);
        enterEepromStatusData(sheet);

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

    /**
     * ***************** CREATE BATTERY DATA ************************
     */
    private void enterBatteryData(Sheet sheet) {
        Row headerRow = sheet.createRow(0);
        Cell cell0 = headerRow.createCell(0);
        Cell cell1 = headerRow.createCell(0);
        Cell cell2 = headerRow.createCell(0);
        Cell cell3 = headerRow.createCell(0);
        Cell cell4 = headerRow.createCell(0);
        Cell cell5 = headerRow.createCell(0);
        Cell cell6 = headerRow.createCell(0);
        Cell cell7 = headerRow.createCell(0);
        Cell cell8 = headerRow.createCell(0);
        Cell cell9 = headerRow.createCell(0);

        cell0.setCellValue("id");
        cell1.setCellValue("Characteristic");
        cell2.setCellValue("flow_rate");
        cell3.setCellValue("pressure");
        cell4.setCellValue("io");
        cell5.setCellValue("respRate");
        cell6.setCellValue("pMax");
        cell7.setCellValue("pMin");
        cell8.setCellValue("airPressure");
        cell9.setCellValue("totalFlow");


        List<BatteryData> dataList = getBatteryData();
        if (dataList != null && dataList.size() > 0) {
            int i = 0;
            for (BatteryData data : dataList) {
                Row dataRow = sheet.createRow(i);
                Cell cellI = dataRow.createCell(0);
                Cell cellII = dataRow.createCell(1);
                Cell cellIII = dataRow.createCell(2);
                Cell cellIV = dataRow.createCell(3);
                Cell cellV = dataRow.createCell(4);
                Cell cellVI = dataRow.createCell(5);
                Cell cellVII = dataRow.createCell(6);
                Cell cellVIII = dataRow.createCell(7);
                Cell cellIX = dataRow.createCell(8);
                Cell cellX = dataRow.createCell(9);

                cellI.setCellValue(data.getId());
                cellII.setCellValue("Battery");
                cellIII.setCellValue(data.getFlow_rate());
                cellIV.setCellValue(data.getPressure());
                cellV.setCellValue(data.getIo());
                cellVI.setCellValue(data.getRespRate());
                cellVII.setCellValue(data.getPMax());
                cellVIII.setCellValue(data.getPMin());
                cellIX.setCellValue(data.getAirPressure());
                cellX.setCellValue(data.getTotalFlow());


                i++;
            }
        } else {
            Log.d(TAG, "createBatteryData: Battery data is null");
        }
    }

    private List<BatteryData> getBatteryData() {
        RepoBattery repoBattery = new RepoBattery(context);
        try {
            return repoBattery.getStaticData();
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
            Log.d(TAG, "getBatteryData: " + e.getMessage());
            return null;
        }
    }

    /**
     * ***************** GET EEPROM STATUS DATA ************************
     */
    private void enterEepromStatusData(Sheet sheet) {
        Row headerRow = sheet.createRow(0);
        Cell cell0 = headerRow.createCell(0);
        Cell cell1 = headerRow.createCell(0);
        Cell cell2 = headerRow.createCell(0);
//        Cell cell3 = headerRow.createCell(0);
//        Cell cell4 = headerRow.createCell(0);
//        Cell cell5 = headerRow.createCell(0);

        cell0.setCellValue("id");
        cell1.setCellValue("Characteristic");
        cell2.setCellValue("timeStamp");
//        cell3.setCellValue("Respiratory Rate");
//        cell4.setCellValue("PMax");
//        cell5.setCellValue("PMin");

        List<EepromStatus> eepromStatusList = getEepromStatusData();
        if (eepromStatusList != null && eepromStatusList.size() > 0) {
            int i = 0;
            for (EepromStatus data : eepromStatusList) {
                Row dataRow = sheet.createRow(i);
                Cell cellI = dataRow.createCell(0);
                Cell cellII = dataRow.createCell(1);
                Cell cellIII = dataRow.createCell(2);
//                Cell cellIV = dataRow.createCell(3);
//                Cell cellV = dataRow.createCell(4);
//                Cell cellVI = dataRow.createCell(4);

                cellI.setCellValue(data.getId());
                cellII.setCellValue("Eeprom Status");
                cellIII.setCellValue(data.getTimeStamp());
//                cellIV.setCellValue(data.getRespRate());
//                cellV.setCellValue(data.getpMax());
//                cellVI.setCellValue(data.getpMin());

                i++;
            }
        } else {
            Log.d(TAG, "enterEepromStatusData: data is null");
        }
    }

    private List<EepromStatus> getEepromStatusData() {
        RepoEepromStatus repoEepromStatus = new RepoEepromStatus(context);
        try {
            return repoEepromStatus.getStaticData();
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
            Log.d(TAG, "getEepromStatusData: " + e.getMessage());
            return null;
        }
    }

    /**
     * ***************** GET EEPROM DATA DATA ************************
     */
//    private void enterEepromData(Sheet sheet) {
//        Row headerRow = sheet.createRow(0);
//        Cell cell0 = headerRow.createCell(0);
//        Cell cell1 = headerRow.createCell(0);
//        Cell cell2 = headerRow.createCell(0);
//        Cell cell3 = headerRow.createCell(0);
//
//        cell0.setCellValue("id");
//        cell1.setCellValue("Characteristic");
//        cell2.setCellValue("Inhalation/Exhalation");
//        cell3.setCellValue("Respiratory Rate");
//
//        List<EepromData> eepromDataList = getEepromData();
//        if (eepromDataList != null && eepromDataList.size() > 0) {
//            int i = 0;
//            for (EepromData data : eepromDataList) {
//                Row dataRow = sheet.createRow(i);
//                Cell cellI = dataRow.createCell(0);
//                Cell cellII = dataRow.createCell(1);
//                Cell cellIII = dataRow.createCell(2);
//                Cell cellIV = dataRow.createCell(3);
//
//                cellI.setCellValue(data.getId());
//                cellII.setCellValue("Eeprom Data");
//                cellIII.setCellValue(data.getAirPressure());
//                cellIV.setCellValue(data.getTotalFlow());
//
//                i++;
//            }
//        } else {
//            Log.d(TAG, "enterEepromStatusData: data is null");
//        }
//    }
//
//    private List<EepromData> getEepromData() {
//        RepoEepromData repoEepromData = new RepoEepromData(context);
//        try {
//            return repoEepromData.getStaticData();
//        } catch (ExecutionException | InterruptedException e) {
//            e.printStackTrace();
//            Log.d(TAG, "getEepromData: " + e.getMessage());
//            return null;
//        }
//    }
}
