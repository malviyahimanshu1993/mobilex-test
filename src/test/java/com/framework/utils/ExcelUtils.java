package com.framework.utils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

/**
 * Excel data utilities for reading test data from Excel files.
 */
public final class ExcelUtils {

    private static final Logger log = LogManager.getLogger(ExcelUtils.class);

    private ExcelUtils() {
    }

    /**
     * Reads Excel file and returns data as list of maps.
     */
    public static List<Map<String, String>> readExcelFile(String filePath, String sheetName) {
        List<Map<String, String>> data = new ArrayList<>();

        try (InputStream is = Files.newInputStream(Paths.get(filePath));
             Workbook workbook = new XSSFWorkbook(is)) {

            Sheet sheet = sheetName != null && !sheetName.isEmpty()
                    ? workbook.getSheet(sheetName)
                    : workbook.getSheetAt(0);

            if (sheet == null) {
                log.error("Sheet not found: {}", sheetName);
                return data;
            }

            data = readSheet(sheet);
            log.info("Read {} rows from Excel file: {}", data.size(), filePath);

        } catch (Exception e) {
            log.error("Failed to read Excel file: {}", filePath, e);
        }

        return data;
    }

    /**
     * Reads Excel file from classpath.
     */
    public static List<Map<String, String>> readExcelFromClasspath(String resourceName, String sheetName) {
        List<Map<String, String>> data = new ArrayList<>();

        try (InputStream is = ExcelUtils.class.getClassLoader().getResourceAsStream(resourceName);
             Workbook workbook = new XSSFWorkbook(is)) {

            if (is == null) {
                log.error("Resource not found: {}", resourceName);
                return data;
            }

            Sheet sheet = sheetName != null && !sheetName.isEmpty()
                    ? workbook.getSheet(sheetName)
                    : workbook.getSheetAt(0);

            if (sheet == null) {
                log.error("Sheet not found: {}", sheetName);
                return data;
            }

            data = readSheet(sheet);

        } catch (Exception e) {
            log.error("Failed to read Excel from classpath: {}", resourceName, e);
        }

        return data;
    }

    /**
     * Converts Excel data to TestNG DataProvider format.
     */
    public static Object[][] excelToDataProvider(String filePath, String sheetName) {
        List<Map<String, String>> data = readExcelFile(filePath, sheetName);
        if (data.isEmpty()) return new Object[0][0];

        Object[][] result = new Object[data.size()][1];
        for (int i = 0; i < data.size(); i++) {
            result[i][0] = data.get(i);
        }
        return result;
    }

    private static List<Map<String, String>> readSheet(Sheet sheet) {
        List<Map<String, String>> data = new ArrayList<>();

        Iterator<Row> rowIterator = sheet.iterator();
        if (!rowIterator.hasNext()) return data;

        Row headerRow = rowIterator.next();
        List<String> headers = new ArrayList<>();
        for (Cell cell : headerRow) {
            headers.add(getCellValue(cell));
        }

        while (rowIterator.hasNext()) {
            Row row = rowIterator.next();
            Map<String, String> rowData = new LinkedHashMap<>();

            boolean hasData = false;
            for (int i = 0; i < headers.size(); i++) {
                Cell cell = row.getCell(i, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
                String value = getCellValue(cell);
                rowData.put(headers.get(i), value);
                if (!value.isEmpty()) hasData = true;
            }

            if (hasData) {
                data.add(rowData);
            }
        }

        return data;
    }

    private static String getCellValue(Cell cell) {
        if (cell == null) return "";

        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue().trim();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return cell.getLocalDateTimeCellValue().toString();
                }
                double numValue = cell.getNumericCellValue();
                if (numValue == Math.floor(numValue)) {
                    return String.valueOf((long) numValue);
                }
                return String.valueOf(numValue);
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:
                try {
                    return String.valueOf(cell.getNumericCellValue());
                } catch (Exception e) {
                    return cell.getStringCellValue();
                }
            case BLANK:
            default:
                return "";
        }
    }
}

