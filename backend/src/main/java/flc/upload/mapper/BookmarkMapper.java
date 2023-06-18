package flc.upload.mapper;

import flc.upload.model.Bookmark;
import flc.upload.model.Tag;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface BookmarkMapper {
    @Insert("insert into bookmark values(null, #{url}, #{title}, #{icon})")
    @Options(useGeneratedKeys = true, keyProperty = "id", keyColumn = "id")
    Integer add(Bookmark bookmark) throws Exception;

    @Delete("delete from bookmark where id=#{id}")
    Integer delete(Integer id) throws Exception;

    @Update("update bookmark set title=#{title}, url=#{url}, icon=#{icon} where id=#{id}")
    Integer update(Bookmark bookmark) throws Exception;

    @Select("select * from bookmark")
    List<Bookmark> findAll() throws Exception;

    @Select("select * from bookmark where id=#{id}")
    Bookmark findById(Integer id) throws Exception;


    @Insert("insert into bookmark_tag (bookmark_id, tag_id) values(#{bookmarkId}, #{tagId})")
    Integer addTag(Integer bookmarkId, Integer tagId) throws Exception;

    @Select("select * from tag")
    List<Tag> findAllTags() throws Exception;
}
