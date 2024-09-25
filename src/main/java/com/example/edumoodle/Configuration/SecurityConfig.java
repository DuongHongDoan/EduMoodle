package com.example.edumoodle.Configuration;

import com.example.edumoodle.Service.CustomerUserDetailsServices;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    CustomerUserDetailsServices customUserDetailsServices;

    @Bean
    public static PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
//        return new Sha512PasswordEncoder();
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

                .requestMatchers("/admin/dashboard", "/admin/users", "/admin/users/create", "/admin/users/add-user",
                        "/admin/users/edit", "/admin/users/edit-user", "/admin/users/delete", "users/search", "/admin/categories",
                        "/admin/categories/create", "/admin/categories/create-category", "/admin/courses", "/admin/courses/category",
                        "/admin/courses/view", "/admin/courses/enrolUser", "/admin/courses/unenrol", "/admin/courses/search").authenticated() // Chỉ yêu cầu người dùng đã xác thực truy cập /signature
                .requestMatchers("/student-test").permitAll()
                .requestMatchers("/register").permitAll()
                .requestMatchers("/imgs/**", "/images/**","/home", "/css/**", "/js/**", "/assets/**").permitAll().and()
                .formLogin()
                .loginPage("/login")
                .loginProcessingUrl("/login")
                .defaultSuccessUrl("/admin/dashboard", true)
                .failureUrl("/login?error=true").permitAll()
                .and()
                .logout()
                .invalidateHttpSession(true)
                .clearAuthentication(true)
                .logoutRequestMatcher(new AntPathRequestMatcher("/logout"))

                .logoutSuccessUrl("/login?logout").permitAll();

        return http.build();

    }

    @Autowired
    public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(customUserDetailsServices).passwordEncoder(passwordEncoder());
    }

}