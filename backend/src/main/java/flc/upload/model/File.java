package flc.upload.model;

public class File {
    private String name;
    private long length;
    private String lastModified;
    private String relativePath;
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
}
