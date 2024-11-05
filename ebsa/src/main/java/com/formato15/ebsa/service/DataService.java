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
            dataEntity.setDepartamentoDANE(rowData.get("departamento_dane"));
            dataEntity.setCiudadDANE(rowData.get("ciudad_dane"));
            dataEntity.setAsentamiento(rowData.get("asentamiento"));
            dataEntity.setRadicadoRecibido(rowData.get("radicado_recibido"));
            dataEntity.setFechaHoraRadicacion(rowData.get("fecha_hora_radicacion"));
            dataEntity.setTipoTramite(rowData.get("tipo_tramite"));
            dataEntity.setGrupoCausal(rowData.get("grupo_causal"));
            dataEntity.setDetalleCausal(rowData.get("detalle_causa"));
            dataEntity.setAccountNumber(rowData.get("numero_cuenta"));
            dataEntity.setNumeroFactura(rowData.get("numero_factura"));
            dataEntity.setTipoRespuesta(rowData.get("tipo_respuesta"));
            dataEntity.setFechaRespuesta(rowData.get("fecha_respuesta"));
            dataEntity.setRadicadoRespuesta(rowData.get("radicado_respuesta"));
            dataEntity.setFechaNotificacion(rowData.get("fecha_notificacion"));
            dataEntity.setTipoNotificacion(rowData.get("tipo_notificacion"));
            dataEntity.setFechaTransferenciaSSPD(rowData.get("fecha_transferencia_sspd"));
            // Completa el resto de los campos
            dataRepository.save(dataEntity);
        }
    }
}
