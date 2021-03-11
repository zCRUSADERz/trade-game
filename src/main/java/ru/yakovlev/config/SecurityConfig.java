/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2021 Yakovlev Alexander
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package ru.yakovlev.config;

import java.util.Map;
import lombok.val;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.security.access.hierarchicalroles.RoleHierarchyImpl;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.argon2.Argon2PasswordEncoder;
import org.springframework.security.crypto.password.DelegatingPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.JdbcUserDetailsManager;
import org.springframework.security.provisioning.UserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;

/**
 * Spring security configuration.
 *
 * @author Yakovlev Aleander (sanyakovlev@yandex.ru)
 * @since 0.1.0
 */
@Configuration
@EnableGlobalMethodSecurity(prePostEnabled = true, securedEnabled = true)
public class SecurityConfig {

    @Bean
    PasswordEncoder argon2Encoder() {
        return new DelegatingPasswordEncoder("argon2", Map.of("argon2", new Argon2PasswordEncoder()));
    }

    @Bean
    SecurityFilterChain defaultSecurityFilterChain(HttpSecurity http) throws Exception {
        http.authorizeRequests().antMatchers("/").permitAll();
        http.authorizeRequests().mvcMatchers("/error").permitAll();
        http.authorizeRequests().antMatchers("/profile/**").permitAll();
        http.authorizeRequests().antMatchers("/orders/sendToExecution").hasRole("ADMIN");
        http.authorizeRequests().antMatchers("/orders/addWorkersForOrderExecution").hasRole("ADMIN");
        http.authorizeRequests().antMatchers("/orders/createOrders").hasAuthority("BATCH_ORDER_CREATION");
        http.authorizeRequests().anyRequest().authenticated().and().httpBasic();
        http.csrf().csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse());
        return http.build();
    }

    @Bean
    UserDetailsManager userDetailsManager(JdbcTemplate template) {
        final var jdbcUserDetailsManager = new JdbcUserDetailsManager();
        jdbcUserDetailsManager.setJdbcTemplate(template);
        jdbcUserDetailsManager.setEnableGroups(true);
        return jdbcUserDetailsManager;
    }

    @Bean
    RoleHierarchy roleHierarchy() {
        val roleHierarchy = new RoleHierarchyImpl();
        roleHierarchy.setHierarchy("ROLE_ADMIN > ROLE_SUPERVISOR");
        return roleHierarchy;
    }

}
