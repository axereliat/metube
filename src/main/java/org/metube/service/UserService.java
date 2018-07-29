package org.metube.service;

import org.metube.domain.model.bindingModel.UserEditBindingModel;
import org.metube.domain.model.bindingModel.UserProfileEditBindingModel;
import org.metube.domain.model.bindingModel.UserRegisterBindingModel;
import org.metube.domain.entity.User;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

public interface UserService {

    String registerUser(UserRegisterBindingModel userRegisterBindingModel, MultipartFile avatar, RedirectAttributes redirAttrs);

    User findById(Integer id);

    boolean existsUserWithUsername(String username);

    List<User> findAll();

    void deleteUserById(Integer id);

    User findByUsername(String username);

    String editUser(UserProfileEditBindingModel userProfileEditBindingModel, RedirectAttributes redirectAttributes, MultipartFile avatar);

    String adminEditUser(UserEditBindingModel userEditBindingModel, Integer id);
}
