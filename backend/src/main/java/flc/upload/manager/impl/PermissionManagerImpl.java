package flc.upload.manager.impl;

import flc.upload.exception.BusinessException;
import flc.upload.manager.PermissionManager;
import flc.upload.mapper.PermissionMapper;
import flc.upload.model.AppConfig;
import flc.upload.model.Permission;
import flc.upload.model.Result;
import flc.upload.util.ResponseUtil;
import org.springframework.stereotype.Service;

@Service
public class PermissionManagerImpl implements PermissionManager {

    private final PermissionMapper permissionMapper;

    public PermissionManagerImpl(PermissionMapper permissionMapper, AppConfig config) {
        this.permissionMapper = permissionMapper;
    }

    @Override
    public Result<?> get(String path) throws Exception {
        Permission permission = permissionMapper.get(path);
        if (permission == null) {
            throw new BusinessException("query.failure");
        }
        return ResponseUtil.buildSuccessResult("query.success", permissionMapper.get(path));
    }

}
