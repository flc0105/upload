package flc.upload.controller;

import flc.upload.annotation.Log;
import flc.upload.annotation.Permission;
import flc.upload.annotation.Token;
import flc.upload.model.AppConfig;
import flc.upload.model.Result;
import flc.upload.util.*;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Api(tags = "Token")
@CrossOrigin(origins = "*")
@RequestMapping("/token")
@RestController
public class TokenController {

//    private final AppConfig appConfig;
    @Value("${password}")
    private String password;

    public TokenController(AppConfig appConfig) {
//        this.appConfig = appConfig;
    }

    @ApiOperation("获取Token")
    @Log
    @PostMapping("/get")
    public Result<?> get(@RequestParam("password") String password, @RequestParam("remark") Optional<String> remark, HttpServletRequest request, HttpServletResponse response) throws Exception {
        if (this.password.equals(password)) {
            String token = JwtUtil.generateToken(remark.orElseGet(() -> RequestUtil.getClientBrowser(request)));
            //String token = remark.map(JwtUtil::generateToken).orElseGet(JwtUtil::generateToken);
            CookieUtil.addCookie("token", token, request, response);
            return ResponseUtil.buildSuccessResult("query.success", token);
        } else {
            return ResponseUtil.buildErrorResult("password.is.incorrect");
        }
    }

//    @ApiOperation("停用Token")
//    @Log
//    @Token
//    @PostMapping("/deactivate")
//    public Result<?> deactivate(@RequestParam String token) throws IllegalAccessException {
//        Field field = Objects.requireNonNull(ReflectionUtil.getFieldByName("deactivatedTokens", appConfig.getClass()), InternationalizationUtil.translate("no.such.configuration"));
//        field.setAccessible(true);
//        List<String> tokens = appConfig.getDeactivatedTokens();
//        tokens.add(token);
//        field.set(appConfig, tokens);
//        return ResponseUtil.buildSuccessResult("update.success");
//    }
}
