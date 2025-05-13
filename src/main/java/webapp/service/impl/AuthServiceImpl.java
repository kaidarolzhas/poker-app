package webapp.service.impl;


import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import webapp.data.dto.LoginRequestDto;
import webapp.data.dto.LoginResponseDto;
import webapp.data.dto.RegistrationRequestDto;
import webapp.data.entity.Role;
import webapp.data.entity.User;
import webapp.data.exception.AuthException;
import webapp.sec.JwtUtil;
import webapp.sec.repository.UserRepositoryJava;
import webapp.service.AuthService;
import webapp.service.RoleService;

import java.util.HashSet;
import java.util.Set;

import static webapp.sec.Constants.INVALID_USERNAME_OR_PASSWORD;
import static webapp.sec.Constants.USERNAME_EXISTS;


@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
    private final UserRepositoryJava repository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final RoleService roleService;

    @Override
    public void registration(RegistrationRequestDto registrationRequest) {
        if (repository.findByUsername(registrationRequest.getUsername()).isPresent()) {
            throw new AuthException(USERNAME_EXISTS);
        }
        create(registrationRequest.getUsername(), registrationRequest.getPassword());
    }

    private void create(String username, String password) {
        Set<Role> role = new HashSet<>();
        role.add(roleService.findByName("PLAYER"));
        repository.save(
                User.builder()
                        .roles(role)
                        .isEnabled(true)
                        .username(username)
                        .password(passwordEncoder.encode(password))
                        .build()
        );
    }

    @Override
    public LoginResponseDto login(LoginRequestDto loginRequestDto) {
        User user = repository.findByUsername(loginRequestDto.getUsername()).orElseThrow(()-> new AuthException(INVALID_USERNAME_OR_PASSWORD));
        if (!passwordEncoder.matches(loginRequestDto.getPassword(), user.getPassword())) {
            throw new AuthException(INVALID_USERNAME_OR_PASSWORD);
        }
        return new LoginResponseDto(jwtUtil.generateToken(user.getId(), user.getRoles()));
    }
}
