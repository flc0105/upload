package flc.upload.controller;

import flc.upload.annotation.Log;
import flc.upload.annotation.Token;
import flc.upload.model.Permission;
import flc.upload.model.Result;
import flc.upload.service.PermissionService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@Api(tags = "权限")
@RestController
public class PermissionController {

    private final PermissionService permissionService;

    public PermissionController(PermissionService permissionService) {
        this.permissionService = permissionService;
    }

//    @Log
//    @Token
    @ApiOperation("修改权限")
    @PostMapping("/permission/set")
    public Result setPermission(@RequestBody Permission permission) throws Exception {
        return permissionService.updatePermissionByPath(permission);
    }
}
