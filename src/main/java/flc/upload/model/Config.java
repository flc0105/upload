package flc.upload.model;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class Config {
    public static String secret;
    public static int expirationTime;

    @Value("${jwt.secret-key}")
    public void setSecret(String secret) {
        Config.secret = secret;
    }

    @Value("${jwt.expiration-time}")
    public void setExpirationTime(int expirationTime) {
        Config.expirationTime = expirationTime;
    }

}
