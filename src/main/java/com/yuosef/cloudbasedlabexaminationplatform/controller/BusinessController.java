package com.yuosef.cloudbasedlabexaminationplatform.controller;

import com.yuosef.cloudbasedlabexaminationplatform.models.Dtos.ApiResponse;
import com.yuosef.cloudbasedlabexaminationplatform.models.Dtos.UserAccountInfo;
import com.yuosef.cloudbasedlabexaminationplatform.models.Dtos.UserDto;
import com.yuosef.cloudbasedlabexaminationplatform.models.User;
import com.yuosef.cloudbasedlabexaminationplatform.services.UserService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.transaction.SystemException;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/business")
@Tag(name = "BusinessController", description = "Your business endpoints here")
public class BusinessController {

    private final UserService userService;

    public BusinessController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/hello") /// this path is Authenticated
    public ResponseEntity<ApiResponse<String>> hello() {
        return ResponseEntity.ok(ApiResponse.ok("Hello, World!"));
    }
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(@AuthenticationPrincipal User user) {
        userService.logout(user);
        return ResponseEntity.ok(ApiResponse.ok("Logged out successfully"));
    }

    @PostMapping("/createAdmin")
    public ResponseEntity<UserDto> createAdmin(@Valid @RequestBody UserAccountInfo userAccountInfo) throws SystemException {
        UserDto user= userService.createAdmin(userAccountInfo);
        return ResponseEntity.status(HttpStatus.CREATED).body(user);
    }

}
