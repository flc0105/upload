package flc.upload.mapper;

import flc.upload.model.Permission;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface PermissionMapper {

    @Update("update permission set isAdmin=#{isAdmin} where path=#{path}")
    Integer updatePermissionByPath(Permission permission) throws Exception;

    @Select("select * from permission where path=#{path}")
    Permission getPermission(@Param("path") String path) throws Exception;

    @Select("select * from permission")
    List<Permission> findAll() throws Exception;

}
