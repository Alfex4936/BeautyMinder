package app.beautyminder.config.chat;

import com.sun.security.auth.UserPrincipal;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.support.DefaultHandshakeHandler;

import java.security.Principal;
import java.util.Map;

public class ClientHandshakeHandler extends DefaultHandshakeHandler{
    @Override
    protected Principal determineUser(ServerHttpRequest req, WebSocketHandler weHandler, Map<String, Object> attributes) {
        return new UserPrincipal((String) attributes.get("username"));
    }

}