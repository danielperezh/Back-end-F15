package com.formato15.ebsa.controllers;



import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.formato15.ebsa.clases.FormatoSiecDTO;
import com.formato15.ebsa.service.Formato15Service;

import java.util.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;



@RestController
@RequestMapping("/api/excel")
public class FileController {

    private static final Logger log = LoggerFactory.getLogger(FileController.class);

    private final Formato15Service formato15Service;

    public FileController(Formato15Service formato15Service) {
        this.formato15Service = formato15Service;
    }

    @GetMapping("/loadFromFile")
    public ResponseEntity<List<Map<String, String>>> loadFile(
            @RequestParam("year") String year,
            @RequestParam("month") String month) {
        try {
            List<Map<String, String>> fileData = formato15Service.readFileFromDirectory(year, month);
            return ResponseEntity.ok(fileData);
        } catch (FileNotFoundException e) {
            log.warn("Archivo no encontrado para el año {} y mes {}: {}", year, month, e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Collections.emptyList());
        } catch (Exception e) {
            log.error("Error al cargar el archivo: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Collections.emptyList());
        }
    }


    @GetMapping("/findFullInformation")
    public ResponseEntity<?> findFullInformation(
            @RequestParam("ano") Integer ano,
            @RequestParam("mes") Integer mes) {
        try {
            List<FormatoSiecDTO> results = formato15Service.findFullInformation(ano, mes);

            if (results.isEmpty()) {
                // Devolver un mensaje de error si no hay resultados
                Map<String, String> errorResponse = new HashMap<>();
                errorResponse.put("error", "No se encontraron datos para la fecha indicada.");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
            }

            return ResponseEntity.ok(results);
        } catch (Exception e) {
            // Manejar cualquier otro error
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Ocurrió un error al procesar la solicitud.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
    
}
