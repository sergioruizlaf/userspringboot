package dekra.user.services;

import dekra.user.model.entities.User;
import dekra.user.model.entities.UserValidationError;

import java.util.List;

public interface UserValidationService {

    UserValidationError createValidationError(String type, String description);

    List<UserValidationError> validateUser(User user);


}
