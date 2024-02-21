package com.example.demo;

import cn.hutool.core.net.URLEncodeUtil;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;

@SpringBootTest
public class AutoPlanApplicationTests {

    public static void main(String[] args) {
        Date date = new Date();

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        System.out.println(simpleDateFormat.format(date));

        String s = "yOM9edJIRhkBylUPZAqVzOgI79gZ1BLpaF5MF9UkbSaLcbol1feOW4koxH5w2EBl0aZglG4N4gdjoZ9RnVqe4yW+hsLZ6Wn/ppMeacRaeqApntFaSImhyHMLe98iCDmU/FaJbJm2xw3r9G0bnw/8IOI8PaY6V1u9b1QfC2tY252o7i0ttFcERguXawr7IEK6HXbCWk0b7Z+UOcjGBAVPSEIRa4nTwWpX8cxLB+No1fethuyQl3XxBvhe2IRV8AOIs0KVocxwQBYcEVLgbNHs6r1KQ9fCN+UIUpgduHAtfxplbH+1Ckmk8Ih/BxtX3nujK0je7+2FU+3ntF4u6G6/EQ==";
        System.out.println(URLEncodeUtil.encode(s));
    }

    @Test
    void test() {
        Random random = new Random();
        int i = random.nextInt(200000 - 100000) + 100000 + 1;
        System.out.println(i);
    }

    /**
     * 随机指定范围内N个不重复的数(不含)
     * 最简单最基本的方法
     * @param min 指定范围最小值
     * @param max 指定范围最大值
     * @param n 随机数个数
     */
    public static int[] randomCommon(int min, int max, int n){
        if (n > (max - min + 1) || max < min) {
            return null;
        }
        int[] result = new int[n];
        int count = 0;
        while(count < n) {
            int num = (int) (Math.random() * (max - min)) + min;
            boolean flag = true;
            for (int j = 0; j < n; j++) {
                if(num == result[j]){
                    flag = false;
                    break;
                }
            }
            if(flag){
                result[count] = num;
                count++;
            }
        }
        return result;
    }

}
