package com.chris.robot_server.util;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * @author Chris
 * @date 9/19/21 10:03 PM
 */
@Slf4j
public class GetMd5 {

    /**
     * 获取文本字段的MD5值
     * @param txt
     * @return
     */
    public static String getMd5(String txt){
        //1.去除符号
        txt = removeAllPunctuation(txt);
        //2.MD5计算
        String rs = "";
        String[] hexDigits = { "0", "1", "2", "3", "4", "5", "6", "7", "8","9", "a", "b", "c", "d", "e", "f" };
        try {
            MessageDigest messageDigest = MessageDigest.getInstance("MD5");
            byte[] b = messageDigest.digest(txt.getBytes());
            StringBuilder resultSb = new StringBuilder();
            for (int j : b) {
                int n = j;
                if (n < 0)
                    n = 256 + n;
                int d1 = n / 16;
                int d2 = n % 16;
                resultSb.append(hexDigits[d1]).append(hexDigits[d2]);
            }
            rs = resultSb.toString();
        } catch (NoSuchAlgorithmException e) {
            // e.printStackTrace();
            log.error("计算文本MD5失败，{}", e.getMessage());
        }
        return rs;
    }

    /**
     * 去除字符串中所有的标点符号、数字
     * @param text
     * @return
     */
    public static String removeAllPunctuation(String text){
        if(StringUtils.isNotEmpty(text)) {
            return text.replaceAll("\\pP|\\pS|\\pC|\\pN|\\pZ", "");
        }
        return text;
    }
}
