package flc.upload.controller;

import flc.upload.model.Result;
import flc.upload.model.Token;
import flc.upload.service.TokenService;
import flc.upload.util.CookieUtil;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/token")
public class TokenController {
    private TokenService tokenService;

    public TokenController(TokenService tokenService) {
        this.tokenService = tokenService;
    }

    @PostMapping("/get")
    public Result get(@RequestParam("password") String password, HttpServletRequest request, HttpServletResponse response) throws Exception {
        Result result = tokenService.get(password);
        if (result.isSuccess()) {
            CookieUtil.addCookie("token", ((Token) result.getDetail()).getValue(), request, response);
        }
        return result;
    }
}
