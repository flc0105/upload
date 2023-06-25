package flc.upload.service.impl;

import flc.upload.manager.PermissionManager;
import flc.upload.mapper.PermissionMapper;
import flc.upload.model.Permission;
import flc.upload.model.Result;
import flc.upload.service.PermissionService;
import flc.upload.util.ResponseUtil;
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
    public Result<?> update(Permission permission) throws Exception {
        return ResponseUtil.buildSuccessResult("update.success", permissionMapper.update(permission));
    }

    @Override
    public Result<?> get(String path) throws Exception {
        return ResponseUtil.buildSuccessResult("query.success", permissionManager.get(path));
    }

    @Override
    public Result<?> findAll() throws Exception {
        return ResponseUtil.buildSuccessResult("query.success", permissionMapper.findAll());
    }
}
