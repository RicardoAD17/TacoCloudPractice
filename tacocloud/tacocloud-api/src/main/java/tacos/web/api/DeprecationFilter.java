package tacos.web.api;

import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
public class DeprecationFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        
        String path = request.getRequestURI();
        
        if (path.startsWith("/api/") && !path.startsWith("/api/v1/")) {
            response.addHeader("Warning", "299 - \"Deprecated API: Please migrate to /api/v1/\"");
            String newPath = "/api/v1" + path.substring(4);
            response.addHeader("Link", "<" + newPath + ">; rel=\"alternate\"");
        }
        
        filterChain.doFilter(request, response);
    }
}