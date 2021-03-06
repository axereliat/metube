package org.metube.service;

import org.apache.catalina.servlet4preview.http.HttpServletRequest;
import org.metube.domain.model.bindingModel.UserEditBindingModel;
import org.metube.domain.model.bindingModel.UserProfileEditBindingModel;
import org.metube.domain.model.bindingModel.UserRegisterBindingModel;
import org.metube.domain.entity.Role;
import org.metube.domain.entity.User;
import org.metube.enumeration.Gender;
import org.metube.exception.ResourceNotFoundException;
import org.metube.repository.RoleRepository;
import org.metube.repository.UserRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.transaction.Transactional;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Transactional
public class UserServiceImpl implements UserService, UserDetailsService {

    private final CloudService cloudService;

    private final UserRepository userRepository;

    private final RoleRepository roleRepository;

    private final RecaptchaService recaptchaService;

    @Autowired
    public UserServiceImpl(CloudService cloudService, UserRepository userRepository, RoleRepository roleRepository, RecaptchaService recaptchaService) {
        this.cloudService = cloudService;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.recaptchaService = recaptchaService;
    }

    private String getFileExtension(String originalFilename) {
        int dotIndex = originalFilename.lastIndexOf('.');

        return originalFilename.substring(dotIndex);
    }

    @Override
    public String registerUser(UserRegisterBindingModel userRegisterBindingModel, MultipartFile avatar, RedirectAttributes redirAttrs, String gRecaptchaResponse, HttpServletRequest httpServletRequest) {
        String error = null;

        if (this.recaptchaService.verifyRecaptcha(httpServletRequest.getRemoteAddr(), gRecaptchaResponse) != null) {
            error = "Please verify that you are not a robot.";
            redirAttrs.addFlashAttribute("error", error);

            return "redirect:/register";
        }

        User userByEmail = this.userRepository.findByEmail(userRegisterBindingModel.getEmail());
        if (userByEmail != null) {
            error = "Email is already taken.";
            redirAttrs.addFlashAttribute("error", error);

            return "redirect:/register";
        }

        if (userRegisterBindingModel.getUsername().equals("") || userRegisterBindingModel.getPassword().equals("") || userRegisterBindingModel.getBirthdate().equals("") || userRegisterBindingModel.getEmail().equals("")) {
            error = "Please fill in all fields.";
            redirAttrs.addFlashAttribute("error", error);
            redirAttrs.addFlashAttribute("username", userRegisterBindingModel.getUsername());

            return "redirect:/register";
        }

        if (!userRegisterBindingModel.getPassword().equals(userRegisterBindingModel.getConfirmPassword())) {
            error = "Passwords do not match.";
            redirAttrs.addFlashAttribute("error", error);
            redirAttrs.addFlashAttribute("username", userRegisterBindingModel.getUsername());

            return "redirect:/register";
        }

        if (this.userRepository.existsByUsername(userRegisterBindingModel.getUsername())) {
            error = "Username " + userRegisterBindingModel.getUsername() + " is already taken.";
            redirAttrs.addFlashAttribute("error", error);
            redirAttrs.addFlashAttribute("username", userRegisterBindingModel.getUsername());

            return "redirect:/register";
        }

        if (!avatar.getOriginalFilename().equals("")) {
            if (!avatar.getOriginalFilename().endsWith(".jpeg") && !avatar.getOriginalFilename().endsWith(".jpg") && !avatar.getOriginalFilename().endsWith(".png")) {
                redirAttrs.addFlashAttribute("error", "Only the following formats are allowed: jpeg, jpg, png");
                return "redirect:/edit";
            }
        }

        ModelMapper modelMapper = new ModelMapper();
        User user = modelMapper.map(userRegisterBindingModel, User.class);

        BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();
        user.setPassword(bCryptPasswordEncoder.encode(user.getPassword()));

        if (this.roleRepository.findAll().size() == 0) {
            Role roleUser = new Role();
            roleUser.setName("USER");
            Role roleAdmin = new Role();
            roleUser.setName("ADMIN");
            this.roleRepository.save(roleUser);
            this.roleRepository.save(roleAdmin);
        }

        Role roleUser = this.roleRepository.findByName("USER");
        Role roleAdmin = this.roleRepository.findByName("ADMIN");

        if (this.userRepository.findAll().size() == 0) {
            user.addRole(roleAdmin);
        }
        user.addRole(roleUser);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");
        LocalDate parsedDate = LocalDate.parse(userRegisterBindingModel.getBirthdate(), formatter);

        user.setBirthdate(parsedDate);

        user.setGender(Gender.valueOf(userRegisterBindingModel.getGender().toUpperCase()));

        try {
            if (avatar.getOriginalFilename().equals("")) {
                user.setAvatar("http://res.cloudinary.com/dr8ovbzd2/image/upload/v1534048322/no-user.png");
            } else {
                user.setAvatar(this.cloudService.upload(avatar));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        this.userRepository.saveAndFlush(user);

        redirAttrs.addFlashAttribute("success", "You have successfully registered");

        return "redirect:/login";
    }

    @Override
    public String editUser(UserProfileEditBindingModel userProfileEditBindingModel, RedirectAttributes redirectAttributes, MultipartFile avatar) {
        UserDetails user = (UserDetails) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal();
        User userEntity = this.userRepository.findByUsername(user.getUsername());

        if (!userProfileEditBindingModel.getOldPassword().equals("")) {
            BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();

            if (!bCryptPasswordEncoder.matches(userProfileEditBindingModel.getOldPassword(), userEntity.getPassword())) {
                redirectAttributes.addFlashAttribute("error", "Incorrect old password!");
                return "redirect:/edit";
            } else {
                userEntity.setPassword(bCryptPasswordEncoder.encode(userProfileEditBindingModel.getNewPassword()));
            }
        }

        if (!avatar.getOriginalFilename().equals("")) {
            if (!avatar.getOriginalFilename().endsWith(".jpeg") && !avatar.getOriginalFilename().endsWith(".jpg") && !avatar.getOriginalFilename().endsWith(".png")) {
                redirectAttributes.addFlashAttribute("error", "Only the following formats are allowed: jpeg, jpg, png");
                return "redirect:/edit";
            }
        }

        if (!userProfileEditBindingModel.getNewUsername().equals("")) {
            if (!userProfileEditBindingModel.getNewUsername().equals(userEntity.getUsername())) {
                if (this.userRepository.existsByUsername(userProfileEditBindingModel.getNewUsername())) {
                    redirectAttributes.addFlashAttribute("error", "Username is already taken!");
                    return "redirect:/edit";
                }
            }
            userEntity.setUsername(userProfileEditBindingModel.getNewUsername());
        }

        try {
            if (!avatar.getOriginalFilename().equals("")) {

                userEntity.setAvatar(this.cloudService.upload(avatar));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        this.userRepository.saveAndFlush(userEntity);

        redirectAttributes.addFlashAttribute("success", "Profile successfully edited!");

        return "redirect:/logout";
    }

    @Override
    public String adminEditUser(UserEditBindingModel userEditBindingModel, Integer id) {
        User user = this.userRepository.findById(id).orElse(null);

        if (user == null) {
            throw new ResourceNotFoundException();
        }
        Set<Role> roles = new HashSet<>();
        for (Integer roleId : userEditBindingModel.getRoles()) {
            roles.add(this.roleRepository.findById(roleId).orElse(null));
        }
        user.setRoles(roles);
        user.setUsername(userEditBindingModel.getUsername());
        this.userRepository.saveAndFlush(user);

        return "redirect:/admin/users/";
    }

    @Override
    public User findById(Integer id) {
        Optional<User> userOptional = this.userRepository.findById(id);

        if (!userOptional.isPresent()) throw new ResourceNotFoundException();

        return userOptional.get();
    }

    @Override
    public boolean existsUserWithUsername(String username) {
        return this.userRepository.findByUsername(username) != null;
    }

    @Override
    public List<User> findAll() {
        return this.userRepository.findAll();
    }

    @Override
    public void deleteUserById(Integer id) {
        this.userRepository.deleteById(id);
    }

    @Override
    public User findByUsername(String username) {
        return this.userRepository.findByUsername(username);
    }

    @Override
    public User findByUsernameAndEmail(String username, String email) {
        return this.userRepository.findByUsernameAndEmail(username, email);
    }

    @Override
    public void save(User user) {
        this.userRepository.saveAndFlush(user);
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = this.userRepository.findByUsername(username);

        if (user == null) {
            throw new UsernameNotFoundException("Invalid User");
        } else {
            Set<GrantedAuthority> grantedAuthorities = user.getRoles()
                    .stream()
                    .map(role -> new SimpleGrantedAuthority(role.getName()))
                    .collect(Collectors.toSet());

            return new org
                    .springframework
                    .security
                    .core
                    .userdetails
                    .User(user.getUsername(), user.getPassword(), grantedAuthorities);
        }
    }
}
