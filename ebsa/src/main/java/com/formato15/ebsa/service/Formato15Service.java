package com.formato15.ebsa.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.formato15.ebsa.clases.FormatoSiec;
import com.formato15.ebsa.repository.Formato15Repository;

@Service
public class Formato15Service {

    @Autowired
    private final Formato15Repository formato15Repository;

    public Formato15Service(Formato15Repository formato15Repository) {
        this.formato15Repository = formato15Repository;
    }

    public List<FormatoSiec> findByYearAndMonth(Integer ano, Integer mes) {
        return formato15Repository.findByYearAndMonth(ano, mes);
    }

}
