package app.beautyminder.config;

import app.beautyminder.config.jwt.TokenProvider;
import app.beautyminder.domain.RefreshToken;
import app.beautyminder.domain.User;
import app.beautyminder.dto.user.LoginResponse;
import app.beautyminder.repository.RefreshTokenRepository;
import app.beautyminder.service.RefreshTokenService;
import app.beautyminder.service.UserDetailService;
import app.beautyminder.service.UserService;
import app.beautyminder.util.CookieUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.firewall.StrictHttpFirewall;
import org.springframework.security.web.servlet.util.matcher.MvcRequestMatcher;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.web.servlet.handler.HandlerMappingIntrospector;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Optional;

import static org.springframework.boot.autoconfigure.security.servlet.PathRequest.toH2Console;
import static org.springframework.security.web.util.matcher.AntPathRequestMatcher.antMatcher;

@RequiredArgsConstructor
@Configuration
public class WebSecurityConfig {

    public static final Duration REFRESH_TOKEN_DURATION = Duration.ofDays(14);
    public static final Duration ACCESS_TOKEN_DURATION = Duration.ofDays(1);
    public static final String REFRESH_TOKEN_COOKIE_NAME = "XRT";

    private final TokenProvider tokenProvider;
    private final RefreshTokenRepository refreshTokenRepository;
    private final RefreshTokenService refreshTokenService;
    private final UserService userService;
    private final UserDetailService userDetailsService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Bean
    public StrictHttpFirewall httpFirewall() {
        StrictHttpFirewall firewall = new StrictHttpFirewall();
        firewall.setAllowSemicolon(true);
        firewall.setAllowBackSlash(true);
        firewall.setAllowUrlEncodedDoubleSlash(true);
        return firewall;
    }

    @Bean
    public WebSecurityCustomizer configure() {
        return (web) -> web.ignoring()

                .requestMatchers(antMatcher(toH2Console().toString()))
                .requestMatchers(antMatcher("/img/**"))
                .requestMatchers(antMatcher("/css/**"))
                .requestMatchers(antMatcher("/js/**"))
                .requestMatchers(antMatcher("/static/**"));
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, HandlerMappingIntrospector introspector) throws Exception {
        MvcRequestMatcher.Builder mvcMatcherBuilder = new MvcRequestMatcher.Builder(introspector).servletPath("/path");

        http.csrf(AbstractHttpConfigurer::disable);
        http.anonymous(AbstractHttpConfigurer::disable);

//                http.httpBasic(AbstractHttpConfigurer::disable);

        http.sessionManagement(s -> s
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        http.addFilterBefore(tokenAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class);


        http.authorizeHttpRequests(auth -> auth
                .requestMatchers(mvcMatcherBuilder.pattern("/")).permitAll()
                .requestMatchers(mvcMatcherBuilder.pattern("/signup")).permitAll()
                .requestMatchers(mvcMatcherBuilder.pattern("/user")).permitAll()
                .requestMatchers(mvcMatcherBuilder.pattern("/admin")).permitAll()
                .requestMatchers(mvcMatcherBuilder.pattern("/api/token")).permitAll()
                .requestMatchers(mvcMatcherBuilder.pattern("/api/**")).authenticated()
                .requestMatchers(mvcMatcherBuilder.pattern("/admin/**")).hasAuthority("ROLE_ADMIN")
                .requestMatchers(mvcMatcherBuilder.pattern("/diaries")).authenticated()
                .requestMatchers(mvcMatcherBuilder.pattern("/protected")).authenticated()
                .anyRequest().permitAll());

        http.formLogin(f -> f
                        .loginPage("/login")
                        .usernameParameter("email")
                        .passwordParameter("password")
                        .permitAll()
//                .defaultSuccessUrl("/articles")
                        .failureHandler((request, response, exception) -> {
                            Optional<String> optionalRefreshToken = getRefreshTokenFromRequest(request);

                            optionalRefreshToken
                                    .filter(tokenProvider::validToken)
                                    .flatMap(refreshTokenService::findUserByRefreshToken)
                                    .ifPresentOrElse(
                                            user -> generateNewAccessTokenAndRespond(user, request, response),
                                            () -> handleFailure(response)
                                    );
                        })
                        .successHandler(((request, response, authentication) -> {
                            app.beautyminder.domain.User user = (app.beautyminder.domain.User) authentication.getPrincipal();
                            String refreshToken = tokenProvider.generateToken(user, REFRESH_TOKEN_DURATION);
                            saveRefreshToken(user, refreshToken);
                            addRefreshTokenToCookie(request, response, refreshToken);

                            // Generate access token
                            String accessToken = tokenProvider.generateToken(user, ACCESS_TOKEN_DURATION);
//                            saveAccessToken(user, accessToken);

                            System.out.println("WHAT IS TOKEN: " + accessToken);

                            // Add the Bearer token as a cookie
//                            CookieUtil.addCookie(response, "BEARER_TOKEN", accessToken, (int) ACCESS_TOKEN_DURATION.toSeconds());

                            response.setContentType("application/json");
                            response.setCharacterEncoding("utf-8");
                            LoginResponse login = new LoginResponse(accessToken, refreshToken, user);

                            String result = objectMapper.registerModule(new JavaTimeModule()).writeValueAsString(login);

                            response.addHeader("Authorization", "Bearer " + accessToken);
                            response.getWriter().write(result);
                        }))

        );


        http.logout(l -> l
                        .permitAll()
                        .logoutUrl("/logout")
                        .invalidateHttpSession(true)
                        .logoutSuccessHandler(((request, response, authentication) -> {
                            if (authentication != null && authentication.getPrincipal() instanceof app.beautyminder.domain.User user) {
                                try {
                                    refreshTokenRepository.deleteByUserId(user.getId());
                                    // Optionally, clear the cookie
                                    CookieUtil.deleteCookie(request, response, REFRESH_TOKEN_COOKIE_NAME);
                                } catch (Exception e) {
                                    // log the error
                                }
                            }

//                    CookieUtil.deleteCookie(request, response, "BEARER_TOKEN");
                            SecurityContextHolder.getContext().setAuthentication(null);

                            response.sendRedirect("/login");
                        }))
        );


        http.exceptionHandling(c -> c
                .defaultAuthenticationEntryPointFor(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED),
                        new AntPathRequestMatcher("/api/**")));

        http.authenticationProvider(authenticationProvider());

        return http.build();
    }


    @Bean
    public TokenAuthenticationFilter tokenAuthenticationFilter() {
        return new TokenAuthenticationFilter(tokenProvider);
    }

    @Bean
    public BCryptPasswordEncoder bCryptPasswordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();

        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(bCryptPasswordEncoder());

        return authProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    private void saveRefreshToken(User user, String newRefreshToken) {
        LocalDateTime expiresAt = LocalDateTime.now().plus(REFRESH_TOKEN_DURATION);

        RefreshToken refreshToken = refreshTokenRepository.findByUserId(user.getId())
                .map(entity -> {
                    entity.update(newRefreshToken);
                    entity.setExpiresAt(expiresAt);
                    return entity;
                })
                .orElse(new RefreshToken(user, newRefreshToken, expiresAt));

        refreshTokenRepository.save(refreshToken);
    }

    private void saveAccessToken(User user, String accessToken) {
        RefreshToken refreshToken = refreshTokenRepository.findByUserId(user.getId())
                .map(entity -> entity.update(accessToken))
                .orElse(new RefreshToken(user, accessToken));

        refreshTokenRepository.save(refreshToken);
    }

    private void addRefreshTokenToCookie(HttpServletRequest request, HttpServletResponse response, String refreshToken) {
        int cookieMaxAge = (int) REFRESH_TOKEN_DURATION.toSeconds();

        CookieUtil.deleteCookie(request, response, REFRESH_TOKEN_COOKIE_NAME);
        CookieUtil.addCookie(response, REFRESH_TOKEN_COOKIE_NAME, refreshToken, cookieMaxAge);
    }

    private void addAccessTokenToCookie(HttpServletRequest request, HttpServletResponse response, String accessToken) {
        int cookieMaxAge = (int) ACCESS_TOKEN_DURATION.toSeconds();
        CookieUtil.addCookie(response, "access_token", accessToken, cookieMaxAge);
    }

    private Optional<String> getRefreshTokenFromRequest(@NotNull HttpServletRequest request) {
        // Retrieve the refresh token from a cookie, database, or other source
        return Optional.ofNullable(request.getCookies())
                .flatMap(cookies -> Arrays.stream(cookies)
                        .filter(c -> REFRESH_TOKEN_COOKIE_NAME.equals(c.getName()))
                        .findFirst())
                .map(Cookie::getValue);
    }

    private void generateNewAccessTokenAndRespond(User user, HttpServletRequest request, HttpServletResponse response) {
        String newAccessToken = tokenProvider.generateToken(user, ACCESS_TOKEN_DURATION);
        String newRefreshToken = tokenProvider.generateToken(user, REFRESH_TOKEN_DURATION);

        saveRefreshToken(user, newRefreshToken);
        addRefreshTokenToCookie(request, response, newRefreshToken);

        response.addHeader("Authorization", "Bearer " + newAccessToken);
        response.setStatus(HttpServletResponse.SC_OK);
    }

    private void handleFailure(HttpServletResponse response) {
        try {
            response.sendRedirect("/login?error");
        } catch (IOException e) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        }
    }

}
