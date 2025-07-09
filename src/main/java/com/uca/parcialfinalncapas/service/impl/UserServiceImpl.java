package com.uca.parcialfinalncapas.service.impl;

import com.uca.parcialfinalncapas.dto.request.UserCreateRequest;
import com.uca.parcialfinalncapas.dto.request.UserUpdateRequest;
import com.uca.parcialfinalncapas.dto.response.UserResponse;
import com.uca.parcialfinalncapas.entities.User;
import com.uca.parcialfinalncapas.exceptions.UserNotFoundException;
import com.uca.parcialfinalncapas.repository.UserRepository;
import com.uca.parcialfinalncapas.service.UserService;
import com.uca.parcialfinalncapas.utils.mappers.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder encoder;   // <─ inyectado desde SecurityConfig

    @Override
    public UserResponse findByCorreo(String correo) {
        return UserMapper.toDTO(
                userRepository.findByCorreo(correo)
                        .orElseThrow(() -> new UserNotFoundException("Usuario no encontrado con correo: " + correo))
        );
    }

    @Override
    public UserResponse save(UserCreateRequest request) {

        if (userRepository.findByCorreo(request.getCorreo()).isPresent()) {
            throw new UserNotFoundException("Ya existe un usuario con el correo: " + request.getCorreo());
        }

        /* --------- aquí encriptamos --------- */
        String hashed = encoder.encode(request.getPassword());
        User entity = UserMapper.toEntityCreate(request);
        entity.setPassword(hashed);

        return UserMapper.toDTO(userRepository.save(entity));
    }

    @Override
    public UserResponse update(UserUpdateRequest request) {

        if (userRepository.findById(request.getId()).isEmpty()) {
            throw new UserNotFoundException("No se encontró un usuario con el ID: " + request.getId());
        }

        return UserMapper.toDTO(
                userRepository.save(UserMapper.toEntityUpdate(request))
        );
    }

    @Override
    public void delete(Long id) {
        if (userRepository.findById(id).isEmpty()) {
            throw new UserNotFoundException("No se encontró un usuario con el ID: " + id);
        }
        userRepository.deleteById(id);
    }

    @Override
    public List<UserResponse> findAll() {
        return UserMapper.toDTOList(userRepository.findAll());
    }
}
