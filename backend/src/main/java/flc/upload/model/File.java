package flc.upload.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel("文件")
public class File {
    @ApiModelProperty("文件名")
    private String name;

    @ApiModelProperty("文件大小")
    private long length;

    @ApiModelProperty("修改时间")
    private String lastModified;

    @ApiModelProperty("相对路径")
    private String relativePath;

    @ApiModelProperty("文件类型")
    private String fileType;

    public File(String name, long length, String lastModified, String relativePath, String fileType) {
        this.name = name;
        this.length = length;
        this.lastModified = lastModified;
        this.relativePath = relativePath;
        this.fileType = fileType;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getLength() {
        return length;
    }

    public void setLength(long length) {
        this.length = length;
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

    public String getFileType() {
        return fileType;
    }

    public void setFileType(String fileType) {
        this.fileType = fileType;
    }

    @Override
    public String toString() {
        return "File{" +
                "name='" + name + '\'' +
                ", length=" + length +
                ", lastModified='" + lastModified + '\'' +
                ", relativePath='" + relativePath + '\'' +
                ", fileType='" + fileType + '\'' +
                '}';
    }
}
