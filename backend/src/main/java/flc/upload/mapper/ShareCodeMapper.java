package flc.upload.mapper;

import flc.upload.model.ShareCode;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface ShareCodeMapper {
    @Insert("insert into share_code values(null, #{code}, #{path})")
    Integer add(ShareCode shareCode) throws Exception;

    @Delete("delete from share_code where code=#{code}")
    Integer delete(String code) throws Exception;

    @Select("select * from share_code")
    List<ShareCode> findAll() throws Exception;

    @Select("select * from share_code where code=#{code}")
    ShareCode findByCode(String code) throws Exception;

    @Select("select * from share_code where path=#{path} limit 1")
    ShareCode findByPath(String path) throws Exception;
}
