package org.metube.service;

import org.metube.domain.model.bindingModel.VideoUploadBindingModel;
import org.metube.domain.entity.*;
import org.metube.exception.ResourceNotFoundException;
import org.metube.repository.CategoryRepository;
import org.metube.repository.TagRepository;
import org.metube.repository.VideoRepository;
import org.metube.domain.model.viewModel.VideoEditViewModel;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.validation.BindingResult;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class VideoServiceImpl implements VideoService {

    private final VideoRepository videoRepository;

    private final UserService userRepository;

    private final CategoryRepository categoryRepository;

    private final TagRepository tagRepository;

    @Autowired
    public VideoServiceImpl(VideoRepository videoRepository, UserService userRepository, CategoryRepository categoryRepository, TagRepository tagRepository) {
        this.videoRepository = videoRepository;
        this.userRepository = userRepository;
        this.categoryRepository = categoryRepository;
        this.tagRepository = tagRepository;
    }

    private Set<Tag> extractTagsFromString(String tagStr) {
        String[] tagNames = tagStr.split(",\\s*");

        Set<Tag> tags = new HashSet<>();
        for (String tagName : tagNames) {
            Tag tag = new Tag();
            tag.setName(tagName);

            if (!this.tagRepository.existsByName(tagName)) {
                this.tagRepository.saveAndFlush(tag);
            }
            tags.add(tag);
        }
        return tags;
    }

    private String extractYoutubeId(String link) {
        String[] tokens = link.split("v=|&");

        return tokens[1];
    }

    @Override
    public String uploadVideo(VideoUploadBindingModel videoUploadBindingModel, BindingResult bindingResult, RedirectAttributes redirectAttributes) {
        List<String> errors = bindingResult.getAllErrors().stream().map(DefaultMessageSourceResolvable::getDefaultMessage).collect(Collectors.toList());
        if (errors.size() > 0) {
            redirectAttributes.addFlashAttribute("video", videoUploadBindingModel);
            redirectAttributes.addFlashAttribute("errors", errors);
            return "redirect:/videos/upload";
        }

        ModelMapper modelMapper = new ModelMapper();
        Video video = modelMapper.map(videoUploadBindingModel, Video.class);
        Category category = this.categoryRepository.findById(videoUploadBindingModel.getCategoryId()).orElse(null);
        video.setCategory(category);
        video.setAddedOn(LocalDateTime.now());

        Set<Tag> tags = this.extractTagsFromString(videoUploadBindingModel.getTagStr());

        video.setTags(tags);

        //video.setId(this.videoRepository.findAll().get(this.videoRepository.findAll().size() - 1).getId() + 1);
        video.setId(UUID.randomUUID().toString());

        UserDetails user = (UserDetails) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal();
        User userEntity = this.userRepository.findByUsername(user.getUsername());
        video.setPublisher(userEntity);

        video.setYoutubeId(this.extractYoutubeId(videoUploadBindingModel.getLink()));

        this.videoRepository.save(video);

        redirectAttributes.addFlashAttribute("success", "You have successfully uploaded your video,");

        return "redirect:/";
    }

    @Override
    public String editVideo(VideoUploadBindingModel videoUploadBindingModel, BindingResult bindingResult, RedirectAttributes redirectAttributes, String id) {
        List<String> errors = bindingResult.getAllErrors().stream().map(DefaultMessageSourceResolvable::getDefaultMessage).collect(Collectors.toList());
        if (errors.size() > 0) {
            VideoEditViewModel videoEditViewModel = new VideoEditViewModel();
            videoEditViewModel.setTitle("asdasda");
            redirectAttributes.addFlashAttribute("video", videoEditViewModel);
            redirectAttributes.addFlashAttribute("errors", errors);
            return "redirect:/videos/edit/" + id;
        }

        Video video = this.findVideoById(id);

        UserDetails user = (UserDetails) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal();
        User userEntity = this.userRepository.findByUsername(user.getUsername());

        if (!userEntity.isPublisher(video) && !userEntity.isAdmin()) {
            return "redirect:/";
        }

        video.setTitle(videoUploadBindingModel.getTitle());
        video.setDescription(videoUploadBindingModel.getDescription());

        Category category = this.categoryRepository.findById(videoUploadBindingModel.getCategoryId()).orElse(null);
        video.setCategory(category);

        Set<Tag> tags = this.extractTagsFromString(videoUploadBindingModel.getTagStr());

        video.setTags(tags);

        video.setYoutubeId(this.extractYoutubeId(videoUploadBindingModel.getLink()));

        this.videoRepository.save(video);

        redirectAttributes.addFlashAttribute("success", "You have successfully edited your video,");

        return "redirect:/videos/details/" + id + "?category=" + video.getCategory().getId();
    }

    @Override
    public Map<String, String> likeVideo(RedirectAttributes redirectAttributes, Integer videoId, Integer categoryId) {
        return null;
    }

    @Override
    public String deleteVideo(VideoUploadBindingModel videoUploadBindingModel, RedirectAttributes redirectAttributes, String id) {
        Video video = this.findVideoById(id);

        if (video == null) {
            throw new ResourceNotFoundException();
        }

        UserDetails user = (UserDetails) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal();
        User userEntity = this.userRepository.findByUsername(user.getUsername());

        if (!userEntity.isPublisher(video) && !userEntity.isAdmin()) {
            return "redirect:/";
        }

        this.videoRepository.deleteById(video.getId());

        redirectAttributes.addFlashAttribute("success", "You have successfully deleted your video.");

        return "redirect:/videos/" + video.getCategory().getId();
    }

    @Override
    public void deleteVideoById(String id) {
        this.videoRepository.deleteById(id);
    }

    @Override
    public Video findVideoById(String id) {
        Optional<Video> videoOptional = this.videoRepository.findById(id);
        if (!videoOptional.isPresent()) {
            throw new ResourceNotFoundException();
        }

        return videoOptional.get();
    }

    @Override
    @Cacheable("all_videos")
    public List<Video> findAllVideos() {
        return this.videoRepository.findAll();
    }

    @CacheEvict(cacheNames = {"all_videos"}, allEntries = true)
    @Scheduled(fixedRate = 50000)
    public void deleteCaches() {
        //System.out.println("deleted cache");
    }
}
