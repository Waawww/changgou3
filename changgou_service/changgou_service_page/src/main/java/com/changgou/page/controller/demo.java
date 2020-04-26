package com.changgou.page.controller;

import java.lang.reflect.Method;

public class demo {

    public static void main(String[] args) throws Exception {
        Person p = new Person("zs", 20);
        Set(p, "age", 35);
        System.out.println(p);
    }


    private static void Set(Object obj, String propertyName, Object value) throws Exception {
        // TODO Auto-generated method stub
        Class c = obj.getClass();

        String className = "set" + propertyName.substring(0, 1).toUpperCase() +
                propertyName.substring(1, propertyName.length());

        System.out.println(Object.class);

        Method m = c.getMethod(className, Object.class);
        m.invoke(obj, value);


    }
}
