package com.yuosef.demo1.cloudexaminationplatform.Services.Impl;

import com.yuosef.demo1.cloudexaminationplatform.Config.JWT.TokenHandler;
import com.yuosef.demo1.cloudexaminationplatform.Daos.AuthorityDao;
import com.yuosef.demo1.cloudexaminationplatform.Daos.UserDao;
import com.yuosef.demo1.cloudexaminationplatform.Models.Authority;
import com.yuosef.demo1.cloudexaminationplatform.Models.Dtos.LoginInfo;
import com.yuosef.demo1.cloudexaminationplatform.Models.Dtos.UserAccountInfo;
import com.yuosef.demo1.cloudexaminationplatform.Models.Dtos.UserDto;
import com.yuosef.demo1.cloudexaminationplatform.Models.Mappers.Usermapper;
import com.yuosef.demo1.cloudexaminationplatform.Models.User;
import com.yuosef.demo1.cloudexaminationplatform.Services.UserService;
import jakarta.transaction.SystemException;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;

@Service
public class UserServiceImpl implements UserService {

    private final UserDao userDao;
    private final PasswordEncoder passwordEncoder;
    private final TokenHandler tokenHandler;
    private final AuthorityDao authorityRepository;
    public UserServiceImpl(UserDao userDao, PasswordEncoder passwordEncoder, TokenHandler tokenHandler, AuthorityDao authorityRepository) {
        this.userDao = userDao;
        this.passwordEncoder = passwordEncoder;
        this.tokenHandler = tokenHandler;
        this.authorityRepository = authorityRepository;
    }


    @Override
    public String login(LoginInfo loginInfo) {
        User user=userDao.findUserByEmail(loginInfo.getEmail());
        if(user ==null)
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,"No user with this credentials.");
        if(!passwordEncoder.matches(loginInfo.getPassword(),user.getPwd()))
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED,"invalid password");
         return tokenHandler.creatToken(user);
    }

    @Override
    public UserDto createUser(UserAccountInfo userAccInfo) throws SystemException {
        User user= userDao.findUserByEmail(userAccInfo.getUser_email());
        if(user!=null)
            throw new SystemException("this email "+userAccInfo.getUser_email()+" is already in use");
        // client not exist
        Authority userRole = authorityRepository.findByUserRole("USER");
        List<Authority> auths = List.of(userRole);

        User user2=new User(userAccInfo.getUser_name(),
                userAccInfo.getUser_email(),
                userAccInfo.getUser_phoneNumber(),
                passwordEncoder.encode(userAccInfo.getUser_password()),
                new Date(System.currentTimeMillis()),
                auths
        );
        return Usermapper.toDto(userDao.save(user2));
    }

    @Override
    public UserDto createAdmin(UserAccountInfo userAccInfo) throws SystemException {
        User user= userDao.findUserByEmail(userAccInfo.getUser_email());
        if(user!=null)
            throw new SystemException("this email "+userAccInfo.getUser_email()+" is already in use");
        // client not exist
        Authority userRole = authorityRepository.findByUserRole("ADMIN");
        List<Authority> auths = List.of(userRole);

        User user2=new User(userAccInfo.getUser_name(),
                userAccInfo.getUser_email(),
                userAccInfo.getUser_phoneNumber(),
                passwordEncoder.encode(userAccInfo.getUser_password()),
                new Date(System.currentTimeMillis()),
                auths
        );
        return Usermapper.toDto(userDao.save(user2));
    }

    @Override
    public User getUserFromToken(String token) {
        String email= tokenHandler.getSubject(token);
        if(Objects.isNull(email)){
            return null;
        }
        Optional<User>user= Optional.ofNullable(userDao.findUserByEmail(email));
        if (!user.isPresent()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"this email dose not exist : "+email);
        }
        return user.get();
    }

    @Override
    public User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return (User) authentication.getPrincipal();
    }
}
