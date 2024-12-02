package com.formato15.ebsa.service;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.ArrayList;
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
    

    public Formato15Service(Formato15Repository formato15Repository) {
        this.formato15Repository = formato15Repository;
    }
    

    // public List<Object[]> findFullInformation(Integer ano, Integer mes) {
    //     return formato15Repository.findFullInformation(ano, mes);
    // }

    public List<FormatoSiecDTO> findFullInformation(Integer ano, Integer mes) {
    List<Object[]> results = formato15Repository.findFullInformation(ano, mes);

    return results.stream()
        .map(row -> {
            FormatoSiecDTO dto = new FormatoSiecDTO();
            dto.setDaneDpto(row[0] != null ? (String) row[0] : "N/A");
            dto.setDaneMpio(row[1] != null ? (String) row[1] : "N/A");
            dto.setDaneAsentamiento(row[2] != null ? (String) row[2] : "0");
            dto.setRadicadoRecibido(row[3] != null ? row[3].toString() : "Sin radicado");
            dto.setFechaReclamacion(row[4] != null ? row[4].toString() : "Fecha no disponible");
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
            dto.setFechaTrasladoSspd(row[15] != null ? row[15].toString() : "No trasladado");
            return dto;
        })
        .collect(Collectors.toList());
}

    

    // public List<FormatoSiecDTO> findFullInformationDTO(Integer ano, Integer mes) {
    //     List<Object[]> results = formato15Repository.findFullInformation(ano, mes);
    //     return results.stream().map(this::mapToDTO).collect(Collectors.toList());
    // }

    // private FormatoSiecDTO mapToDTO(Object[] row) {
    //     FormatoSiecDTO dto = new FormatoSiecDTO();
    //     dto.setDaneDpto((String) row[0]);
    //     dto.setDaneMpio((String) row[1]);
    //     dto.setDaneAsentamiento((String) row[2]);
    //     dto.setRadicadoRecibido((String) row[3]);
    //     dto.setFechaReclamacion((String) row[4]);
    //     dto.setTipoTramite((String) row[5]);
    //     dto.setGrupoCausal((String) row[6]);
    //     dto.setDetalleCausal((String) row[7]);
    //     dto.setNiu((String) row[8]);
    //     dto.setIdFactura((String) row[9]);
    //     dto.setTipoRespuesta((String) row[10]);
    //     dto.setFechaRespuesta((String) row[11]);
    //     dto.setRadicadoRespuesta((String) row[12]);
    //     dto.setFechaNotificacion((String) row[13]);
    //     dto.setTipoNotificacion((String) row[14]);
    //     dto.setFechaTrasladoSspd((String) row[15]);
    //     return dto;
    // }

    public List<Map<String, String>> readFileFromDirectory() throws IOException, CsvException {
        File directory = new File("C:/Users/usuario/Downloads/Formatos pruebas/prueba");
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
                    // Intentar convertir a entero si es un número
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

            DataFormatter formatter = new DataFormatter(); // Formateador de celdas

            // Leer las filas
            while (rows.hasNext()) {
                Row row = rows.next();
                Map<String, String> rowData = new LinkedHashMap<>(); // Usar LinkedHashMap para mantener el orden
                for (int i = 0; i < headers.size(); i++) {
                    Cell cell = row.getCell(i);
                    String cellValue = formatter.formatCellValue(cell);

                    // Si es numérico, intentar formatear como entero
                    if (cell != null && cell.getCellType() == CellType.NUMERIC && cellValue.contains(".")) {
                        cellValue = formatAsInteger(cellValue);
                    }

                    rowData.put(headers.get(i), cellValue);
                }
                data.add(rowData);
            }
        }
        return data;
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
