package dekra.user.services.impl;

import dekra.user.model.entities.User;
import dekra.user.model.entities.UserValidationError;
import dekra.user.services.UserService;
import dekra.user.services.UserValidationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.validation.Validation;
import javax.validation.Validator;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserValidationServiceImpl implements UserValidationService {

    private final UserService userService;


    public UserValidationError createValidationError(String error, String description) {
        UserValidationError result = new UserValidationError();
        result.setError(error);
        result.setDescription(description);
        return result;
    }

    public List<UserValidationError> validateUser(User user) {

        List<UserValidationError> validationErrors = new ArrayList<>();

        Validator validator = Validation.buildDefaultValidatorFactory().getValidator();
        validator.validate(user).stream().forEach(violation ->
                validationErrors.add(createValidationError(violation.getPropertyPath().toString(), violation.getMessage())));

        return validationErrors;

    }


}
