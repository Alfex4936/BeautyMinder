package app.beautyminder.config;


import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpServletResponseWrapper;

import java.io.IOException;

public class HttpServletResponseCapturingWrapper extends HttpServletResponseWrapper {

    private int httpStatus = SC_OK; // Default to 200

    public HttpServletResponseCapturingWrapper(HttpServletResponse response) {
        super(response);
    }

    @Override
    public void sendError(int sc) throws IOException {
        super.sendError(sc);
        this.httpStatus = sc;
    }

    @Override
    public void sendError(int sc, String msg) throws IOException {
        super.sendError(sc, msg);
        this.httpStatus = sc;
    }

    @Override
    public void sendRedirect(String location) throws IOException {
        super.sendRedirect(location);
        this.httpStatus = SC_MOVED_TEMPORARILY;
    }

    public int getStatus() {
        return httpStatus;
    }

    @Override
    public void setStatus(int sc) {
        super.setStatus(sc);
        this.httpStatus = sc;
    }
}