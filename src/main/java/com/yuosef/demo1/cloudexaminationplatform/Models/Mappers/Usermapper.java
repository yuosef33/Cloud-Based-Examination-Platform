package com.yuosef.demo1.cloudexaminationplatform.Models.Mappers;

import com.yuosef.demo1.cloudexaminationplatform.Models.Dtos.UserDto;
import com.yuosef.demo1.cloudexaminationplatform.Models.User;

public class Usermapper {

   public static UserDto toDto(User user){
    return new  UserDto(user.getId(),
            user.getName(),
            user.getEmail(),
            user.getMobileNumber(),
            user.getPwd(),
            user.getCreateDt(),
            user.getAuthorities(),
            user.getTemplates());
   };
    public static User toEntity(UserDto userDto){
       return new User(userDto.getId(),
               userDto.getName(),
               userDto.getEmail(),
               userDto.getMobileNumber(),
               userDto.getPwd(),
               userDto.getCreateDt(),
               userDto.getAuthorities(),
               userDto.getTemplates());
   };

}
