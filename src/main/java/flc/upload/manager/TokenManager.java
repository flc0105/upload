package flc.upload.manager;

import flc.upload.exception.VerifyFailedException;
import flc.upload.util.JwtUtil;
import org.springframework.stereotype.Service;

@Service
public class TokenManager {

    public void verify(String token) {
        if (token == null) {
            throw new VerifyFailedException("没有权限");
        }
        if (!JwtUtil.validateToken(token)) {
            throw new VerifyFailedException("token验证失败");
        }
    }
}
