package com.snxy.pay.util;

import com.alibaba.fastjson.JSON;
import com.snxy.common.exception.BizException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.beanutils.BeanUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * Created by lvhai on 2018/11/9.
 */
@Slf4j
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

    public  static   byte[] getReqByte(Map<String,String> map,String key) throws IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        // 去掉Map中参数，然后进行sign
        Map<String, String> filterParaMap = SignUtils.paraFilter(map);
        StringBuilder sb = new StringBuilder();
        SignUtils.buildPayParams(sb,filterParaMap,false);
        String sign = MD5.sign(sb.toString(),"&key=" + key,"utf-8").toUpperCase();
        map.put("sign",sign);
        log.info("afterSignMap  : [{}]",map);
        // 请求字符串
        String reqStr = XmlUtils.parseXML(map);
        return reqStr.getBytes();
    }



    public static  <T> T  parse2Bean(String resultStr,Class<T> targetObj) throws Exception{
        T bean = null;
        if(resultStr != null && resultStr.startsWith("<xml")){
            Map<String,String> resultMap = XmlUtils.xml2map(resultStr, "xml");
            log.info("resultMap : [{}]",resultMap);
            bean = targetObj.newInstance();
            bean = map2Bean(resultMap,targetObj);
            if(bean != null){
                log.info("wxPayResp JSON  :  [{}]", JSON.toJSONString(bean));
            }else{
                throw new BizException("map 转bean 出错");
            }
        }
        return bean;
    }

    public  static  <T> T map2Bean(Map<String, String> map, Class<T> targetObj)throws Exception {
        T bean =  targetObj.newInstance();
        BeanUtils.populate(bean, map);
        return bean;
    }


}
