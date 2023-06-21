package flc.upload.service;

import flc.upload.model.Permission;
import flc.upload.model.Result;

public interface PermissionService {
    Result updatePermissionByPath(Permission permission) throws Exception;

    Result getPermission(String path) throws Exception;

    Result findAll() throws Exception;
}
