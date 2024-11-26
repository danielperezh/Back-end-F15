package com.formato15.ebsa.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.formato15.ebsa.clases.FormatoSiec;

@Repository
public interface Formato15Repository extends JpaRepository<FormatoSiec, Long> {
    @Query("SELECT f FROM FormatoSiec f WHERE f.ano = :ano AND f.mes = :mes")
    List<FormatoSiec> findByYearAndMonth(@Param("ano") Integer ano, @Param("mes") Integer mes);
}

