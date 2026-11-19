package com.sharpcrm;

import com.sharpcrm.common.JWTUtils;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.SignatureException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * 纯单元测试：直接实例化 JWTUtils 并注入配置，不加载 Spring 容器、不依赖数据库。
 */
class JWTUtilsTest {

    private JWTUtils jwtUtils;

    @BeforeEach
    void setUp() {
        jwtUtils = new JWTUtils();
        ReflectionTestUtils.setField(jwtUtils, "expire", 60_000L);
        ReflectionTestUtils.setField(jwtUtils, "signature", "sharp-crm-secret");
    }

    @Test
    void makeAndParseTokenRoundTrip() {
        Map<String, Object> claims = new HashMap<>();
        claims.put("username", "zhangsan");
        claims.put("userId", "1");

        String token = jwtUtils.makeToken(claims);

        assertNotNull(token);
        Map<String, Object> parsed = jwtUtils.parseToken(token);
        assertEquals("zhangsan", parsed.get("username"));
        assertEquals("1", parsed.get("userId"));
    }

    @Test
    void parseTokenWithWrongSignatureThrows() {
        Map<String, Object> claims = new HashMap<>();
        claims.put("username", "lisi");
        String token = jwtUtils.makeToken(claims);

        ReflectionTestUtils.setField(jwtUtils, "signature", "wrong-secret");

        assertThrows(SignatureException.class, () -> jwtUtils.parseToken(token));
    }

    @Test
    void parseExpiredTokenThrows() {
        ReflectionTestUtils.setField(jwtUtils, "expire", -1000L);
        String token = jwtUtils.makeToken(new HashMap<>());

        assertThrows(ExpiredJwtException.class, () -> jwtUtils.parseToken(token));
    }
}
