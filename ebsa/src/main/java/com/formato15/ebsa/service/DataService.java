package com.formato15.ebsa.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.formato15.ebsa.clases.Formato15;
import com.formato15.ebsa.repository.DataRepository;

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
                // dataEntity.setDepartamentoDane(rowData.get("Departamento DANE"));
                // dataEntity.setCiudadDane(rowData.get("Ciudad DANE"));
                // dataEntity.setAsentamiento(rowData.get("Asentamiento"));
                int departamentoDane = Integer.parseInt(rowData.get("Departamento DANE"));
                String ciudadDane = String.valueOf(rowData.get("Ciudad DANE"));
                String asentamiento = String.valueOf(rowData.get("Asentamiento"));
                String ubicacion = String.valueOf(departamentoDane) + String.valueOf(ciudadDane) + String.valueOf(asentamiento); // cambiar a entero
                dataEntity.setCodigoDane(ubicacion);
                dataEntity.setRadicadoRecibido(rowData.get("Radicado Recibido"));
                dataEntity.setFechaHoraRadicacion(rowData.get("Fecha y Hora Radicación"));
                dataEntity.setTipoTramite(rowData.get("Tipo trámite"));
                dataEntity.setGrupoCausal(rowData.get("Grupo Causal"));
                dataEntity.setDetalleCausal(rowData.get("Detalle Causal"));
                dataEntity.setNumeroCuenta(rowData.get("es>Account Number"));
                dataEntity.setNumeroFactura(rowData.get("Número Factura"));
                dataEntity.setTipoRespuesta(rowData.get("Tipo Respuesta"));
                dataEntity.setFechaRespuesta(rowData.get("Fecha Respuesta"));
                dataEntity.setRadicadoRespuesta(rowData.get("Radicado Respuesta"));
                dataEntity.setFechaNotificacion(rowData.get("Fecha Notificación"));
                dataEntity.setTipoNotificacion(rowData.get("Tipo Notificación"));
                dataEntity.setFechaTransferenciaSspd(rowData.get("Fecha Transferencia SSPD"));
                
                dataRepository.save(dataEntity);
            } catch (Exception e) {
                // Manejar la excepción (por ejemplo, registrarla o devolver un error)
                System.err.println("Error al guardar datos: " + e.getMessage());
            }

        }
    }
}
