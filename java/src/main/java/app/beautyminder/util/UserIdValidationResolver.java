package app.beautyminder.util;

import app.beautyminder.service.auth.UserService;
import lombok.AllArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

@AllArgsConstructor
@Component
public class UserIdValidationResolver implements HandlerMethodArgumentResolver {

    private final UserService userService;

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.hasParameterAnnotation(ValidUserId.class) && parameter.getParameterType().equals(String.class);
    }

    @Override
    public Object resolveArgument(@NotNull MethodParameter parameter, ModelAndViewContainer mavContainer, NativeWebRequest webRequest, WebDataBinderFactory binderFactory) {
        String userId = webRequest.getParameter("userId");

        // Check for -1
        if ("-1".equals(userId)) {
            throw new IllegalArgumentException("User ID cannot be -1");
        }

        // Check if user exists in the database
        userService.findById(userId);

        return userId;
    }
}
