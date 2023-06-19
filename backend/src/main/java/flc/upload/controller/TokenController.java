package flc.upload.controller;

import flc.upload.annotation.Log;
import flc.upload.annotation.Token;
import flc.upload.model.AppConfig;
import flc.upload.model.Result;
import flc.upload.util.CommonUtil;
import flc.upload.util.CookieUtil;
import flc.upload.util.JwtUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.Field;
import java.util.List;

@Api(tags = "Token")
@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/token")
public class TokenController {

    private final AppConfig config;

    public TokenController(AppConfig config) {
        this.config = config;
    }

    @Value("${password}")
    private String password;

    @Log
    @ApiOperation("Token_获取")
    @PostMapping("/get")
    public Result get(@RequestParam("password") String password, HttpServletRequest request, HttpServletResponse response) throws Exception {
        if (this.password.equals(password)) {
//        if (config.getPassword().equals(password)) {
            String token = JwtUtil.generateToken();
            CookieUtil.addCookie("token", token, request, response);
            return new Result<>(true, "获取成功", token);
        } else {
            return new Result<>(false, "密码错误");
        }
    }

    @Log
    @ApiOperation("Token_获取_带用户名")
    @PostMapping("/getWithUsername")
    public Result getWithUsername(@RequestParam("password") String password, String username, HttpServletRequest request, HttpServletResponse response) throws Exception {
        if (this.password.equals(password)) {
//        if (config.getPassword().equals(password)) {
            String token = JwtUtil.generateTokenWithUsername(username);
            CookieUtil.addCookie("token", token, request, response);
            return new Result<>(true, "获取成功", token);
        } else {
            return new Result<>(false, "密码错误");
        }
    }

    @Log
    @ApiOperation("Token_禁用")
//    @Token
    @PostMapping("/deactivateToken")
    public Result deactivateToken(@RequestParam String token) {
        Field field = CommonUtil.getFieldByName("deactivatedTokens", config.getClass());
        if (field != null) {
            try {
                field.setAccessible(true);
                List<String> tokens = config.getDeactivatedTokens();
                tokens.add(token);
                field.set(config, tokens);
                System.out.println(config.getDeactivatedTokens());
                return new Result(true, "添加成功", null);

            } catch (IllegalAccessException e) {
                e.printStackTrace();
                return new Result(false, "添加失败" + e.getMessage(), null);
            }
        } else {
            return new Result(false, "添加失败：没有找到配置项", null);
        }
    }
}
