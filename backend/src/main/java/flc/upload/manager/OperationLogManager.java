package flc.upload.manager;

import com.baomidou.mybatisplus.core.metadata.IPage;
import flc.upload.model.OperationLog;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public interface OperationLogManager {

    void add(OperationLog operationLog);

    void deleteById(int id);

    void deleteAll();

    List<OperationLog> list();

    IPage<OperationLog> page(int page, int size);
}
