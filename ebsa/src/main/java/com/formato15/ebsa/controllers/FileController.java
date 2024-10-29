package com.formato15.ebsa.controllers;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;

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

    // Códigos de ciudades permitidos
    private static final Set<String> CODES_DEPARTAMENTO_15 = new HashSet<>(Arrays.asList(
        "001", "022", "047", "051", "087", "090", "092", "097", "104", "106", 
        "109", "114", "131", "135", "162", "172", "176", "180", "183", "185", 
        "187", "189", "204", "212", "215", "218", "223", "224", "226", "232", 
        "236", "238", "244", "248", "272", "276", "293", "296", "299", "317", 
        "322", "325", "332", "362", "367", "368", "377", "380", "401", "403", 
        "407", "425", "442", "455", "464", "466", "469", "476", "480", "491", 
        "494", "500", "507", "511", "514", "516", "518", "522", "531", "533", 
        "537", "542", "550", "572", "580", "599", "600", "621", "632", "638", 
        "646", "660", "664", "667", "673", "676", "681", "686", "690", "693", 
        "696", "720", "723", "740", "753", "755", "757", "759", "761", "762", 
        "763", "764", "774", "776", "778", "790", "798", "804", "806", "808", 
        "810", "814", "816", "820", "822", "832", "835", "837", "839", "842", 
        "861", "879", "897"
    ));

    private static final Set<String> CODES_DEPARTAMENTO_68 = new HashSet<>(Arrays.asList(
        "001", "013", "020", "051", "077", "079", "081", "092", "101", "121", 
        "132", "147", "152", "160", "162", "167", "169", "176", "179", "190", 
        "207", "209", "211", "217", "229", "235", "245", "250", "255", "264", 
        "266", "271", "276", "296", "298", "307", "318", "320", "322", "324", 
        "327", "344", "368", "370", "377", "385", "397", "406", "418", "425", 
        "432", "444", "464", "468", "498", "500", "502", "522", "524", "533", 
        "547", "549", "572", "573", "575", "615", "655", "669", "673", "679", 
        "682", "684", "686", "689", "705", "720", "745", "755", "770", "773", 
        "780", "820", "855", "861", "867", "872", "895"
    ));


    @PostMapping("/validation")
    public ResponseEntity<String> uploadExcel(@RequestParam("file") MultipartFile file) {
        try (Workbook workbook = new XSSFWorkbook(file.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);
            
            // Validar encabezado del archivo
            Row headerRow = sheet.getRow(0);
            if (!validateColumns(headerRow)) {
                String errorMessage = "El archivo contiene columnas no permitidas. Verifique que el archivo contenga solo las columnas requeridas.";
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorMessage);
            }

            // Buscar el índice de las columnas necesarias
            int departamentoDANEIndex = findColumnIndex(headerRow, "Departamento DANE");
            int ciudadDANEIndex = findColumnIndex(headerRow, "Ciudad DANE");
            int fechaRadicacionIndex = findColumnIndex(headerRow, "Fecha y Hora Radicación");
            int fechaRespuestaIndex = findColumnIndex(headerRow, "Fecha Respuesta");
            int fechaNotificacionIndex = findColumnIndex(headerRow, "Fecha Notificación");
            int facturaColumnIndex = findColumnIndex(headerRow, "Número Factura");

            // Validar cada fila del archivo
            for (Row row : sheet) {
                if (row.getRowNum() > 0) { // Ignorar la fila de encabezado
                    // Validar Departamento y Ciudad
                    Cell departamentoDANE = row.getCell(departamentoDANEIndex);
                    Cell ciudadDANE = row.getCell(ciudadDANEIndex);

                    String departamentoDANEValue = getCellStringValue(departamentoDANE);
                    String ciudadDANEValue = getCellStringValue(ciudadDANE);

                    // Validación para código de departamento nulo o igual a "0"
                    if (departamentoDANEValue == null || "0".equals(departamentoDANEValue)) {
                        String errorMessage = String.format("El código del departamento no puede ser nulo o igual a 0 en la fila %d.", row.getRowNum() + 1);
                        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorMessage);
                    }

                    // Validaciones de Ciudad DANE para departamentos específicos
                    if ("15".equals(departamentoDANEValue) && !CODES_DEPARTAMENTO_15.contains(ciudadDANEValue)) {
                        String errorMessage = String.format("El código de ciudad %s no es válido para el departamento 15 en la fila %d.", ciudadDANEValue, row.getRowNum() + 1);
                        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorMessage);
                    } else if ("68".equals(departamentoDANEValue) && !CODES_DEPARTAMENTO_68.contains(ciudadDANEValue)) {
                        String errorMessage = String.format("El código de ciudad %s no es válido para el departamento 68 en la fila %d.", ciudadDANEValue, row.getRowNum() + 1);
                        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorMessage);
                    }

                    // Validar fechas
                    Cell fechaRadicacionCell = row.getCell(fechaRadicacionIndex);
                    Cell fechaRespuestaCell = row.getCell(fechaRespuestaIndex);
                    Cell fechaNotificacionCell = row.getCell(fechaNotificacionIndex);

                    // Validar Fecha Respuesta
                    if (fechaRadicacionCell != null && fechaRespuestaCell != null) {
                        if (fechaRespuestaCell.getCellType() == CellType.NUMERIC && DateUtil.isCellDateFormatted(fechaRespuestaCell)) {
                            Date fechaRadicacion = fechaRadicacionCell.getDateCellValue();
                            Date fechaRespuesta = fechaRespuestaCell.getDateCellValue();

                            if (fechaRespuesta.before(fechaRadicacion)) {
                                String errorMessage = String.format("La fecha de respuesta en la fila %d, columna '%s' debe ser mayor o igual a la fecha y hora de radicación en la fila %d, columna '%s'.",
                                        row.getRowNum() + 1,
                                        headerRow.getCell(fechaRespuestaIndex).getStringCellValue(),
                                        row.getRowNum() + 1,
                                        headerRow.getCell(fechaRadicacionIndex).getStringCellValue());
                                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorMessage);
                            }
                        }
                    }

                    // Validar Fecha Notificación
                    if (fechaRespuestaCell != null && fechaNotificacionCell != null) {
                        if (fechaNotificacionCell.getCellType() == CellType.NUMERIC && DateUtil.isCellDateFormatted(fechaNotificacionCell)) {
                            Date fechaRespuesta = fechaRespuestaCell.getDateCellValue();
                            Date fechaNotificacion = fechaNotificacionCell.getDateCellValue();

                            if (fechaNotificacion.before(fechaRespuesta)) {
                                String errorMessage = String.format("La fecha de notificación en la fila %d, columna '%s' debe ser mayor o igual a la fecha y hora de respuesta en la fila %d, columna '%s'.",
                                        row.getRowNum() + 1,
                                        headerRow.getCell(fechaNotificacionIndex).getStringCellValue(),
                                        row.getRowNum() + 1,
                                        headerRow.getCell(fechaRespuestaIndex).getStringCellValue());
                                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorMessage);
                            }
                        }
                    }

                    // Modificar "Número Factura" a "N"
                    Cell facturaCell = row.getCell(facturaColumnIndex);
                    if (facturaCell != null) {
                        facturaCell.setCellValue("N");
                    }
                }
            }

            // Respuesta exitosa si el archivo cumple con todas las validaciones
            return ResponseEntity.ok("El archivo ha sido validado exitosamente.");
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al procesar el archivo: " + e.getMessage());
        }
    }

    @PostMapping("/preview")
    public ResponseEntity<List<Map<String, String>>> previewExcel(@RequestParam("file") MultipartFile file) {
        List<Map<String, String>> rows = new ArrayList<>();

        try (Workbook workbook = new XSSFWorkbook(file.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);

            // Validate header row
            Row headerRow = sheet.getRow(0);
            if (!validateColumns(headerRow)) {
                return ResponseEntity.badRequest().body(Collections.singletonList(Map.of("error", "El archivo contiene columnas no permitidas. Verifique que el archivo contenga solo las columnas requeridas.")));
            }

            for (Row row : sheet) {
                if (row.getRowNum() == 0) continue; // Skip header row

                Map<String, String> rowData = new LinkedHashMap<>();
                for (Cell cell : row) {
                    String columnName = headerRow.getCell(cell.getColumnIndex()).getStringCellValue();
                    rowData.put(columnName, getCellStringValue(cell));
                }
                rows.add(rowData);
            }
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Collections.singletonList(Map.of("error", "Error al procesar el archivo: " + e.getMessage())));
        }

        return ResponseEntity.ok(rows);
    }

    // Método para validar que solo las columnas permitidas están presentes
    private boolean validateColumns(Row headerRow) {
        for (Cell cell : headerRow) {
            if (cell.getCellType() == CellType.STRING) {
                String columnName = cell.getStringCellValue();
                if (!ALLOWED_COLUMNS.contains(columnName)) {
                    return false;
                }
            } else if (cell.getCellType() == CellType.NUMERIC) {
                String columnName = String.valueOf((int) cell.getNumericCellValue()); // Convierte el valor numérico a String
                if (!ALLOWED_COLUMNS.contains(columnName)) {
                    return false;
                }
            }
        }
        return true;
    }

    // Método para encontrar el índice de una columna por su nombre
    private int findColumnIndex(Row headerRow, String columnName) {
        for (Cell cell : headerRow) {
            if (columnName.equals(getCellStringValue(cell))) {
                return cell.getColumnIndex();
            }
        }
        return -1;
    }

    // Método para obtener el valor de una celda como String
    private String getCellStringValue(Cell cell) {
        if (cell == null) return "";
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue();
            case NUMERIC:
                return String.valueOf((int) cell.getNumericCellValue()); // Asumiendo que el valor numérico debe tratarse como entero
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            default:
                return "";
        }
    }
}
