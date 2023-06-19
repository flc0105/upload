package flc.upload.util;

import org.apache.ibatis.session.SqlSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class MyBatisUtil {

    private final SqlSession sqlSession;

    @Autowired
    public MyBatisUtil(SqlSession sqlSession) {
        this.sqlSession = sqlSession;
    }

    public List<Map<String, Object>> executeQuery(String sql) {
        return sqlSession.selectList("executeQuery", sql);
    }

    public Integer executeQueryCount(String sql) {
        return sqlSession.selectOne("executeQueryCount", sql);
    }

    public Integer executeStatement(String sql) {
        return sqlSession.update("executeStatement", sql);
    }
}

