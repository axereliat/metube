package org.metube;

import org.junit.Before;
import org.junit.Test;
import org.metube.repository.*;
import org.metube.service.*;

import static junit.framework.TestCase.assertEquals;
import static org.mockito.Mockito.mock;

public class VideoServiceTests {

    private VideoService videoService;

    @Before
    public void init() {
        VideoRepository videoRepository = mock(VideoRepository.class);
        TagRepository tagRepository = mock(TagRepository.class);
        CategoryRepository categoryRepository = mock(CategoryRepository.class);
        UserRepository userRepository = mock(UserRepository.class);
        RoleRepository roleRepository = mock(RoleRepository.class);
        CloudService cloudService = mock(CloudService.class);
        UserService userService = new UserServiceImpl(cloudService, userRepository, roleRepository);
        this.videoService = new VideoServiceImpl(videoRepository, userService, categoryRepository, tagRepository);
    }

    @Test
    public void upload_video_shouldRedirectOnErrors() {
        //this.videoService.
        assertEquals(1, 2);
    }
}
