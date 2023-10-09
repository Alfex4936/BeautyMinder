package app.beautyminder.config;

import app.beautyminder.config.jwt.TokenProvider;
import app.beautyminder.domain.RefreshToken;
import app.beautyminder.domain.User;
import app.beautyminder.dto.user.LoginResponse;
import app.beautyminder.repository.RefreshTokenRepository;
import app.beautyminder.service.auth.RefreshTokenService;
import app.beautyminder.service.auth.UserDetailService;
import app.beautyminder.service.auth.UserService;
import app.beautyminder.util.CookieUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
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
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.servlet.handler.HandlerMappingIntrospector;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.springframework.boot.autoconfigure.security.servlet.PathRequest.toH2Console;
import static org.springframework.security.config.Customizer.withDefaults;
import static org.springframework.security.web.util.matcher.AntPathRequestMatcher.antMatcher;

@RequiredArgsConstructor
@Configuration
@EnableMethodSecurity
@EnableWebSecurity
public class WebSecurityConfig {

    public static final Duration REFRESH_TOKEN_DURATION = Duration.ofDays(14);
    //    public static final Duration ACCESS_TOKEN_DURATION = Duration.ofDays(1);
    public static final Duration ACCESS_TOKEN_DURATION = Duration.ofSeconds(3000);
    public static final String REFRESH_TOKEN_COOKIE_NAME = "XRT";
    private final TokenProvider tokenProvider;
    private final RefreshTokenRepository refreshTokenRepository;
    private final RefreshTokenService refreshTokenService;
    private final UserService userService;
    private final UserDetailService userDetailsService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private static final Logger logger = LoggerFactory.getLogger(WebSecurityConfig.class);


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
                .requestMatchers(antMatcher("/static/**"))
                // swagger
                .requestMatchers(antMatcher("/v3/api-docs/**"))
                .requestMatchers(antMatcher("/proxy/**"))
                .requestMatchers(antMatcher("/swagger-ui/**"));
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of("*"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, HandlerMappingIntrospector introspector) throws Exception {
        MvcRequestMatcher.Builder mvcMatcherBuilder = new MvcRequestMatcher.Builder(introspector).servletPath("/path");

        http.csrf(AbstractHttpConfigurer::disable);
        http.cors(withDefaults());
        http.anonymous(AbstractHttpConfigurer::disable);
//                http.httpBasic(AbstractHttpConfigurer::disable);

        http.sessionManagement(s -> s
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        http.authorizeHttpRequests(auth -> auth
                .requestMatchers(HttpMethod.OPTIONS, "/*").permitAll() // preflight request: ex) POST -> OPTIONS -> POST
                .requestMatchers(antMatcher("/")).permitAll()
                .requestMatchers(antMatcher("/api/**")).permitAll()
                .requestMatchers(antMatcher(HttpMethod.POST, "/user/forgot-password")).permitAll()
                .requestMatchers(antMatcher(HttpMethod.GET, "/user/reset-password")).permitAll()
                .requestMatchers(antMatcher(HttpMethod.POST, "/user/reset-password")).permitAll()
                .requestMatchers(antMatcher("/user/signup")).permitAll()
                .requestMatchers(antMatcher("/user/signup-admin")).permitAll()
                .requestMatchers(antMatcher("/gpt/**")).permitAll()
                .requestMatchers(antMatcher("/login")).permitAll()
                .requestMatchers(antMatcher("/login?error")).permitAll()
                .anyRequest().authenticated());

        http.formLogin(f -> f
                        .loginPage("/login")
                        .usernameParameter("email")
                        .passwordParameter("password")
                        .permitAll()
//                .defaultSuccessUrl("/articles")
                        .failureHandler((request, response, exception) -> {
                            logger.warn("Login failed: {}", exception.getMessage());

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
                            logger.info("Login successful for user: {}", authentication.getName());

                            app.beautyminder.domain.User user = (app.beautyminder.domain.User) authentication.getPrincipal();
                            String refreshToken = tokenProvider.generateToken(user, REFRESH_TOKEN_DURATION);
                            saveRefreshToken(user, refreshToken);
                            addRefreshTokenToCookie(request, response, refreshToken);

                            // Generate access token
                            String accessToken = tokenProvider.generateToken(user, ACCESS_TOKEN_DURATION);
                            logger.info("ACCESS TOKEN: {}", accessToken);

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
                            logger.info("Logout successful for user: {}", authentication != null ? authentication.getName() : "Unknown");

                            if (authentication != null && authentication.getPrincipal() instanceof app.beautyminder.domain.User user) {
                                try {
                                    refreshTokenRepository.deleteByUserId(user.getId());
                                    // Optionally, clear the cookie
                                    CookieUtil.deleteCookie(request, response, REFRESH_TOKEN_COOKIE_NAME);
                                } catch (Exception e) {
                                    // log the error
                                    logger.error(e.getMessage());
                                }
                            }

                            SecurityContextHolder.getContext().setAuthentication(null);

                            response.sendRedirect("/login");
                        }))
        );


        http.exceptionHandling(c -> c
                .defaultAuthenticationEntryPointFor(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED),
                        new AntPathRequestMatcher("/api/**")));

        http.authenticationProvider(authenticationProvider());
        http.addFilterBefore(tokenAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }


    @Bean
    public TokenAuthenticationFilter tokenAuthenticationFilter() {
        return new TokenAuthenticationFilter(tokenProvider, refreshTokenService, refreshTokenRepository);
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
        logger.debug("Saving refresh token for user: {}", user.getUsername());

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
        logger.debug("Saving access token for user: {}", user.getUsername());

        RefreshToken refreshToken = refreshTokenRepository.findByUserId(user.getId())
                .map(entity -> entity.update(accessToken))
                .orElse(new RefreshToken(user, accessToken));

        refreshTokenRepository.save(refreshToken);
    }

    private void addRefreshTokenToCookie(HttpServletRequest request, HttpServletResponse response, String refreshToken) {
        logger.debug("Adding refresh token to cookie.");

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
        logger.info("Generating new access token using refresh token for user: {}", user.getUsername());

        String newAccessToken = tokenProvider.generateToken(user, ACCESS_TOKEN_DURATION);
        String newRefreshToken = tokenProvider.generateToken(user, REFRESH_TOKEN_DURATION);

        saveRefreshToken(user, newRefreshToken);
        addRefreshTokenToCookie(request, response, newRefreshToken);

        response.addHeader("Authorization", "Bearer " + newAccessToken);
        response.setStatus(HttpServletResponse.SC_OK);
    }

    private void handleFailure(HttpServletResponse response) {
        logger.warn("Handling failure, redirecting to login page.");

        // Set the content type of the response to JSON
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        // Create a JSON object with the error message
        String jsonMessage = "{\"msg\":\"Unauthorized. Please provide correct user info.\"}";

        // Write the JSON message to the response
        try {
            response.getOutputStream().write(jsonMessage.getBytes());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        try {
            response.sendRedirect("/login?error");
        } catch (IOException e) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        }
    }

}
