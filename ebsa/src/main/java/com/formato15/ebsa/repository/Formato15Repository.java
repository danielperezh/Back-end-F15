package com.formato15.ebsa.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.formato15.ebsa.clases.FormatoSiec;

@Repository
public interface Formato15Repository extends JpaRepository<FormatoSiec, Long> {

    @Query(value = """
        SELECT 
           substr(f15.codigo_dane, 1, 2) AS dane_dpto,
           substr(f15.codigo_dane, 3, 3) AS dane_mpio,
           substr(f15.codigo_dane, 6, 3) AS dane_asentamiento,
           (SELECT f1.radicado_recibido
              FROM formato_15 f1
             WHERE f1.ano = f15.ano
               AND f1.mes = f15.mes
               AND f1.niu = f15.niu
               AND f1.codigo_dane = f15.codigo_dane
               AND TO_CHAR(f1.fecha_reclamacion, 'DD-MM-YYYY') = TO_CHAR(f15.fecha_reclamacion, 'DD-MM-YYYY')
               AND f1.tipo_tramite = f15.tipo_tramite
               AND f1.detalle_causal = f15.detalle_causal
               AND NVL(f1.id_factura, 0) = NVL(f15.id_factura, 0)
               AND NVL(f1.tipo_respuesta, 0) = NVL(f15.tipo_respuesta, 0)
               AND f1.fecha_respuesta = f15.fecha_respuesta
               AND f1.fecha_notificacion = f15.fecha_notificacion
               AND f1.tipo_notificacion = f15.tipo_notificacion
               AND f1.fecha_traslado_sspd = f15.fecha_traslado_sspd
               AND ROWNUM = 1) AS radicado_recibido,
           f15.fecha_reclamacion,
           f15.tipo_tramite,
           f15.grupo_causal,
           f15.detalle_causal,
           f15.niu,
           f15.id_factura,
           f15.tipo_respuesta,
           f15.fecha_respuesta,
           (SELECT f1.radicado_respuesta
              FROM formato_15 f1
             WHERE f1.ano = f15.ano
               AND f1.mes = f15.mes
               AND f1.niu = f15.niu
               AND f1.codigo_dane = f15.codigo_dane
               AND TO_CHAR(f1.fecha_reclamacion, 'DD-MM-YYYY') = TO_CHAR(f15.fecha_reclamacion, 'DD-MM-YYYY')
               AND f1.tipo_tramite = f15.tipo_tramite
               AND f1.detalle_causal = f15.detalle_causal
               AND NVL(f1.id_factura, 0) = NVL(f15.id_factura, 0)
               AND NVL(f1.tipo_respuesta, 0) = NVL(f15.tipo_respuesta, 0)
               AND f1.fecha_respuesta = f15.fecha_respuesta
               AND f1.fecha_notificacion = f15.fecha_notificacion
               AND f1.tipo_notificacion = f15.tipo_notificacion
               AND f1.fecha_traslado_sspd = f15.fecha_traslado_sspd
               AND ROWNUM = 1) AS radicado_respuesta,
           f15.fecha_notificacion,
           f15.tipo_notificacion,
           f15.fecha_traslado_sspd
        FROM formato_15 f15
        WHERE f15.ano = :ano
          AND f15.mes = :mes
        """, nativeQuery = true)
    List<Object[]> findFullInformation(@Param("ano") Integer ano, @Param("mes") Integer mes);
}

