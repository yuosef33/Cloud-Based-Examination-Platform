package com.yuosef.cloudbasedlabexaminationplatform.models.Mappers;

import com.yuosef.cloudbasedlabexaminationplatform.models.Dtos.UserDto;
import com.yuosef.cloudbasedlabexaminationplatform.models.User;

public class Usermapper {

   public static UserDto toDto(User user){
    return new  UserDto(user.getId(),
            user.getName(),
            user.getEmail(),
            user.getMobileNumber(),
            user.getPwd(),
            user.getCreatedAt(),
            user.getUpdatedAt(),
            user.getRoles(),
            user.getAuthProvider(),
            user.getTemplates(),
            user.getInstances());
   };
    public static User toEntity(UserDto userDto){
       return new User(userDto.getId(),
               userDto.getName(),
               userDto.getEmail(),
               userDto.getMobileNumber(),
               userDto.getPwd(),
               userDto.getAuthorities(),
               userDto.getAuthProvider(),
               userDto.getTemplates(),
               userDto.getInstances());
   };

}
