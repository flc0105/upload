package flc.upload.manager;

import flc.upload.model.Result;

public interface PermissionManager {
    Result<?> get(String path) throws Exception;
}
