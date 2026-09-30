package tacos.web.filter;

import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.UUID;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class CorrelationIdFilter extends OncePerRequestFilter {

    public static final String CORRELATION_ID_HEADER = "X-Correlation-Id";
    public static final String CORRELATION_ID_KEY = "correlationId";

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String reqCorId = request.getHeader(CORRELATION_ID_HEADER);
       
        String correlationId = sanitizeOrGenerate(reqCorId);

        response.setHeader(CORRELATION_ID_HEADER, correlationId);
        try {
            MDC.put(CORRELATION_ID_KEY, correlationId);
            filterChain.doFilter(request, response);
            
        } finally {
            MDC.remove(CORRELATION_ID_KEY);
        }
    }

    private String sanitizeOrGenerate(String id) {

        if (id != null && id.length() <= 36 && id.matches("^[a-zA-Z0-9-]+$")) {
            return id;
        }
        return UUID.randomUUID().toString();
    }
}