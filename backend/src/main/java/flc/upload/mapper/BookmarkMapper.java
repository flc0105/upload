package flc.upload.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import flc.upload.model.Bookmark;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface BookmarkMapper extends BaseMapper<Bookmark> {

    @Insert("INSERT INTO bookmark (name, url, icon, parentId, bookmarkType) VALUES (#{name}, #{url}, #{icon}, #{parentId}, #{bookmarkType})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void addBookmark(Bookmark bookmark);

//    @Delete("DELETE FROM bookmark WHERE id = #{id}")
    @Delete("DELETE FROM bookmark WHERE id = #{id}")
    void deleteBookmarkById(@Param("id") Integer id);

    @Delete("DELETE FROM bookmark WHERE parentId = #{parentId}")
    void deleteBookmarkByParentId(@Param("parentId") Integer parentId);

    @Update("UPDATE bookmark SET name = #{name}, url = #{url}, icon = #{icon}, parentId = #{parentId}, bookmarkType = #{bookmarkType} WHERE id = #{id}")
    void updateBookmark(Bookmark bookmark);

    @Select("SELECT * FROM bookmark WHERE parentId = #{parentId}")
    List<Bookmark> getBookmarksByParentId(Integer parentId);

    @Select("SELECT * FROM bookmark WHERE id = #{id}")
    Bookmark findById(Integer id);

//    @Select("SELECT * FROM bookmark WHERE name = #{name} and parentId = #{parentId} and bookmarkType = 0")
//    List<Bookmark> findByName(String name, Integer parentId);

    @Select("SELECT * FROM bookmark")
    @Results(id = "bookmarkMap", value = {
            @Result(property = "parentId", column = "parentId"),
            @Result(property = "bookmarkType", column = "bookmarkType")
    })
    List<Bookmark> getAllBookmarks();


}
