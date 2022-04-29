package flc.upload.mapper;

import flc.upload.model.Text;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface TextMapper {

    @Select("select * from text order by time desc")
    List<Text> findAll();

    @Insert("insert into text values(null,#{author},#{data},#{time})")
    Integer add(Text text) throws Exception;

    @Delete("delete from text where id=#{id}")
    Integer delete(Integer id);
}
