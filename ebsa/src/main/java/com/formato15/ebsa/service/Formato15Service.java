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
    

    // public List<Object[]> findFullInformation(Integer ano, Integer mes) {
    //     return formato15Repository.findFullInformation(ano, mes);
    // }

    public List<FormatoSiecDTO> findFullInformation(Integer ano, Integer mes) {
    List<Object[]> results = formato15Repository.findFullInformation(ano, mes);
    SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");

    return results.stream()
        .map(row -> {
            FormatoSiecDTO dto = new FormatoSiecDTO();
            dto.setDaneDpto(row[0] != null ? (String) row[0] : "N/A");
            dto.setDaneMpio(row[1] != null ? (String) row[1] : "N/A");
            dto.setDaneAsentamiento(row[2] != null ? (String) row[2] : "0");
            dto.setRadicadoRecibido(row[3] != null ? row[3].toString() : "Sin radicado");
            // Convertir fecha_reclamacion a dd-MM-yyyy
            dto.setFechaReclamacion(row[4] != null ? dateFormat.format(row[4]) : "Fecha no disponible");
            dto.setTipoTramite(row[5] != null ? row[5].toString() : "N/A");
            dto.setGrupoCausal(row[6] != null ? row[6].toString() : "N/A");
            dto.setDetalleCausal(row[7] != null ? row[7].toString() : "N/A");
            dto.setNiu(row[8] != null ? row[8].toString() : "000");
            dto.setIdFactura(row[9] != null ? row[9].toString() : "0");
            dto.setTipoRespuesta(row[10] != null ? row[10].toString() : "0");
            dto.setFechaRespuesta(row[11] != null ? row[11].toString() : "No hay respuesta");
            dto.setRadicadoRespuesta(row[12] != null ? row[12].toString() : "N/A");
            dto.setFechaNotificacion(row[13] != null ? row[13].toString() : "No notificado");
            dto.setTipoNotificacion(row[14] != null ? row[14].toString() : "N/A");
            dto.setFechaTrasladoSspd(row[15] != null ? row[15].toString() : "N");
            return dto;
        })
        .collect(Collectors.toList());
    }


    public List<Map<String, String>> readFileFromDirectory() throws IOException, CsvException {
        File directory = new File("C:/Users/dperez.EBSA0/Downloads/Formato15p");
        File[] files = directory.listFiles((dir, name) -> name.endsWith(".csv") || name.endsWith(".xls") || name.endsWith(".xlsx"));

        if (files == null || files.length == 0) {
            throw new FileNotFoundException("No se encontró ningún archivo CSV/XLS en la ruta especificada.");
        }

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
            List<String[]> rows = csvReader.readAll();
            String[] headers = rows.get(0); // Encabezados de la primera fila
    
            for (int i = 1; i < rows.size(); i++) {
                String[] row = rows.get(i);
                Map<String, String> rowData = new LinkedHashMap<>(); // Usar LinkedHashMap para mantener el orden
                for (int j = 0; j < headers.length; j++) {
                    String value = row[j];
    
                    // Validar y convertir fechas
                    if (isDate(value)) {
                        value = formatDate(value); // Convertir al formato dd-MM-yyyy
                    }
    
                    // Si es un número decimal, convertir a entero
                    rowData.put(headers[j], isNumeric(value) && value.contains(".") ? formatAsInteger(value) : value);
                }
                data.add(rowData);
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
                    if ("Fecha Transferencia SSPD".equals(headers.get(i)) && (cellValue == null || cellValue.trim().isEmpty())) {
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
            new SimpleDateFormat(DATE_FORMAT).parse(value); // Intentar parsear al formato dd-MM-yyyy
            return true;
        } catch (ParseException e) {
            return false;
        }
    }
    
    // Método para convertir una fecha al formato dd-MM-yyyy
    private String formatDate(String value) {
        try {
            // Detectar el formato actual de la fecha
            String[] possibleFormats = { "dd-MM-yyyy HH:mm", "dd-MM-yy HH:mm", "yyyy-MM-dd HH:mm", "yyyy-MM-dd", "MM/dd/yyyy", "dd-MM-yyyy" };
            Date date = null;
            for (String format : possibleFormats) {
                try {
                    date = new SimpleDateFormat(format).parse(value);
                    break;
                } catch (ParseException ignored) {
                    // Intentar el siguiente formato
                }
            }
            if (date != null) {
                return new SimpleDateFormat(DATE_FORMAT).format(date); // Formatear al formato deseado
            } else {
                return value; // Si no se puede parsear, devolver el valor original
            }
        } catch (Exception e) {
            return "Formato inválido"; // Manejo de errores
        }
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
