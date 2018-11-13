package com.weijuju.iag.excel.utils;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * 工具类
 *
 * @author 89005691
 * @create 2018-11-12 16:48
 */
@Slf4j
public class CommUtils {

    private CommUtils(){

    }

    public static final String[] RANDOMS =  {"0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "a", "b", "c", "d", "e", "f", "g", "h", "i", "j", "k", "l", "m", "n", "o", "p", "q", "r", "s", "t", "u", "v", "w", "x", "y", "z", "A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z"};

    public static final String FORMAT_YYYY_MM_DD_HH_MM_SS = "yyyy-MM-dd HH:mm:ss";

    public static final String FORMAT_YYYY_MM_DD_HH_MM = "yyyy-MM-dd HH:mm";

    public static final String FORMAT_YYYY_MM_DD_HH = "yyyy-MM-dd HH";

    public static final String FORMAT_YYYY_MM_DD = "yyyy-MM-dd";

    public static final String FORMAT_YYYY_MM = "yyyy-MM";

    public static final String FORMAT_YYYY = "yyyy";

    /**
     * list为空
     */
    public static boolean listIsNull(List list) {

        if (list == null || list.isEmpty()) {
            return true;
        }

        return false;
    }

    /**
     * set不为空
     */
    public static boolean setIsNotNull(Set set) {

        if (set != null && !set.isEmpty()) {
            return true;
        }

        return false;
    }

    /**
     * list不为空
     */
    public static boolean listIsNotNull(List list) {

        if (list != null && !list.isEmpty()) {
            return true;
        }

        return false;
    }

    /**
     * map不为空
     */
    public static boolean mapIsNotNull(Map obj) {

        if (obj != null && obj.size() > 0) {
            return true;
        }

        return false;
    }

    /**
     * 生成时间
     */
    public static Date generateTime() {

        return Calendar.getInstance().getTime();
    }

    /**
     * 生成年初时间
     */
    public static Date generateYearStartDay() {

        Calendar currCal = Calendar.getInstance();
        int currentYear = currCal.get(Calendar.YEAR);
        return getYearFirst(currentYear);
    }

    /**
     * 获取某年第一天日期
     */
    public static Date getYearFirst(int year) {
        Calendar calendar = Calendar.getInstance();
        calendar.clear();
        calendar.set(Calendar.YEAR, year);
        return calendar.getTime();
    }

    /**
     * 生成年末时间
     */
    public static Date generateYearEndDay() {

        Calendar currCal = Calendar.getInstance();
        int currentYear = currCal.get(Calendar.YEAR);
        return getYearLast(currentYear);
    }

    /**
     * 获取某年最后一天日期
     */
    public static Date getYearLast(int year) {
        Calendar calendar = Calendar.getInstance();
        calendar.clear();
        calendar.set(Calendar.YEAR, year + 1);
        calendar.add(Calendar.SECOND, -1);

        return calendar.getTime();
    }

    /**
     * 生成查询起始时间
     */
    public static Date generateStartTime(String time) throws Exception {

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(CommUtils.FORMAT_YYYY_MM_DD);

        return simpleDateFormat.parse(time);
    }

    /**
     * 生成查询结束时间
     */
    public static Date generateEndTime(String time) {

        try {

            time += " 23:59:59";

            SimpleDateFormat simpleDateFormat = new SimpleDateFormat(CommUtils.FORMAT_YYYY_MM_DD_HH_MM_SS);

            return simpleDateFormat.parse(time);
        } catch (Exception e) {

            log.error("转换格式出错", e);
            return null;
        }
    }

    /**
     * 转换时间为文本
     */
    public static String changeTimeByParam(Date time, String format) {

        if (time == null) {

            return "";
        }

        try {

            SimpleDateFormat simpleDateFormat = new SimpleDateFormat(format);

            return simpleDateFormat.format(time);
        } catch (Exception e) {

            log.error("转换格式出错", e);
            return "";
        }
    }

    /**
     * 生成uuid
     *
     * @return
     */
    public static String generateUUID() {

        return UUID.randomUUID().toString().replaceAll("-", "");
    }

    /**
     * 生成任意位随机数
     */
    public static String generateCountRand(int count) {

        Random random = new Random();

        StringBuilder builder = new StringBuilder();

        for (int i = 0; i < count; i++) {
            int randCount = random.nextInt(RANDOMS.length - 1);
            builder.append(RANDOMS[randCount]);
        }

        return builder.toString();
    }

    /**
     * 获取yyyymm
     */
    public static String generateTimeByYYYYMM() throws Exception {

        Date time = generateTime();

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMM");

        return simpleDateFormat.format(time);
    }

    public static boolean stringIsLong(String content) {

        try {

            Long.parseLong(content);
        } catch (Exception e) {

            return false;
        }

        return true;
    }

    public static boolean stringIsInt(String content) {

        try {

            Integer.parseInt(content);
        } catch (Exception e) {

            return false;
        }

        return true;
    }

    /**
     * 是否可以转为数值
     *
     * @param content
     * @return
     */
    public static boolean stringIsNumber(String content) {

        try {

            new BigDecimal(content);
        } catch (Exception e) {

            return false;
        }

        return true;
    }

    /**
     * 校验是否是日期
     * @param str
     * @return
     */
    public static Date isValidDate(String str, String template) {

        Date result = null;
        // 指定日期格式为yyyy-MM-dd HH:mm:ss
        SimpleDateFormat format = new SimpleDateFormat(template);
        try {

            // 宽松地验证日期
            format.setLenient(false);
            result = format.parse(str);
        } catch (ParseException e) {

            result = null;
            log.error("校验是否是日期出错", e);
        }

        return result;
    }

    public static List<Long> generateByLongArrays(String str, String patten){

        List<Long> result = new ArrayList<>();

        if(StringUtils.isNotEmpty(str)){

            List<String> ids = Arrays.asList(str.split(patten));

            for(String temp : ids){

                result.add(Long.parseLong(temp));
            }
        }

        return result;
    }

    public static List<Integer> generateByIntArrays(String str){

        List<Integer> result = new ArrayList<>();

        if(StringUtils.isNotEmpty(str)){

            List<String> ids = Arrays.asList(str.split(","));

            for(String temp : ids){

                result.add(Integer.parseInt(temp));
            }
        }

        return result;
    }

    public static String formatBeforeTime(Date date) {

        // 获取当前时间
        Long now = System.currentTimeMillis();
        Long formatDate = date.getTime();
        long l = now - formatDate;
        long day = l / (24 * 60 * 60 * 1000);
        long hour = (l / (60 * 60 * 1000) - day * 24);
        long min = ((l / (60 * 1000)) - day * 24 * 60 - hour * 60);
        long s = (l / 1000 - day * 24 * 60 * 60 - hour * 60 * 60 - min * 60);

        String patten = "";

        if (day > 0) {
            patten = day + "天前";
        } else if (hour > 0) {
            patten = hour + "小时前";
        } else if (min > 0) {

            patten = min + "分钟前";
        } else if (s > 0) {
            patten = s + "秒前";
        }

        return patten;
    }
}
