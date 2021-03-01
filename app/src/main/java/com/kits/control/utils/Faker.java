package com.kits.control.utils;

import java.io.UnsupportedEncodingException;
import java.util.Random;

public class Faker {

    public static String getRandomWord(int min,int max){
        int count = (int) (Math.random()*(max-min)+min);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i< count;i++){
            sb.append(getRandomHz());
        }
        return sb.toString();
    }

    public static String getRandomHz() {
        String str = "";
        int hightPos;
        int lowPos;

        Random random = new Random();

        hightPos = (176 + Math.abs(random.nextInt(39)));
        lowPos = (161 + Math.abs(random.nextInt(93)));

        byte[] b = new byte[2];
        b[0] = (Integer.valueOf(hightPos)).byteValue();
        b[1] = (Integer.valueOf(lowPos)).byteValue();

        try {
            str = new String(b, "GBK");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            System.out.println("错误");
        }
        return str;
    }
}
