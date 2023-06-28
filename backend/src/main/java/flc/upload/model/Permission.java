package flc.upload.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel("权限")
public class Permission {

    @ApiModelProperty("id")
    private Integer id;

    @ApiModelProperty("接口地址")
    private String path;

    @ApiModelProperty("是否需要管理员权限")
    private Integer isAdmin;

    @ApiModelProperty("接口描述")
    private String desc;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public Integer getIsAdmin() {
        return isAdmin;
    }

    public void setIsAdmin(Integer isAdmin) {
        this.isAdmin = isAdmin;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    @Override
    public String toString() {
        return "Permission{" +
                "id=" + id +
                ", path='" + path + '\'' +
                ", isAdmin=" + isAdmin +
                ", desc='" + desc + '\'' +
                '}';
    }
}
