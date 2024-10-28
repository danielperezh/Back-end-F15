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
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/api/excel")
public class FileController {

    // Definir columnas permitidas
    private static final Set<String> ALLOWED_COLUMNS = new HashSet<>() {{
        add("Departamento DANE");
        add("Ciudad DANE");
        add("Asentamiento");
        add("Radicado Recibido");
        add("Fecha y Hora Radicación");
        add("Tipo trámite");
        add("Grupo Causal");
        add("Detalle Causal");
        add("es>Account Number");
        add("Número Factura");
        add("Tipo Respuesta");
        add("Fecha Respuesta");
        add("Radicado Respuesta");
        add("Fecha Notificación");
        add("Tipo Notificación");
        add("Fecha Transferencia SSPD");
    }};

    @PostMapping("/upload-merge")
    public ResponseEntity<byte[]> uploadAndMergeExcel(@RequestParam("files") List<MultipartFile> files) throws IOException {
        if (files.isEmpty() || files.size() != 2) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }

        Workbook mergedWorkbook = new XSSFWorkbook();
        Sheet mergedSheet = mergedWorkbook.createSheet("Merged Data");

        // Variables para comparar encabezados
        Row headerRow = null;
        boolean isFirstFile = true;
        int rowCount = 0;

        for (MultipartFile file : files) {
            Workbook workbook = new XSSFWorkbook(file.getInputStream());
            Sheet sheet = workbook.getSheetAt(0);
            boolean skipHeader = false;

            // Validar las columnas del archivo actual
            Row currentHeaderRow = sheet.getRow(0);
            if (!validateColumns(currentHeaderRow)) {
                workbook.close();
                String errorMessage = "El archivo contiene columnas no permitidas. Verifique que el archivo contenga solo las columnas requeridas.";
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorMessage.getBytes(StandardCharsets.UTF_8));
            }


            // Buscar el índice de la columna "Número Factura"
            int facturaColumnIndex = findColumnIndex(currentHeaderRow, "Número Factura");

            for (Row row : sheet) {
                if (row.getRowNum() == 0) {
                    if (isFirstFile) {
                        headerRow = row;
                        isFirstFile = false;
                    } else {
                        if (areHeadersEqual(headerRow, row)) {
                            skipHeader = true;
                        }
                    }
                }

                if (skipHeader && row.getRowNum() == 0) {
                    continue;
                }

                Row newRow = mergedSheet.createRow(rowCount++);
                for (int col = 0; col < row.getLastCellNum(); col++) {
                    Cell oldCell = row.getCell(col);
                    Cell newCell = newRow.createCell(col);
                
                    if (oldCell != null) {
                        if (col == facturaColumnIndex && row.getRowNum() > 0) { // Verificar que no sea la primera fila
                            // Modificar "Número Factura" a "N" solo en filas debajo del encabezado
                            newCell.setCellValue("N");
                        } else {
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
                                case FORMULA:
                                    newCell.setCellFormula(oldCell.getCellFormula());
                                    break;
                                default:
                                    newCell.setCellValue(""); // Celda vacía si es de tipo desconocido
                            }
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

    // Método para validar que solo las columnas permitidas están presentes
    private boolean validateColumns(Row headerRow) {
        for (Cell cell : headerRow) {
            String columnName;
            if (cell.getCellType() == CellType.STRING) {
                columnName = cell.getStringCellValue();
            } else if (cell.getCellType() == CellType.NUMERIC) {
                columnName = String.valueOf((int) cell.getNumericCellValue()); // Convierte el valor numérico a String
            } else {
                continue; // O ignora celdas que no sean de tipo STRING o NUMERIC
            }

            if (!ALLOWED_COLUMNS.contains(columnName)) {
                return false;
            }
        }
        return true;
    }


    // Método para encontrar el índice de una columna por su nombre
    private int findColumnIndex(Row headerRow, String columnName) {
        for (Cell cell : headerRow) {
            if (columnName.equals(cell.getStringCellValue())) {
                return cell.getColumnIndex();
            }
        }
        return -1;
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
