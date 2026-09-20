package unla.tp.tp_distribuidos.configuration;


import jakarta.servlet.http.HttpServletResponse;
import unla.tp.tp_distribuidos.services.implementation.UsuarioService;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import static org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher.pathPattern;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;


@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfiguration {

    private final UsuarioService usuarioService;
    private final UserDetailsService userDetailsService;

    public SecurityConfiguration(UsuarioService usuarioService, UserDetailsService userDetailsService) {
        this.usuarioService = usuarioService;
        this.userDetailsService = userDetailsService;
    }

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http, AuthenticationProvider authenticationProvider) throws Exception{
        return http
            .csrf(AbstractHttpConfigurer::disable)
            .cors(AbstractHttpConfigurer::disable)
            .authenticationProvider(authenticationProvider)
            .authorizeHttpRequests(auth -> auth
                    .requestMatchers(
                            "/css/**",
                            "/js/**",
                            "/swagger-ui/**",
                            "/swagger-ui.html",
                            "/v3/api-docs/**",
                            "/login",
                            "/login?error",
                            "/auth/login",
                            "/auth/loginProcess",
                            "/auth/loginSuccess",
                            "/auth/logout"
                    ).permitAll()
		    .requestMatchers("/graphql").authenticated()
                     .anyRequest().hasRole("ADMIN")
		    .requestMatchers("/historial").hasRole("CLIENTE")
                    
            )
            .httpBasic(Customizer.withDefaults())
            .formLogin(form -> form
                    .loginPage("/login")
                    .loginProcessingUrl("/login")
                    .defaultSuccessUrl("/", true)
                    .failureUrl("/login?error")
                    .permitAll()
            )
            .logout(logout -> logout
                    .logoutUrl("/logout")
                    .logoutSuccessUrl("/login?logout")
                    .permitAll()
            )
            .exceptionHandling(exception -> exception
    .defaultAuthenticationEntryPointFor(
            (request, response, authException) -> {
                response.setHeader(
                        "WWW-Authenticate",
                        "Basic realm=\"Rentar\", charset=\"UTF-8\""
                );
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
            },
            pathPattern("/historial")
    )
                    .defaultAuthenticationEntryPointFor(
                            new org.springframework.security.web.authentication.HttpStatusEntryPoint(
                                    HttpStatus.UNAUTHORIZED
                            ),
                            pathPattern("/api_rest/v1/**")
                    )
                    .defaultAccessDeniedHandlerFor(
                            (request, response, accessDeniedException) -> {
                                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                                response.setContentType("application/json");
                                response.getWriter()
                                        .write("{\"error\":\"Acceso denegado\"}");
                            },
                            pathPattern("/api_rest/v1/**")
                    )
            )
            .build();
    }

    @Bean
    AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }
    
    @Bean
    AuthenticationProvider authenticationProvider(){
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(usuarioService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    @Bean
    PasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder();
    }
}