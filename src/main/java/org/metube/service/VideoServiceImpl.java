package org.metube.service;

import org.metube.entity.Video;
import org.metube.repository.VideoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.List;

@Service
@Transactional
public class VideoServiceImpl implements VideoService {

    private final VideoRepository videoRepository;

    @Autowired
    public VideoServiceImpl(VideoRepository videoRepository) {
        this.videoRepository = videoRepository;
    }

    @Override
    public void uploadVideo(Video video) {
        this.videoRepository.saveAndFlush(video);
    }

    @Override
    public void deleteVideoById(Integer id) {
        this.videoRepository.deleteById(id);
    }

    @Override
    public Video findVideoById(Integer id) {
        return this.videoRepository.findById(id).orElse(null);
    }

    @Override
    public List<Video> findAllVideos() {
        return this.videoRepository.findAll();
    }
}
