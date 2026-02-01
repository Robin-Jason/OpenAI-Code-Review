package org.example.types.utils;

import java.util.Random;

public class RandomStringUtils {

    /**
     * 生成指定长度的随机字符串。
     *
     * @param length 长度
     * @return 随机字符串
     */
    public static String randomNumeric(int length) {
        String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        Random random = new Random();
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(characters.charAt(random.nextInt(characters.length())));
        }
        return sb.toString();
    }

}
