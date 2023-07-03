package flc.upload.mapper;

import flc.upload.model.Bookmark;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface BookmarkMapper {

    @Insert("INSERT INTO bookmark (name, url, icon, parentId, bookmarkType) VALUES (#{name}, #{url}, #{icon}, #{parentId}, #{bookmarkType})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void addBookmark(Bookmark bookmark);

    @Delete("DELETE FROM bookmark WHERE id = #{id}")
    void deleteBookmarkById(Integer id);

    @Update("UPDATE bookmark SET name = #{name}, url = #{url}, icon = #{icon}, parentId = #{parentId}, bookmarkType = #{bookmarkType} WHERE id = #{id}")
    void updateBookmark(Bookmark bookmark);

    @Select("SELECT * FROM bookmark WHERE parentId = #{parentId}")
    List<Bookmark> getBookmarksByParentId(Integer parentId);

    @Select("SELECT * FROM bookmark")
    @Results(id = "bookmarkMap", value = {
            @Result(property = "parentId", column = "parentId"),
            @Result(property = "bookmarkType", column = "bookmarkType")
    })
    List<Bookmark> getAllBookmarks();


}
