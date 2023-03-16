package flc.upload.model;

public class Paste {
    private Integer id;
    private String title;
    private String text;
    private String time;
    private String expiredDate;
    private boolean isPrivate;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getExpiredDate() {
        return expiredDate;
    }

    public void setExpireDate(String expiredDate) {
        this.expiredDate = expiredDate;
    }

    public boolean isPrivate() {
        return isPrivate;
    }

    public void setPrivate(boolean aPrivate) {
        isPrivate = aPrivate;
    }
}
