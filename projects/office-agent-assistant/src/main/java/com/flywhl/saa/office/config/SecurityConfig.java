package com.flywhl.saa.office.config;

import java.nio.charset.StandardCharsets;
import java.util.List;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;

import com.flywhl.saa.office.model.entity.SysUser;
import com.flywhl.saa.office.repository.SysUserRepository;
import com.nimbusds.jose.jwk.source.ImmutableSecret;

/**
 * JWT 资源服务器 + RBAC。
 *
 * @author flywhl
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private static final List<String> DOC_PATHS = List.of(
            "/doc.html", "/webjars/**", "/v3/api-docs/**", "/swagger-ui/**");

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.POST, "/api/auth/login").permitAll()
                        .requestMatchers("/actuator/health").permitAll()
                        .requestMatchers(DOC_PATHS.toArray(String[]::new)).permitAll()
                        .requestMatchers("/api/**").authenticated()
                        .anyRequest().permitAll())
                .oauth2ResourceServer(oauth2 -> oauth2.jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter())));
        return http.build();
    }

    @Bean
    JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtGrantedAuthoritiesConverter authoritiesConverter = new JwtGrantedAuthoritiesConverter();
        authoritiesConverter.setAuthoritiesClaimName("role");
        authoritiesConverter.setAuthorityPrefix("ROLE_");
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(authoritiesConverter);
        return converter;
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

    @Bean
    UserDetailsService userDetailsService(SysUserRepository userRepository) {
        return username -> userRepository.findByUsername(username)
                .filter(SysUser::getEnabled)
                .map(this::toUserDetails)
                .orElseThrow(() -> new UsernameNotFoundException("用户不存在或已停用: " + username));
    }

    @Bean
    AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }

    @Bean
    JwtEncoder jwtEncoder(OfficeProperties properties) {
        return new NimbusJwtEncoder(new ImmutableSecret<>(jwtSecretKey(properties)));
    }

    @Bean
    JwtDecoder jwtDecoder(OfficeProperties properties) {
        return NimbusJwtDecoder.withSecretKey(jwtSecretKey(properties)).build();
    }

    private org.springframework.security.core.userdetails.UserDetails toUserDetails(SysUser user) {
        return User.builder()
                .username(user.getUsername())
                .password(user.getPasswordHash())
                .roles(user.getRole())
                .build();
    }

    private static SecretKey jwtSecretKey(OfficeProperties properties) {
        byte[] secretBytes = properties.security().jwt().secret().getBytes(StandardCharsets.UTF_8);
        return new SecretKeySpec(secretBytes, "HmacSHA256");
    }
}
