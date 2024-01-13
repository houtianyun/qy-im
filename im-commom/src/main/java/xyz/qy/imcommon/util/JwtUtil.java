package xyz.qy.imcommon.util;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTDecodeException;
import com.auth0.jwt.exceptions.JWTVerificationException;

public class JwtUtil {
    /**
     * 根据token获取userId
     * @param token
     * @return
     * */
    public static Long getUserId(String token) {
        try {
            String userId = JWT.decode(token).getAudience().get(0);
            return Long.parseLong(userId);
        }catch (JWTDecodeException e) {
            return null;
        }
    }

    /**
     * 根据token获取自定义数据info
     * @param token
     * @return
     * */
    public static String getInfo(String token) {
        try {
            return JWT.decode(token).getClaim("info").asString();
        }catch (JWTDecodeException e) {
            return null;
        }
    }

    /**
     * 校验token
     * @param token
     * @param secret
     * @return
     * */
    public static boolean checkSign(String token,String secret) {
        try{
            Algorithm algorithm  = Algorithm.HMAC256(secret);
            JWTVerifier verifier = JWT.require(algorithm).build();
            verifier.verify(token);
            return true;
        }catch (JWTVerificationException e) {
            return false;
        }
    }
}
