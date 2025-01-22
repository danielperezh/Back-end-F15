package com.formato15.ebsa.controllers;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

import org.apache.commons.io.output.ByteArrayOutputStream;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.formato15.ebsa.clases.Cuenta;
import com.formato15.ebsa.service.CuentaService;

@RestController
@RequestMapping("/api")
public class ValidateAndSaveFileController {


    private List<Map<String, String>> savedData = new ArrayList<>();
    private static final Logger log = LoggerFactory.getLogger(FileController.class);

    @Autowired
    private CuentaService cuentaService;

    private static final List<Integer> CODIGOS_DETALLE_CAUSAL_P = Arrays.asList(303, 304, 305, 306);
    private static final List<Integer> CODIGOS_DETALLE_CAUSAL_F = Arrays.asList(101, 102, 103, 104, 105, 106, 107, 108,
            109, 110, 111, 112, 113, 114, 115, 116, 117, 118, 119, 120, 121, 122, 123, 124);


    @PostMapping("/validateAndSaveFile")
    public ResponseEntity<?> validateAndSaveFile(@RequestBody List<Map<String, String>> editedData, @RequestParam(required = false, defaultValue = "json") String returnType) {

        //Obtener el usuario autenticado
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String usuarioLogueado = authentication != null ? authentication.getName() : "UsuarioDesconocido";

        System.out.println("-- Usuario --: " + usuarioLogueado);
         
         

        // Mapa de equivalencias para encabezados
        Map<String, String> headerMappings = Map.ofEntries(
            Map.entry("columna_1", "daneDpto"),
            Map.entry("columna_2", "daneMpio"),
            Map.entry("columna_3", "daneAsentamiento"),
            Map.entry("columna_4", "radicadoRecibido"),
            Map.entry("columna_5", "fechaReclamacion"),
            Map.entry("columna_6", "tipoTramite"),
            Map.entry("columna_7", "grupoCausal"),
            Map.entry("columna_8", "detalleCausal"),
            Map.entry("columna_9", "niu"),
            Map.entry("columna_10", "idFactura"),
            Map.entry("columna_11", "tipoRespuesta"),
            Map.entry("columna_12", "fechaRespuesta"),
            Map.entry("columna_13", "radicadoRespuesta"),
            Map.entry("columna_14", "fechaNotificacion"),
            Map.entry("columna_15", "tipoNotificacion"),
            Map.entry("columna_16", "fechaTrasladoSspd")
        );

        // Normalizar los datos con encabezados consistentes
        List<Map<String, String>> normalizedData = editedData.stream()
            .map(row -> normalizeRow(row, headerMappings))
            .collect(Collectors.toList());

        this.savedData = new ArrayList<>(normalizedData);

        // Validación de datos

        int rowIndex = 0; // Índice para las filas
        for (Map<String, String> rowData : savedData) {
            rowIndex++;
            String departamentoDANEValue = rowData.get("daneDpto");
            String ciudadDANEValue = rowData.get("daneMpio");
            String grupoCausal = rowData.get("grupoCausal");
            String detalleCausalStr = rowData.get("detalleCausal");
            String accountNumber = rowData.get("niu");
            String accion = "MODIFICAR"; // Obtén el usuario autenticado
            // String nombreArchivo = "formato15.xlsx"; 


            // Validar que accountNumber no sea nulo
            if (accountNumber == null || accountNumber == String.valueOf(0)) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body("El número de cuenta no puede ser nulo.");
            }

            // Obtener los primeros 6 dígitos del número de cuenta
            Long matricula;
            try {
                matricula = Long.parseLong(accountNumber.substring(0, 6));
            } catch (NumberFormatException e) {
                String errorMessage = String.format("Error en la fila %d, columna 'Número de Cuenta': El número de cuenta debe ser numérico y tener al menos 6 dígitos.", rowIndex);
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorMessage);
            }

            Integer departamentoDANE;
            Integer ciudadDANE;
            

            //||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||
            //Validacion que los codigos DANE pasados para la ciudad y departamento sean validos 
            //||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||
            try {
                departamentoDANE = Integer.parseInt(departamentoDANEValue);
                ciudadDANE = Integer.parseInt(ciudadDANEValue);
            } catch (NumberFormatException e) {
                String errorMessage = String.format("Error en la fila %d: Los valores de 'Departamento DANE' o 'Ciudad DANE' deben ser numéricos.", rowIndex);
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorMessage);
            }
            //||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||
            //Validacion que los codigos DANE pasados para la ciudad y departamento sean validos 
            //||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||


            // Validar Departamento y Ciudad                                            !!!!! Falta la validacion del municipio ¡¡¡¡¡¡¡¡¡¡¡
            if (departamentoDANEValue == null || "0".equals(departamentoDANEValue)) {
                String errorMessage = String.format("Error en la fila %d, columna 'Departamento DANE': El código del departamento no puede ser nulo o igual a 0.", rowIndex);
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorMessage);
            }
            

            // Consultar en la base de datos los datos asociados a la matrícula
            Optional<Cuenta> cuentaOptional = cuentaService.getCuentaPorMatricula(matricula);
            
            
            // Validar discrepancias
            if (cuentaOptional.isPresent()) {
                Cuenta cuenta = cuentaOptional.get();
            
                boolean hasChanges = false; // Bandera para verificar si hubo cambios
            
                // Validar y corregir los datos de departamento y ciudad
                if (!cuenta.getDepartamento().equals(departamentoDANE)) {
                    // Registro para cambio en Departamento DANE
                    // Auditoria auditoriaDepartamento = new Auditoria();
                    // auditoriaDepartamento.setUsuario(usuarioLogueado);
                    // auditoriaDepartamento.setAccion(accion);
                    // auditoriaDepartamento.setCampoModificado("Departamento DANE");
                    // auditoriaDepartamento.setValorAnterior(departamentoDANEValue); 
                    // auditoriaDepartamento.setValorNuevo(String.valueOf(cuenta.getDepartamento())); 
                    // auditoriaDepartamento.setFechaModificacion(LocalDateTime.now());
                    // auditoriaRepository.save(auditoriaDepartamento);
            
                    hasChanges = true; // Indicar que hubo un cambio
                }
            
                if (!cuenta.getMunicipio().equals(ciudadDANE)) {
                    // Registro para cambio en Ciudad DANE
                    // Auditoria auditoriaMunicipio = new Auditoria();
                    // auditoriaMunicipio.setUsuario(usuarioLogueado);
                    // auditoriaMunicipio.setAccion(accion);
                    // auditoriaMunicipio.setCampoModificado("Ciudad DANE");
                    // auditoriaMunicipio.setValorAnterior(ciudadDANEValue); 
                    // auditoriaMunicipio.setValorNuevo(String.valueOf(cuenta.getMunicipio()));
                    // auditoriaMunicipio.setFechaModificacion(LocalDateTime.now());
                    // auditoriaRepository.save(auditoriaMunicipio);
            
                    hasChanges = true; // Indicar que hubo un cambio
                }
            
                // Solo corregir los datos en rowData si hubo cambios
                if (hasChanges) {
                    rowData.put("daneDpto", String.valueOf(cuenta.getDepartamento()));
                    rowData.put("daneMpio", String.valueOf(cuenta.getMunicipio()));
            
                    // Registrar un mensaje de advertencia en los logs
                    log.warn(String.format(
                            "Fila con número de cuenta %s: El Departamento DANE (%s) o Ciudad DANE (%s) eran incorrectos. Se corrigieron automáticamente a Departamento: %s, Municipio: %s.",
                            accountNumber, departamentoDANEValue, ciudadDANEValue, cuenta.getDepartamento(), cuenta.getMunicipio()));
                } else {
                    log.info(String.format("No hubo cambios en Departamento DANE (%s) ni Ciudad DANE (%s) para la cuenta %s.",
                            departamentoDANE, ciudadDANE, accountNumber));
                }
            
            } else {
                // Si no se encuentra la matrícula en la base de datos, registrar un error
                log.error(String.format("No se encontró información para la matrícula %s en la base de datos.", matricula));
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(String.format("No se encontró información para la matrícula %s en la base de datos.", matricula));
            }

        
            //||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||
            // Validación de Grupo Causal y Detalle Causal [Correcto, falta el envio de datos a la tabla AUDITORIA]
            //||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||
            try {
                Integer detalleCausal = Integer.parseInt(detalleCausalStr);

                if ("P".equalsIgnoreCase(grupoCausal) && !CODIGOS_DETALLE_CAUSAL_P.contains(detalleCausal)) {
                    // Registro de auditoría
                    // Auditoria auditoria = new Auditoria();
                    // auditoria.setUsuario(usuarioLogueado);
                    // auditoria.setAccion(accion);
                    // auditoria.setCampoModificado("Detalle Causal");
                    // auditoria.setValorAnterior(detalleCausalStr);
                    // auditoria.setValorNuevo(rowData.get("detalleCausal")); 
                    // auditoria.setFechaModificacion(LocalDateTime.now());
                    // auditoriaRepository.save(auditoria);

                    String errorMessage = String.format(
                    "Error en la fila <b>%d</b>, columna <b>'Detalle Causal'</b>: Para el grupo causal 'P', el código de detalle causal debe ser uno de los siguientes: 303, 304, 305, 306.",
                    rowIndex
                    );
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorMessage);
                } else if ("F".equalsIgnoreCase(grupoCausal) && !CODIGOS_DETALLE_CAUSAL_F.contains(detalleCausal)) {
                    // Registro de auditoría
                    // Auditoria auditoria = new Auditoria();
                    // auditoria.setUsuario(usuarioLogueado);
                    // auditoria.setAccion(accion);
                    // auditoria.setCampoModificado("Detalle Causal");
                    // auditoria.setValorAnterior(detalleCausalStr);
                    // auditoria.setValorNuevo(rowData.get("detalleCausal"));
                    // auditoria.setFechaModificacion(LocalDateTime.now());
                    // auditoriaRepository.save(auditoria);
                    
                    String errorMessage = String.format(
                    "Error en la fila <b>%d</b>, columna <b>'Detalle Causal'</b>: Para el grupo causal 'F', el código de detalle causal debe estar entre 101 y 124.",
                    rowIndex
                    );
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorMessage);
                }
            } catch (NumberFormatException e) {

                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body("Error: El valor de Detalle Causal debe ser un número entero.");
            }
            //||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||
            // Validación de Grupo Causal y Detalle Causal [Correcto, falta el envio de datos a la tabla AUDITORIA]
            //||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||


            //||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||
            // Validación de fechas [Correcto, falta el envio de datos a la tabla AUDITORIA] - falta validar que hay algunas fechas mayores de tipo notificacion que estan votando error de formato.
            //||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||
            try {
                Date fechaRadicacion = parseDate(rowData.get("fechaReclamacion"));
                Date fechaRespuesta = parseDate(rowData.get("fechaRespuesta"));
                Date fechaNotificacion = parseDate(rowData.get("fechaNotificacion"));

                if (fechaRespuesta != null && fechaRadicacion != null && fechaRespuesta.before(fechaRadicacion)) {

                     // Agregar fila y columna al mensaje de error
                     String errorMessage = String.format(
                        "Error en la fila <b>%d</b>, columna '<b>Fecha Respuesta</b>': La fecha de respuesta debe ser mayor o igual a la fecha de radicación.",
                        rowIndex
                    );
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorMessage);
                }
                if (fechaNotificacion != null && fechaRespuesta != null && fechaRadicacion != null && fechaNotificacion.before(fechaRespuesta)) {

                    String errorMessage = String.format(
                    "Error en la fila <b>%d</b>, columna <b>'Fecha Notificación'</b>: La fecha de notificación debe ser mayor o igual a la fecha de respuesta.",
                    rowIndex
                    );
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorMessage);
                }
            } catch (ParseException e) {

                String errorMessage = String.format("Error en la fila %d: Formato de fecha incorrecto.", rowIndex);
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorMessage);
                
            }
            //||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||
            // Validación de fechas [Correcto, falta el envio de datos a la tabla AUDITORIA]
            //||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||
            
            
        }
        // Verificar el tipo de retorno solicitado
        if ("json".equalsIgnoreCase(returnType)) {
            // Retornar los datos validados en formato JSON
            return ResponseEntity.ok(savedData);
        }
    

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

            // int rowIndex = 1;
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
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al generar el archivo Excel.");
        }
        
    }

    public Date parseDate(String dateString) throws ParseException {
        if (dateString == null || dateString.trim().isEmpty()) {
            return null;
        }
    
        List<String> dateFormats = List.of("dd-MM-yyyy", "dd/MM/yyyy HH:mm:ss");
        ParseException parseException = null;
    
        for (String format : dateFormats) {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat(format);
                sdf.setLenient(false); // Evita fechas inválidas como 31-02-2023
                return sdf.parse(dateString);
            } catch (ParseException e) {
                parseException = e;
            }
        }
    
        // Si ninguno funcionó, lanza una excepción
        throw parseException;
    }

    private Map<String, String> normalizeRow(Map<String, String> row, Map<String, String> headerMappings) {
        return row.entrySet().stream()
            .collect(Collectors.toMap(
                entry -> headerMappings.getOrDefault(entry.getKey(), entry.getKey()),
                Map.Entry::getValue
            ));
    }
}
