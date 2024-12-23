package com.formato15.ebsa.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
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
    public void registrarCambio(String accion, String campoModificado, String valorAnterior, String valorNuevo) {
        String usuarioLogueado = obtenerUsuarioLogueado();

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
     * Método para obtener el usuario logueado desde el contexto de seguridad.
     */
    // private String obtenerUsuarioLogueado() {
    //     Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    //     if (principal instanceof UserDetails) {
    //         return ((UserDetails) principal).getUsername();
    //     } else {
    //         return principal.toString();
    //     }
    // }

    private String obtenerUsuarioLogueado() {
        // Obtener el objeto principal del contexto de seguridad
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        
        // Imprimir el tipo y contenido del principal para depuración
        System.out.println("Objeto principal obtenido del contexto de seguridad: " + principal);
        System.out.println("Tipo del objeto principal: " + principal.getClass().getName());
    
        // Verificar si el principal es una instancia de UserDetails
        if (principal instanceof UserDetails) {
            String username = ((UserDetails) principal).getUsername();
            System.out.println("Usuario logueado identificado como: " + username);
            return username;
        } else {
            System.out.println("El usuario logueado no es una instancia de UserDetails, retornando: " + principal.toString());
            return principal.toString();
        }
    }
    
}
