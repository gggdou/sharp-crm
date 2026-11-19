package com.sharpcrm.controller;

import com.sharpcrm.common.RedisCache;
import com.sharpcrm.common.Result;
import com.sharpcrm.common.VerifyCodeUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * 验证码接口（放行登录前调用）
 */
@Tag(name = "验证码", description = "图形验证码生成")
@RestController
public class VerifyCodeController {

    @Autowired
    private RedisCache redisCache;

    @Operation(summary = "生成图形验证码", description = "返回 uuid 与 base64 图片，验证码存 Redis 5 分钟")
    @GetMapping("/captcha")
    public Result captcha() {
        String[] pair = VerifyCodeUtils.generate();
        String uuid = UUID.randomUUID().toString().replace("-", "");
        redisCache.set("captcha:" + uuid, pair[1], 5, TimeUnit.MINUTES);

        Map<String, Object> data = new HashMap<>();
        data.put("uuid", uuid);
        data.put("img", pair[0]);
        return Result.success(data);
    }
}
