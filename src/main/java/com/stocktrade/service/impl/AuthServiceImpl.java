package com.stocktrade.service.impl;

import com.stocktrade.dto.JwtResponse;
import com.stocktrade.dto.LoginRequest;
import com.stocktrade.dto.RegisterRequest;
import com.stocktrade.dto.UserDto;
import com.stocktrade.entity.Role;
import com.stocktrade.entity.User;
import com.stocktrade.entity.UserStatus;
import com.stocktrade.entity.WalletTransaction;
import com.stocktrade.entity.WalletTransactionType;
import com.stocktrade.exception.UserNotFoundException;
import com.stocktrade.repository.UserRepository;
import com.stocktrade.repository.WalletTransactionRepository;
import com.stocktrade.security.JwtUtils;
import com.stocktrade.security.UserDetailsImpl;
import com.stocktrade.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final WalletTransactionRepository walletTransactionRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;

    @Override
    public JwtResponse authenticateUser(LoginRequest loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getUsernameOrEmail(), loginRequest.getPassword())
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = jwtUtils.generateJwtToken(authentication);

        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        String role = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .findFirst()
                .orElse("ROLE_USER");

        User user = userRepository.findById(userDetails.getId())
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        return JwtResponse.builder()
                .token(jwt)
                .type("Bearer")
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .name(user.getName())
                .role(role)
                .walletBalance(user.getWalletBalance())
                .status(user.getStatus().name())
                .build();
    }

    @Override
    @Transactional
    public UserDto registerUser(RegisterRequest registerRequest) {
        if (userRepository.existsByUsername(registerRequest.getUsername())) {
            throw new IllegalArgumentException("Username is already taken!");
        }

        if (userRepository.existsByEmail(registerRequest.getEmail())) {
            throw new IllegalArgumentException("Email is already in use!");
        }

        // Give new users initial 50,000 virtual trading cash
        BigDecimal initialBalance = new BigDecimal("50000.00");

        User user = User.builder()
                .name(registerRequest.getName())
                .email(registerRequest.getEmail().toLowerCase().trim())
                .username(registerRequest.getUsername().trim())
                .password(passwordEncoder.encode(registerRequest.getPassword()))
                .role(Role.ROLE_USER)
                .walletBalance(initialBalance)
                .status(UserStatus.ACTIVE)
                .createdAt(LocalDateTime.now())
                .build();

        User savedUser = userRepository.save(user);

        // Record initial signup bonus in wallet transactions
        WalletTransaction welcomeDeposit = WalletTransaction.builder()
                .user(savedUser)
                .type(WalletTransactionType.DEPOSIT)
                .amount(initialBalance)
                .transactionDate(LocalDateTime.now())
                .status("COMPLETED")
                .build();
        walletTransactionRepository.save(welcomeDeposit);

        return mapToUserDto(savedUser);
    }

    @Override
    @Transactional(readOnly = true)
    public UserDto getCurrentUser(String username) {
        User user = userRepository.findByUsernameOrEmail(username, username)
                .orElseThrow(() -> new UserNotFoundException("User not found: " + username));
        return mapToUserDto(user);
    }

    private UserDto mapToUserDto(User user) {
        return UserDto.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .username(user.getUsername())
                .role(user.getRole().name())
                .walletBalance(user.getWalletBalance())
                .status(user.getStatus().name())
                .createdAt(user.getCreatedAt())
                .build();
    }
}
