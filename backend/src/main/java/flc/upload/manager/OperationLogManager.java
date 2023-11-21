package flc.upload.manager;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public interface OperationLogManager {

    Map<String, Long> cachedApis = new HashMap<>();


    void add(Map<String, String> map);

    void clear();

    List<Map<String, String>> list();

    Map<String, Object> page(int page);
}
