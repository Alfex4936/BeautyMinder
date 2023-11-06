package app.beautyminder.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

@Slf4j
public class CustomLoggerFilter extends OncePerRequestFilter {

    private static final String CORRELATION_ID_HEADER_NAME = "X-Correlation-ID";

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        MultiReadHttpServletRequest wrappedRequest = new MultiReadHttpServletRequest(request);
        HttpServletResponseCapturingWrapper wrappedResponse = new HttpServletResponseCapturingWrapper(response);

        try {
            // Generate or get existing correlation ID
            String correlationId = Optional.ofNullable(request.getHeader(CORRELATION_ID_HEADER_NAME))
                    .orElse(java.util.UUID.randomUUID().toString());
            MDC.put("correlationId", correlationId); // for logging

            // If you want to ensure the correlation ID is sent back in the response for the client to correlate
            response.addHeader(CORRELATION_ID_HEADER_NAME, correlationId);

            String queryString = request.getQueryString() != null ? "?" + URLDecoder.decode(request.getQueryString(), StandardCharsets.UTF_8) : "";
            String pathWithQueryString = request.getRequestURI() + queryString;

            String username = request.getUserPrincipal() != null ? request.getUserPrincipal().getName() : "anonymous";
            String userIp = getRemoteIP(request);

            log.info("BEMINDER: Incoming request {} {} from IP {} for user {}", request.getMethod(), pathWithQueryString, userIp, username);

            long startTime = System.currentTimeMillis();
            filterChain.doFilter(wrappedRequest, wrappedResponse);
            long duration = System.currentTimeMillis() - startTime;

            if (wrappedResponse.getStatus() != HttpServletResponse.SC_OK) {
                // Log the request body for non-200 responses
                String requestBody = wrappedRequest.getBody();
                log.info("BEMINDER: Error with request body: {}", requestBody);
            }

            log.info("BEMINDER: Outgoing response for {} {} with status {} for user {} took {}ms",
                    request.getMethod(), pathWithQueryString, response.getStatus(), username, duration);
        } catch (Exception e) {
            log.error("BEMINDER: Error processing the request", e);
            throw e;
        } finally {
            // Clear MDC data
            MDC.clear();
        }
    }

    public static String getRemoteIP(HttpServletRequest request) {
        // List of headers that might contain the client IP if request passed through proxies
        String[] headerNames = {
                "X-Forwarded-For",
                "Proxy-Client-IP",
                "WL-Proxy-Client-IP",
                "HTTP_X_FORWARDED_FOR",
                "X-Real-IP"
        };

        for (String header : headerNames) {
            String ip = request.getHeader(header);
            if (ip != null && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip)) {
                // In case the header contains a list of IPs, take the first valid IP
                return ip.split(",")[0].trim();
            }
        }

        // If none of the headers contain an IP, fall back to the remote address
        return request.getRemoteAddr();
    }
}