package com.yuosef.demo1.cloudexaminationplatform.Services;

import com.yuosef.demo1.cloudexaminationplatform.Models.Dtos.LoginInfo;
import com.yuosef.demo1.cloudexaminationplatform.Models.Dtos.UserAccountInfo;
import com.yuosef.demo1.cloudexaminationplatform.Models.Dtos.UserDto;
import com.yuosef.demo1.cloudexaminationplatform.Models.User;
import jakarta.transaction.SystemException;

public interface UserService {
    String login(LoginInfo loginInfo);
    UserDto createUser(UserAccountInfo clientAccInfo) throws SystemException;
    UserDto createAdmin(UserAccountInfo clientAccInfo) throws SystemException;
    User getUserFromToken(String token) ;
    User getCurrentUser();


}
