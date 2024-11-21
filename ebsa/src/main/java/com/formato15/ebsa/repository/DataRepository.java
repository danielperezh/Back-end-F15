package com.formato15.ebsa.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.formato15.ebsa.clases.Formato15;

@Repository
public interface DataRepository extends JpaRepository<Formato15, Long> {
}


