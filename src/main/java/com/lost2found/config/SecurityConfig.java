package com.lost2found.config;

import com.lost2found.service.CustomUserDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Autowired
    private CustomUserDetailsService userDetailsService;

    // ✅ Password Encoder Bean
    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // ✅ Authentication Provider
    @Bean
    public DaoAuthenticationProvider authenticationProvider() {

        DaoAuthenticationProvider authProvider =
                new DaoAuthenticationProvider();

        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());

        return authProvider;
    }

    // ✅ Security Filter Chain
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http)
            throws Exception {

        http

                // ✅ Disable CSRF for testing
                .csrf(csrf -> csrf.disable())

                // ✅ URL Permissions
                .authorizeHttpRequests(auth -> auth

                        .requestMatchers(
                                "/",
                                "/home",
                                "/register",
                                "/user/register",
                                "/login",
                                "/forgot-password/**",
                                "/TermsAndCondition",
                                "/css/**",
                                "/js/**",
                                "/images/**",
                                "/videos/**"
                        ).permitAll()

                        .anyRequest().authenticated()
                )

                // ✅ Login Configuration
                .formLogin(form -> form

                        .loginPage("/login")
                        .loginProcessingUrl("/login")

                        .defaultSuccessUrl("/index", true)

                        .failureUrl("/login?error=true")

                        .permitAll()
                )

                // ✅ Logout Configuration
                .logout(logout -> logout

                        .logoutUrl("/logout")

                        .logoutSuccessUrl("/home")

                        .invalidateHttpSession(true)

                        .deleteCookies("JSESSIONID")

                        .permitAll()
                )

                // ✅ Authentication Provider
                .authenticationProvider(authenticationProvider())

                .httpBasic(Customizer.withDefaults());

        return http.build();
    }
}