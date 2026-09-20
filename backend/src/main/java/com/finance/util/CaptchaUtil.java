package com.finance.util;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.Random;

public class CaptchaUtil {

    private static final char[] CHARS = "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghjkmnpqrstuvwxyz0123456789".toCharArray();
    private static final int WIDTH = 120;
    private static final int HEIGHT = 40;
    private static final int FONT_SIZE = 24;
    private static final int LINE_COUNT = 5;
    private static final int DOT_COUNT = 30;

    public static String generateCode(int length) {
        StringBuilder sb = new StringBuilder();
        Random random = new Random();
        for (int i = 0; i < length; i++) {
            sb.append(CHARS[random.nextInt(CHARS.length)]);
        }
        return sb.toString();
    }

    public static String generateImageBase64(String code) {
        BufferedImage image = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = image.createGraphics();

        // 设置背景色
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, WIDTH, HEIGHT);

        // 设置字体
        Font font = new Font("Arial", Font.BOLD, FONT_SIZE);
        g.setFont(font);

        Random random = new Random();

        // 绘制干扰线
        for (int i = 0; i < LINE_COUNT; i++) {
            int x1 = random.nextInt(WIDTH);
            int y1 = random.nextInt(HEIGHT);
            int x2 = random.nextInt(WIDTH);
            int y2 = random.nextInt(HEIGHT);
            g.setColor(getRandomColor(160, 200));
            g.drawLine(x1, y1, x2, y2);
        }

        // 绘制干扰点
        for (int i = 0; i < DOT_COUNT; i++) {
            int x = random.nextInt(WIDTH);
            int y = random.nextInt(HEIGHT);
            g.setColor(getRandomColor(160, 200));
            g.fillOval(x, y, 2, 2);
        }

        // 绘制验证码字符
        char[] chars = code.toCharArray();
        for (int i = 0; i < chars.length; i++) {
            g.setColor(getRandomColor(50, 150));
            g.drawChars(chars, i, 1, 20 + i * 25, HEIGHT / 2 + 8);
        }

        g.dispose();

        // 将图片转为Base64
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            ImageIO.write(image, "png", baos);
            return "data:image/png;base64," + Base64.getEncoder().encodeToString(baos.toByteArray());
        } catch (IOException e) {
            throw new RuntimeException("验证码图片生成失败", e);
        }
    }

    private static Color getRandomColor(int min, int max) {
        Random random = new Random();
        return new Color(random.nextInt(max - min) + min,
                random.nextInt(max - min) + min,
                random.nextInt(max - min) + min);
    }
}