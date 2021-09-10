package dekra.user.controller;


import dekra.user.exception.ErrorMessage;
import dekra.user.model.entities.User;
import dekra.user.services.UserService;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Log4j2
@RequiredArgsConstructor
@Api(tags = "authentication")
@RestController
@RequestMapping(path = "auth")
public class AuthController {

    private final UserService userService;
    private final UserDetailsService userDetailsService;
    @Autowired
    private PasswordEncoder passwordEncoder;

    @PostMapping("/login")
    @ApiOperation(value = "Log user", notes = "This method logs an user into the application and provides authorization.")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK. User log in successfully", response = User.class ),
            @ApiResponse(code = 400, message = "Bad Request. User can´t be logged", response = String.class) })
    public ResponseEntity login(@RequestParam("username") String username, @RequestParam("password") String pwd) {

        try {
            User user = (User) userDetailsService.loadUserByUsername(username);
            if(passwordEncoder.matches(pwd, user.getPassword()) || user.getPassword().equals(pwd))  {
                String token = getJWTToken(username);
                user.setLastLogging(LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS));
                user.setToken(token);
                userService.saveUser(user);
                return new ResponseEntity<>(user, HttpStatus.OK);
            } else {
                log.error("Password not valid");
                return new ResponseEntity<>(new ErrorMessage("Password not valid"), HttpStatus.BAD_REQUEST);
            }

        }catch (UsernameNotFoundException e) {
             log.error(String.format("User - %s - does not exist" , username));
             return new ResponseEntity<>(new ErrorMessage(String.format("User - %s - does not exist" , username)), HttpStatus.BAD_REQUEST);
        }

    }


    /**
     * Get the jwt token of a determined user
     * @param username, the username to assign the token
     * @return the jwt token
     */
    private String getJWTToken(String username) {
        String secretKey = "mySecretKey";
        List<GrantedAuthority> grantedAuthorities = AuthorityUtils
                .commaSeparatedStringToAuthorityList("ROLE_USER");

        String token = Jwts
                .builder()
                .setId("sergioJWT")
                .setSubject(username)
                .claim("authorities",
                        grantedAuthorities.stream()
                                .map(GrantedAuthority::getAuthority)
                                .collect(Collectors.toList()))
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + 600000))
                .signWith(SignatureAlgorithm.HS512,
                        secretKey.getBytes()).compact();

        return "Bearer " + token;
    }
}
