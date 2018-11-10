package com.snxy.pay.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * Created by lvhai on 2018/11/9.
 */
public class CommonUtils {
    private static final Logger LOG = LoggerFactory.getLogger(CommonUtils.class);

    public static <T> SortedMap<String, String> Objet2SortedMap(T parameter, Class<T> tclass, String... ignoreFields) {
        SortedMap<String, String> map = new TreeMap<String, String>();
        Field[] fields = tclass.getDeclaredFields();
        Arrays.stream(fields).forEach((Field field) -> {
            field.setAccessible(true);
            boolean addToMap = true;
//                    !Arrays.stream(defaultIgnoreFields)
//                    .anyMatch(ignoreField -> ignoreField.equals(field.getName()));

            if (ignoreFields != null && ignoreFields.length > 0) {

                addToMap = !Arrays.stream(ignoreFields)
                        .anyMatch(ignoreField -> ignoreField.equals(field.getName()));
            }

            if (addToMap == true) {
                try {
                    Object fieldVal = field.get(parameter);
                    String addVal = "";
                    if (fieldVal != null) {
                        addVal = fieldVal.toString();
                    }
                    map.put(field.getName(), addVal);
                } catch (Exception e) {
                    LOG.error("不能获取字段{}的值,异常原因{}", field.getName(), e.getMessage());
                }

            }
        });
        return map;
    }
}
