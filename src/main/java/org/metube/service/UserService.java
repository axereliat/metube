package org.metube.service;

import org.metube.entity.User;

import java.util.List;

public interface UserService {

    void registerUser(User user);

    User findById(Integer id);

    boolean existsUserWithUsername(String username);

    List<User> findAll();

    void deleteUserById(Integer id);

    User findByUsername(String username);
}
