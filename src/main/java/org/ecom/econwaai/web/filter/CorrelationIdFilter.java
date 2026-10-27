package org.ecom.econwaai.web.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.ecom.econwaai.common.context.OrgContext;
import org.ecom.econwaai.common.context.OrgContextHolder;
import org.ecom.econwaai.common.util.CorrelationIdUtils;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Filter that attaches a unique Correlation ID (X-Correlation-ID) to every incoming HTTP request.
 * 
 * Runs with HIGHEST_PRECEDENCE so that every downstream filter, controller, service,
 * and database query has access to the correlation ID for distributed tracing and logging.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class CorrelationIdFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        // 1. Check if the client or API Gateway already sent an X-Correlation-ID header
        String correlationId = request.getHeader(CorrelationIdUtils.HEADER_NAME);

        // 2. If no correlation ID was provided in the request, generate a new unique UUID
        if (correlationId == null || correlationId.isBlank()) {
            correlationId = CorrelationIdUtils.generate();
        }

        MDC.put(CorrelationIdUtils.MDC_KEY, correlationId);
        response.setHeader(CorrelationIdUtils.HEADER_NAME, correlationId);

        // 5. Initialize the tenant & request context holder with an anonymous context carrying this correlation ID
        // Allows any downstream service to access OrgContextHolder.getCorrelationId()
        OrgContextHolder.set(OrgContext.anonymous(correlationId));

        try {
            // 6. Proceed with the remaining filters and target controller
            filterChain.doFilter(request, response);
        } finally {
            // 7. Always clean up ThreadLocal variables in the finally block
            // Prevents memory leaks and data bleeding across reused virtual or platform threads
            MDC.remove(CorrelationIdUtils.MDC_KEY);
            OrgContextHolder.clear();
        }
    }
}
