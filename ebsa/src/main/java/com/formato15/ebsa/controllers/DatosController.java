package com.formato15.ebsa.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.formato15.ebsa.service.AuditoriaService;

import java.util.Map;

@RestController
@RequestMapping("/api/datos")
public class DatosController {

    @Autowired
    private AuditoriaService auditoriaService;

    @PostMapping("/guardar")
    public String guardarDatos(@RequestBody Map<String, Object> datosModificados) {
        // Simula los datos originales (pueden provenir de la base de datos)
        Map<String, Object> datosOriginales = obtenerDatosOriginales();

        String usuario = "admin"; // Usuario autenticado
        String rolUsuario = "ADMIN";
        String nombreArchivo = "formato15.xlsx";
        String accion = "MODIFICAR";

        // Detectar cambios
        for (String campo : datosModificados.keySet()) {
            Object valorNuevo = datosModificados.get(campo);
            Object valorAnterior = datosOriginales.get(campo);

            // Comparar valores
            if (valorAnterior != null && !valorAnterior.equals(valorNuevo)) {
                // Registrar auditoría
                auditoriaService.registrarCambio(usuario, rolUsuario, nombreArchivo, accion,
                        campo, valorAnterior.toString(), valorNuevo.toString());
            }
        }

        // Aquí guardarías los datos modificados en la base de datos

        return "Datos guardados y auditoría registrada.";
    }

    private Map<String, Object> obtenerDatosOriginales() {
        // Simulación de datos originales (deberías consultarlos desde la base de datos)
        return Map.of(
                "campo1", "valor1_original",
                "campo2", "valor2_original",
                "campo3", "valor3_original"
        );
    }

    
}


