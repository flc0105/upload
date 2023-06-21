package flc.upload.manager.impl;

import flc.upload.exception.BusinessException;
import flc.upload.manager.PermissionManager;
import flc.upload.mapper.PermissionMapper;
import flc.upload.model.AppConfig;
import flc.upload.model.Permission;
import flc.upload.model.Result;
import org.springframework.stereotype.Service;

@Service
public class PermissionManagerImpl implements PermissionManager {

    private final PermissionMapper permissionMapper;

    private final AppConfig config;

    public PermissionManagerImpl(PermissionMapper permissionMapper, AppConfig config) {
        this.permissionMapper = permissionMapper;
        this.config = config;
    }

    @Override
    public Result getPermission(String path) throws Exception {
        Permission permission = permissionMapper.getPermission(path);
        if (permission == null) {
            throw new BusinessException("未找到该Path");
        }
        return new Result(true, "查询成功", permissionMapper.getPermission(path));
    }


}
