package flc.upload.service;

import flc.upload.model.Permission;
import flc.upload.model.Result;

public interface PermissionService {
    Result<?> update(Permission permission) throws Exception;

    Result<?> get(String path) throws Exception;

    Result<?> findAll() throws Exception;
}
