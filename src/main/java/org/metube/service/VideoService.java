package org.metube.service;

import org.metube.bindingModel.VideoUploadBindingModel;
import org.metube.entity.Video;
import org.springframework.validation.BindingResult;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Map;

public interface VideoService {

    String uploadVideo(VideoUploadBindingModel videoUploadBindingModel, BindingResult bindingResult, RedirectAttributes redirectAttributes);

    void deleteVideoById(String id);

    Video findVideoById(String id);

    List<Video> findAllVideos();

    String editVideo(VideoUploadBindingModel videoUploadBindingModel, BindingResult bindingResult, RedirectAttributes redirectAttributes, String id);

    String deleteVideo(VideoUploadBindingModel videoUploadBindingModel, RedirectAttributes redirectAttributes, String id);

    Map<String, String> likeVideo(RedirectAttributes redirectAttributes, Integer videoId, Integer categoryId);
}
