package com.h_group.gvs_terceirizacoes.service;

import com.h_group.gvs_terceirizacoes.dto.request.RegisterRequest;
import com.h_group.gvs_terceirizacoes.entity.User;
import com.h_group.gvs_terceirizacoes.entity.UserRole;
import com.h_group.gvs_terceirizacoes.repository.UserRepository;
import jakarta.validation.Validator;
import java.util.Locale;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class InitialAdminService implements ApplicationRunner {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final Validator validator;
    private final String email;
    private final String password;
    private final String name;

    public InitialAdminService(UserRepository userRepository, PasswordEncoder passwordEncoder, Validator validator,
            @Value("${gvs.bootstrap-admin.email:}") String email,
            @Value("${gvs.bootstrap-admin.password:}") String password,
            @Value("${gvs.bootstrap-admin.name:Administrador GVS}") String name) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.validator = validator;
        this.email = email.trim().toLowerCase(Locale.ROOT);
        this.password = password;
        this.name = name;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (email.isEmpty() && password.isEmpty()) {
            return;
        }
        if (!validator.validate(new RegisterRequest(name, email, password)).isEmpty()) {
            throw new IllegalStateException("Invalid initial admin configuration: check name, email and password requirements");
        }
        if (userRepository.existsByRole(UserRole.ADM)) {
            return;
        }
        if (userRepository.existsByEmailIgnoreCase(email)) {
            throw new IllegalStateException("Initial admin email already belongs to a user; choose a new email or perform an authorized database migration");
        }
        User admin = new User();
        admin.setName(name.trim());
        admin.setEmail(email);
        admin.setPassword(passwordEncoder.encode(password));
        admin.setRole(UserRole.ADM);
        userRepository.save(admin);
    }
}
