package cn.itcast;


import cn.itcast.common.JWTUtils;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

@SpringBootTest
public class JWTUtilsTest {

    // 测试生成JWT
    @Autowired
    private JWTUtils jwtUtils;


    @Test
    public void testMakeToken() throws UnsupportedEncodingException {
        Map<String,Object> claims = new HashMap<>();
        claims.put("username","zhangsan");
        claims.put("password","123456");
        String token = jwtUtils.makeToken(claims);
        System.out.println("生成token："+token);
    }



    @Test
    public void testParseToken() throws Exception {
        String token = "eyJhbGciOiJIUzI1NiJ9.eyJwYXNzd29yZCI6IjEyMzQ1NiIsImV4cCI6MjEwMzEwNzQ5MSwiaWF0IjoxNzkyMDY3NDkwLCJ1c2VybmFtZSI6InpoYW5nc2FuIn0.5uNnrj2NeWou1H6S9mGxfMD0IXW9DIhweDLMETHgegU";
        Map<String,Object> map = jwtUtils.parseToken(token);
        System.out.println("解析得到载荷："+map);
    }



}
