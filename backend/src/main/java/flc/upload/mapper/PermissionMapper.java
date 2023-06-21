package flc.upload.mapper;

import flc.upload.model.Permission;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface PermissionMapper {

    @Update("update permission set isAdmin=#{isAdmin} where path=#{path}")
    Integer updatePermissionByPath(Permission permission) throws Exception;

    @Select("select * from permission where path=#{path}")
    Permission getPermission(String path) throws Exception;

}
