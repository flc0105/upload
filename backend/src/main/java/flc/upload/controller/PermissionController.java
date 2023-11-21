package flc.upload.controller;

import flc.upload.annotation.OperationLog;
import flc.upload.annotation.Token;
import flc.upload.model.Permission;
import flc.upload.model.Result;
import flc.upload.service.PermissionService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.GetMapping;
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


    @ApiOperation("权限_修改")
    @OperationLog
    @Token
    @PostMapping("/permission/update")
    public Result<?> update(@RequestBody Permission permission) throws Exception {
        return permissionService.update(permission);
    }

    @ApiOperation("权限_查询")
    @OperationLog
    @Token
    @GetMapping("/permission/list")
    public Result<?> list() throws Exception {
        return permissionService.findAll();
    }

}
