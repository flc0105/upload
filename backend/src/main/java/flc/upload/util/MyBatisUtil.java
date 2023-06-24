package flc.upload.util;

import org.apache.ibatis.session.SqlSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * MyBatis 工具类，提供对 SQL 查询和语句执行的封装和常用方法。
 * 通过注入 SqlSession 实例进行数据库操作。
 */
@Component
public class MyBatisUtil {

    private final SqlSession sqlSession;

    /**
     * 构造函数，注入 SqlSession 实例。
     *
     * @param sqlSession SqlSession 实例
     */
    @Autowired
    public MyBatisUtil(SqlSession sqlSession) {
        this.sqlSession = sqlSession;
    }

    /**
     * 执行 SQL 查询语句，并返回结果集。
     *
     * @param sql SQL 查询语句
     * @return 查询结果集，每行数据以 Map 的形式存储，键为列名，值为对应的数据
     */
    public List<Map<String, Object>> executeQuery(String sql) {
        return sqlSession.selectList("executeQuery", sql);
    }

    /**
     * 执行 SQL 查询语句，并返回结果的记录数。
     *
     * @param sql SQL 查询语句
     * @return 查询结果的记录数
     */
    public Integer executeQueryCount(String sql) {
        return sqlSession.selectOne("executeQueryCount", sql);
    }

    /**
     * 执行 SQL 语句，如 INSERT、UPDATE、DELETE 等操作，并返回受影响的行数。
     *
     * @param sql SQL 语句
     * @return 受影响的行数
     */
    public Integer executeStatement(String sql) {
        return sqlSession.update("executeStatement", sql);
    }
}

