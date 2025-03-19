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
import org.springframework.web.bind.annotation.*;

import com.formato15.ebsa.clases.AuditEntry;
import com.formato15.ebsa.clases.Cuenta;
import com.formato15.ebsa.service.AuditoriaService;
import com.formato15.ebsa.service.CuentaService;

@RestController
@RequestMapping("/api")
public class ValidateAndSaveFileController {

    // Se mantiene la data editada
    private List<Map<String, String>> savedData = new ArrayList<>();
    // Aquí se almacenará la copia original (inmutable) cuando se cargue el archivo
    private List<Map<String, String>> originalData = new ArrayList<>();

    private static final Logger log = LoggerFactory.getLogger(ValidateAndSaveFileController.class);

    @Autowired
    private CuentaService cuentaService;

    // Inyectamos el servicio de auditoría
    @Autowired
    private AuditoriaService auditoriaService;

    private static final List<Integer> CODIGOS_DETALLE_CAUSAL_P = Arrays.asList(303, 304, 305, 306);
    private static final List<Integer> CODIGOS_DETALLE_CAUSAL_F = Arrays.asList(
            101, 102, 103, 104, 105, 106, 107, 108, 109, 110, 111, 112, 113, 114, 115, 116, 117, 118, 119, 120,
            121, 122, 123, 124, 125, 126, 127, 128, 129);

    /**
     * Método para realizar una copia profunda de la data utilizando LinkedHashMap
     * para conservar el orden de las columnas.
     */
    private List<Map<String, String>> deepCopy(List<Map<String, String>> data) {
        List<Map<String, String>> copy = new ArrayList<>();
        for (Map<String, String> row : data) {
            Map<String, String> rowCopy = new LinkedHashMap<>(row);
            copy.add(rowCopy);
        }
        return copy;
    }

    /**
     * Método auxiliar para normalizar cada fila usando el mapeo de encabezados.
     * Se usa un collector que devuelve un LinkedHashMap para conservar el orden.
     */
    private Map<String, String> normalizeRow(Map<String, String> row, Map<String, String> headerMappings) {
        return row.entrySet().stream()
                .collect(Collectors.toMap(
                        entry -> headerMappings.getOrDefault(entry.getKey(), entry.getKey()),
                        Map.Entry::getValue,
                        (v1, v2) -> v1,
                        LinkedHashMap::new));
    }

    /**
     * Método de validación y guardado.
     * Se recorren todas las filas acumulando los mensajes de error y registrando
     * auditorías.
     */
    @PostMapping("/validateAndSaveFile")
    public ResponseEntity<?> validateAndSaveFile(@RequestBody List<Map<String, String>> editedData,
            @RequestParam(required = false, defaultValue = "json") String returnType) {

        // Obtener el usuario autenticado
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String usuarioLogueado = (authentication != null) ? authentication.getName() : "UsuarioDesconocido";
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
                Map.entry("columna_16", "fechaTrasladoSspd"));

        // Normalizamos la data
        List<Map<String, String>> normalizedData = editedData.stream()
                .map(row -> normalizeRow(row, headerMappings))
                .collect(Collectors.toList());
        // Guardamos la data en savedData usando LinkedHashMap para preservar el orden
        this.savedData = normalizedData.stream()
                .map(LinkedHashMap::new)
                .collect(Collectors.toList());

        // Si originalData no ha sido inicializada o si su tamaño es distinto al de
        // savedData, se actualiza.
        if (originalData == null || originalData.size() != savedData.size()) {
            originalData = deepCopy(this.savedData);
        }

        // Listas para acumular mensajes de error y entradas de auditoría
        List<String> errorMessages = new ArrayList<>();
        List<AuditEntry> auditEntries = new ArrayList<>();

        // Iterar por cada fila para validar
        for (int i = 0; i < savedData.size(); i++) {
            List<String> rowErrors = new ArrayList<>();
            Map<String, String> rowData = savedData.get(i);
            Map<String, String> originalRow = originalData.get(i);
            int rowIndex = i + 1;

            String departamentoDANEValue = rowData.get("daneDpto");
            String ciudadDANEValue = rowData.get("daneMpio");
            String grupoCausal = rowData.get("grupoCausal");
            String detalleCausalStr = rowData.get("detalleCausal");
            String accountNumber = rowData.get("niu");

            // Valores originales para auditoría
            // final String originalGrupoCausal = originalRow.get("grupoCausal");
            // final String originalDetalleCausal = originalRow.get("detalleCausal");
            // final String originalFechaRespuesta = originalRow.get("fechaRespuesta");
            // final String originalFechaNotificacion = originalRow.get("fechaNotificacion");

            // Validar accountNumber
            // if (accountNumber == null || accountNumber.trim().isEmpty() ||
            // accountNumber.trim().length() < 6) {
            // rowErrors.add(String.format(
            // "Error en la fila <b>%d</b>: El número de cuenta es inválido. No puede ser
            // nulo y tiene que tener al menos 8, 9 o 10 dígitos.",
            // rowIndex));
            // errorMessages.addAll(rowErrors);
            // continue;
            // }

            // Long matricula = null;
            // try {
            // matricula = Long.parseLong(accountNumber.substring(0, 6));
            // } catch (NumberFormatException e) {
            // rowErrors.add(String.format(
            // "Error en la fila <b>%d</b>, columna 'Número de Cuenta': El número de cuenta
            // debe ser numérico.",
            // rowIndex));
            // errorMessages.addAll(rowErrors);
            // continue;
            // }

            // Validar accountNumber
            if (accountNumber == null || accountNumber.trim().isEmpty() ||
                    !(accountNumber.trim().length() == 8 || accountNumber.trim().length() == 9
                            || accountNumber.trim().length() == 10)) {
                rowErrors.add(String.format(
                        "Error en la fila <b>%d</b>: El número de cuenta es inválido. Debe tener 8, 9 o 10 dígitos.",
                        rowIndex));
                errorMessages.addAll(rowErrors);
                continue;
            }

            int len = accountNumber.trim().length();
            String matriculaStr;
            String digitosChequeoStr;
            if (len == 10) {
                // Para 10 dígitos: 7 para matrícula y 3 para dígitos de chequeo.
                matriculaStr = accountNumber.substring(0, 7);
                digitosChequeoStr = accountNumber.substring(7);
            } else {
                // Para 8 o 9 dígitos: 6 para matrícula y el resto para dígitos de chequeo.
                matriculaStr = accountNumber.substring(0, 6);
                digitosChequeoStr = accountNumber.substring(6);
            }

            Long matricula;
            Long digitosChequeo;
            try {
                matricula = Long.parseLong(matriculaStr);
                digitosChequeo = Long.parseLong(digitosChequeoStr);
            } catch (NumberFormatException e) {
                rowErrors.add(String.format(
                        "Error en la fila <b>%d</b>, columna 'Número de Cuenta': El número de cuenta debe ser numérico.",
                        rowIndex));
                errorMessages.addAll(rowErrors);
                continue;
            }

            // Validar valores numéricos de Departamento y Ciudad DANE
            Integer departamentoDANE = null;
            try {
                departamentoDANE = Integer.parseInt(departamentoDANEValue);
            } catch (NumberFormatException e) {
                rowErrors.add(String.format(
                        "Error en la fila <b>%d</b>, columna <b>'Departamento DANE'</b>: El valor ingresado '%s' debe ser numérico.",
                        rowIndex, departamentoDANEValue));
            }

            Integer ciudadDANE = null;
            try {
                ciudadDANE = Integer.parseInt(ciudadDANEValue);
            } catch (NumberFormatException e) {
                rowErrors.add(String.format(
                        "Error en la fila <b>%d</b>, columna <b>'Ciudad DANE'</b>: El valor ingresado '%s' debe ser numérico.",
                        rowIndex, ciudadDANEValue));
            }

            if (departamentoDANEValue == null || "0".equals(departamentoDANEValue)) {
                rowErrors.add(String.format(
                        "Error en la fila <b>%d</b>, columna <b>'Departamento DANE'</b>: El código del departamento no puede ser igual a 0.",
                        rowIndex));
            }
            if (ciudadDANEValue == null || "0".equals(ciudadDANEValue)) {
                rowErrors.add(String.format(
                        "Error en la fila <b>%d</b>, columna <b>'Ciudad DANE'</b>: El código de la ciudad no puede ser igual a 0.",
                        rowIndex));
            }

            // Consultar datos de la matrícula en la BD
            // Optional<Cuenta> cuentaOptional =
            // cuentaService.getCuentaPorMatricula(matricula);
            // if (cuentaOptional.isPresent()) {
            // Cuenta cuenta = cuentaOptional.get();

            // // Validación del Departamento DANE:
            // if (!cuenta.getDepartamento().equals(departamentoDANE)) {
            // rowErrors.add(String.format(
            // "Error en la fila <b>%d</b>, columna <b>'Departamento DANE'</b>: El valor
            // ingresado '%s' es incorrecto. Debería ser '%s' según el número de cuenta
            // <b>'%s'</b>.",
            // rowIndex, departamentoDANEValue, String.valueOf(cuenta.getDepartamento()),
            // rowData.get("niu")));
            // } else {
            // String originalDepartamento = originalRow.get("daneDpto");
            // if (originalDepartamento != null &&
            // !originalDepartamento.equals(departamentoDANEValue)) {
            // auditEntries.add(new AuditEntry(rowIndex, usuarioLogueado, "MODIFICAR",
            // "Departamento DANE", originalDepartamento, departamentoDANEValue));
            // }
            // }

            // // Validación de la Ciudad DANE:
            // if (!cuenta.getMunicipio().equals(ciudadDANE)) {
            // rowErrors.add(String.format(
            // "Error en la fila <b>%d</b>, columna <b>'Ciudad DANE'</b>: El valor ingresado
            // '%s' es incorrecto. Debería ser '%s' según el número de cuenta <b>'%s'</b>.",
            // rowIndex, ciudadDANEValue, String.valueOf(cuenta.getMunicipio()),
            // rowData.get("niu")));
            // } else {
            // String originalCiudad = originalRow.get("daneMpio");
            // if (originalCiudad != null && !originalCiudad.equals(ciudadDANEValue)) {
            // auditEntries.add(new AuditEntry(rowIndex, usuarioLogueado, "MODIFICAR",
            // "Ciudad DANE", originalCiudad, ciudadDANEValue));
            // }
            // }
            // } else {
            // rowErrors.add(String.format(
            // "Error en la fila <b>%d</b>: No se encontró información para la matrícula %s
            // en la base de datos.",
            // rowIndex, matricula));
            // errorMessages.addAll(rowErrors);
            // continue;
            // }

            // --- NUEVA VALIDACIÓN PARA "Radicado Recibido" (columna 4) ---
            String radicadoRecibidoValue = rowData.get("radicadoRecibido");
            if (radicadoRecibidoValue == null || radicadoRecibidoValue.trim().isEmpty()) {
                rowErrors.add(String.format(
                        "Error en la fila <b>%d</b>: El campo 'Radicado Recibido' es obligatorio y no puede estar vacío.",
                        rowIndex));
            } else {
                // Validar que contenga al menos un dígito (puede ser número o alfanumérico)
                if (!radicadoRecibidoValue.matches(".*\\d.*")) {
                    rowErrors.add(String.format(
                            "Error en la fila <b>%d</b>: El campo 'Radicado Recibido' debe contener al menos un dígito.",
                            rowIndex));
                }
                // Auditoría: si el valor original es diferente al actual, se registra cambio.

                // String originalRadicadoRecibido = originalRow.get("radicadoRecibido");
                // if (originalRadicadoRecibido != null && !originalRadicadoRecibido.equals(radicadoRecibidoValue)) {
                //     auditEntries.add(new AuditEntry(
                //             rowIndex,
                //             usuarioLogueado,
                //             "MODIFICAR",
                //             "Radicado Recibido",
                //             originalRadicadoRecibido,
                //             radicadoRecibidoValue));
                // }
            }

            // Consultar datos de la cuenta en la BD usando ambos campos
            Optional<Cuenta> cuentaOptional = cuentaService.getCuentaPorNumeroCompleto(matricula, digitosChequeo);
            if (cuentaOptional.isPresent()) {
                Cuenta cuenta = cuentaOptional.get();

                // Validación del Departamento DANE:
                if (!cuenta.getDepartamento().equals(departamentoDANE)) {
                    rowErrors.add(String.format(
                            "Error en la fila <b>%d</b>, columna <b>'Departamento DANE'</b>: El valor ingresado '%s' es incorrecto. Debería ser '%s' según el número de cuenta <b>'%s'</b>.",
                            rowIndex, departamentoDANEValue, String.valueOf(cuenta.getDepartamento()),
                            rowData.get("niu")));
                } else {
                    // String originalDepartamento = originalRow.get("daneDpto");
                    // if (originalDepartamento != null && !originalDepartamento.equals(departamentoDANEValue)) {
                    //     auditEntries.add(new AuditEntry(rowIndex, usuarioLogueado, "MODIFICAR",
                    //             "Departamento DANE", originalDepartamento, departamentoDANEValue));
                    // }
                }

                // Validación de la Ciudad DANE:
                if (!cuenta.getMunicipio().equals(ciudadDANE)) {
                    rowErrors.add(String.format(
                            "Error en la fila <b>%d</b>, columna <b>'Ciudad DANE'</b>: El valor ingresado '%s' es incorrecto. Debería ser '%s' según el número de cuenta <b>'%s'</b>.",
                            rowIndex, ciudadDANEValue, String.valueOf(cuenta.getMunicipio()), rowData.get("niu")));
                } else {
                    // String originalCiudad = originalRow.get("daneMpio");
                    // if (originalCiudad != null && !originalCiudad.equals(ciudadDANEValue)) {
                    //     auditEntries.add(new AuditEntry(rowIndex, usuarioLogueado, "MODIFICAR",
                    //             "Ciudad DANE", originalCiudad, ciudadDANEValue));
                    // }
                }
            } else {
                rowErrors.add(String.format(
                        "Error en la fila <b>%d</b>: No se encontró información para el número de cuenta %s en la base de datos.",
                        rowIndex, accountNumber));
                errorMessages.addAll(rowErrors);
                continue;
            }

            // Validación del campo "Tipo Tramite" (columna 6)
            String tipoTramiteValue = rowData.get("tipoTramite");
            try {
                int tipoTramite = Integer.parseInt(tipoTramiteValue);
                if (tipoTramite < 1 || tipoTramite > 5) {
                    rowErrors.add(String.format(
                            "Error en la fila <b>%d</b>, columna <b>'Tipo Tramite'</b>: El valor ingresado '%s' es incorrecto. Solo se permiten números del 1 al 5.",
                            rowIndex, tipoTramiteValue));
                } else {
                    // String originalTipoTramite = originalRow.get("tipoTramite");
                    // if (originalTipoTramite != null && !originalTipoTramite.equals(tipoTramiteValue)) {
                    //     auditEntries.add(new AuditEntry(
                    //             rowIndex,
                    //             usuarioLogueado,
                    //             "MODIFICAR",
                    //             "Tipo Tramite",
                    //             originalTipoTramite,
                    //             tipoTramiteValue));
                    // }
                }
            } catch (NumberFormatException e) {
                rowErrors.add(String.format(
                        "Error en la fila <b>%d</b>, columna <b>'Tipo Tramite'</b>: El valor ingresado '%s' debe ser numérico.",
                        rowIndex, tipoTramiteValue));
            }

            // Validación del Grupo Causal y Detalle Causal
            try {
                Integer detalleCausal = Integer.parseInt(detalleCausalStr);
                if ("P".equalsIgnoreCase(grupoCausal) && !CODIGOS_DETALLE_CAUSAL_P.contains(detalleCausal)) {
                    rowErrors.add(String.format(
                            "Error en la fila <b>%d</b>, columna <b>'Grupo Causal'</b>: Para el grupo causal 'P', el código de detalle causal debe ser uno de los siguientes: 303, 304, 305, 306.",
                            rowIndex));
                }
                if ("F".equalsIgnoreCase(grupoCausal) && !CODIGOS_DETALLE_CAUSAL_F.contains(detalleCausal)) {
                    rowErrors.add(String.format(
                            "Error en la fila <b>%d</b>, columna <b>'Grupo Causal'</b>: Para el grupo causal 'F', el código de detalle causal debe estar entre 101 y 129.",
                            rowIndex));
                }
                if ("P".equalsIgnoreCase(grupoCausal)) {
                    if (detalleCausal < 303 || detalleCausal > 306) {
                        rowErrors.add(String.format(
                                "Error en la fila <b>%d</b>, columna <b>'Detalle Causal'</b>: El código de detalle causal debe estar entre 303 y 306.",
                                rowIndex));
                    }
                } else if ("F".equalsIgnoreCase(grupoCausal)) {
                    if (detalleCausal < 101 || detalleCausal > 129) {
                        rowErrors.add(String.format(
                                "Error en la fila <b>%d</b>, columna <b>'Detalle Causal'</b>: El código de detalle causal debe estar entre 101 y 129.",
                                rowIndex));
                    }
                }
                // if (originalDetalleCausal != null && !originalDetalleCausal.equals(detalleCausalStr)) {
                //     auditEntries.add(new AuditEntry(rowIndex, usuarioLogueado, "MODIFICAR",
                //             "Detalle Causal", originalDetalleCausal, rowData.get("detalleCausal")));
                // }
                // if (originalGrupoCausal != null && !originalGrupoCausal.equals(grupoCausal)) {
                //     auditEntries.add(new AuditEntry(rowIndex, usuarioLogueado, "MODIFICAR",
                //             "Grupo Causal", originalGrupoCausal, rowData.get("grupoCausal")));
                // }
            } catch (NumberFormatException e) {
                rowErrors.add(String.format(
                        "Error en la fila <b>%d</b>, columna <b>'Detalle Causal'</b>: El valor de Detalle Causal debe ser un número entero.",
                        rowIndex));
            }

            // ----- VALIDACIÓN DEL NÚMERO FACTURA (columna 10) -----
            String idFacturaValue = rowData.get("idFactura");
            if ("F".equalsIgnoreCase(grupoCausal)) {
                try {
                    Long facturaNumero = Long.parseLong(idFacturaValue);
                    // String originalFactura = originalRow.get("idFactura");
                    // if (originalFactura != null && !originalFactura.equals(idFacturaValue)) {
                    //     auditEntries.add(new AuditEntry(
                    //             rowIndex,
                    //             usuarioLogueado,
                    //             "MODIFICAR",
                    //             "Número Factura",
                    //             originalFactura,
                    //             idFacturaValue));
                    // }
                } catch (NumberFormatException e) {
                    rowErrors.add(String.format(
                            "Error en la fila <b>%d</b>, columna <b>'Número Factura'</b>: Para el grupo causal 'F', el número de factura debe ser un número válido.",
                            rowIndex));
                }
            } else if ("P".equalsIgnoreCase(grupoCausal)) {
                if (!"N".equalsIgnoreCase(idFacturaValue)) {
                    rowErrors.add(String.format(
                            "Error en la fila <b>%d</b>, columna <b>'Número Factura'</b>: Para el grupo causal 'P', el campo de número de factura debe ser 'N'",
                            rowIndex));
                } else {
                    // String originalFactura = originalRow.get("idFactura");
                    // if (originalFactura != null && !originalFactura.equalsIgnoreCase(idFacturaValue)) {
                    //     auditEntries.add(new AuditEntry(
                    //             rowIndex,
                    //             usuarioLogueado,
                    //             "MODIFICAR",
                    //             "Número Factura",
                    //             originalFactura,
                    //             idFacturaValue));
                    // }
                }
            }
            // -------------------------------------------------------

            // Validación del campo "Tipo Respuesta" (columna 11)
            try {
                int tipoRespuesta = Integer.parseInt(rowData.get("tipoRespuesta"));
                if (tipoRespuesta < 1 || tipoRespuesta > 11) {
                    rowErrors.add(String.format(
                            "Error en la fila <b>%d</b>, columna <b>'Tipo Respuesta'</b>: El valor ingresado '%s' es incorrecto. Solo se permiten números del 1 al 11.",
                            rowIndex, rowData.get("tipoRespuesta")));
                } else {
                    // String originalTipoRespuesta = originalRow.get("tipoRespuesta");
                    // if (originalTipoRespuesta != null && !originalTipoRespuesta.equals(rowData.get("tipoRespuesta"))) {
                    //     auditEntries.add(new AuditEntry(
                    //             rowIndex,
                    //             usuarioLogueado,
                    //             "MODIFICAR",
                    //             "Tipo Respuesta",
                    //             originalTipoRespuesta,
                    //             rowData.get("tipoRespuesta")));
                    // }
                }
            } catch (NumberFormatException e) {
                rowErrors.add(String.format(
                        "Error en la fila <b>%d</b>, columna <b>'Tipo Respuesta'</b>: El valor ingresado '%s' debe ser numérico.",
                        rowIndex, rowData.get("tipoRespuesta")));
            }

            // Validación de la columna 13: Radicado Respuesta
            String radicadoRespuestaValue = rowData.get("radicadoRespuesta");
            String tipoRespuestaStr = rowData.get("tipoRespuesta");
            if ("9".equals(tipoRespuestaStr) || "10".equals(tipoRespuestaStr)) {
                if (radicadoRespuestaValue != null && !radicadoRespuestaValue.trim().isEmpty()) {
                    rowErrors.add(String.format(
                            "Error en la fila <b>%d</b>, columna <b>'Radicado Respuesta'</b>: Para el tipo de respuesta '%s', el campo debe estar vacío.",
                            rowIndex, tipoRespuestaStr));
                }
            } else {
                if ("P".equalsIgnoreCase(grupoCausal)) {
                    if (radicadoRespuestaValue == null || radicadoRespuestaValue.trim().isEmpty()) {
                        rowErrors.add(String.format(
                                "Error en la fila <b>%d</b>, columna <b>'Radicado Respuesta'</b>: Para el grupo causal 'P', el campo no puede estar vacío.",
                                rowIndex));
                    }
                    // try {
                    // Long.parseLong(radicadoRespuestaValue);
                    // } catch (NumberFormatException ex) {
                    // rowErrors.add(String.format(
                    // "Error en la fila <b>%d</b>, columna <b>'Radicado Respuesta'</b>: Para el
                    // grupo causal 'P', el valor '%s' debe ser numérico.",
                    // rowIndex, radicadoRespuestaValue));
                    // }

                    //registro a auditoria de radicado respuesta ---
                    // String originalRadicado = originalRow.get("radicadoRespuesta");
                    // if (originalRadicado != null && !originalRadicado.equals(radicadoRespuestaValue)) {
                    //     auditEntries.add(new AuditEntry(
                    //             rowIndex,
                    //             usuarioLogueado,
                    //             "MODIFICAR",
                    //             "Radicado Respuesta",
                    //             originalRadicado,
                    //             radicadoRespuestaValue));
                    // }
                } else if ("F".equalsIgnoreCase(grupoCausal)) {
                    if (radicadoRespuestaValue == null || radicadoRespuestaValue.trim().isEmpty()) {
                        rowErrors.add(String.format(
                                "Error en la fila <b>%d</b>, columna <b>'Radicado Respuesta'</b>: Para el grupo causal 'F', el campo no puede estar vacío.",
                                rowIndex));
                    } else {
                        try {
                            Long.parseLong(radicadoRespuestaValue);
                            rowErrors.add(String.format(
                                    "Error en la fila <b>%d</b>, columna <b>'Radicado Respuesta'</b>: Para el grupo causal 'F', el valor '%s' debe ser alfanumérico (no solo números).",
                                    rowIndex, radicadoRespuestaValue));
                        } catch (NumberFormatException ex) {
                            // Asumimos que es alfanumérico
                        }
                        // String originalRadicado = originalRow.get("radicadoRespuesta");
                        // if (originalRadicado != null && !originalRadicado.equals(radicadoRespuestaValue)) {
                        //     auditEntries.add(new AuditEntry(
                        //             rowIndex,
                        //             usuarioLogueado,
                        //             "MODIFICAR",
                        //             "Radicado Respuesta",
                        //             originalRadicado,
                        //             radicadoRespuestaValue));
                        // }
                    }
                }
            }

            // Validación de fechas (Columna 12: Fecha Respuesta y Columna 14: Fecha
            // Notificación)
            try {
                String fechaRespuestaStr = rowData.get("fechaRespuesta");
                String fechaNotificacionStr = rowData.get("fechaNotificacion");

                boolean esTipo9o10 = "9".equals(tipoRespuestaStr) || "10".equals(tipoRespuestaStr);
                if (esTipo9o10) {
                    if (fechaRespuestaStr != null && !fechaRespuestaStr.trim().isEmpty()) {
                        rowErrors.add(String.format(
                                "Error en la fila <b>%d</b>, columna <b>'Fecha Respuesta'</b>: Para el tipo de respuesta '%s', la fecha de respuesta debe estar vacía.",
                                rowIndex, tipoRespuestaStr));
                    }
                    if (fechaNotificacionStr != null && !fechaNotificacionStr.trim().isEmpty()) {
                        rowErrors.add(String.format(
                                "Error en la fila <b>%d</b>, columna <b>'Fecha Notificación'</b>: Para el tipo de respuesta '%s', la fecha de notificación debe estar vacía.",
                                rowIndex, tipoRespuestaStr));
                    }
                } else {
                    Date fechaRadicacion = parseDate(rowData.get("fechaReclamacion"));
                    Date fechaRespuesta = (fechaRespuestaStr == null || fechaRespuestaStr.trim().isEmpty())
                            ? null
                            : parseDate(fechaRespuestaStr);
                    Date fechaNotificacion = (fechaNotificacionStr == null || fechaNotificacionStr.trim().isEmpty())
                            ? null
                            : parseDate(fechaNotificacionStr);

                    if (fechaRespuesta != null && fechaRadicacion != null && fechaRespuesta.before(fechaRadicacion)) {
                        rowErrors.add(String.format(
                                "Error en la fila <b>%d</b>, columna <b>'Fecha Respuesta'</b>: La fecha de respuesta debe ser mayor o igual a la fecha de radicación.",
                                rowIndex));
                    }

                    if (fechaNotificacion != null && fechaRespuesta != null &&
                            fechaNotificacion.before(fechaRespuesta)) {
                        rowErrors.add(String.format(
                                "Error en la fila <b>%d</b>, columna <b>'Fecha Notificación'</b>: La fecha de notificación debe ser mayor o igual a la fecha de respuesta.",
                                rowIndex));
                    }

                    // String currentFechaRespuesta = rowData.get("fechaRespuesta");
                    // if (currentFechaRespuesta != null && !currentFechaRespuesta.isEmpty() &&
                    //         originalFechaRespuesta != null && !currentFechaRespuesta.equals(originalFechaRespuesta)) {
                    //     auditEntries.add(new AuditEntry(
                    //             rowIndex, usuarioLogueado, "MODIFICAR",
                    //             "Fecha Respuesta", originalFechaRespuesta, currentFechaRespuesta));
                    // }

                    // String currentFechaNotificacion = rowData.get("fechaNotificacion");
                    // if (currentFechaNotificacion != null && !currentFechaNotificacion.isEmpty() &&
                    //         originalFechaNotificacion != null
                    //         && !currentFechaNotificacion.equals(originalFechaNotificacion)) {
                    //     auditEntries.add(new AuditEntry(
                    //             rowIndex, usuarioLogueado, "MODIFICAR",
                    //             "Fecha Notificación", originalFechaNotificacion, currentFechaNotificacion));
                    // }
                }
            } catch (ParseException e) {
                String columnaError = "Desconocida";
                if (rowData.get("fechaReclamacion") != null && !isValidDateFormat(rowData.get("fechaReclamacion"))) {
                    columnaError = "Fecha Reclamación";
                } else if (rowData.get("fechaRespuesta") != null && !isValidDateFormat(rowData.get("fechaRespuesta"))) {
                    columnaError = "Fecha Respuesta";
                } else if (rowData.get("fechaNotificacion") != null
                        && !isValidDateFormat(rowData.get("fechaNotificacion"))) {
                    columnaError = "Fecha Notificación";
                }
                rowErrors.add(String.format(
                        "Error en la fila <b>%d</b>, columna <b>'%s'</b>: Formato de fecha incorrecto.",
                        rowIndex, columnaError));
            }

            // ----- VALIDACIÓN DEL CAMPO "Tipo Notificación" (columna 15) -----
            String tipoNotificacionValue = rowData.get("tipoNotificacion");
            try {
                int tipoNotificacion = Integer.parseInt(tipoNotificacionValue);

                // Obtenemos el valor de "tipoRespuesta" (columna 11)
                int tipoRespuesta = 0;
                try {
                    tipoRespuesta = Integer.parseInt(rowData.get("tipoRespuesta"));
                } catch (NumberFormatException ex) {
                    tipoRespuesta = 0;
                }

                if (tipoRespuesta == 9 || tipoRespuesta == 10) {
                    // Para tipoRespuesta 9 o 10, "Tipo Notificación" debe ser 5.
                    if (tipoNotificacion != 5) {
                        rowErrors.add(String.format(
                                "Error en la fila <b>%d</b>, columna <b>'Tipo Notificación'</b>: Para un tipo de respuesta '%d', el tipo de notificación debe ser '5'.",
                                rowIndex, tipoRespuesta));
                    } else {
                        // String originalTipoNotificacion = originalRow.get("tipoNotificacion");
                        // if (originalTipoNotificacion != null
                        //         && !originalTipoNotificacion.equals(tipoNotificacionValue)) {
                        //     auditEntries.add(new AuditEntry(
                        //             rowIndex,
                        //             usuarioLogueado,
                        //             "MODIFICAR",
                        //             "Tipo Notificación",
                        //             originalTipoNotificacion,
                        //             tipoNotificacionValue));
                        // }
                    }
                } else {
                    // En condiciones normales, "Tipo Notificación" debe ser 1 o 2.
                    if (tipoNotificacion != 1 && tipoNotificacion != 2) {
                        rowErrors.add(String.format(
                                "Error en la fila <b>%d</b>, columna <b>'Tipo Notificación'</b>: El valor ingresado '%s' es incorrecto. Solo se permiten los números '1' o '2'.",
                                rowIndex, tipoNotificacionValue));
                    } else {
                        // String originalTipoNotificacion = originalRow.get("tipoNotificacion");
                        // if (originalTipoNotificacion != null
                        //         && !originalTipoNotificacion.equals(tipoNotificacionValue)) {
                        //     auditEntries.add(new AuditEntry(
                        //             rowIndex,
                        //             usuarioLogueado,
                        //             "MODIFICAR",
                        //             "Tipo Notificación",
                        //             originalTipoNotificacion,
                        //             tipoNotificacionValue));
                        // }
                    }
                }
            } catch (NumberFormatException e) {
                rowErrors.add(String.format(
                        "Error en la fila <b>%d</b>, columna <b>'Tipo Notificación'</b>: El valor ingresado '%s' debe ser numérico.",
                        rowIndex, tipoNotificacionValue));
            }
            // -------------------------------------------------------

            // ----- VALIDACIÓN DEL CAMPO "Fecha Transferencia SSPD" (columna 16) -----
            String fechaTrasladoSspdValue = rowData.get("fechaTrasladoSspd");
            String tipoTramiteStr = rowData.get("tipoTramite");
            try {
                int tipoTramite = Integer.parseInt(tipoTramiteStr);
                if (tipoTramite == 5) {
                    // Para Tipo Tramite 5, se requiere que se ingrese una fecha válida en formato
                    // DD-MM-YYYY.
                    if (fechaTrasladoSspdValue == null || fechaTrasladoSspdValue.trim().isEmpty()) {
                        rowErrors.add(String.format(
                                "Error en la fila <b>%d</b>, columna <b>'Fecha Transferencia SSPD'</b>: Para un Tipo Tramite '5', se debe ingresar una fecha en formato DD-MM-YYYY.",
                                rowIndex));
                    } else {
                        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
                        sdf.setLenient(false);
                        try {
                            sdf.parse(fechaTrasladoSspdValue.trim());
                            // Auditoría: Si el valor es correcto y se ha modificado, se registra.
                            // String originalFechaTraslado = originalRow.get("fechaTrasladoSspd");
                            // if (originalFechaTraslado != null
                            //         && !originalFechaTraslado.equals(fechaTrasladoSspdValue)) {
                            //     auditEntries.add(new AuditEntry(
                            //             rowIndex,
                            //             usuarioLogueado,
                            //             "MODIFICAR",
                            //             "Fecha Transferencia SSPD",
                            //             originalFechaTraslado,
                            //             fechaTrasladoSspdValue));
                            // }
                        } catch (ParseException pe) {
                            rowErrors.add(String.format(
                                    "Error en la fila <b>%d</b>, columna <b>'Fecha Transferencia SSPD'</b>: El valor ingresado '%s' no tiene el formato DD-MM-YYYY.",
                                    rowIndex, fechaTrasladoSspdValue));
                        }
                    }
                } else {
                    // Si el Tipo Tramite no es 5, el campo debe estar vacío.
                    if (fechaTrasladoSspdValue != null && !fechaTrasladoSspdValue.trim().isEmpty()) {
                        rowErrors.add(String.format(
                                "Error en la fila <b>%d</b>, columna <b>'Fecha Transferencia SSPD'</b>: Para un Tipo Tramite distinto de '5', el campo debe estar vacío.",
                                rowIndex));
                    }
                }
            } catch (NumberFormatException nfe) {
                rowErrors.add(String.format(
                        "Error en la fila <b>%d</b>: El valor ingresado para 'Tipo Tramite' debe ser numérico para validar 'Fecha Transferencia SSPD'.",
                        rowIndex));
            }
            // -------------------------------------------------------------------------

            // Acumular errores de la fila
            if (!rowErrors.isEmpty()) {
                errorMessages.addAll(rowErrors);
            }
        } // Fin del for de validación

        // Si se encontraron errores, se retornan en el mismo modal.
        if (!errorMessages.isEmpty()) {
            String allErrors = String.join("<br>", errorMessages);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(allErrors);
        }

        // Enviar la auditoría acumulada
        // if (!auditEntries.isEmpty()) {
        //     System.out.println("Enviando auditoría batch con los siguientes registros:");
        //     for (AuditEntry entry : auditEntries) {
        //         System.out.println("Fila " + entry.getRowIndex() +
        //                 " | Usuario: " + entry.getUsuario() +
        //                 " | Acción: " + entry.getAccion() +
        //                 " | Campo: " + entry.getCampo() +
        //                 " | Valor Original: " + entry.getValorOriginal() +
        //                 " | Valor Nuevo: " + entry.getValorNuevo());
        //     }
        //     auditoriaService.registrarCambiosBatch(auditEntries);
        // }

        // Resto de la generación del archivo Excel o respuesta JSON...
        if ("json".equalsIgnoreCase(returnType)) {
            return ResponseEntity.ok(savedData);
        }

        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Hoja1");
            Row headerRow = sheet.createRow(0);
            Map<String, String> firstRow = savedData.get(0);
            int cellIndex = 0;
            for (String key : firstRow.keySet()) {
                Cell cell = headerRow.createCell(cellIndex++);
                cell.setCellValue(key);
            }
            int excelRowIndex = 1;
            for (Map<String, String> rowData : savedData) {
                Row row = sheet.createRow(excelRowIndex++);
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

    // Método auxiliar para verificar el formato de la fecha
    private boolean isValidDateFormat(String dateStr) {
        String[] formats = { "dd-MM-yyyy", "yyyy-MM-dd", "dd/MM/yyyy", "dd-MM-yy HH:mm", "yyyy-MM-dd HH:mm" };
        for (String format : formats) {
            try {
                new SimpleDateFormat(format).parse(dateStr);
                return true;
            } catch (ParseException ignored) {
            }
        }
        return false;
    }

    // Método auxiliar para parsear la fecha
    public Date parseDate(String dateString) throws ParseException {
        if (dateString == null || dateString.trim().isEmpty()) {
            return null;
        }
        List<String> dateFormats = List.of("dd-MM-yyyy", "DD-MM-YYYY");
        ParseException parseException = null;
        for (String format : dateFormats) {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat(format);
                sdf.setLenient(false);
                return sdf.parse(dateString);
            } catch (ParseException e) {
                parseException = e;
            }
        }
        throw parseException;
    }

    @GetMapping("/filteredData")
    public ResponseEntity<List<Map<String, String>>> getFilteredData(@RequestParam(required = false) String query) {
        if (query == null || query.isEmpty()) {
            return ResponseEntity.ok(savedData);
        }
        List<Map<String, String>> filteredData = savedData.stream()
                .filter(row -> row.values().stream()
                        .anyMatch(value -> value.toLowerCase().contains(query.toLowerCase())))
                .collect(Collectors.toList());
        return ResponseEntity.ok(filteredData);
    }
}
