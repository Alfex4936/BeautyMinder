package app.beautyminder.config;

import app.beautyminder.config.jwt.TokenProvider;
import app.beautyminder.domain.KeywordRank;
import app.beautyminder.domain.RefreshToken;
import app.beautyminder.domain.User;
import app.beautyminder.dto.user.LoginResponse;
import app.beautyminder.repository.RefreshTokenRepository;
import app.beautyminder.repository.UserRepository;
import app.beautyminder.service.MongoService;
import app.beautyminder.service.auth.RefreshTokenService;
import app.beautyminder.service.auth.UserDetailService;
import app.beautyminder.util.CookieUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.jetbrains.annotations.NotNull;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
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
import static org.springframework.security.web.util.matcher.AntPathRequestMatcher.antMatcher;

@Slf4j
@RequiredArgsConstructor
@Configuration
@EnableMethodSecurity
@EnableWebSecurity
public class WebSecurityConfig {

    public static final Duration REFRESH_TOKEN_DURATION = Duration.ofDays(21);
    public static final Duration ACCESS_TOKEN_DURATION = Duration.ofDays(14);
    public static final String REFRESH_TOKEN_COOKIE_NAME = "XRT";
    private final TokenProvider tokenProvider;
    private final RefreshTokenRepository refreshTokenRepository;
    private final RefreshTokenService refreshTokenService;
    private final UserDetailService userDetailsService;
    private final MongoService mongoService;
    private final ObjectMapper objectMapper;

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

    // Whitelist approach
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, HandlerMappingIntrospector introspector) throws Exception {
        MvcRequestMatcher.Builder mvcMatcherBuilder = new MvcRequestMatcher.Builder(introspector).servletPath("/path");

        http.csrf(AbstractHttpConfigurer::disable);
        http.cors(corsConfigurer -> corsConfigurer.configurationSource(corsConfigurationSource()));
        http.anonymous(AbstractHttpConfigurer::disable);
//                http.httpBasic(AbstractHttpConfigurer::disable);

        http.sessionManagement(s -> s
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        http.authorizeHttpRequests(auth -> auth
                .requestMatchers(HttpMethod.OPTIONS, "/*").permitAll() // preflight request: ex) POST -> OPTIONS -> POST
                .requestMatchers(antMatcher("/")).permitAll()
                .requestMatchers(antMatcher("/api/**")).permitAll()

//                .requestMatchers(antMatcher("/expiry/**")).permitAll()
//                .requestMatchers(antMatcher("/es-index/**")).permitAll()
//                .requestMatchers(antMatcher("/todo/**")).permitAll()
                .requestMatchers(antMatcher("/gpt/review/**")).permitAll()
                .requestMatchers(antMatcher("/redis/**")).permitAll()
                .requestMatchers(antMatcher("/cosmetic/hit/**")).permitAll()
                .requestMatchers(antMatcher("/cosmetic/click/**")).permitAll()
//                .requestMatchers(antMatcher("/search/**")).permitAll()
//                .requestMatchers(antMatcher("/data-view/**")).permitAll()
                .requestMatchers(antMatcher("/baumann/survey")).permitAll()
                .requestMatchers(antMatcher("/search/test")).permitAll()
//                .requestMatchers(antMatcher("/review/**")).permitAll()

                .requestMatchers(antMatcher("/login")).permitAll()
                .requestMatchers(antMatcher("/login?error")).permitAll()

                .requestMatchers(antMatcher("/user/forgot-password")).permitAll()
                .requestMatchers(antMatcher("/user/sms/**")).permitAll()
                .requestMatchers(antMatcher(HttpMethod.GET, "/user/reset-password")).permitAll()
                .requestMatchers(antMatcher(HttpMethod.POST, "/user/reset-password")).permitAll()
                .requestMatchers(antMatcher("/user/signup")).permitAll()
                .requestMatchers(antMatcher("/user/signup-admin")).permitAll()
                .requestMatchers(antMatcher("/user/sms/send")).permitAll()

                .anyRequest().permitAll());

        http.formLogin(f -> f
                        .loginPage("/login")
                        .usernameParameter("email")
                        .passwordParameter("password")
                        .permitAll()
//                .defaultSuccessUrl("/articles")
                        .failureHandler((request, response, exception) -> {
                            log.warn("Login failed: {}", exception.getMessage());

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
                            log.info("Login successful for user: {}", authentication.getName());

                            var user = (app.beautyminder.domain.User) authentication.getPrincipal();
                            mongoService.touch(User.class, user.getId(), "lastLogin");

                            var refreshToken = tokenProvider.generateToken(user, REFRESH_TOKEN_DURATION);
                            saveRefreshToken(user, refreshToken);
                            addRefreshTokenToCookie(request, response, refreshToken);

                            // Generate access token
                            var accessToken = tokenProvider.generateToken(user, ACCESS_TOKEN_DURATION);
                            log.info("ACCESS TOKEN: {}", accessToken);

                            response.setContentType("application/json");
                            response.setCharacterEncoding("utf-8");
                            LoginResponse login = new LoginResponse(accessToken, refreshToken, user);

                            var result = objectMapper.writeValueAsString(login);

                            response.addHeader("Authorization", "Bearer " + accessToken);
                            response.getWriter().write(result);
                        }))
        );


        http.logout(l -> l
                .permitAll()
                .logoutUrl("/logout")
                .invalidateHttpSession(true)
                .logoutSuccessHandler(((request, response, authentication) -> {
                    log.info("Logout successful for user: {}", authentication != null ? authentication.getName() : "Unknown");

                    if (authentication != null && authentication.getPrincipal() instanceof app.beautyminder.domain.User user) {
                        try {
                            refreshTokenRepository.deleteByUserId(new ObjectId(user.getId()));
                            // Optionally, clear the cookie
                            CookieUtil.deleteCookie(request, response, REFRESH_TOKEN_COOKIE_NAME);
                        } catch (Exception e) {
                            // log the error
                            log.error(e.getMessage());
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
    public FilterRegistrationBean<CustomLoggerFilter> loggingFilter() {
        FilterRegistrationBean<CustomLoggerFilter> registrationBean = new FilterRegistrationBean<>();
        registrationBean.setFilter(new CustomLoggerFilter());
        registrationBean.addUrlPatterns("/*");
        return registrationBean;
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
        log.debug("Saving refresh token for user: {}", user.getUsername());

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
        log.debug("Saving access token for user: {}", user.getUsername());

        RefreshToken refreshToken = refreshTokenRepository.findByUserId(user.getId())
                .map(entity -> entity.update(accessToken))
                .orElse(new RefreshToken(user, accessToken));

        refreshTokenRepository.save(refreshToken);
    }

    private void addRefreshTokenToCookie(HttpServletRequest request, HttpServletResponse response, String refreshToken) {
        log.debug("Adding refresh token to cookie.");

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
        log.info("Generating new access token using refresh token for user: {}", user.getUsername());

        String newAccessToken = tokenProvider.generateToken(user, ACCESS_TOKEN_DURATION);
        String newRefreshToken = tokenProvider.generateToken(user, REFRESH_TOKEN_DURATION);

        saveRefreshToken(user, newRefreshToken);
        addRefreshTokenToCookie(request, response, newRefreshToken);

        response.addHeader("Authorization", "Bearer " + newAccessToken);
        response.setStatus(HttpServletResponse.SC_OK);
    }

    private void handleFailure(HttpServletResponse response) {
        log.warn("Handling failure, redirecting to login page.");

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
