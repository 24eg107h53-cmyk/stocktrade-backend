package com.stocktrade.service;

import com.stocktrade.dto.JwtResponse;
import com.stocktrade.dto.LoginRequest;
import com.stocktrade.dto.RegisterRequest;
import com.stocktrade.dto.UserDto;

public interface AuthService {
    JwtResponse authenticateUser(LoginRequest loginRequest);
    UserDto registerUser(RegisterRequest registerRequest);
    UserDto getCurrentUser(String username);
}
