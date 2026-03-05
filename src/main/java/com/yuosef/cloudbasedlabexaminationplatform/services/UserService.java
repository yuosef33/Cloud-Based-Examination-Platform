package com.yuosef.cloudbasedlabexaminationplatform.services;

import com.yuosef.cloudbasedlabexaminationplatform.models.Dtos.*;
import com.yuosef.cloudbasedlabexaminationplatform.models.User;
import jakarta.transaction.SystemException;

public interface UserService {
    AuthResponse login(LoginInfo loginInfo);
    UserDto createUser(UserAccountInfo clientAccInfo) throws SystemException;
    User getUserByEmail(String email) throws SystemException;
    AuthResponse refreshToken(RefreshTokenRequest request);
    void logout(User user);
    AuthResponse exchangeCode(String code);
    UserDto createAdmin(UserAccountInfo clientAccInfo) throws SystemException;
    User getUserFromToken(String token) throws SystemException;
    User getCurrentUser();
}
