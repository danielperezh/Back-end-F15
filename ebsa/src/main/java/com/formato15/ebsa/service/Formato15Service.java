package com.formato15.ebsa.service;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.poi.ss.usermodel.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.formato15.ebsa.clases.FormatoSiecDTO;
import com.formato15.ebsa.repository.Formato15Repository;

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvException;

@Service
public class Formato15Service {

    @Autowired
    private final Formato15Repository formato15Repository;

    private static final String DATE_FORMAT = "dd-MM-yyyy";

    public Formato15Service(Formato15Repository formato15Repository) {
        this.formato15Repository = formato15Repository;
    }

    public List<FormatoSiecDTO> findFullInformation(Integer ano, Integer mes) {
        List<Object[]> results = formato15Repository.findFullInformation(ano, mes);
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");

        return results.stream()
                .map(row -> {
                    FormatoSiecDTO dto = new FormatoSiecDTO();
                    dto.setDaneDpto(row[0] != null ? (String) row[0] : "");
                    dto.setDaneMpio(row[1] != null ? (String) row[1] : "");
                    dto.setDaneAsentamiento(row[2] != null ? (String) row[2] : "");
                    dto.setRadicadoRecibido(row[3] != null ? row[3].toString() : "");
                    // Convertir fecha_reclamacion a dd-MM-yyyy
                    dto.setFechaReclamacion(row[4] != null ? dateFormat.format(row[4]) : "");
                    dto.setTipoTramite(row[5] != null ? row[5].toString() : "");
                    dto.setGrupoCausal(row[6] != null ? row[6].toString() : "");
                    dto.setDetalleCausal(row[7] != null ? row[7].toString() : "");
                    dto.setNiu(row[8] != null ? row[8].toString() : "");
                    dto.setIdFactura(row[9] != null ? row[9].toString() : "");
                    dto.setTipoRespuesta(row[10] != null ? row[10].toString() : "");
                    dto.setFechaRespuesta(row[11] != null ? row[11].toString() : "");
                    dto.setRadicadoRespuesta(row[12] != null ? row[12].toString() : "");
                    dto.setFechaNotificacion(row[13] != null ? row[13].toString() : "");
                    dto.setTipoNotificacion(row[14] != null ? row[14].toString() : "");
                    dto.setFechaTrasladoSspd(row[15] != null ? row[15].toString() : "");
                    return dto;
                })
                .collect(Collectors.toList());
    }

    public List<Map<String, String>> readFileFromDirectory(String year, String month) throws IOException, CsvException {
        //File directory = new File("//172.16.10.26/CREG Report Repository");
        // File directory = new File("C:/Users/dperez.EBSA0/Downloads/Formato15p");
        File directory = new File("C:/Users/usuario/Downloads/Formatos pruebas/prueba");

        // Filtrar archivos por año y mes en el nombre (formato esperado:
        // formato_15_YYYYMM.ext)
        String filePattern = String.format("formato_15_%s%s", year, month);
        File[] files = directory.listFiles((dir, name) -> (name.contains(filePattern)
                && (name.endsWith(".csv") || name.endsWith(".xls") || name.endsWith(".xlsx"))));

        if (files == null || files.length == 0) {
            throw new FileNotFoundException("No se encontró ningún archivo correspondiente al patrón: " + filePattern);
        }

        // Leer el primer archivo encontrado
        File file = files[0];
        if (file.getName().endsWith(".csv")) {
            return readCSVFile(file);
        } else {
            return readExcelFile(file);
        }
    }

    private List<Map<String, String>> readCSVFile(File file) throws IOException, CsvException {
        List<Map<String, String>> data = new ArrayList<>();
        try (CSVReader csvReader = new CSVReader(new FileReader(file))) {
            List<String[]> rows = csvReader.readAll(); // Leer todas las filas del archivo

            for (int i = 0; i < rows.size(); i++) { // Iterar desde la primera fila
                String[] row = rows.get(i);
                Map<String, String> rowData = new LinkedHashMap<>(); // Mantener el orden de las columnas

                for (int j = 0; j < row.length; j++) {
                    String columnName = "columna_" + (j + 1); // Generar nombre de columna dinámico
                    String value = row[j];


                    // Si estamos en la columna 10 (índice 9), asignamos "N" independientemente de su valor
                    if (j == 9) {
                        value = "N"; // Asignamos "N" a todos los valores de la columna 10
                    }

                    // Aplicar conversión de fechas solo a las columnas mencionadas
                    if (columnName.equals("columna_5") || columnName.equals("columna_12")
                            || columnName.equals("columna_14")) {
                        value = formatDate(value);
                    }

                    // Validar y convertir fechas si es necesario
                    if (isDate(value)) {
                        value = formatDate(value);
                    }

                    // Convertir números decimales a enteros si aplica
                    rowData.put(columnName, isNumeric(value) && value.contains(".") ? formatAsInteger(value) : value);
                }

                data.add(rowData); // Agregar la fila completa
            }
        }
        return data;
    }

    private List<Map<String, String>> readExcelFile(File file) throws IOException {
        List<Map<String, String>> data = new ArrayList<>();
        try (Workbook workbook = WorkbookFactory.create(file)) {
            Sheet sheet = workbook.getSheetAt(0);
            Iterator<Row> rows = sheet.iterator();

            // Leer encabezados
            Row headerRow = rows.next();
            List<String> headers = new ArrayList<>();
            for (Cell cell : headerRow) {
                headers.add(cell.getStringCellValue());
            }

            // Leer las filas
            while (rows.hasNext()) {
                Row row = rows.next();
                if (row == null) {
                    continue; // Ignorar filas vacías
                }

                Map<String, String> rowData = new LinkedHashMap<>();
                for (int i = 0; i < headers.size(); i++) {
                    Cell cell = row.getCell(i);
                    String cellValue = "";

                    if (cell != null) {
                        // Validar y formatear fechas si el tipo de celda es DATE
                        if (cell.getCellType() == CellType.NUMERIC && DateUtil.isCellDateFormatted(cell)) {
                            cellValue = formatExcelDate(cell.getDateCellValue());
                        } else {
                            // Si no es fecha, usar el valor formateado
                            cellValue = new DataFormatter().formatCellValue(cell);
                        }
                    }

                    // Asignar valor "N" si es nulo o vacío para la columna "Fecha de Traslado SSPD"
                    if ("Fecha Transferencia SSPD".equals(headers.get(i))
                            && (cellValue == null || cellValue.trim().isEmpty())) {
                        cellValue = "N";
                    }

                    rowData.put(headers.get(i), cellValue);
                }
                data.add(rowData);
            }
        }
        return data;
    }

    // Método para formatear una fecha al formato dd-MM-yyyy
    private String formatExcelDate(Date date) {
        SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT);
        return dateFormat.format(date);
    }

    // Método para verificar si el valor es una fecha
    private boolean isDate(String value) {
        try {
            new SimpleDateFormat(DATE_FORMAT).parse(value);
            return true;
        } catch (ParseException e) {
            return false;
        }
    }

    // Método para convertir una fecha al formato dd-MM-yyyy
    private String formatDate(String value) {
        if (value == null || value.trim().isEmpty()) {
            return "";
        }
    
        String[] possibleFormats = {
            "dd-MM-yyyy HH:mm:ss", "dd-MM-yy HH:mm", "dd-MM-yyyy",
            "yyyy-MM-dd HH:mm", "yyyy-MM-dd", "dd/MM/yyyy", "dd/MM/yyyy HH:mm:ss"
        };
    
        for (String format : possibleFormats) {
            try {
                Date date = new SimpleDateFormat(format).parse(value);
                return new SimpleDateFormat(DATE_FORMAT).format(date);
            } catch (ParseException ignored) {
            }
        }
    
        return value; // Si el formato no es válido, devolver el valor original
    }
    

    private String formatAsInteger(String value) {
        try {
            double doubleValue = Double.parseDouble(value);
            int intValue = (int) doubleValue; // Convertir a entero
            return String.valueOf(intValue);
        } catch (NumberFormatException e) {
            return value; // Si no es un número, retornar el valor original
        }
    }

    private boolean isNumeric(String value) {
        try {
            Double.parseDouble(value);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }
    
}
