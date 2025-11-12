package main.java.com.example.nminfinder.service;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.PriorityQueue;
import java.util.Collections;

@Service
public class NumberService {

    public int findNthMinNumber(String filePath, int n) throws Exception {
        System.out.println("Starting findNthMinNumber with filePath: " + filePath + ", n: " + n);

        if (n <= 0) {
            throw new IllegalArgumentException("N должно быть положительным числом");
        }

        int[] numbers = readNumbersFromExcel(filePath);
        System.out.println("Read " + numbers.length + " numbers from file");

        if (numbers.length < n) {
            throw new IllegalArgumentException("В файле меньше " + n + " чисел. Найдено: " + numbers.length);
        }

        int result = findNthMinWithHeap(numbers, n);
        System.out.println("Found " + n + "th min number: " + result);
        return result;
    }

    private int[] readNumbersFromExcel(String filePath) throws Exception {
        System.out.println("Reading Excel file: " + filePath);

        try (FileInputStream file = new FileInputStream(filePath);
             Workbook workbook = new XSSFWorkbook(file)) {

            Sheet sheet = workbook.getSheetAt(0);
            int rowCount = sheet.getPhysicalNumberOfRows();
            System.out.println("Excel sheet has " + rowCount + " rows");

            int[] numbers = new int[rowCount];
            int validNumberCount = 0;

            for (int i = 0; i < rowCount; i++) {
                Row row = sheet.getRow(i);
                if (row != null) {
                    Cell cell = row.getCell(0);
                    if (cell != null && cell.getCellType() == CellType.NUMERIC) {
                        numbers[validNumberCount++] = (int) cell.getNumericCellValue();
                    }
                }
            }

            System.out.println("Found " + validNumberCount + " valid numbers");

            int[] result = new int[validNumberCount];
            System.arraycopy(numbers, 0, result, 0, validNumberCount);
            return result;
        }
    }

    private int findNthMinWithHeap(int[] numbers, int n) {
        PriorityQueue<Integer> maxHeap = new PriorityQueue<>(n, Collections.reverseOrder());

        for (int num : numbers) {
            if (maxHeap.size() < n) {
                maxHeap.offer(num);
            } else if (num < maxHeap.peek()) {
                maxHeap.poll();
                maxHeap.offer(num);
            }
        }

        return maxHeap.peek();
    }

    public void createTestFile() throws Exception {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Numbers");

        int[] testNumbers = {10, 5, 8, 3, 1, 9, 2, 7, 6, 4};

        for (int i = 0; i < testNumbers.length; i++) {
            Row row = sheet.createRow(i);
            Cell cell = row.createCell(0);
            cell.setCellValue(testNumbers[i]);
        }

        try (FileOutputStream outputStream = new FileOutputStream("C:\\test_numbers.xlsx")) {
            workbook.write(outputStream);
        }
        workbook.close();
        System.out.println("Тестовый файл создан: C:\\test_numbers.xlsx");
    }
}