package main.java.com.example.nminfinder.controller;

import main.java.com.example.nminfinder.service.NumberService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.io.FileOutputStream;

@RestController
@RequestMapping("/api/numbers")
@Tag(name = "Number Finder", description = "API для поиска N-го минимального числа в Excel файле")
public class NumberController {

    @Autowired
    private NumberService numberService;

    @GetMapping("/find-nth-min")
    @Operation(summary = "Найти N-ное минимальное число в Excel файле")
    public ResponseEntity<?> findNthMinNumber(
            @Parameter(description = "Путь к файлу Excel (.xlsx)", required = true, example = "test_numbers.xlsx")
            @RequestParam String filePath,

            @Parameter(description = "Позиция N (начинается с 1)", required = true, example = "3")
            @RequestParam int n) {

        System.out.println("=== НОВЫЙ ЗАПРОС ===");
        System.out.println("filePath: " + filePath);
        System.out.println("n: " + n);

        try {
            String resolvedPath = resolveFilePath(filePath);
            File file = new File(resolvedPath);

            if (!file.exists()) {
                String error = "Файл не существует: " + resolvedPath;
                return ResponseEntity.badRequest().body(error);
            }

            int result = numberService.findNthMinNumber(resolvedPath, n);
            return ResponseEntity.ok("N-ное минимальное число: " + result);

        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Ошибка: " + e.getMessage());
        }
    }

    private String resolveFilePath(String filePath) {
        File file = new File(filePath);
        if (file.exists()) return filePath;

        File currentDirFile = new File(System.getProperty("user.dir"), filePath);
        if (currentDirFile.exists()) return currentDirFile.getAbsolutePath();

        File homeDirFile = new File(System.getProperty("user.home"), filePath);
        if (homeDirFile.exists()) return homeDirFile.getAbsolutePath();

        return filePath;
    }

    @GetMapping("/test")
    @Operation(summary = "Тестовый endpoint для проверки работы сервиса")
    public ResponseEntity<String> test() {
        return ResponseEntity.ok("Сервис работает! Время: " + java.time.LocalDateTime.now());
    }

    @PostMapping("/create-test-file")
    @Operation(summary = "Создать тестовый Excel файл")
    public ResponseEntity<String> createTestFile() {
        try {
            String filePath = "test_numbers.xlsx";

            Workbook workbook = new XSSFWorkbook();
            Sheet sheet = workbook.createSheet("Numbers");

            int[] testNumbers = {10, 5, 8, 3, 1, 9, 2, 7, 6, 4};

            for (int i = 0; i < testNumbers.length; i++) {
                Row row = sheet.createRow(i);
                Cell cell = row.createCell(0);
                cell.setCellValue(testNumbers[i]);
            }

            try (FileOutputStream outputStream = new FileOutputStream(filePath)) {
                workbook.write(outputStream);
            }
            workbook.close();

            return ResponseEntity.ok("Тестовый файл создан: " + new File(filePath).getAbsolutePath());

        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Ошибка создания файла: " + e.getMessage());
        }
    }
}