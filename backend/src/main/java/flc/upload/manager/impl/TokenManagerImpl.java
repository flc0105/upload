package flc.upload.manager.impl;

import flc.upload.exception.VerifyFailedException;
import flc.upload.manager.TokenManager;
import flc.upload.model.AppConfig;
import flc.upload.util.JwtUtil;
import org.springframework.stereotype.Service;

@Service
public class TokenManagerImpl implements TokenManager {

    private final AppConfig appConfig;

    public TokenManagerImpl(AppConfig appConfig) {
        this.appConfig = appConfig;
    }

    public void verify(String token) {

        if (token == null) {
            throw new VerifyFailedException("no.permission");
        }

        if (appConfig.getDeactivatedTokens().contains(token)) {
            throw new VerifyFailedException("token.deactivated");
        }

        if (!JwtUtil.validateToken(token)) {
            throw new VerifyFailedException("token.verification.failed");
        }

    }
}
