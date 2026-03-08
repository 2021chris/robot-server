package com.chris.robot_server.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.Period;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Date;

/**
 * @author Chris
 * @date 9/21/21 1:54 PM
 */
public class DateUtil {

    /**
     * 获取当前北京时间
     * @return
     */
    public static Date ZonedBeijingNowDateTime() {
        Instant nowUtc = Instant.now();

        // 转换为北京时间
        ZonedDateTime beijingTime = nowUtc.atZone(ZoneId.of("Asia/Shanghai"));

        // 如果需要调整（例如，只取日期或特定时间），这里可以操作 beijingTime

        // 转换为 java.util.Date（内部是 UTC 时间戳，表示北京当前时刻）
        Date newTime = Date.from(beijingTime.toInstant());

        return newTime;
    }

    public static String calcDiffDate(Date start, Date end){
        long diff = end.getTime() - start.getTime();
        long days = (long)(diff / (1000 * 60 * 60 * 24));
        long hours = (long)(diff / (1000 * 60 * 60)) % 24;
        long minutes = (long)(diff / (1000 * 60)) % 60;
        long seconds = (long)(diff / (1000)) % 60;
        long ms = (long)(diff % 1000);
        return String.format("%s天，%s小时，%s分钟，%s秒，%s毫秒", days, hours, minutes, seconds, ms);
    }

    public static String getTodayStr() {
        LocalDate today = LocalDate.now();
        // 定义日期格式
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        // 格式化当前日期并返回
        return today.format(formatter);
    }


    /**
     * MM/dd/yyyy格式字符串转成yyyy-MM-dd格式date数据
     * @param birthday
     * @return
     */
    public static Date stringToDate(String birthday){
        // 定义输入和输出的日期格式
        SimpleDateFormat inputFormat = new SimpleDateFormat("MM/dd/yyyy");
        SimpleDateFormat outputFormat = new SimpleDateFormat("yyyy-MM-dd");

        // 解析输入字符串为Date对象
        try {
            Date date = inputFormat.parse(birthday);
            // 用输出格式格式化Date对象
            String outputDateStr = outputFormat.format(date);

            // 将字符串转换回Date对象（如果需要Date类型输出）
            return outputFormat.parse(outputDateStr);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }


    public static int calculateAge(String birthDateString) {
        // 定义日期格式
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");

        try {
            // 解析出生日期字符串为 LocalDate 对象
            LocalDate birthDate = LocalDate.parse(birthDateString, formatter);

            // 获取当前日期
            LocalDate currentDate = LocalDate.now();

            // 计算两个日期之间的期间，并获取年份差作为年龄
            return Period.between(birthDate, currentDate).getYears();
        } catch (DateTimeParseException e) {
            // 如果日期字符串格式不正确，抛出异常或处理错误
            System.err.println("日期格式不正确: " + birthDateString);
            return -1; // 返回一个错误指示值，或者你可以选择抛出异常
        }
    }
}
