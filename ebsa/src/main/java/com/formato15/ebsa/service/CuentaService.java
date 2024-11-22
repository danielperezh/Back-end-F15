package com.formato15.ebsa.service;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.formato15.ebsa.clases.Cuenta;
import com.formato15.ebsa.repository.CuentaRepository;

@Service
public class CuentaService {

    @Autowired
    private CuentaRepository cuentaRepository;

    public Optional<Cuenta> getCuentaPorMatricula(Long matricula) {
        return cuentaRepository.findByMatricula(matricula);
    }
}

