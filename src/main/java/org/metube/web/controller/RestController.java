package org.metube.web.controller;

import org.metube.domain.entity.User;
import org.metube.domain.entity.Video;
import org.metube.service.UserService;
import org.metube.service.VideoService;
import org.metube.domain.model.viewModel.VideoListViewModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@org.springframework.web.bind.annotation.RestController
public class RestController {

    private static final int VIDEOS_PER_PAGE = 6;

    private final UserService userService;

    private final VideoService videoService;

    @Autowired
    public RestController(UserService userService, VideoService videoService) {
        this.userService = userService;
        this.videoService = videoService;
    }

    private Comparator<Video> defineComparator(String sort) {
        Comparator<Video> comparator = (x1, x2) -> Integer.compare(x2.getViews(), x1.getViews());
        switch (sort) {
            case "views":
                comparator = (x1, x2) -> Integer.compare(x2.getViews(), x1.getViews());
                break;
            case "title":
                comparator = (x1, x2) -> x1.getTitle().compareTo(x2.getTitle());
                break;
            case "likes":
                comparator = (x1, x2) -> Integer.compare(x2.getUsersLiked().size(), x1.getUsersLiked().size());
                break;
            case "date":
                comparator = (x1, x2) -> x2.getAddedOn().compareTo(x1.getAddedOn());
                break;
        }

        return comparator;
    }

    @GetMapping(value = "/api/videos/{id}", produces = "application/json")
    public List<VideoListViewModel> listAjax(@PathVariable Integer id, Integer page, String search, String sort) {
        if (sort == null) sort = "views";
        if (page == null) page = 1;
        if (search == null) search = "";

        Comparator<Video> comparator = this.defineComparator(sort);

        final String srch = search.toLowerCase();
        List<Video> allVideosByCategory = this.videoService.findAllVideos().stream()
                .filter(x -> x.getCategory().getId().equals(id) && (x.getTags().stream().filter(y -> y.getName().toLowerCase().contains(srch)).count() > 0 || x.getTitle().toLowerCase().contains(srch)))
                .sorted(comparator)
                .collect(Collectors.toList());;

        List<Video> videos = allVideosByCategory.stream()
                .skip(VIDEOS_PER_PAGE * (page - 1))
                .limit(VIDEOS_PER_PAGE)
                .collect(Collectors.toList());

        int pages = allVideosByCategory.size() / VIDEOS_PER_PAGE;

        List<VideoListViewModel> videoListViewModels = new ArrayList<>();

        for (Video video : videos) {
            VideoListViewModel videoListViewModel = new VideoListViewModel();
            videoListViewModel.setTitle(video.getTitle());
            videoListViewModel.setVideoUrl("http://img.youtube.com/vi/" + video.getYoutubeId() +"/0.jpg");
            videoListViewModel.setId(String.valueOf(video.getId()));
            videoListViewModel.setCategoryId(String.valueOf(video.getCategory().getId()));
            videoListViewModel.setSearch(search);

            videoListViewModels.add(videoListViewModel);
        }

        return videoListViewModels;
    }
    @GetMapping("/api/profile/{id}")
    public List<VideoListViewModel> profileAjax(@PathVariable Integer id, Integer page) {
        if (page == null) page = 1;

        User user = this.userService.findById(id);

        List<Video> videos = user.getVideos().stream()
                .skip(VIDEOS_PER_PAGE * (page - 1))
                .limit(VIDEOS_PER_PAGE)
                .sorted((x1, x2) -> x2.getAddedOn().compareTo(x1.getAddedOn()))
                .collect(Collectors.toList());

        int pages = user.getVideos().size() / VIDEOS_PER_PAGE;


        List<VideoListViewModel> videoListViewModels = new ArrayList<>();

        for (Video video : videos) {
            VideoListViewModel videoListViewModel = new VideoListViewModel();
            videoListViewModel.setTitle(video.getTitle());
            videoListViewModel.setVideoUrl("http://img.youtube.com/vi/" + video.getYoutubeId() +"/0.jpg");
            videoListViewModel.setId(String.valueOf(video.getId()));

            videoListViewModels.add(videoListViewModel);
        }

        return videoListViewModels;
    }
}
