package flc.upload.controller;

import flc.upload.model.Result;
import flc.upload.util.CookieUtil;
import flc.upload.util.JwtUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/token")
public class TokenController {

    @Value("${password}")
    private String password;

    @PostMapping("/get")
    public Result get(@RequestParam("password") String password, HttpServletRequest request, HttpServletResponse response) throws Exception {
        if (this.password.equals(password)) {
            String token = JwtUtil.generateToken();
            CookieUtil.addCookie("token", token, request, response);
            return new Result<>(true, "获取成功", token);
        } else {
            return new Result<>(false, "密码错误");
        }
    }
}
