package flc.upload.manager;

import flc.upload.exception.VerifyFailedException;
import flc.upload.model.AppConfig;
import flc.upload.util.JwtUtil;
import org.springframework.stereotype.Service;

@Service
public class TokenManager {

    private final AppConfig config;

    public TokenManager(AppConfig config) {
        this.config = config;
    }

    public void verify(String token) {

        if (token == null) {
            throw new VerifyFailedException("没有权限");
        }

        if (config.getDeactivatedTokens().contains(token)) {
            throw new VerifyFailedException("该token已被禁用");
        }

        if (!JwtUtil.validateToken(token)) {
            throw new VerifyFailedException("token验证失败");
        }

    }
}
