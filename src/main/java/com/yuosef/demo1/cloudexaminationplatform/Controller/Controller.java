package com.yuosef.demo1.cloudexaminationplatform.Controller;

import com.yuosef.demo1.cloudexaminationplatform.Models.Dtos.LoginInfo;
import com.yuosef.demo1.cloudexaminationplatform.Models.Dtos.UserAccountInfo;
import com.yuosef.demo1.cloudexaminationplatform.Models.Dtos.UserDto;
import com.yuosef.demo1.cloudexaminationplatform.Services.TerraformService;
import com.yuosef.demo1.cloudexaminationplatform.Services.UserService;
import jakarta.transaction.SystemException;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class Controller {

    private final TerraformService terraformService;
    private final UserService userService;

    public Controller(TerraformService terraformService, UserService userService) {
        this.terraformService = terraformService;
        this.userService = userService;

    }

    @PostMapping("/ec2")
    public String createEc2() throws Exception {
        return terraformService.apply();
    }

    @GetMapping("/hello")
    public String hello() {
        return "Hello, World!";
    }

    @PostMapping("/createUser")
    public ResponseEntity<UserDto> createUser(@Valid @RequestBody UserAccountInfo userAccountInfo) throws SystemException {
         UserDto user= userService.createUser(userAccountInfo);
        return ResponseEntity.status(HttpStatus.CREATED).body(user);
    }
    @PostMapping("/Login")
    public ResponseEntity<Map<String,String>> login(@RequestBody LoginInfo loginInfo){
        return ResponseEntity.ok(new HashMap<>(Map.of("Token",userService.login(loginInfo))));
    }

}
