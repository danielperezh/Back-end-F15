package com.formato15.ebsa.repository;

import com.formato15.ebsa.clases.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UsuarioRepositorio extends JpaRepository<Usuario, String> {
    Optional<Usuario> findByUsuario(String usuario);
}
