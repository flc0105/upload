package flc.upload.mapper;

import flc.upload.model.Paste;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface PasteMapper {
    @Insert("insert into paste values(null,#{title},#{text},#{time},#{expiredDate},#{isPrivate})")
    @Options(useGeneratedKeys = true, keyProperty = "id", keyColumn = "id")
    Integer add(Paste paste) throws Exception;

    @Delete("delete from paste where id=#{id}")
    Integer delete(Integer id) throws Exception;

    @UpdateProvider(type = PasteSqlProvider.class, method = "updatePaste")
    Integer update(Paste paste) throws Exception;

    @Select("select * from paste where isPrivate!=true order by time desc")
    List<Paste> findAll() throws Exception;

    @Select("select * from paste where id=#{id}")
    Paste findById(Integer id) throws Exception;

    @Select("select * from paste order by time desc limit 1")
    Paste findLast() throws Exception;

    @Select("select * from paste where isPrivate=true order by time desc")
    List<Paste> findPrivate() throws Exception;

    @Delete("delete from paste where expiredDate <= #{date} and expiredDate != -1")
    Integer deleteExpired(String date) throws Exception;

}
