package flc.upload.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;
import java.util.Map;

@Mapper
public interface SqlMapper {
    @Select("${sql}")
    List<Map<String, Object>> executeQuery(String sql);

    @Select("${sql}")
    Integer executeQueryCount(@Param("sql") String sql);

    @Update("${sql}")
    Integer executeStatement(String sql);

}