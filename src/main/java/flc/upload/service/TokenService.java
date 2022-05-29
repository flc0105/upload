package flc.upload.service;

import flc.upload.manager.TokenManager;
import flc.upload.model.Result;
import flc.upload.model.Token;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class TokenService {
    private TokenManager tokenManager;

    public TokenService(TokenManager tokenManager) {
        this.tokenManager = tokenManager;
    }

    @Value("${password}")
    private String password;

    public Result get(String password) throws Exception {
        if (this.password.equals(password)) {
            Token token = new Token(UUID.randomUUID().toString());
            if (tokenManager.get(token)) {
                return new Result<>(true, "获取成功", token);
            } else {
                return new Result<>(false, "获取失败");
            }
        } else {
            return new Result<>(false, "密码错误");
        }
    }

}
