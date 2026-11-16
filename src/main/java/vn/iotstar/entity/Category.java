package vn.iotstar.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.util.*;

@Entity
@Table(name = "categories", uniqueConstraints = @UniqueConstraint(name = "uk_category_name", columnNames = "CategoryName"))
@NamedQuery(name = "Category.findAll", query = "SELECT c FROM Category c ORDER BY c.categoryid DESC")
public class Category implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "CategoryId")
    private int categoryid;
    @NotBlank(message = "Tên danh mục không được để trống")
    @Size(max = 50, message = "Tên danh mục tối đa 50 ký tự")
    @Column(name = "CategoryName", nullable = false, length = 50)
    private String categoryname;
    @Size(max = 500)
    @Column(name = "Images", length = 500)
    private String images;
    @Min(0) @Max(1)
    @Column(name = "status", nullable = false)
    private int status = 1;
    @OneToMany(mappedBy = "category")
    private List<Video> videos = new ArrayList<>();
    public Category() { }
    public int getCategoryid() { return categoryid; }
    public void setCategoryid(int id) { this.categoryid = id; }
    public String getCategoryname() { return categoryname; }
    public void setCategoryname(String name) { this.categoryname = name; }
    public String getImages() { return images; }
    public void setImages(String images) { this.images = images; }
    public int getStatus() { return status; }
    public void setStatus(int status) { this.status = status; }
    public List<Video> getVideos() { return videos; }
    public Video addVideo(Video video) { videos.add(video); video.setCategory(this); return video; }
    public Video removeVideo(Video video) { videos.remove(video); video.setCategory(null); return video; }
}
