package com.odin.odin.service;

import com.odin.odin.repository.LogAccesosRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class LogCleanupService {

    private static final Logger log = LoggerFactory.getLogger(LogCleanupService.class);
    private final LogAccesosRepository logAccesosRepository;

    public LogCleanupService(LogAccesosRepository logAccesosRepository) {
        this.logAccesosRepository = logAccesosRepository;
    }

    /**
     * Limpieza automática: se ejecuta todos los domingos a las 03:00 AM.
     * Depura los logs de acceso con más de 90 días de antigüedad para mantener la BD ligera.
     */
    @Scheduled(cron = "0 0 3 * * SUN")
    public void limpiarLogsAutomatico() {
        try {
            LocalDateTime limite = LocalDateTime.now().minusDays(90);
            int eliminados = logAccesosRepository.eliminarLogsAnterioresA(limite);
            log.info("[ODIN AUDITORÍA] Limpieza automática ejecutada: {} logs de acceso eliminados (anteriores a {}).", eliminados, limite);
        } catch (Exception e) {
            log.error("[ODIN AUDITORÍA] Error al ejecutar limpieza automática de logs: {}", e.getMessage());
        }
    }

    /**
     * Limpieza manual invocada por el administrador según los días especificados.
     */
    public int limpiarLogsManual(int dias) {
        if (dias < 7) {
            dias = 7; // Mínimo de seguridad para proteger registros recientes
        }
        LocalDateTime limite = LocalDateTime.now().minusDays(dias);
        return logAccesosRepository.eliminarLogsAnterioresA(limite);
    }
}
