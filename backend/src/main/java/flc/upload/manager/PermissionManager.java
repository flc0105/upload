package flc.upload.manager;

import flc.upload.model.Result;

public interface PermissionManager {
    Result getPermission(String path) throws Exception;
}
