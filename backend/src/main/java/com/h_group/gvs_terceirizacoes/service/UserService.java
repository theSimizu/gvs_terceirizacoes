package com.h_group.gvs_terceirizacoes.service;

import com.h_group.gvs_terceirizacoes.config.JwtService;
import com.h_group.gvs_terceirizacoes.dto.request.LoginRequest;
import com.h_group.gvs_terceirizacoes.dto.request.RegisterRequest;
import com.h_group.gvs_terceirizacoes.dto.response.AuthResponse;
import com.h_group.gvs_terceirizacoes.dto.response.UserResponse;
import com.h_group.gvs_terceirizacoes.entity.User;
import com.h_group.gvs_terceirizacoes.entity.UserRole;
import com.h_group.gvs_terceirizacoes.exception.ApiException;
import com.h_group.gvs_terceirizacoes.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;
import java.util.UUID;

/**
 * Serviço transacional de cadastro, autenticação e consulta da identidade.
 * O cadastro público produz contas USER; respostas nunca incluem senha ou hash.
 */
@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public UserService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    /**
     * Cadastra uma conta comum e protege a senha com o encoder configurado.
     * @param request dados de entrada validados pelo controller
     * @return identificação da conta criada, com perfil USER
     * @throws com.h_group.gvs_terceirizacoes.exception.ApiException se o e-mail já existir
     */
    @Transactional
    public UserResponse register(RegisterRequest request) {
        String email = normalizeEmail(request.email());

        if (userRepository.existsByEmailIgnoreCase(email)) {
            throw new ApiException("E-mail already registered", HttpStatus.CONFLICT);
        }

        User user = new User();
        user.setName(request.name().trim());
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setRole(UserRole.USER);

        return UserResponse.from(userRepository.save(user));
    }

    /**
     * Confere a senha e emite a sessão autenticada.
     * @param request e-mail e senha informados
     * @return token e identificação pública da conta
     * @throws com.h_group.gvs_terceirizacoes.exception.ApiException se a credencial for inválida
     */
    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        String email = normalizeEmail(request.email());
        User user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new ApiException("Invalid email or password", HttpStatus.UNAUTHORIZED));

        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new ApiException("Invalid email or password", HttpStatus.UNAUTHORIZED);
        }

        return new AuthResponse(jwtService.generateToken(user), UserResponse.from(user));
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }

    /**
     * Consulta a identidade e o perfil atuais sem expor credenciais.
     * @param id identificador validado pelo filtro
     * @return dados públicos da conta
     * @throws com.h_group.gvs_terceirizacoes.exception.ApiException se a conta não existir
     */
    @Transactional(readOnly = true)
    public UserResponse currentUser(UUID id) {
        return UserResponse.from(userRepository.findById(id)
                .orElseThrow(() -> new ApiException("User not found", HttpStatus.UNAUTHORIZED)));
    }
}
