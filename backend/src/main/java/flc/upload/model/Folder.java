package flc.upload.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel("文件夹")
public class Folder {
    @ApiModelProperty("文件夹名")
    private String name;
    @ApiModelProperty("修改时间")
    private String lastModified;
    @ApiModelProperty("相对路径")
    private String relativePath;

    public Folder(String name, String lastModified, String relativePath) {
        this.name = name;
        this.lastModified = lastModified;
        this.relativePath = relativePath;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLastModified() {
        return lastModified;
    }

    public void setLastModified(String lastModified) {
        this.lastModified = lastModified;
    }

    public String getRelativePath() {
        return relativePath;
    }

    public void setRelativePath(String relativePath) {
        this.relativePath = relativePath;
    }
}
