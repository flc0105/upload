package flc.upload.mapper;

import flc.upload.model.Paste;
import org.apache.ibatis.jdbc.SQL;

public class PasteSqlProvider {
    public String updatePaste(Paste paste) {
        return new SQL() {{
            UPDATE("paste");
            if (paste.getText() != null) {
                SET("text = #{text}");
            }
            if (paste.getTitle() != null) {
                SET("title = #{title}");
            }
            if (paste.isPrivate() != null) {
                SET("isPrivate = #{isPrivate}");
            }
            WHERE("id = #{id}");
        }}.toString();
    }
}