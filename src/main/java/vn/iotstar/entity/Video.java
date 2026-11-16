package vn.iotstar.entity;

import jakarta.persistence.*;
import java.io.Serializable;

@Entity @Table(name = "videos")
@NamedQuery(name = "Video.findAll", query = "SELECT v FROM Video v")
public class Video implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id @Column(name = "VideoId", length = 50)
    private String videoId;
    private int active;
    @Column(name = "Description", length = 500)
    private String description;
    @Column(name = "Poster", length = 500)
    private String poster;
    @Column(name = "Title", length = 500)
    private String title;
    private int views;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "CategoryId", foreignKey = @ForeignKey(name = "fk_video_category"))
    private Category category;
    public Video() { }
    public String getVideoId() { return videoId; }
    public void setVideoId(String value) { videoId = value; }
    public int getActive() { return active; }
    public void setActive(int value) { active = value; }
    public String getDescription() { return description; }
    public void setDescription(String value) { description = value; }
    public String getPoster() { return poster; }
    public void setPoster(String value) { poster = value; }
    public String getTitle() { return title; }
    public void setTitle(String value) { title = value; }
    public int getViews() { return views; }
    public void setViews(int value) { views = value; }
    public Category getCategory() { return category; }
    public void setCategory(Category value) { category = value; }
}
