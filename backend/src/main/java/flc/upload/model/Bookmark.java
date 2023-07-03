package flc.upload.model;

import flc.upload.enums.BookmarkType;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.util.List;

@ApiModel("书签")
public class Bookmark {
    @ApiModelProperty("id")
    private Integer id;

    @ApiModelProperty("url")
    private String url;

    @ApiModelProperty("名称")
    private String name;

    @ApiModelProperty("图标")
    private String icon;

    @ApiModelProperty("父级目录id")
    private Integer parentId;

    @ApiModelProperty("书签类型")
    private Integer bookmarkType;

    public boolean isDirectory() {
        return bookmarkType == BookmarkType.DIRECTORY.getValue();
    }

    public boolean isBookmark() {
        return bookmarkType == BookmarkType.BOOKMARK.getValue();
    }

    public String getBookmarkTypeStr() {
        return bookmarkType == BookmarkType.DIRECTORY.getValue() ? "directory" : "bookmark";
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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public Integer getParentId() {
        return parentId;
    }

    public void setParentId(Integer parentId) {
        this.parentId = parentId;
    }

    public Integer getBookmarkType() {
        return bookmarkType;
    }

    public void setBookmarkType(Integer bookmarkType) {
        this.bookmarkType = bookmarkType;
    }
}
