package com.example.edumoodle.Service;

import com.example.edumoodle.Model.UserRoleEntity;
import com.example.edumoodle.Model.UsersEntity;
import com.example.edumoodle.Repository.UsersRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

//kiem tra trung email
@Service
public class CustomerUserDetailsServices implements UserDetailsService {

    @Autowired
    private UsersRepository userRepository;

    @Autowired
    private UserInterface userInterface;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        UsersEntity user = userInterface.findByUsername(username);
        if(user == null) {
            throw new UsernameNotFoundException("Username or Password not found");

        }

        Collection<GrantedAuthority> grantedAuthorities = new HashSet<>();
        Set<UserRoleEntity> roles = user.getUserRole();

        if (roles != null && !roles.isEmpty()) {
            for (UserRoleEntity userRole : roles) {
                grantedAuthorities.add(new SimpleGrantedAuthority(userRole.getRolesEntity().getName()));
            }
        }
        return new CustomUserDetails(user, grantedAuthorities);
    }
}
