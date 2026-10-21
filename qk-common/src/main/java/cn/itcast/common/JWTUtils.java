package cn.itcast.common;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.Map;

@Component
public class JWTUtils {
    @Value("${token.expire}")
    private long expire;
    @Value("${token.signature}")
    private String signature;

    public Map<String, Object> parseToken(String token) {

        Jws<Claims> jws = Jwts.parser()
                .setSigningKey(signature.getBytes())
                .parseClaimsJws(token);
        Claims body = jws.getBody();

        return body;
    }

    public String makeToken(Map<String, Object> claims) {
        String token = Jwts.builder()
                // 自定义载荷
                .setClaims(claims)
                // 签发时间
                .setIssuedAt(new Date())
                // 签名算法+密钥
                .signWith(SignatureAlgorithm.HS256, signature.getBytes())
                // 过期时间
                .setExpiration(new Date(System.currentTimeMillis() + expire))
                .compact();
        return token;
    }

}
