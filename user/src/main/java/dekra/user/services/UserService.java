package dekra.user.services;

import dekra.user.model.entities.User;

import java.util.List;

public interface UserService {

    User findUserById(Long userId);

    User saveUser(User user);

    void deleteUser(Long userId);

    List<User> getUsers();

    boolean findByUserName(String username);


}
