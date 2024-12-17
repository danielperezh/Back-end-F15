package com.formato15.ebsa.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.formato15.ebsa.clases.Auditoria;
import com.formato15.ebsa.repository.AuditoriaFormato15Repository;

@Service
public class AuditoriaService {

    @Autowired
    private AuditoriaFormato15Repository auditoriaRepository;

    public void registrarCambio(String usuario, String rolUsuario, String nombreArchivo, String accion,
                                 String campoModificado, String valorAnterior, String valorNuevo) {
        Auditoria auditoria = new Auditoria();
        auditoria.setUsuario(usuario);
        // auditoria.setRolUsuario(rolUsuario);
        auditoria.setNombreArchivo(nombreArchivo);
        auditoria.setAccion(accion);
        auditoria.setCampoModificado(campoModificado);
        auditoria.setValorAnterior(valorAnterior);
        auditoria.setValorNuevo(valorNuevo);

        auditoriaRepository.save(auditoria);
    }
}