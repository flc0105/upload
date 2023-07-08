package flc.upload.model;

import flc.upload.enums.BookmarkType;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

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

    /**
     * 从另一个书签对象中更新属性值
     *
     * @param bookmark 要从中获取属性值的书签对象
     */
    public void updatePropertiesFromBookmark(Bookmark bookmark) {
        if (bookmark.getName() != null) {
            setName(bookmark.getName().trim());
        }
        if (bookmark.getUrl() != null) {
            setUrl((getUrl().contains("://") ? getUrl() : "http://" + getUrl()));
        }
        if (bookmark.getIcon() != null) {
            setIcon(bookmark.getIcon());
        }
    }


    public boolean isDirectoryNameInvalid() {
        if (getName() == null || getName().trim().length() == 0 || name.equals("Home")) {
            return true;
        }

        return name.contains("/") || name.contains("\\") || name.contains("?") || name.contains("#") || name.contains(".");
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
        this.url = (url.contains("://") ? url : "http://" + url);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = (name != null ? name.trim() : null);
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
