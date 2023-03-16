package flc.upload.mapper;

import flc.upload.model.Bookmark;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface BookmarkMapper {
    @Insert("insert into bookmark values(null, #{url}, #{title}, #{icon})")
    @Options(useGeneratedKeys = true, keyProperty = "id", keyColumn = "id")
    Integer add(Bookmark bookmark) throws Exception;

    @Delete("delete from bookmark where id=#{id}")
    Integer delete(Integer id) throws Exception;

    @Update("update bookmark set title=#{title}, icon=#{icon} where id=#{id}")
    Integer update(Bookmark bookmark) throws Exception;

    @Select("select * from bookmark")
    List<Bookmark> findAll() throws Exception;

    @Select("select * from bookmark where id=#{id}")
    Bookmark findById(Integer id) throws Exception;
}
