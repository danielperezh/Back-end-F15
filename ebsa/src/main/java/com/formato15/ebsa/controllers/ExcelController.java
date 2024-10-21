package com.formato15.ebsa.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/excel")
public class ExcelController {

    @CrossOrigin(origins = "http://localhost:8080") // Habilitar CORS solo para este endpoint.
    @PostMapping("/upload")
    public ResponseEntity<?> uploadExcelFiles(@RequestParam("files") MultipartFile[] files) {
        // Lógica de la subida y unión de archivos Excel.
        return ResponseEntity.ok().body("Archivos cargados y combinados correctamente.");
    }
}
