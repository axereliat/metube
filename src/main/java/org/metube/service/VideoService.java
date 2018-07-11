package org.metube.service;

import org.metube.entity.Video;

import java.util.List;

public interface VideoService {

    void uploadVideo(Video video);

    void deleteVideoById(Integer id);

    Video findVideoById(Integer id);

    List<Video> findAllVideos();
}
