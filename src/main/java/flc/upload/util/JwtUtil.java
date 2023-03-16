package flc.upload.util;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import flc.upload.model.Config;
import org.springframework.stereotype.Component;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
public class JwtUtil {

    public static String generateToken() {
        Map<String, Date> dateMap = JwtUtil.getDateMap(Config.expirationTime);
        return JWT.create()
                .withExpiresAt(dateMap.get("exp"))
                .withIssuedAt(dateMap.get("iat"))
                .sign(Algorithm.HMAC256(Config.secret));
    }

    public static boolean validateToken(String token) {
        try {
            JWTVerifier jwtVerifier = JWT.require(Algorithm.HMAC256(Config.secret)).build();
            jwtVerifier.verify(token);
        } catch (JWTVerificationException e) {
            return false;
        }
        return true;
    }

    private static Map<String, Date> getDateMap(int seconds) {
        Map<String, Date> map = new HashMap<>();
        Calendar calendar = Calendar.getInstance();
        Date now = calendar.getTime();
        calendar.add(Calendar.SECOND, seconds);
        map.put("iat", now);
        map.put("exp", calendar.getTime());
        return map;
    }

}
