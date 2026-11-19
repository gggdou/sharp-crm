package com.sharpcrm.common;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.util.Base64;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 图形验证码生成工具：输出 Base64 图片与验证码文本
 */
public final class VerifyCodeUtils {

    private static final char[] CHARS = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789".toCharArray();
    private static final int WIDTH = 120;
    private static final int HEIGHT = 40;

    private VerifyCodeUtils() {
    }

    /**
     * 生成验证码，返回 [0]=Base64图片(data:image/png;base64,...), [1]=验证码文本
     */
    public static String[] generate() {
        BufferedImage image = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = image.createGraphics();
        try {
            g.setColor(Color.WHITE);
            g.fillRect(0, 0, WIDTH, HEIGHT);

            StringBuilder code = new StringBuilder(4);
            ThreadLocalRandom random = ThreadLocalRandom.current();
            for (int i = 0; i < 4; i++) {
                char ch = CHARS[random.nextInt(CHARS.length)];
                code.append(ch);
                g.setColor(new Color(random.nextInt(100), random.nextInt(100), random.nextInt(100)));
                g.setFont(new Font("Arial", Font.BOLD, 24));
                g.drawString(String.valueOf(ch), 20 + i * 25, 28 + random.nextInt(5));
            }
            // 干扰线
            for (int i = 0; i < 6; i++) {
                g.setColor(new Color(random.nextInt(200), random.nextInt(200), random.nextInt(200)));
                g.drawLine(random.nextInt(WIDTH), random.nextInt(HEIGHT),
                        random.nextInt(WIDTH), random.nextInt(HEIGHT));
            }

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            ImageIO.write(image, "png", out);
            String base64 = "data:image/png;base64," + Base64.getEncoder().encodeToString(out.toByteArray());
            return new String[]{base64, code.toString()};
        } catch (Exception e) {
            throw new RuntimeException("生成验证码失败", e);
        } finally {
            g.dispose();
        }
    }
}
