package org.metube.domain.model.bindingModel;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

public class VideoUploadBindingModel {

    @NotEmpty(message = "Please enter a title.")
    @Size(min = 5, max = 30, message = "Title must be between 5 and 30 symbols long.")
    private String title;

    @NotEmpty(message = "Please enter a description.")
    @Size(max = 1000, message = "Description must not be longer than 200 symbols")
    private String description;

    @NotEmpty(message = "Please enter a youtube link.")
    @Pattern(regexp = "^(https?\\:\\/\\/)?((www\\.)?youtube\\.com|youtu\\.?be)\\/watch\\?v=(.+)$", message = "Invalid youtube url!")
    private String link;

    @NotEmpty(message = "Please enter some tags.")
    private String tagStr;

    private Integer categoryId;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public Integer getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Integer categoryId) {
        this.categoryId = categoryId;
    }

    public String getTagStr() {
        return tagStr;
    }

    public void setTagStr(String tagStr) {
        this.tagStr = tagStr;
    }
}
