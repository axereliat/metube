package org.metube.domain.entity;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Table(name = "cateogries")
@Entity
public class Category {

    private Integer id;

    private String name;

    private Set<Video> videos;

    public Category() {
        this.videos = new HashSet<>();
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @OneToMany(mappedBy = "category")
    public Set<Video> getVideos() {
        return videos;
    }

    public void setVideos(Set<Video> videos) {
        this.videos = videos;
    }
}
