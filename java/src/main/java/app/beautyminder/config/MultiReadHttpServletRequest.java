package app.beautyminder.config;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.util.ContentCachingRequestWrapper;

import java.nio.charset.StandardCharsets;

public class MultiReadHttpServletRequest extends ContentCachingRequestWrapper {

    public MultiReadHttpServletRequest(HttpServletRequest request) {
        super(request);
    }

    public String getBody() {
//        this.getParameterMap()
        byte[] content = this.getContentAsByteArray();
        return new String(content, StandardCharsets.UTF_8);
    }
}