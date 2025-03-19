package com.formato15.ebsa.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.formato15.ebsa.clases.Cuenta;

// @Repository
// public interface CuentaRepository extends JpaRepository<Cuenta, Long> {

//     // Consulta para verificar matrícula y obtener departamento y municipio
//     @Query("SELECT c FROM Cuenta c WHERE c.matricula = :matricula")
//     Optional<Cuenta> findByMatricula(@Param("matricula") Long matricula);
// }

@Repository
public interface CuentaRepository extends JpaRepository<Cuenta, Long> {

    // @Query("SELECT c FROM Cuenta c WHERE CONCAT(c.matricula, c.digitosChequeo) = :numeroCompleto")
    // Optional<Cuenta> findByNumeroCompleto(@Param("numeroCompleto") String numeroCompleto);

    Optional<Cuenta> findByMatriculaAndDigitosChequeo(Long matricula, Long digitosChequeo);

    // Alternativamente, con @Query:
    // @Query("SELECT c FROM Cuenta c WHERE c.matricula = :matricula AND c.digitosChequeo = :digitosChequeo")
    // Optional<Cuenta> findByMatriculaAndDigitosChequeo(@Param("matricula") Long matricula, @Param("digitosChequeo") Long digitosChequeo);
}

