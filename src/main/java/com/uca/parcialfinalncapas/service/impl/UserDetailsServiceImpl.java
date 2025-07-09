package com.uca.parcialfinalncapas.service.impl;

import com.uca.parcialfinalncapas.entities.User;
import com.uca.parcialfinalncapas.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String correo) throws UsernameNotFoundException {
        User user = userRepository.findByCorreo(correo)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado"));

        return new org.springframework.security.core.userdetails.User(
                user.getCorreo(),
                user.getPassword(),
                List.of(new SimpleGrantedAuthority(user.getNombreRol()))
        );

    }
}

