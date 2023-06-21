package flc.upload.service;

import flc.upload.model.Permission;
import flc.upload.model.Result;

public interface PermissionService {
    Result updatePermissionByPath(Permission permission);

    Result getPermission(String path) throws Exception;
}
