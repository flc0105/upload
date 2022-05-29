package flc.upload.manager;

import flc.upload.exception.VerifyFailedException;
import flc.upload.mapper.TokenMapper;
import flc.upload.model.Token;
import org.springframework.stereotype.Service;

@Service
public class TokenManager {

    private TokenMapper tokenMapper;

    public TokenManager(TokenMapper tokenMapper) {
        this.tokenMapper = tokenMapper;
    }

    public boolean get(Token token) throws Exception {
        return tokenMapper.get(token) != 0;
    }

    public void verify(String token) throws Exception {
        if (tokenMapper.verify(token) == 0) {
            throw new VerifyFailedException("没有权限");
        }
    }
}
