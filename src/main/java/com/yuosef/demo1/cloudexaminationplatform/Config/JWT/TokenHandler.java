package com.yuosef.demo1.cloudexaminationplatform.Config.JWT;


import com.yuosef.demo1.cloudexaminationplatform.Models.User;
import com.yuosef.demo1.cloudexaminationplatform.sitting.TokenConfig;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.time.Duration;
import java.util.Date;

@Component
public class TokenHandler {

       private JwtBuilder jwtBuilder;
       private JwtParser jwtParser;

       private String secret ;

       private Duration duration;

      private final TokenConfig tokenConfig;

       public TokenHandler(TokenConfig tokenConfig){
           this.tokenConfig = tokenConfig;
            this.duration=tokenConfig.getDuration();
            this.secret=tokenConfig.getSecret();
           Key key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
              jwtBuilder= Jwts.builder().signWith(key);
              jwtParser=Jwts.parser().setSigningKey(key).build();
       }
       public String creatToken(User user){
              Date issued =new Date();
              Date expiration=Date.from(issued.toInstant().plus(duration));
              return jwtBuilder.setSubject(user.getEmail())
                      .setIssuedAt(issued)
                      .setExpiration(expiration)
                      .claim("name",user.getName())
                      .claim("roles",user.getAuthorities()).compact();
       }


       public boolean isValidToken(String token){
           try {


               if (jwtParser.isSigned(token)) {

                   Claims claims = jwtParser.parseClaimsJws(token).getBody();
                   Date issue = claims.getIssuedAt();
                   Date expired = claims.getExpiration();

                   return expired.after(new Date()) && issue.before(expired);
               }
           }catch (Exception e){
               System.out.println(e.getMessage());
           }
           return false;
       }


         public String getSubject(String token){
        return  jwtParser.parseClaimsJws(token).getBody().getSubject();
    }
       }



