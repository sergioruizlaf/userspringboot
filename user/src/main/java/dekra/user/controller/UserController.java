package dekra.user.controller;

import dekra.user.aop.LogRequest;
import dekra.user.exception.ErrorMessage;
import dekra.user.model.entities.User;
import dekra.user.model.entities.UserValidationError;
import dekra.user.services.UserService;
import dekra.user.services.UserValidationService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.persistence.EntityNotFoundException;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@RequiredArgsConstructor
@Log4j2
@Api(tags = "user controller")
@RestController
@RequestMapping("/api/user")
public class UserController {

    private final UserService userService;

    private final UserValidationService userValidationService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @LogRequest
    @PostMapping(value = "/create")
    @ApiOperation(value = "Create user", notes = "This method creates a new user in database")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK. User created successfully", response = User.class ),
            @ApiResponse(code = 400, message = "Bad Request. User couldn't be created", response = String.class),
            @ApiResponse(code = 500, message = "Internal server error. User couldn't be created", response = String.class)})
    public ResponseEntity createUser(
            @RequestParam String username, @RequestParam String password, @RequestParam(required = false) String name,
                                    @RequestParam(required = false) String surname, @RequestParam(required = false) String email,
            @RequestParam(required = false ) boolean active, @RequestParam(required = false) Integer age) {
        try {

            if (!userService.findByUserName(username)) {

                //set values to user
                User user = new User(username, password, name, surname, email, active, age, LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS));

                //Perform validations
                List<UserValidationError> validationErrors  = userValidationService.validateUser(user);

                if (validationErrors.isEmpty()) {
                    //encrypt password
                    user.setPassword(passwordEncoder.encode(password));

                    // Save user on DDBB
                    User userSaved = userService.saveUser(user);
                    return new ResponseEntity<>(userSaved, HttpStatus.OK);
                } else {
                    return new ResponseEntity<>(validationErrors, HttpStatus.BAD_REQUEST);
                }
            } else {
                return new ResponseEntity<>(new ErrorMessage("Username already exists"), HttpStatus.BAD_REQUEST);
            }
        } catch (Exception e) {
            log.error("Error creating user", e.getLocalizedMessage());
            return new ResponseEntity<>(new ErrorMessage(e.getLocalizedMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
        }

    }

    @LogRequest
    @DeleteMapping(value = "/delete/{id}")
    @ApiOperation(value = "Delete user", notes = "This method removes an user from database. Needs authorization to run it")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK. User removed successfully", response = String.class ),
            @ApiResponse(code = 400, message = "Bad Request. User couldn't be removed", response = String.class),
            @ApiResponse(code = 403, message = "Forbidden. Need to login (authorize) first", response = String.class) })
    public ResponseEntity deleteUser(@PathVariable(value = "id") Long userId) {
        Map<String, Boolean> response = new HashMap<>();
        try {
            User user = userService.findUserById(userId);
            userService.deleteUser(userId);

            return new ResponseEntity<>(String.format("User with id: %s has been removed successfully" , userId), HttpStatus.OK);

        }  catch (EntityNotFoundException e) {
            log.error(String.format("Error removing user, user with id: %s can't be found" , userId));
            return new ResponseEntity<>(new ErrorMessage(String.format("Error removing user, user with id: %s can't be found" , userId)), HttpStatus.BAD_REQUEST);
        }
    }

    @LogRequest
    @PatchMapping("/update/{id}")
    @ApiOperation(value = "Update user", notes = "This method updates an user in database. Needs authorization to run it.")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK. Users updated successfully", response = User.class ),
            @ApiResponse(code = 400, message = "Bad Request. User can´t be updated", response = String.class),
            @ApiResponse(code = 403, message = "Forbidden. Need to login (authorize) first", response = String.class) })
    public ResponseEntity updateUser(@PathVariable Long id, @RequestParam (required = false) String name, @RequestParam (required = false) String surname,
                                       @RequestParam (required = false) boolean active, @RequestParam (required = false) Integer age) {
        try {
            List<UserValidationError> validationErrors = new ArrayList<UserValidationError>();
            //Check if user is in database
            User user = userService.findUserById(id);

            if(!StringUtils.isEmpty(name))
                user.setName(name);
            if(!StringUtils.isEmpty(surname))
                user.setSurname(surname);
            if(age != null)
                user.setAge(age);
            user.setActive(active);

            validationErrors = userValidationService.validateUser(user);

            if (validationErrors.isEmpty()) {
                return new ResponseEntity<User>(userService.saveUser(user), HttpStatus.OK);
            } else {
                return new ResponseEntity<>(validationErrors, HttpStatus.BAD_REQUEST);
            }
        } catch (EntityNotFoundException e) {
            return new ResponseEntity(new ErrorMessage(String.format("Error updating user, user with id: %s can't be found" , id)), HttpStatus.BAD_REQUEST);
        }
    }

    @LogRequest
    @GetMapping("/list")
    @ApiOperation(value = "List users", notes = "This method list all users")
    @Operation(security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK. Users retrieved successfully", response = List.class ),
            @ApiResponse(code = 400, message = "Bad Request", response = String.class) })
    public List<User> getAllUsers() {

        return userService.getUsers();
    }

}
