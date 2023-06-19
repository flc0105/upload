package flc.upload.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import javax.validation.constraints.NotNull;

@ApiModel("Paste")
public class Paste {
    @NotNull(message = "ID不能为空")
    @ApiModelProperty("Paste id")
    private Integer id;
    @ApiModelProperty("Paste标题")
    private String title;
    @ApiModelProperty("Paste正文")
    private String text;
    @ApiModelProperty("添加时间")
    private String time;
    @ApiModelProperty("过期时间")
    private String expiredDate;
    @ApiModelProperty("是否私密")
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

    public void setExpiredDate(String expiredDate) {
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
