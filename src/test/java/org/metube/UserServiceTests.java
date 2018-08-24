package org.metube;

import static junit.framework.TestCase.*;

import org.apache.catalina.servlet4preview.http.HttpServletRequest;
import org.junit.Before;
import org.junit.Test;
import org.metube.domain.model.bindingModel.UserRegisterBindingModel;
import org.metube.repository.RoleRepository;
import org.metube.repository.UserRepository;
import org.metube.service.CloudService;
import org.metube.service.RecaptchaService;
import org.metube.service.UserService;
import static org.mockito.Mockito.*;

import org.metube.service.UserServiceImpl;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

//@RunWith(SpringRunner.class)
//@SpringBootTest
public class UserServiceTests {

    private UserService userService;

    @Before
    public void init() {
        UserRepository userRepository = mock(UserRepository.class);
        RoleRepository roleRepository = mock(RoleRepository.class);
        CloudService cloudService = mock(CloudService.class);
        RecaptchaService recaptchaService = mock(RecaptchaService.class);
        //when(userRepository.saveAndFlush(any())).thenAnswer(i -> i.getArgument(0));
        this.userService = new UserServiceImpl(cloudService, userRepository, roleRepository, recaptchaService);
    }

    @Test
    public void register_user_shouldRedirectOnEmptyUsername() {
        UserRegisterBindingModel userRegisterBindingModel = new UserRegisterBindingModel();
        userRegisterBindingModel.setUsername("");

        RedirectAttributes redirectAttributes = mock(RedirectAttributes.class);
        MultipartFile avatar = mock(MultipartFile.class);
        String result = this.userService.registerUser(userRegisterBindingModel, avatar, redirectAttributes, "ads", mock(HttpServletRequest.class));

        assertEquals("Empty username - no redirect.", "redirect:/register", result);
    }

    @Test
    public void register_user_shouldRedirectOnEmptyPassword() {
        UserRegisterBindingModel userRegisterBindingModel = new UserRegisterBindingModel();
        userRegisterBindingModel.setUsername("");
        userRegisterBindingModel.setBirthdate("");
        userRegisterBindingModel.setPassword("");

        RedirectAttributes redirectAttributes = mock(RedirectAttributes.class);
        MultipartFile avatar = mock(MultipartFile.class);
        String result = this.userService.registerUser(userRegisterBindingModel, avatar, redirectAttributes, "ads", mock(HttpServletRequest.class));

        assertEquals("Empty password - no redirect.", "redirect:/register", result);
    }

    @Test
    public void register_user_shouldRedirectOnPasswordsMismatch() {
        UserRegisterBindingModel userRegisterBindingModel = new UserRegisterBindingModel();
        userRegisterBindingModel.setUsername("sd");
        userRegisterBindingModel.setBirthdate("sd");
        userRegisterBindingModel.setPassword("sd");
        userRegisterBindingModel.setPassword("asd");
        userRegisterBindingModel.setConfirmPassword("aadsds");

        RedirectAttributes redirectAttributes = mock(RedirectAttributes.class);
        MultipartFile avatar = mock(MultipartFile.class);
        String result = this.userService.registerUser(userRegisterBindingModel, avatar, redirectAttributes, "ads", mock(HttpServletRequest.class));

        assertEquals("Passwords mismatch - no redirect.", "redirect:/register", result);
    }
}
