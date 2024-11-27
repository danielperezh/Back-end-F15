package com.formato15.ebsa.service;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.formato15.ebsa.clases.FormatoSiecDTO;
import com.formato15.ebsa.repository.Formato15Repository;

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvException;

import org.apache.poi.ss.usermodel.*;
import java.util.*;

@Service
public class Formato15Service {

    @Autowired
    private final Formato15Repository formato15Repository;

    public Formato15Service(Formato15Repository formato15Repository) {
        this.formato15Repository = formato15Repository;
    }

    public List<Object[]> findFullInformation(Integer ano, Integer mes) {
        return formato15Repository.findFullInformation(ano, mes);
    }

    public List<FormatoSiecDTO> findFullInformationDTO(Integer ano, Integer mes) {
    List<Object[]> results = formato15Repository.findFullInformation(ano, mes);
    return results.stream().map(this::mapToDTO).collect(Collectors.toList());
    }

    private FormatoSiecDTO mapToDTO(Object[] row) {
        FormatoSiecDTO dto = new FormatoSiecDTO();
        dto.setDaneDpto((String) row[0]);
        dto.setDaneMpio((String) row[1]);
        dto.setDaneAsentamiento((String) row[2]);
        dto.setRadicadoRecibido((String) row[3]);
        dto.setFechaReclamacion((String) row[4]);
        dto.setTipoTramite((String) row[5]);
        dto.setGrupoCausal((String) row[6]);
        dto.setDetalleCausal((String) row[7]);
        dto.setNiu((String) row[8]);
        dto.setIdFactura((String) row[9]);
        dto.setTipoRespuesta((String) row[10]);
        dto.setFechaRespuesta((String) row[11]);
        dto.setRadicadoRespuesta((String) row[12]);
        dto.setFechaNotificacion((String) row[13]);
        dto.setTipoNotificacion((String) row[14]);
        dto.setFechaTrasladoSspd((String) row[15]);
        return dto;
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
                String[] headers = rows.get(0);
                for (int i = 1; i < rows.size(); i++) {
                    String[] row = rows.get(i);
                    Map<String, String> rowData = new HashMap<>();
                    for (int j = 0; j < headers.length; j++) {
                        rowData.put(headers[j], row[j]);
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

                Row headerRow = rows.next();
                List<String> headers = new ArrayList<>();
                for (Cell cell : headerRow) {
                    headers.add(cell.getStringCellValue());
                }

                while (rows.hasNext()) {
                    Row row = rows.next();
                    Map<String, String> rowData = new HashMap<>();
                    for (int i = 0; i < headers.size(); i++) {
                        Cell cell = row.getCell(i);
                        rowData.put(headers.get(i), cell != null ? cell.toString() : "");
                    }
                    data.add(rowData);
                }
            }
            return data;
        }

}
