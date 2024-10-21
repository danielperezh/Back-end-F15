package com.formato15.ebsa.controllers;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/excel")
public class FileController {

    @PostMapping("/upload-merge")
    public ResponseEntity<byte[]> uploadAndMergeExcel(@RequestParam("files") List<MultipartFile> files) throws IOException {
        if (files.isEmpty() || files.size() != 2) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }

        Workbook mergedWorkbook = new XSSFWorkbook();
        Sheet mergedSheet = mergedWorkbook.createSheet("Merged Data");

        // Variables para comparar encabezados
        Row headerRow = null;
        boolean isFirstFile = true; // Para indicar si estamos en el primer archivo
        int rowCount = 0;

        for (MultipartFile file : files) {
            Workbook workbook = new XSSFWorkbook(file.getInputStream());
            Sheet sheet = workbook.getSheetAt(0);
            boolean skipHeader = false; // Variable para decidir si saltar el encabezado

            for (Row row : sheet) {
                // Si estamos en la primera fila de la primera hoja, consideramos que es el encabezado
                if (row.getRowNum() == 0) {
                    if (isFirstFile) {
                        headerRow = row; // Guardamos el encabezado del primer archivo
                        isFirstFile = false;
                    } else {
                        // Si no es el primer archivo, comparamos el encabezado
                        if (areHeadersEqual(headerRow, row)) {
                            skipHeader = true; // Si son iguales, saltamos este encabezado
                        }
                    }
                }

                // Si estamos saltando el encabezado, continuamos con los datos
                if (skipHeader && row.getRowNum() == 0) {
                    continue;
                }

                // Copiar filas y columnas al archivo combinado
                Row newRow = mergedSheet.createRow(rowCount++);
                for (int col = 0; col < row.getLastCellNum(); col++) {
                    Cell oldCell = row.getCell(col);
                    Cell newCell = newRow.createCell(col);

                    if (oldCell != null) {
                        switch (oldCell.getCellType()) {
                            case STRING:
                                newCell.setCellValue(oldCell.getStringCellValue());
                                break;
                            case NUMERIC:
                                newCell.setCellValue(oldCell.getNumericCellValue());
                                break;
                            case BOOLEAN:
                                newCell.setCellValue(oldCell.getBooleanCellValue());
                                break;
                            default:
                                newCell.setCellValue("");
                        }
                    }
                }
            }
            workbook.close();
        }

        // Escribir los datos combinados en el archivo de salida
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        mergedWorkbook.write(outputStream);
        mergedWorkbook.close();

        byte[] excelBytes = outputStream.toByteArray();
        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=merged.xlsx");

        return new ResponseEntity<>(excelBytes, headers, HttpStatus.OK);
    }

    // Método para comparar si los encabezados son iguales
    private boolean areHeadersEqual(Row header1, Row header2) {
        if (header1.getLastCellNum() != header2.getLastCellNum()) {
            return false;
        }
        for (int col = 0; col < header1.getLastCellNum(); col++) {
            Cell cell1 = header1.getCell(col);
            Cell cell2 = header2.getCell(col);

            if (cell1 == null || cell2 == null || cell1.getCellType() != cell2.getCellType()) {
                return false;
            }

            if (cell1.getCellType() == CellType.STRING && !cell1.getStringCellValue().equals(cell2.getStringCellValue())) {
                return false;
            }
        }
        return true;
    }
}
