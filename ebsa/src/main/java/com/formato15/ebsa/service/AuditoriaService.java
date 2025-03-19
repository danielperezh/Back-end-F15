package com.formato15.ebsa.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.formato15.ebsa.clases.Auditoria;
import com.formato15.ebsa.clases.AuditEntry;
import com.formato15.ebsa.repository.AuditoriaFormato15Repository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class AuditoriaService {

    @Autowired
    private AuditoriaFormato15Repository auditoriaRepository;

    /**
     * Registra un cambio individual en la auditoría.
     */
    public void registrarCambio(String usuarioLogueado, String accion, String campoModificado, String valorAnterior, String valorNuevo) {
        Auditoria auditoria = new Auditoria();
        auditoria.setUsuario(usuarioLogueado);
        auditoria.setAccion(accion);
        auditoria.setCampoModificado(campoModificado);
        auditoria.setValorAnterior(valorAnterior);
        auditoria.setValorNuevo(valorNuevo);
        auditoria.setFechaModificacion(LocalDateTime.now());
        auditoriaRepository.save(auditoria);
    }
    
    /**
     * Registra en lote (batch) una lista de entradas de auditoría.
     * Se espera que la información de cada cambio se encuentre en un objeto AuditEntry,
     * el cual se convierte a la entidad persistente Auditoria.
     */
    public void registrarCambiosBatch(List<AuditEntry> auditEntries) {
        List<Auditoria> auditorias = new ArrayList<>();
        for (AuditEntry entry : auditEntries) {
            Auditoria auditoria = new Auditoria();
            auditoria.setUsuario(entry.getUsuario());
            auditoria.setAccion(entry.getAccion());
            auditoria.setCampoModificado(entry.getCampo());
            auditoria.setValorAnterior(entry.getValorOriginal());
            auditoria.setValorNuevo(entry.getValorNuevo());
            auditoria.setFechaModificacion(LocalDateTime.now());
            auditorias.add(auditoria);
        }
        auditoriaRepository.saveAll(auditorias);
    }
}
