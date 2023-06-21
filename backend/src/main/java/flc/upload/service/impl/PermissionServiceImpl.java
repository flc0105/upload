package flc.upload.service.impl;

import flc.upload.manager.PermissionManager;
import flc.upload.mapper.PermissionMapper;
import flc.upload.model.Permission;
import flc.upload.model.Result;
import flc.upload.service.PermissionService;
import org.springframework.stereotype.Service;

@Service
public class PermissionServiceImpl implements PermissionService {
    private final PermissionMapper permissionMapper;

    private final PermissionManager permissionManager;

    public PermissionServiceImpl(PermissionMapper permissionMapper, PermissionManager permissionManager) {
        this.permissionMapper = permissionMapper;
        this.permissionManager = permissionManager;
    }


    @Override
    public Result updatePermissionByPath(Permission permission) throws Exception {
        return new Result(true, "更改成功", permissionMapper.updatePermissionByPath(permission));
    }

    @Override
    public Result getPermission(String path) throws Exception {
        return permissionManager.getPermission(path);
    }

    @Override
    public Result findAll() throws Exception {
        return new Result(true, "查询成功", permissionMapper.findAll());
    }
}
