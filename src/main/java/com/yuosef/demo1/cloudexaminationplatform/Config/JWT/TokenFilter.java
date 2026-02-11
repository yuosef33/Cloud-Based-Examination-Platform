package com.yuosef.demo1.cloudexaminationplatform.Config.JWT;

import com.yuosef.demo1.cloudexaminationplatform.Models.Authority;
import com.yuosef.demo1.cloudexaminationplatform.Models.User;
import com.yuosef.demo1.cloudexaminationplatform.Services.UserService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Component
public class TokenFilter extends OncePerRequestFilter {

    private final TokenHandler tokenHandler;
    private final UserService userService;

    public TokenFilter(TokenHandler tokenHandler, UserService userService) {
        this.tokenHandler = tokenHandler;

        this.userService = userService;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        if (request.getServletPath().contains("Login") ||
                request.getServletPath().contains("signup")||
                request.getServletPath().contains("createUser") ||
                request.getServletPath().contains("v3") ||
                request.getServletPath().contains("swagger-ui")||
                request.getServletPath().contains("OAuth2")||
                request.getServletPath().contains("forget_password")||
                request.getServletPath().contains("ForgetPassword")

        ){
            return true;
        }
        return false;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String token=request.getHeader("Authorization");
        if ( token ==null||!token.startsWith("Bearer")){
            response.reset();
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }
        token=token.substring(7);


          if(!tokenHandler.isValidToken(token))
          {
              response.reset();
              response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
              return;
          }


            User user=userService.getUserFromToken(token);
            if(Objects.isNull(user)){
                response.reset();
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                return;
            }


        UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken=new UsernamePasswordAuthenticationToken(user,null,convertTheAuth(user.getAuthorities()));
        SecurityContextHolder.getContext().setAuthentication(usernamePasswordAuthenticationToken);
        filterChain.doFilter(request,response);

    }

    private List<GrantedAuthority> convertTheAuth (List<Authority> auths){
        List<GrantedAuthority> mainAuths=new ArrayList<>();
        for(Authority auth: auths){
            SimpleGrantedAuthority x =new SimpleGrantedAuthority("ROLE_"+auth.getUserRole());
            mainAuths.add(x);
        }
        return mainAuths;
    }


}
