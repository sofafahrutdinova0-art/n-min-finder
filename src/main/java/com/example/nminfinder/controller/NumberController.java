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
            // Пробуем разные варианты пути
            String resolvedPath = resolveFilePath(filePath);
            File file = new File(resolvedPath);

            if (!file.exists()) {
                String error = "Файл не существует: " + resolvedPath +
                        "\nПроверьте пути через /api/numbers/paths";
                System.out.println("ОШИБКА: " + error);
                return ResponseEntity.badRequest().body(error);
            }

            System.out.println("Файл найден: " + resolvedPath);
            System.out.println("Размер: " + file.length() + " байт");

            int result = numberService.findNthMinNumber(resolvedPath, n);
            String success = "N-ное минимальное число: " + result;
            System.out.println("УСПЕХ: " + success);
            return ResponseEntity.ok(success);

        } catch (Exception e) {
            String error = "Ошибка: " + e.getMessage();
            System.out.println("ОШИБКА: " + error);
            e.printStackTrace();
            return ResponseEntity.badRequest().body(error);
        }
    }

    // Метод для разрешения пути к файлу
    private String resolveFilePath(String filePath) {
        File file = new File(filePath);
        if (file.exists()) return filePath;

        // Пробуем в текущей рабочей директории
        File currentDirFile = new File(System.getProperty("user.dir"), filePath);
        if (currentDirFile.exists()) return currentDirFile.getAbsolutePath();

        // Пробуем в домашней директории
        File homeDirFile = new File(System.getProperty("user.home"), filePath);
        if (homeDirFile.exists()) return homeDirFile.getAbsolutePath();

        // Возвращаем оригинальный путь (вызовет ошибку, но покажет какой путь проверялся)
        return filePath;
    }

    @GetMapping("/test")
    @Operation(summary = "Тестовый endpoint для проверки работы сервиса")
    public ResponseEntity<String> test() {
        return ResponseEntity.ok("Сервис работает! Время: " + java.time.LocalDateTime.now());
    }

    @GetMapping("/check-file")
    @Operation(summary = "Проверить существование файла")
    public ResponseEntity<?> checkFile(
            @Parameter(description = "Путь к файлу для проверки", required = true, example = "test_numbers.xlsx")
            @RequestParam String filePath) {
        try {
            String resolvedPath = resolveFilePath(filePath);
            File file = new File(resolvedPath);
            if (file.exists()) {
                return ResponseEntity.ok("Файл существует: " + resolvedPath +
                        "\nРазмер: " + file.length() + " байт" +
                        "\nАбсолютный путь: " + file.getAbsolutePath());
            } else {
                return ResponseEntity.badRequest().body("Файл НЕ существует: " + resolvedPath +
                        "\nТекущая рабочая директория: " + System.getProperty("user.dir") +
                        "\nДомашняя директория: " + System.getProperty("user.home"));
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Ошибка проверки файла: " + e.getMessage());
        }
    }

    @PostMapping("/create-test-file")
    @Operation(summary = "Создать тестовый Excel файл")
    public ResponseEntity<String> createTestFile() {
        try {
            // Создаем тестовый файл в папке пользователя или временной директории
            String userHome = System.getProperty("user.home");
            String filePath = userHome + "\\test_numbers.xlsx";

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

            return ResponseEntity.ok("Тестовый файл создан: " + filePath +
                    "\nАбсолютный путь: " + new File(filePath).getAbsolutePath());

        } catch (Exception e) {
            // Если не получилось в домашней директории, пробуем в текущей
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

                return ResponseEntity.ok("Тестовый файл создан в текущей директории: " +
                        new File(filePath).getAbsolutePath());

            } catch (Exception ex) {
                return ResponseEntity.badRequest().body("Ошибка создания файла: " + ex.getMessage() +
                        "\nТекущая директория: " + System.getProperty("user.dir"));
            }
        }
    }

    @GetMapping("/paths")
    @Operation(summary = "Получить информацию о путях")
    public ResponseEntity<String> getPathsInfo() {
        String info = "Информация о путях:\n" +
                "Текущая рабочая директория: " + System.getProperty("user.dir") + "\n" +
                "Домашняя директория пользователя: " + System.getProperty("user.home") + "\n" +
                "Временная директория: " + System.getProperty("java.io.tmpdir") + "\n" +
                "Разделитель пути: " + File.separator + "\n";

        return ResponseEntity.ok(info);
    }

    @GetMapping("/create-test-file-in-project")
    @Operation(summary = "Создать тестовый файл в папке проекта")
    public ResponseEntity<String> createTestFileInProject() {
        try {
            // Создаем файл прямо в папке проекта
            String projectDir = System.getProperty("user.dir");
            String filePath = projectDir + "\\test_numbers.xlsx";

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

            return ResponseEntity.ok("Тестовый файл создан в папке проекта: " + filePath);

        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Ошибка создания файла в проекте: " + e.getMessage());
        }
    }
}