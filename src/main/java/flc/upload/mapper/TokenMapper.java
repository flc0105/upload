package flc.upload.mapper;

import flc.upload.model.Token;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface TokenMapper {
    @Insert("insert into token values(#{value})")
    Integer get(Token token) throws Exception;

    @Select("select count(*) from token where value=#{token}")
    Integer verify(String token) throws Exception;
}
