package com.formato15.ebsa.controllers;

import org.apache.commons.io.output.ByteArrayOutputStream;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.formato15.ebsa.clases.Cuenta;
import com.formato15.ebsa.clases.Formato15;
import com.formato15.ebsa.clases.FormatoSiec;
import com.formato15.ebsa.service.CuentaService;
import com.formato15.ebsa.service.DataService;
import com.formato15.ebsa.service.Formato15Service;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



@RestController
@RequestMapping("/api/excel")
public class FileController {

    private static final Logger log = LoggerFactory.getLogger(FileController.class);

    private final Formato15Service formato15Service;

    public FileController(Formato15Service formato15Service) {
            this.formato15Service = formato15Service;
    }

    // Definir columnas permitidas
    private static final Set<String> ALLOWED_COLUMNS = new HashSet<>() {
        {
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
        }
    };

    // Códigos de ciudades permitidos
    private static final Set<String> CODES_DEPARTAMENTO_15 = new HashSet<>(Arrays.asList(
            "001", "1", "022", "047", "051", "087", "090", "092", "097", "104", "106",
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
            "861", "879", "897"));

    private static final Set<String> CODES_DEPARTAMENTO_68 = new HashSet<>(Arrays.asList(
            "001", "1", "013", "020", "051", "077", "079", "081", "092", "101", "121",
            "132", "147", "152", "160", "162", "167", "169", "176", "179", "190",
            "207", "209", "211", "217", "229", "235", "245", "250", "255", "264",
            "266", "271", "276", "296", "298", "307", "318", "320", "322", "324",
            "327", "344", "368", "370", "377", "385", "397", "406", "418", "425",
            "432", "444", "464", "468", "498", "500", "502", "522", "524", "533",
            "547", "549", "572", "573", "575", "615", "655", "669", "673", "679",
            "682", "684", "686", "689", "705", "720", "745", "755", "770", "773",
            "780", "820", "855", "861", "867", "872", "895"));

    private static final List<Integer> CODIGOS_DETALLE_CAUSAL_P = Arrays.asList(303, 304, 305, 306);
    private static final List<Integer> CODIGOS_DETALLE_CAUSAL_F = Arrays.asList(101, 102, 103, 104, 105, 106, 107, 108,
            109, 110, 111, 112, 113, 114, 115, 116, 117, 118, 119, 120, 121, 122, 123, 124);

    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd-MM-yyyy");

    // Variable temporal para almacenar los datos guardados
    private List<Map<String, String>> savedData = new ArrayList<>();

    // @PostMapping("/validation")
    // public ResponseEntity<String> uploadExcel(@RequestParam("file") MultipartFile file) {
    //     try (Workbook workbook = new XSSFWorkbook(file.getInputStream())) {
    //         Sheet sheet = workbook.getSheetAt(0);

    //         // Validar encabezado del archivo
    //         Row headerRow = sheet.getRow(0);
    //         if (!validateColumns(headerRow)) {
    //             String errorMessage = "El archivo contiene columnas no permitidas. Verifique que el archivo contenga solo las columnas requeridas.";
    //             return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorMessage);
    //         }

    //         // Buscar el índice de las columnas necesarias
    //         int departamentoDANEIndex = findColumnIndex(headerRow, "Departamento DANE");
    //         int ciudadDANEIndex = findColumnIndex(headerRow, "Ciudad DANE");
    //         int fechaRadicacionIndex = findColumnIndex(headerRow, "Fecha y Hora Radicación");
    //         int fechaRespuestaIndex = findColumnIndex(headerRow, "Fecha Respuesta");
    //         int fechaNotificacionIndex = findColumnIndex(headerRow, "Fecha Notificación");

    //         // Validar cada fila del archivo
    //         for (Row row : sheet) {
    //             if (row.getRowNum() > 0) { // Ignorar la fila de encabezado
    //                 // Validar Departamento y Ciudad
    //                 Cell departamentoDANE = row.getCell(departamentoDANEIndex);
    //                 Cell ciudadDANE = row.getCell(ciudadDANEIndex);

    //                 String departamentoDANEValue = getCellStringValue(departamentoDANE);
    //                 String ciudadDANEValue = getCellStringValue(ciudadDANE);

    //                 // Validación para código de departamento nulo o igual a "0"
    //                 if (departamentoDANEValue == null || "0".equals(departamentoDANEValue)) {
    //                     String errorMessage = String.format(
    //                             "El código del departamento no puede ser nulo o igual a 0 en la fila %d.",
    //                             row.getRowNum() + 1);
    //                     return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorMessage);
    //                 }

    //                 // Validaciones de Ciudad DANE para departamentos específicos
    //                 if ("15".equals(departamentoDANEValue) && !CODES_DEPARTAMENTO_15.contains(ciudadDANEValue)) {
    //                     String errorMessage = String.format(
    //                             "El código de ciudad %s no es válido para el departamento 15 en la fila %d.",
    //                             ciudadDANEValue, row.getRowNum() + 1);
    //                     return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorMessage);
    //                 } else if ("68".equals(departamentoDANEValue) && !CODES_DEPARTAMENTO_68.contains(ciudadDANEValue)) {
    //                     String errorMessage = String.format(
    //                             "El código de ciudad %s no es válido para el departamento 68 en la fila %d.",
    //                             ciudadDANEValue, row.getRowNum() + 1);
    //                     return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorMessage);
    //                 }

    //                 // Validar fechas
    //                 Cell fechaRadicacionCell = row.getCell(fechaRadicacionIndex);
    //                 Cell fechaRespuestaCell = row.getCell(fechaRespuestaIndex);
    //                 Cell fechaNotificacionCell = row.getCell(fechaNotificacionIndex);

    //                 // Validar Fecha Respuesta
    //                 if (fechaRadicacionCell != null && fechaRespuestaCell != null) {
    //                     if (fechaRespuestaCell.getCellType() == CellType.NUMERIC
    //                             && DateUtil.isCellDateFormatted(fechaRespuestaCell)) {
    //                         Date fechaRadicacion = fechaRadicacionCell.getDateCellValue();
    //                         Date fechaRespuesta = fechaRespuestaCell.getDateCellValue();

    //                         if (fechaRespuesta.before(fechaRadicacion)) {
    //                             String errorMessage = String.format(
    //                                     "La fecha de respuesta en la fila %d, columna '%s' debe ser mayor o igual a la fecha y hora de radicación en la fila %d, columna '%s'.",
    //                                     row.getRowNum() + 1,
    //                                     headerRow.getCell(fechaRespuestaIndex).getStringCellValue(),
    //                                     row.getRowNum() + 1,
    //                                     headerRow.getCell(fechaRadicacionIndex).getStringCellValue());
    //                             return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorMessage);
    //                         }
    //                     }
    //                 }

    //                 // Validar Fecha Notificación
    //                 if (fechaRespuestaCell != null && fechaNotificacionCell != null) {
    //                     if (fechaNotificacionCell.getCellType() == CellType.NUMERIC
    //                             && DateUtil.isCellDateFormatted(fechaNotificacionCell)) {
    //                         Date fechaRespuesta = fechaRespuestaCell.getDateCellValue();
    //                         Date fechaNotificacion = fechaNotificacionCell.getDateCellValue();

    //                         if (fechaNotificacion.before(fechaRespuesta)) {
    //                             String errorMessage = String.format(
    //                                     "La fecha de notificación en la fila %d, columna '%s' debe ser mayor o igual a la fecha y hora de respuesta en la fila %d, columna '%s'.",
    //                                     row.getRowNum() + 1,
    //                                     headerRow.getCell(fechaNotificacionIndex).getStringCellValue(),
    //                                     row.getRowNum() + 1,
    //                                     headerRow.getCell(fechaRespuestaIndex).getStringCellValue());
    //                             return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorMessage);
    //                         }
    //                     }
    //                 }

    //             }
    //         }

    //         // Respuesta exitosa si el archivo cumple con todas las validaciones
    //         return ResponseEntity.ok("El archivo ha sido validado exitosamente.");
    //     } catch (IOException e) {
    //         return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
    //                 .body("Error al procesar el archivo: " + e.getMessage());
    //     }
    // }

    @PostMapping("/preview")
    public ResponseEntity<List<Map<String, String>>> previewExcel(@RequestParam("file") MultipartFile file) {
        List<Map<String, String>> rows = new ArrayList<>();

        try (Workbook workbook = new XSSFWorkbook(file.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);
            Row headerRow = sheet.getRow(0);

            // Validar las columnas del archivo
            if (!validateColumns(headerRow)) {
                return ResponseEntity.badRequest().body(
                        Collections.singletonList(Map.of("error", "El archivo contiene columnas no permitidas."))
                );
            }

            // Obtener los nombres de las columnas en el orden original
            List<String> columnNames = new ArrayList<>();
            for (Cell cell : headerRow) {
                columnNames.add(cell.getStringCellValue());
            }

            // Procesar las filas del archivo
            for (Row row : sheet) {
                if (row.getRowNum() == 0) // Saltar la fila del encabezado
                    continue;

                Map<String, String> rowData = new LinkedHashMap<>();

                // Variables para los valores que serán validados
                String departamentoDANEValue = null;
                String ciudadDANEValue = null;
                String matricula = null;
                String accountNumber = null;

                // Iterar sobre las columnas y obtener los datos de la fila
                for (int i = 0; i < columnNames.size(); i++) {
                    String columnName = columnNames.get(i);
                    Cell cell = row.getCell(i);

                    // Obtener los valores relevantes para la validación
                    if ("Departamento DANE".equals(columnName)) {
                        departamentoDANEValue = getCellStringValue(cell);
                    } else if ("Ciudad DANE".equals(columnName)) {
                        ciudadDANEValue = getCellStringValue(cell);
                    } else if ("Matrícula".equals(columnName)) {
                        matricula = getCellStringValue(cell);
                    } else if ("Account Number".equals(columnName)) {
                        accountNumber = getCellStringValue(cell);
                    }

                    // Procesar columnas de fecha
                    if (columnName.equals("Fecha y Hora Radicación") || columnName.equals("Fecha Respuesta")
                            || columnName.equals("Fecha Notificación") || columnName.equals("Fecha Transferencia SSPD")) {
                        if (columnName.equals("Fecha Transferencia SSPD") 
                                && (cell == null || getCellStringValue(cell).isEmpty())) {
                            rowData.put(columnName, "N");
                        } else {
                            rowData.put(columnName, formatCellDate(cell));
                        }
                    } else {
                        rowData.put(columnName, getCellStringValue(cell));
                    }
                }

                // Validar y corregir los datos con cuentaService
                if (matricula != null) {
                    // Convertir la matrícula a Long
                    Long matriculaLong = Long.valueOf(matricula);
                    Optional<Cuenta> cuentaOptional = cuentaService.getCuentaPorMatricula(matriculaLong);

                    if (cuentaOptional.isPresent()) {
                        Cuenta cuenta = cuentaOptional.get();

                        Integer departamentoDANE = Integer.valueOf(departamentoDANEValue);
                        Integer ciudadDANE = Integer.valueOf(ciudadDANEValue);

                        // Validar y corregir los datos de "Departamento DANE" y "Ciudad DANE"
                        if (!cuenta.getDepartamento().equals(departamentoDANE) || 
                            !cuenta.getMunicipio().equals(ciudadDANE)) {

                            // Corregir los valores en rowData
                            rowData.put("Departamento DANE", String.valueOf(cuenta.getDepartamento()));
                            rowData.put("Ciudad DANE", String.valueOf(cuenta.getMunicipio()));

                            // Registrar un mensaje de advertencia en los logs
                            log.warn(String.format(
                                "Fila con número de cuenta %s: El Departamento DANE (%s) o Ciudad DANE (%s) eran incorrectos. "
                                + "Se corrigieron automáticamente a Departamento: %s, Municipio: %s.",
                                accountNumber, departamentoDANEValue, ciudadDANEValue, 
                                cuenta.getDepartamento(), cuenta.getMunicipio()
                            ));
                        }
                    } else {
                        // Si no se encuentra la matrícula en la base de datos, registrar un error
                        log.error(String.format("No se encontró información para la matrícula %s en la base de datos.", matricula));
                        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                .body(Collections.singletonList(Map.of(
                                        "error", String.format("No se encontró información para la matrícula %s en la base de datos.", matricula)
                                )));
                    }
                }

                // Agregar los datos procesados a la lista de filas
                rows.add(rowData);
            }
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Collections.singletonList(Map.of("error", "Error al procesar el archivo: " + e.getMessage())));
        }

        // Retornar las filas procesadas
        return ResponseEntity.ok(rows);
    }

    

    // @GetMapping("/searchByYearAndMonth")
    // public ResponseEntity<List<FormatoSiec>> searchByYearAndMonth(
    //         @RequestParam("ano") Integer ano,
    //         @RequestParam("mes") Integer mes) {
    //     try {
    //         List<FormatoSiec> results = formato15Service.findByYearAndMonth(ano, mes);
    //         if (results.isEmpty()) {
    //             return ResponseEntity.status(HttpStatus.NOT_FOUND)
    //                     .body(Collections.emptyList());
    //         }
    //         return ResponseEntity.ok(results);
    //     } catch (Exception e) {
    //         return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
    //                 .body(Collections.emptyList());
    //     }
    // }

    @GetMapping("/searchByYearAndMonth")
    public ResponseEntity<List<FormatoSiec>> searchByYearAndMonth(
            @RequestParam("ano") Integer ano,
            @RequestParam("mes") Integer mes) {
        try {
            List<FormatoSiec> results = formato15Service.findByYearAndMonth(ano, mes);
            if (results.isEmpty()) {
                return ResponseEntity.notFound().build(); // Return 404 Not Found
            } else {
                return ResponseEntity.ok(results); // Return 200 OK with the results
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                   .body(Collections.emptyList());
        }
    }


    @Autowired
    private DataService dataService;

    @Autowired
    private CuentaService cuentaService;

    // Método para validar y guardar datos en un solo paso
    @PostMapping("/validateAndSaveFile")
    public ResponseEntity<?> validateAndSaveFile(@RequestBody List<Map<String, String>> editedData) {
        this.savedData = new ArrayList<>(editedData);

        // Validación de datos
        for (Map<String, String> rowData : savedData) {
            String departamentoDANEValue = rowData.get("Departamento DANE");
            String ciudadDANEValue = rowData.get("Ciudad DANE");
            String grupoCausal = rowData.get("Grupo Causal");
            String detalleCausalStr = rowData.get("Detalle Causal");
            String accountNumber = rowData.get("es>Account Number");

            // Obtener los primeros 6 dígitos del número de cuenta
            //String matricula = accountNumber.substring(0, 6);
            Long matricula = Long.parseLong(accountNumber.substring(0, 6)); // Asumiendo que es un Long
            Integer departamentoDANE = Integer.parseInt(departamentoDANEValue); // Asumiendo que es un Integer
            Integer ciudadDANE = Integer.parseInt(ciudadDANEValue); // Asumiendo que es un Integer

            // Validar Departamento y Ciudad
            if (departamentoDANEValue == null || "0".equals(departamentoDANEValue)) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body("El código del departamento no puede ser nulo o igual a 0.");
            }
            if ("15".equals(departamentoDANEValue) && !CODES_DEPARTAMENTO_15.contains(ciudadDANEValue)) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(String.format("El código de ciudad %s no es válido para el departamento 15.",
                                ciudadDANEValue));
            } else if ("68".equals(departamentoDANEValue) && !CODES_DEPARTAMENTO_68.contains(ciudadDANEValue)) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(String.format("El código de ciudad %s no es válido para el departamento 68.",
                                ciudadDANEValue));
            }

            // Consultar en la base de datos los datos asociados a la matrícula
            // Optional<Cuenta> cuentaOptional = cuentaService.getCuentaPorMatricula(matricula);

            // if (cuentaOptional.isPresent()) {
            //     Cuenta cuenta = cuentaOptional.get();
            
            //     // Validar que los datos de departamento y ciudad coincidan
            //     if (!cuenta.getDepartamento().equals(departamentoDANE) || 
            //         !cuenta.getMunicipio().equals(ciudadDANE)) {
            //         return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            //                 .body(String.format(
            //                     "Error en la fila con número de cuenta %s: El Departamento DANE (%s) o Ciudad DANE (%s) no coincide con los datos en la base de datos (Departamento: %s, Municipio: %s).",
            //                     accountNumber, departamentoDANEValue, ciudadDANEValue, cuenta.getDepartamento(), cuenta.getMunicipio()
            //                 ));
            //     }
            // }

            // Consultar en la base de datos los datos asociados a la matrícula
            Optional<Cuenta> cuentaOptional = cuentaService.getCuentaPorMatricula(matricula);

            if (cuentaOptional.isPresent()) {
                Cuenta cuenta = cuentaOptional.get();

                // Validar y corregir los datos de departamento y ciudad
                if (!cuenta.getDepartamento().equals(departamentoDANE) || 
                    !cuenta.getMunicipio().equals(ciudadDANE)) {
                    
                    // Corregir los valores en rowData
                    rowData.put("Departamento DANE", String.valueOf(cuenta.getDepartamento()));
                    rowData.put("Ciudad DANE", String.valueOf(cuenta.getMunicipio()));

                    // Registrar un mensaje de advertencia en los logs
                    log.warn(String.format(
                        "Fila con numero de cuenta %s: El Departamento DANE (%s) o Ciudad DANE (%s) eran incorrectos. Se corrigieron automaticamente a Departamento: %s, Municipio: %s.",
                        accountNumber, departamentoDANEValue, ciudadDANEValue, cuenta.getDepartamento(), cuenta.getMunicipio()
                    ));
                }
            } else {
                // Si no se encuentra la matrícula en la base de datos, registrar un error
                log.error(String.format("No se encontró información para la matrícula %s en la base de datos.", matricula));
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(String.format("No se encontró información para la matrícula %s en la base de datos.", matricula));
            }


            // Validación de Grupo Causal y Detalle Causal
            try {
                Integer detalleCausal = Integer.parseInt(detalleCausalStr);

                if ("P".equalsIgnoreCase(grupoCausal) && !CODIGOS_DETALLE_CAUSAL_P.contains(detalleCausal)) {
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .body("Error: Para el grupo causal 'P', el código de detalle causal debe ser uno de los siguientes: 303, 304, 305, 306.");
                } else if ("F".equalsIgnoreCase(grupoCausal) && !CODIGOS_DETALLE_CAUSAL_F.contains(detalleCausal)) {
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .body("Error: Para el grupo causal 'F', el código de detalle causal debe estar entre 101 y 124.");
                }
            } catch (NumberFormatException e) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body("Error: El valor de Detalle Causal debe ser un número entero.");
            }

            // Validación de fechas
            try {
                Date fechaRadicacion = parseDate(rowData.get("Fecha y Hora Radicación"));
                Date fechaRespuesta = parseDate(rowData.get("Fecha Respuesta"));
                Date fechaNotificacion = parseDate(rowData.get("Fecha Notificación"));

                if (fechaRespuesta != null && fechaRadicacion != null && fechaRespuesta.before(fechaRadicacion)) {
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .body("La fecha de respuesta debe ser mayor o igual a la fecha y hora de radicación.");
                }
                if (fechaNotificacion != null && fechaRespuesta != null && fechaNotificacion.before(fechaRespuesta)) {
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .body("La fecha de notificación debe ser mayor o igual a la fecha de respuesta.");
                }
            } catch (ParseException e) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body("Error al analizar las fechas. Asegúrese de que el formato de fecha sea correcto.");
            }
        }
        // Esta correcion se debe realizar una vez se haga la previsualizacion 
        // Guardar datos temporalmente y generar el archivo de Excel
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Hoja1");
            Row headerRow = sheet.createRow(0);
            Map<String, String> firstRow = savedData.get(0);
            int cellIndex = 0;

            for (String key : firstRow.keySet()) {
                Cell cell = headerRow.createCell(cellIndex++);
                cell.setCellValue(key);
            }

            int rowIndex = 1;
            for (Map<String, String> rowData : savedData) {
                Row row = sheet.createRow(rowIndex++);
                cellIndex = 0;
                for (String value : rowData.values()) {
                    Cell cell = row.createCell(cellIndex++);
                    cell.setCellValue(value);
                }
            }

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);
            byte[] fileBytes = outputStream.toByteArray();

            HttpHeaders headers = new HttpHeaders();
            headers.setContentDispositionFormData("attachment", "Formato15.xlsx");
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);

            return new ResponseEntity<>(fileBytes, headers, HttpStatus.OK);
            //return ResponseEntity.ok("Los datos han sido validados exitosamente.");
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al generar el archivo Excel.");
        }
    }

    // Método para enviar los datos a la base de datos
    @PostMapping("/sendToDatabase")
    public ResponseEntity<String> sendDataToDatabase() {
        if (savedData == null || savedData.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("No hay datos validados para enviar.");
        }

        try {
            dataService.saveData(savedData);
            return ResponseEntity.ok("Los datos han sido enviados exitosamente a la base de datos.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Error al guardar los datos en la base de datos.");
        }
    }


    // Método auxiliar para convertir una cadena de texto a un objeto Date
    private Date parseDate(String dateStr) throws ParseException {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
        return dateFormat.parse(dateStr);
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
                String columnName = String.valueOf((int) cell.getNumericCellValue()); // Convierte el valor numérico a
                                                                                      // String
                if (!ALLOWED_COLUMNS.contains(columnName)) {
                    return false;
                }
            }
        }
        return true;
    }

    // Método para encontrar el índice de una columna por su nombre
    // private int findColumnIndex(Row headerRow, String columnName) {
    //     for (Cell cell : headerRow) {
    //         if (columnName.equals(getCellStringValue(cell))) {
    //             return cell.getColumnIndex();
    //         }
    //     }
    //     return -1;
    // }

    // Método para obtener el valor de una celda como String
    private String getCellStringValue(Cell cell) {
        if (cell == null)
            return "";
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue();
            case NUMERIC:
                return String.valueOf((int) cell.getNumericCellValue()); // Asumiendo que el valor numérico debe
                                                                         // tratarse como entero
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            default:
                return "";
        }
    }

    private String formatCellDate(Cell cell) {
        if (cell != null && cell.getCellType() == CellType.NUMERIC && DateUtil.isCellDateFormatted(cell)) {
            return DATE_FORMAT.format(cell.getDateCellValue());
        }
        return "";
    }

    // private Date getDateFromCell(Cell cell) {
    //     if (cell != null && cell.getCellType() == CellType.NUMERIC && DateUtil.isCellDateFormatted(cell)) {
    //         return cell.getDateCellValue();
    //     }
    //     return null;
    // }
}
