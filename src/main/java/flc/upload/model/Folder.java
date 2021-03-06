package flc.upload.model;

public class Folder {
    private String name;
    private String lastModified;
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
