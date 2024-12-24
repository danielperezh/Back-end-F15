package com.formato15.ebsa.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.formato15.ebsa.clases.Auditoria;
import com.formato15.ebsa.repository.AuditoriaFormato15Repository;

import java.time.LocalDateTime;

@Service
public class AuditoriaService {

    @Autowired
    private AuditoriaFormato15Repository auditoriaRepository;

    /**
     * Método para registrar un cambio en la auditoría.
     */
    public void registrarCambio(String usuarioLogueado,String accion, String campoModificado, String valorAnterior, String valorNuevo) {

        Auditoria auditoria = new Auditoria();
        auditoria.setUsuario(usuarioLogueado);
        auditoria.setAccion(accion);
        auditoria.setCampoModificado(campoModificado);
        auditoria.setValorAnterior(valorAnterior);
        auditoria.setValorNuevo(valorNuevo);
        auditoria.setFechaModificacion(LocalDateTime.now());

        auditoriaRepository.save(auditoria);
    }
   
}
