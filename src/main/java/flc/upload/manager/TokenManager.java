package flc.upload.manager;

import flc.upload.exception.VerifyFailedException;
import flc.upload.util.JwtUtil;
import org.springframework.stereotype.Service;

@Service
public class TokenManager {

    public void verify(String token) {
        if (!JwtUtil.validateToken(token)) {
            throw new VerifyFailedException("没有权限");
        }
    }
}
