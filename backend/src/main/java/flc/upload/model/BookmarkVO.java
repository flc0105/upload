package flc.upload.model;

import java.util.List;

public class BookmarkVO {
    private Integer id;
    private String type;
    private String name;
    private String url;
    private String icon;
    private List<BookmarkVO> children;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public List<BookmarkVO> getChildren() {
        return children;
    }

    public void setChildren(List<BookmarkVO> children) {
        this.children = children;
    }
}

