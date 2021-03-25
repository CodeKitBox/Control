package com.kits.control;

import org.junit.Test;

public class SimpleTest {
    @Test
    public void test1(){
        MyExtendB extendB = new MyExtendB();
        if (extendB instanceof MyBase){
            System.out.println("===extendB instanceof MyBase===");
        }else {
            System.out.println("===extendB not instanceof MyBase===");
        }
    }
}
