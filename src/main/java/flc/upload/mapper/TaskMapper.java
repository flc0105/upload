package flc.upload.mapper;

import flc.upload.model.Task;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface TaskMapper {
    @Insert("insert into task values(null, #{text}, #{checked})")
    Integer add(Task task) throws Exception;

    @Delete("delete from task where id=#{id}")
    Integer delete(Integer id) throws Exception;

    @Update("update task set checked=#{checked} where id=#{id}")
    Integer update(Task task) throws Exception;

    @Select("select * from task order by checked")
    List<Task> findAll() throws Exception;
}
