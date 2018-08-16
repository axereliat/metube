package org.metube.entity;

import org.metube.enumeration.Gender;

import javax.persistence.*;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Table(name = "users")
@Entity
public class User {

    private Integer id;

    private String username;

    private String password;

    private LocalDate birthdate;

    private Gender gender;

    private String avatar;

    private Set<Role> roles;

    private Set<Video> videos;

    private Set<Comment> comments;

    private Set<Video> likedVideos;

    private Set<Notification> notifications;

    public User() {
        this.roles = new HashSet<>();
        this.videos = new HashSet<>();
        this.comments = new HashSet<>();
        this.likedVideos = new HashSet<>();
        this.notifications = new HashSet<>();
    }

    public User(String username, String password) {
        this.username = username;
        this.password = password;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    @Column(unique = true)
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public LocalDate getBirthdate() {
        return birthdate;
    }

    public void setBirthdate(LocalDate birthdate) {
        this.birthdate = birthdate;
    }

    public Gender getGender() {
        return gender;
    }

    public void setGender(Gender gender) {
        this.gender = gender;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "users_roles",
            joinColumns = @JoinColumn(name = "user_id", referencedColumnName = "id"),
            inverseJoinColumns = @JoinColumn(name = "role_id", referencedColumnName = "id"))
    public Set<Role> getRoles() {
        return roles;
    }

    public void setRoles(Set<Role> roles) {
        this.roles = roles;
    }

    public void addRole(Role role) {
        this.roles.add(role);
    }

    @OneToMany(mappedBy = "publisher")
    public Set<Video> getVideos() {
        return videos;
    }

    public void setVideos(Set<Video> videos) {
        this.videos = videos;
    }

    @OneToMany(mappedBy = "author")
    public Set<Comment> getComments() {
        return comments;
    }

    public void setComments(Set<Comment> comments) {
        this.comments = comments;
    }

    @OneToMany(mappedBy = "user")
    public Set<Notification> getNotifications() {
        return notifications;
    }

    public void setNotifications(Set<Notification> notifications) {
        this.notifications = notifications;
    }

    @ManyToMany
    @JoinTable(name = "likes",
            joinColumns = @JoinColumn(name = "user_id", referencedColumnName = "id"),
            inverseJoinColumns = @JoinColumn(name = "video_id", referencedColumnName = "id"))
    public Set<Video> getLikedVideos() {
        return likedVideos;
    }

    public void setLikedVideos(Set<Video> likedVideos) {
        this.likedVideos = likedVideos;
    }

    @Transient
    public boolean isPublisher(Video video) {
        return Integer.compare(video.getPublisher().getId(), this.id) == 0;
    }

    @Transient
    public boolean isAdmin() {
        return this.getRoles()
                .stream()
                .anyMatch(role -> role.getName().equals("ADMIN"));
    }

    @Transient
    public boolean isAuthorOfComment(Comment comment) {
        return comment.getAuthor().getId().equals(this.id);
    }

    @Transient
    public void likeVideo(Video video) {
        this.likedVideos.add(video);
    }

    @Transient
    public void unlikeVideo(Video video) {
        this.likedVideos.remove(video);
    }

    @Transient
    public boolean hasLikedVideo(Video video) {
        return this.likedVideos.contains(video);
    }

    @Transient
    public void clearNotifications() {
        this.notifications.clear();
    }
}
