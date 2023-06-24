package flc.upload.util;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import flc.upload.model.AppConfig;
import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * JWT令牌生成工具类。
 */
@Component
public class JwtUtil {

    private static AppConfig appConfig;

    @Autowired
    public void setAppConfig(AppConfig appConfig) {
        JwtUtil.appConfig = appConfig;
    }

    /**
     * 生成 JWT Token。
     * 使用配置文件中的过期时间和密钥生成带有过期时间和签发时间的 JWT Token。
     *
     * @return 生成的 JWT Token
     */
    public static String generateToken() {
        Map<String, Date> dateMap = JwtUtil.getDateMap(appConfig.getJwtExpirationTime());
        return JWT.create()
                .withExpiresAt(dateMap.get("exp"))
                .withIssuedAt(dateMap.get("iat"))
                .sign(Algorithm.HMAC256(appConfig.getJwtSecretKey()));
    }

    /**
     * 生成带有备注字段的JWT令牌。
     *
     * @param remark 备注信息，将包含在令牌中
     * @return 生成的JWT令牌
     */
    public static String generateToken(String remark) {
        Map<String, Date> dateMap = JwtUtil.getDateMap(appConfig.getJwtExpirationTime());
        return JWT.create()
                .withExpiresAt(dateMap.get("exp"))
                .withIssuedAt(dateMap.get("iat"))
                .withClaim("remark", remark)
                .sign(Algorithm.HMAC256(appConfig.getJwtSecretKey()));
    }

    /**
     * 获取JWT令牌中的备注信息。
     *
     * @param token 要解析的JWT令牌
     * @return JWT令牌中的备注信息，如果解析失败则返回空字符串
     */
    public static String getRemark(String token) {
        try {
            JWTVerifier verifier = JWT.require(Algorithm.HMAC256(appConfig.getJwtSecretKey()))
                    .build();
            DecodedJWT decodedJWT = verifier.verify(token);
            return decodedJWT.getClaim("remark").asString();
        } catch (Exception e) {
            return Strings.EMPTY;
        }
    }

    /**
     * 验证JWT令牌的有效性。
     *
     * @param token 要验证的JWT令牌
     * @return 如果令牌有效，则返回true；否则返回false
     */
    public static boolean validateToken(String token) {
        try {
            JWTVerifier jwtVerifier = JWT.require(Algorithm.HMAC256(appConfig.getJwtSecretKey())).build();
            jwtVerifier.verify(token);
        } catch (JWTVerificationException e) {
            return false;
        }
        return true;
    }

    /**
     * 返回JWT令牌过期时间的日期映射。
     *
     * @param seconds 令牌有效时间（以秒为单位）
     * @return 包含"exp"（过期时间）和"iat"（签发时间）日期的映射
     */
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
