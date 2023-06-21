package flc.upload.service.impl;

import flc.upload.manager.PermissionManager;
import flc.upload.mapper.PermissionMapper;
import flc.upload.model.Permission;
import flc.upload.model.Result;
import flc.upload.service.PermissionService;
import org.apache.commons.lang.NotImplementedException;
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
    public Result updatePermissionByPath(Permission permission) {
        throw new NotImplementedException();
    }

    @Override
    public Result getPermission(String path) throws Exception {
        return permissionManager.getPermission(path);
//        Permission permission = permissionMapper.getPermission(path);
//        if (permission == null) {
//            throw new BusinessException("未找到该Path");
//        }
//        return new Result(true, "查询成功", permissionMapper.getPermission(path));
    }
}
