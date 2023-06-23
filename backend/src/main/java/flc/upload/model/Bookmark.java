package flc.upload.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.util.List;

@ApiModel("书签")
public class Bookmark {
    @ApiModelProperty("书签id")
    private Integer id;
    @ApiModelProperty("url")
    private String url;
    @ApiModelProperty("标题")
    private String title;
    @ApiModelProperty("图标base64")
    private String icon;
    @ApiModelProperty("标签列表")
    private List<Tag> tags;

    public void update(String title, String url) {
        if (title != null) {
            setTitle(title);
        }
        if (url != null) {
            setUrl(url);
        }

    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public List<Tag> getTags() {
        return tags;
    }

    public void setTags(List<Tag> tags) {
        this.tags = tags;
    }
}
