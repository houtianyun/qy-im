package xyz.qy.implatform.util;

import com.alibaba.fastjson.JSON;
import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTDecodeException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import xyz.qy.imclient.IMClient;
import xyz.qy.implatform.config.JwtProperties;
import xyz.qy.implatform.entity.User;
import xyz.qy.implatform.session.UserSession;
import xyz.qy.implatform.vo.LoginVO;

import java.util.Date;

@Component
public class JwtUtil {
    @Autowired
    private JwtProperties jwtProperties;

    @Autowired
    private IMClient imClient;

    /**
     * 生成jwt字符串，30分钟后过期  JWT(json web token)
     *
     * @param userId
     * @param info
     * @param expireIn
     * @param secret
     * @return
     */
    public static String sign(Long userId, String info, long expireIn, String secret) {
        try {
            Date date = new Date(System.currentTimeMillis() + expireIn * 1000);
            Algorithm algorithm = Algorithm.HMAC256(secret);
            return JWT.create()
                    //将userId保存到token里面
                    .withAudience(userId.toString())
                    //存放自定义数据
                    .withClaim("info", info)
                    //五分钟后token过期
                    .withExpiresAt(date)
                    //token的密钥
                    .sign(algorithm);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 根据token获取userId
     *
     * @param token
     * @return
     */
    public static Long getUserId(String token) {
        try {
            String userId = JWT.decode(token).getAudience().get(0);
            return Long.parseLong(userId);
        } catch (JWTDecodeException e) {
            return null;
        }
    }

    /**
     * 根据token获取自定义数据info
     *
     * @param token
     * @return
     */
    public static String getInfo(String token) {
        try {
            return JWT.decode(token).getClaim("info").asString();
        } catch (JWTDecodeException e) {
            return null;
        }
    }

    /**
     * 校验token
     *
     * @param token
     * @param secret
     * @return
     */
    public static boolean checkSign(String token, String secret) {
        Algorithm algorithm = Algorithm.HMAC256(secret);
        JWTVerifier verifier = JWT.require(algorithm)
                //.withClaim("username, username)
                .build();
        verifier.verify(token);
        return true;
    }

    /**
     * 生成token
     *
     * @param user 用户信息
     * @return 用户token
     */
    public LoginVO createToken(User user, Integer terminal) {
        // 生成token
        UserSession session = BeanUtils.copyProperties(user, UserSession.class);
        session.setUserId(user.getId());
        session.setTerminal(terminal);
        String strJson = JSON.toJSONString(session);
        String accessToken = JwtUtil.sign(user.getId(),strJson,jwtProperties.getAccessTokenExpireIn(),jwtProperties.getAccessTokenSecret());
        String refreshToken = JwtUtil.sign(user.getId(),strJson,jwtProperties.getRefreshTokenExpireIn(),jwtProperties.getRefreshTokenSecret());
        LoginVO vo = new LoginVO();
        vo.setAccessToken(accessToken);
        vo.setAccessTokenExpiresIn(jwtProperties.getAccessTokenExpireIn());
        vo.setRefreshToken(refreshToken);
        vo.setRefreshTokenExpiresIn(jwtProperties.getRefreshTokenExpireIn());
        return vo;
    }
}
