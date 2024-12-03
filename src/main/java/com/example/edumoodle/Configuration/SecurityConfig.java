package com.example.edumoodle.Configuration;

import com.example.edumoodle.Service.CustomerUserDetailsServices;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    CustomerUserDetailsServices customUserDetailsServices;

    @Bean
    public static PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

//    // Cho phép tất cả các yêu cầu không cần xác thực
//    @SuppressWarnings("removal")
//    @Bean
//    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
//        http.csrf().disable().authorizeHttpRequests().anyRequest().permitAll();  // Cho phép tất cả các yêu cầu không cần xác thực
//        return http.build();
//    }
    @SuppressWarnings("removal")
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http)throws Exception {

        http.csrf().disable().authorizeHttpRequests()
                .requestMatchers("/user/home", "/user/courses/search", "/user/courses", "/user/courses/category").permitAll()
                .requestMatchers("/admin/**").hasAnyAuthority("ADMIN", "editingteacher")
                .requestMatchers("/manage/**").hasAnyAuthority("editingteacher", "ADMIN")
                .requestMatchers("/user/**").hasAnyAuthority("editingteacher", "student")
                .anyRequest().authenticated().and()
                .formLogin()
                .loginPage("/login")
                .loginProcessingUrl("/login")
                .usernameParameter("username").passwordParameter("password")
                .successHandler(customAuthenticationSuccessHandler())
                .failureUrl("/login?error=true").permitAll()
                .and()
                .logout()
                .invalidateHttpSession(true)
                .clearAuthentication(true)
                .logoutRequestMatcher(new AntPathRequestMatcher("/logout"))

                .logoutSuccessUrl("/login").permitAll();

        return http.build();

    }

    @Bean
    WebSecurityCustomizer webSecurityCustomizer() {
        return (web) -> web.debug(true).ignoring()
                .requestMatchers("/imgs/**", "/images/**","/home", "/css/**", "/js/**", "/assets/**", "/CSV/**");
    }

    @Bean
    public AuthenticationSuccessHandler customAuthenticationSuccessHandler() {
        return new CustomAuthenticationSuccessHandler();
    }

    @Autowired
    public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(customUserDetailsServices).passwordEncoder(passwordEncoder());
    }

}