package com.formato15.ebsa.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.formato15.ebsa.clases.Formato15;
import com.formato15.ebsa.repository.DataRepository;

import java.sql.Timestamp;
import java.time.LocalDate;
//import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@Service
public class DataService {

    @Autowired
    private DataRepository dataRepository;

    public void saveData(List<Map<String, String>> data) {
        for (Map<String, String> rowData : data) {
            Formato15 dataEntity = new Formato15();
            try {
                // int departamentoDane = Integer.parseInt(rowData.get("Departamento DANE"));
                // String ciudadDane = String.valueOf(rowData.get("Ciudad DANE"));
                // String asentamiento = String.valueOf(rowData.get("Asentamiento"));
                // String ubicacion = String.valueOf(departamentoDane) + String.valueOf(ciudadDane) + String.valueOf(asentamiento); // cambiar a entero
                // dataEntity.setCodigoDane(ubicacion);
                 // Convertir los valores a Integer correctamente
                int departamentoDane = Integer.parseInt(rowData.get("Departamento DANE"));
                int ciudadDane = Integer.parseInt(rowData.get("Ciudad DANE"));
                String asentamiento = String.valueOf(rowData.get("Asentamiento"));

                // Concatenar los valores y convertir a Integer
                String ubicacionStr = String.valueOf(departamentoDane) 
                                    + String.valueOf(ciudadDane) 
                                    + String.valueOf(asentamiento);
                Integer codigoDane = Integer.parseInt(ubicacionStr); // Convertir a Integer

                // Asignar el valor a la entidad
                dataEntity.setCodigoDane(codigoDane);
                dataEntity.setRadicadoRecibido(rowData.get("Radicado Recibido"));

                // Procesar fecha_reclamacion
                String fechaReclamacionStr = rowData.get("Fecha y Hora Radicación");
                LocalDate fechaReclamacion = LocalDate.parse(
                    fechaReclamacionStr,
                    DateTimeFormatter.ofPattern("dd-MM-yyyy")
                );

                // Asignar año y mes a la entidad
                dataEntity.setAno(fechaReclamacion.getYear());
                dataEntity.setMes(fechaReclamacion.getMonthValue());

                // Asignar fecha_reclamacion como Timestamp
                dataEntity.setFechaHoraRadicacion(
                    Timestamp.valueOf(
                        LocalDate.parse(
                            fechaReclamacionStr,
                            DateTimeFormatter.ofPattern("dd-MM-yyyy")
                        ).atStartOfDay() // Establece la hora como 00:00
                    )
                );


                // dataEntity.setFechaHoraRadicacion(
                //     Timestamp.valueOf(
                //         LocalDateTime.parse(
                //             rowData.get("Fecha y Hora Radicación"),
                //             DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")
                //         )
                //     )
                // );

                dataEntity.setTipoTramite(rowData.get("Tipo trámite"));
                dataEntity.setGrupoCausal(rowData.get("Grupo Causal"));
                dataEntity.setDetalleCausal(rowData.get("Detalle Causal"));
                dataEntity.setNumeroCuenta(rowData.get("es>Account Number"));
                // Convertir "Número Factura" a Integer antes de asignarlo
                String numeroFacturaStr = rowData.get("Número Factura");
                Integer numeroFactura = (numeroFacturaStr != null && !numeroFacturaStr.isEmpty()) 
                                        ? Integer.parseInt(numeroFacturaStr) 
                                        : null;
                dataEntity.setNumeroFactura(numeroFactura);

                String tipoRespuestaStr = rowData.get("Tipo Respuesta");
                Integer tipoRespuesta = (tipoRespuestaStr != null && !tipoRespuestaStr.isEmpty()) 
                                        ? Integer.parseInt(tipoRespuestaStr) 
                                        : null;
                dataEntity.setTipoRespuesta(tipoRespuesta);

                // dataEntity.setTipoRespuesta(rowData.get("Tipo Respuesta"));
                dataEntity.setFechaRespuesta(rowData.get("Fecha Respuesta"));
                dataEntity.setRadicadoRespuesta(rowData.get("Radicado Respuesta"));
                dataEntity.setFechaNotificacion(rowData.get("Fecha Notificación"));
                dataEntity.setTipoNotificacion(rowData.get("Tipo Notificación"));
                dataEntity.setFechaTransferenciaSspd(rowData.get("Fecha Transferencia SSPD"));
                // if (dataEntity.getIdFormato15() == null) {
                //     dataEntity.setIdFormato15(generaIdUnico()); // Implementa una función para generar IDs únicos
                // }
                
                
                dataRepository.save(dataEntity);
            } catch (Exception e) {
                // Manejar la excepción (por ejemplo, registrarla o devolver un error)
                System.err.println("Error al guardar datos: " + e.getMessage());
            }

        }
    }
}
