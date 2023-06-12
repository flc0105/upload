package flc.upload.model;

import javax.validation.constraints.NotNull;

public class Paste {
    @NotNull(message = "ID不能为空")
    private Integer id;
    private String title;
    private String text;
    private String time;
    private String expiredDate;
    private Boolean isPrivate;

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

    public Boolean isPrivate() {
        return isPrivate;
    }

    public void setPrivate(Boolean aPrivate) {
        isPrivate = aPrivate;
    }

    @Override
    public String toString() {
        return "Paste{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", text='" + text + '\'' +
                ", time='" + time + '\'' +
                ", expiredDate='" + expiredDate + '\'' +
                ", isPrivate=" + isPrivate +
                '}';
    }
}
