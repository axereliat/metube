package org.metube.controller;

import org.metube.bindingModel.VideoUploadBindingModel;
import org.metube.entity.*;
import org.metube.repository.UserRepository;
import org.metube.service.*;
import org.metube.viewModel.VideoEditViewModel;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.validation.Valid;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/videos")
public class VideoController {

    private final int VIDEOS_PER_PAGE = 6;

    private final UserRepository userRepository;

    private final CategoryService categoryService;

    private final VideoService videoService;

    private final CommentService commentService;

    private final TagService tagService;

    private final NotificationService notificationService;

    @Autowired
    public VideoController(UserRepository userRepository, CategoryService categoryService, VideoService videoService, CommentService commentService, TagService tagService, NotificationService notificationService) {
        this.userRepository = userRepository;
        this.categoryService = categoryService;
        this.videoService = videoService;
        this.commentService = commentService;
        this.tagService = tagService;
        this.notificationService = notificationService;
    }

    private Set<Tag> extractTagsFromString(String tagStr) {
        String[] tagNames = tagStr.split(",\\s*");

        Set<Tag> tags = new HashSet<>();
        for (String tagName : tagNames) {
            Tag tag = new Tag();
            tag.setName(tagName);

            if (!this.tagService.exists(tagName)) {
                this.tagService.saveTag(tag);
            }
            tags.add(tag);
        }
        return tags;
    }

    private String extractYoutubeId(String link) {
        String[] tokens = link.split("v=|&");

        return tokens[1];
    }

    @GetMapping("/{id}")
    public String list(Model model, @PathVariable Integer id, Integer page, String search) {
        if (page == null) page = 1;
        if (search == null) search = "";

        final String srch = search.toLowerCase();
        List<Video> allVideosByCategory = this.videoService.findAllVideos().stream()
                .filter(x -> x.getCategory().getId().equals(id) && (x.getTags().stream().filter(y -> y.getName().toLowerCase().contains(srch)).count() > 0 || x.getTitle().toLowerCase().contains(srch)))
                .collect(Collectors.toList());;

        List<Video> videos = allVideosByCategory.stream()
                .skip(VIDEOS_PER_PAGE * (page - 1))
                .limit(VIDEOS_PER_PAGE)
                .collect(Collectors.toList());

        int pages = allVideosByCategory.size() / VIDEOS_PER_PAGE;

        model.addAttribute("search", search);
        model.addAttribute("categoryId", id);
        model.addAttribute("videos", videos);
        model.addAttribute("pages", pages);
        model.addAttribute("view", "/video/list");

        return "base-layout";
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/upload")
    public String upload(Model model) {
        model.addAttribute("video", new VideoUploadBindingModel());
        model.addAttribute("categories", this.categoryService.findAll());
        model.addAttribute("view", "/video/upload");

        return "base-layout";
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/upload")
    public String uploadProcess(@Valid @ModelAttribute VideoUploadBindingModel videoUploadBindingModel, BindingResult bindingResult, RedirectAttributes redirectAttributes) {
        List<String> errors = bindingResult.getAllErrors().stream().map(DefaultMessageSourceResolvable::getDefaultMessage).collect(Collectors.toList());
        if (errors.size() > 0) {
            redirectAttributes.addFlashAttribute("video", videoUploadBindingModel);
            redirectAttributes.addFlashAttribute("errors", errors);
            return "redirect:/videos/upload";
        }

        ModelMapper modelMapper = new ModelMapper();
        Video video = modelMapper.map(videoUploadBindingModel, Video.class);
        Category category = this.categoryService.findById(videoUploadBindingModel.getCategoryId());
        video.setCategory(category);
        video.setAddedOn(LocalDateTime.now());

        Set<Tag> tags = this.extractTagsFromString(videoUploadBindingModel.getTagStr());

        video.setTags(tags);

        video.setId(this.videoService.findAllVideos().get(this.videoService.findAllVideos().size() - 1).getId() + 1);

        UserDetails user = (UserDetails) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal();
        User userEntity = this.userRepository.findByUsername(user.getUsername());
        video.setPublisher(userEntity);

        video.setYoutubeId(this.extractYoutubeId(videoUploadBindingModel.getLink()));

        this.videoService.uploadVideo(video);

        redirectAttributes.addFlashAttribute("success", "You have successfully uploaded your video,");

        return "redirect:/";
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/details/{id}")
    public String details(Model model, @PathVariable Integer id, Integer category) {
        Video video = this.videoService.findVideoById(id);
        List<Comment> comments = video.getComments().stream().sorted((x1, x2) -> x2.getAddedOn().compareTo(x1.getAddedOn())).collect(Collectors.toList());
        UserDetails user = (UserDetails) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal();

        User userEntity = this.userRepository.findByUsername(user.getUsername());
        model.addAttribute("user", userEntity);
        model.addAttribute("comments", comments);
        model.addAttribute("categoryId", category);
        model.addAttribute("video", video);
        model.addAttribute("view", "/video/details");

        return "base-layout";
    }

    @RequestMapping(value="/details/{id}", method=RequestMethod.POST, produces=MediaType.APPLICATION_JSON_VALUE)
    public @ResponseBody Map<String, String> addingCommentProcess(Model model, @PathVariable Integer id, Integer category, String comment) {
        Video video = this.videoService.findVideoById(id);

        UserDetails user = (UserDetails) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal();
        User userEntity = this.userRepository.findByUsername(user.getUsername());

        if (!userEntity.getId().equals(video.getPublisher().getId())) {
            User publisher = video.getPublisher();
            String msg = String.format("%s commented on your video \"%s\"", userEntity.getUsername(), video.getTitle());
            Notification notification = new Notification(msg, publisher, video);
            this.notificationService.save(notification);
        }

        Comment commentEntity = new Comment();
        commentEntity.setContent(comment);
        commentEntity.setVideo(video);
        commentEntity.setAddedOn(LocalDateTime.now());
        commentEntity.setAuthor(userEntity);
        this.commentService.saveComment(commentEntity);

        Map<String, String> map = new HashMap<>();
        map.put("commentId", commentEntity.getId().toString());
        map.put("videoId", id.toString());
        map.put("categoryId", category.toString());
        map.put("username", commentEntity.getAuthor().getUsername());
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");;
        map.put("addedOn", commentEntity.getAddedOn().format(formatter));
        map.put("comment", commentEntity.getContent());
        map.put("avatar", commentEntity.getAuthor().getAvatar());
        boolean canDelete = userEntity.isAuthorOfComment(commentEntity) || userEntity.isPublisher(videoService.findVideoById(id));
        map.put("canDelete", canDelete ? "1" : "0");

        return map;
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping(value = "/comments/delete/{commentId}/{videoId}/{categoryId}", produces = "application/json")
    public @ResponseBody Map<String, String> deleteComment(RedirectAttributes redirectAttributes, @PathVariable Integer commentId, @PathVariable Integer videoId, @PathVariable Integer categoryId) {
        Video video = this.videoService.findVideoById(videoId);
        Comment comment = this.commentService.findById(commentId);

        UserDetails user = (UserDetails) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal();
        User userEntity = this.userRepository.findByUsername(user.getUsername());

        Map<String, String> map = new HashMap<>();
        map.put("error", "0");
        map.put("message", "");
        map.put("commentId", "0");

        if (!userEntity.isAuthorOfComment(comment) && !userEntity.isPublisher(video)) {
            map.put("error", "1");
            map.put("message", "You are not allowed to do this!");
            return map;
        }

        this.commentService.deleteCommentById(commentId);
        redirectAttributes.addFlashAttribute("success", "Comment successfully deleted.");

        map.put("commentId", String.valueOf(comment.getId()));
        return map;

        //return "redirect:/videos/details/" + videoId + "?category=" + categoryId;
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping(value = "/likes/{videoId}/{categoryId}", produces = "application/json")
    public @ResponseBody Map<String, String> likeVideo(RedirectAttributes redirectAttributes, @PathVariable Integer videoId, @PathVariable Integer categoryId) {
        UserDetails user = (UserDetails) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal();
        User userEntity = this.userRepository.findByUsername(user.getUsername());
        Video video = this.videoService.findVideoById(videoId);

        if (userEntity.hasLikedVideo(video)) {
            userEntity.unlikeVideo(video);
            redirectAttributes.addFlashAttribute("success", "You just unliked this video.");
        } else {

            if (!userEntity.getId().equals(video.getPublisher().getId())) {
                String msg = String.format("%s just liked your video \"%s\"", userEntity.getUsername(), video.getTitle());
                Notification notification = new Notification(msg, video.getPublisher(), video);
                this.notificationService.save(notification);
            }

            userEntity.likeVideo(video);
            redirectAttributes.addFlashAttribute("success", "You just liked this video.");
        }
        this.userRepository.saveAndFlush(userEntity);

        Map<String, String> map = new HashMap<>();
        map.put("liked", userEntity.hasLikedVideo(video) ? "1" : "0");
        map.put("likesCount", String.valueOf(video.getUsersLiked().size()));
        return map;
//        return "redirect:/videos/details/" + videoId + "?category=" + categoryId;
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/edit/{id}")
    public String edit(Model model, @PathVariable Integer id) {
        Video video = this.videoService.findVideoById(id);

        VideoEditViewModel videoEditViewModel = new VideoEditViewModel();
        videoEditViewModel.setTitle(video.getTitle());
        videoEditViewModel.setDescription(video.getDescription());
        videoEditViewModel.setLink("https://www.youtube.com/watch?v=" + video.getYoutubeId());
        videoEditViewModel.setId(video.getId());

        String[] tagNames = video.getTags().stream().map(Tag::getName).toArray(String[]::new);
        videoEditViewModel.setTagStr(String.join(", ", tagNames));

        model.addAttribute("video", videoEditViewModel);
        model.addAttribute("categories", this.categoryService.findAll());
        model.addAttribute("view", "/video/edit");

        return "base-layout";
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/edit/{id}")
    public String editProcess(@Valid @ModelAttribute VideoUploadBindingModel videoUploadBindingModel, BindingResult bindingResult, RedirectAttributes redirectAttributes, @PathVariable Integer id) {
        List<String> errors = bindingResult.getAllErrors().stream().map(DefaultMessageSourceResolvable::getDefaultMessage).collect(Collectors.toList());
        if (errors.size() > 0) {
            VideoEditViewModel videoEditViewModel = new VideoEditViewModel();
            videoEditViewModel.setTitle("asdasda");
            redirectAttributes.addFlashAttribute("video", videoEditViewModel);
            redirectAttributes.addFlashAttribute("errors", errors);
            return "redirect:/videos/edit/" + id;
        }

        Video video = this.videoService.findVideoById(id);

        UserDetails user = (UserDetails) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal();
        User userEntity = this.userRepository.findByUsername(user.getUsername());

        if (!userEntity.isPublisher(video) && !userEntity.isAdmin()) {
            return "redirect:/";
        }

        video.setTitle(videoUploadBindingModel.getTitle());
        video.setDescription(videoUploadBindingModel.getDescription());

        Category category = this.categoryService.findById(videoUploadBindingModel.getCategoryId());
        video.setCategory(category);

        Set<Tag> tags = this.extractTagsFromString(videoUploadBindingModel.getTagStr());

        video.setTags(tags);

        video.setYoutubeId(this.extractYoutubeId(videoUploadBindingModel.getLink()));

        this.videoService.uploadVideo(video);

        redirectAttributes.addFlashAttribute("success", "You have successfully edited your video,");

        return "redirect:/videos/details/" + id + "?category=" + video.getCategory().getId();
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/delete/{id}")
    public String delete(Model model, @PathVariable Integer id) {
        Video video = this.videoService.findVideoById(id);

        VideoEditViewModel videoEditViewModel = new VideoEditViewModel();
        videoEditViewModel.setTitle(video.getTitle());
        videoEditViewModel.setDescription(video.getDescription());
        videoEditViewModel.setLink("https://www.youtube.com/watch?v=" + video.getYoutubeId());
        videoEditViewModel.setId(video.getId());

        String[] tagNames = video.getTags().stream().map(Tag::getName).toArray(String[]::new);
        videoEditViewModel.setTagStr(String.join(", ", tagNames));

        model.addAttribute("video", videoEditViewModel);
        model.addAttribute("categories", this.categoryService.findAll());
        model.addAttribute("view", "/video/delete");

        return "base-layout";
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/delete/{id}")
    public String deleteProcess(VideoUploadBindingModel videoUploadBindingModel, RedirectAttributes redirectAttributes, @PathVariable Integer id) {
        Video video = this.videoService.findVideoById(id);

        UserDetails user = (UserDetails) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal();
        User userEntity = this.userRepository.findByUsername(user.getUsername());

        if (!userEntity.isPublisher(video) && !userEntity.isAdmin()) {
            return "redirect:/";
        }

        this.videoService.deleteVideoById(video.getId());

        redirectAttributes.addFlashAttribute("success", "You have successfully deleted your video,");

        return "redirect:/videos/" + video.getCategory().getId();
    }
}
