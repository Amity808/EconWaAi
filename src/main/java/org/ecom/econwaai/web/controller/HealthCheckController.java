package org.ecom.econwaai.web.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.ecom.econwaai.common.context.OrgContextHolder;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/health")
@Tag(name = "Health Check", description = "Application and system liveness verification")
public class HealthCheckController {

    @GetMapping
    @Operation(summary = "Liveness check", description = "Returns service status, system timestamp, and correlation ID")
    public ResponseEntity<Map<String, Object>> checkHealth() {
        String correlationId = OrgContextHolder.getCorrelationId().orElse("unknown");
        return ResponseEntity.ok(Map.of(
                "status", "UP",
                "service", "EconWaAi Backend",
                "timestamp", Instant.now(),
                "correlationId", correlationId
        ));
    }
}
