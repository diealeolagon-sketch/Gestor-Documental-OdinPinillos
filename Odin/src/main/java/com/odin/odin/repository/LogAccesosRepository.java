package com.odin.odin.repository;

import com.odin.odin.model.LogAccesos;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

public interface LogAccesosRepository extends JpaRepository<LogAccesos, Long> {

    interface LogAccesoDTO {
        Long getId_log();
        Long getId_usuario();
        String getNombre_usuario();
        String getCorreo_usuario();
        String getNum_identificacion();
        String getAccion();
        String getIp();
        String getUser_agent();
        Boolean getExito();
        String getMotivo();
        LocalDateTime getFecha();
    }

    @Query(value = """
        SELECT l.id_log AS id_log,
               l.id_usuario AS id_usuario,
               u.nombre AS nombre_usuario,
               u.correo AS correo_usuario,
               u.num_identificacion AS num_identificacion,
               l.accion AS accion,
               l.ip AS ip,
               l.user_agent AS user_agent,
               l.exito AS exito,
               l.motivo AS motivo,
               l.fecha AS fecha
        FROM log_accesos l
        LEFT JOIN usuarios u ON l.id_usuario = u.id_usuario
        WHERE (:accion IS NULL OR :accion = '' OR UPPER(l.accion) = UPPER(:accion))
          AND (:exito IS NULL OR l.exito = :exito)
          AND (:buscar IS NULL OR :buscar = '' OR
               LOWER(COALESCE(u.nombre, '')) LIKE LOWER(CONCAT('%', :buscar, '%')) OR
               LOWER(COALESCE(u.correo, '')) LIKE LOWER(CONCAT('%', :buscar, '%')) OR
               LOWER(COALESCE(u.num_identificacion, '')) LIKE LOWER(CONCAT('%', :buscar, '%')) OR
               LOWER(COALESCE(l.ip, '')) LIKE LOWER(CONCAT('%', :buscar, '%')) OR
               LOWER(COALESCE(l.motivo, '')) LIKE LOWER(CONCAT('%', :buscar, '%')))
        ORDER BY l.fecha DESC
        """,
        countQuery = """
        SELECT COUNT(l.id_log)
        FROM log_accesos l
        LEFT JOIN usuarios u ON l.id_usuario = u.id_usuario
        WHERE (:accion IS NULL OR :accion = '' OR UPPER(l.accion) = UPPER(:accion))
          AND (:exito IS NULL OR l.exito = :exito)
          AND (:buscar IS NULL OR :buscar = '' OR
               LOWER(COALESCE(u.nombre, '')) LIKE LOWER(CONCAT('%', :buscar, '%')) OR
               LOWER(COALESCE(u.correo, '')) LIKE LOWER(CONCAT('%', :buscar, '%')) OR
               LOWER(COALESCE(u.num_identificacion, '')) LIKE LOWER(CONCAT('%', :buscar, '%')) OR
               LOWER(COALESCE(l.ip, '')) LIKE LOWER(CONCAT('%', :buscar, '%')) OR
               LOWER(COALESCE(l.motivo, '')) LIKE LOWER(CONCAT('%', :buscar, '%')))
        """,
        nativeQuery = true)
    Page<LogAccesoDTO> buscarLogs(
            @Param("buscar") String buscar,
            @Param("accion") String accion,
            @Param("exito") Boolean exito,
            Pageable pageable
    );

    @Modifying
    @Transactional
    @Query("DELETE FROM LogAccesos l WHERE l.fecha < :fechaLimite")
    int eliminarLogsAnterioresA(@Param("fechaLimite") LocalDateTime fechaLimite);
}
