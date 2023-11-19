package app.beautyminder.aop;

import app.beautyminder.domain.User;
import app.beautyminder.util.AuthenticatedUser;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.stream.IntStream;

/*
AOP to translate a user into authenticated user if on validation.

Works for all mapping functions (GET, POST, ...)
 */
@Aspect
@Component
public class AuthenticatedUserAspect {

    @Pointcut("@annotation(org.springframework.web.bind.annotation.RequestMapping) " +
            "|| @annotation(org.springframework.web.bind.annotation.GetMapping)" +
            "|| @annotation(org.springframework.web.bind.annotation.PostMapping)" +
            "|| @annotation(org.springframework.web.bind.annotation.PatchMapping)" +
            "|| @annotation(org.springframework.web.bind.annotation.PutMapping)" +
            "|| @annotation(org.springframework.web.bind.annotation.DeleteMapping)")
    public void mappingAnnotations() {
    }

    @Around("mappingAnnotations()")
    public Object addAuthenticatedUser(ProceedingJoinPoint joinPoint) throws Throwable {
        var signature = (MethodSignature) joinPoint.getSignature();
        var method = signature.getMethod();

        Annotation[][] parameterAnnotations = method.getParameterAnnotations();
        Object[] args = joinPoint.getArgs();

        for (int i = 0; i < parameterAnnotations.length; i++) {
            if (Arrays.stream(parameterAnnotations[i]).anyMatch(annotation -> annotation.annotationType() == AuthenticatedUser.class)) {
                Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
                if (authentication == null || !authentication.isAuthenticated()) {
                    throw new IllegalStateException("No authenticated user found");
                }
                if (authentication.getPrincipal() instanceof User user) {
                    args[i] = user;
                }
                // Once handled, no need to check other annotations of this parameter
                break;
            }
        }

        return joinPoint.proceed(args);
    }
}