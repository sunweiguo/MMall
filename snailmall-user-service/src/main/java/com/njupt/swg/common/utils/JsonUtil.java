package com.njupt.swg.common.utils;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.SerializationConfig;
import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.codehaus.jackson.type.JavaType;
import org.codehaus.jackson.type.TypeReference;

import java.io.IOException;
import java.text.SimpleDateFormat;

/**
 * jackson的序列化和反序列化
 */
@Slf4j
public class JsonUtil {
    private static ObjectMapper objectMapper = new ObjectMapper();

    static {
        //所有字段都列入进行转换
        objectMapper.setSerializationInclusion(JsonSerialize.Inclusion.ALWAYS);
        //取消默认转换timestamp形式
        objectMapper.configure(SerializationConfig.Feature.WRITE_DATES_AS_TIMESTAMPS,false);
        //忽略空bean转json的错误
        objectMapper.configure(SerializationConfig.Feature.FAIL_ON_EMPTY_BEANS,false);
        //统一时间的格式
        objectMapper.setDateFormat(new SimpleDateFormat(DateTimeUtil.STANDARD_FORMAT));
        //忽略json存在属性，但是java对象不存在属性的错误
        objectMapper.configure(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES,false);
    }

    /**
     * 序列化方法，将对象转为字符串
     * @param obj
     * @param <T>
     * @return
     */
    public static <T> String obj2String(T obj){
        if(obj == null){
            return null;
        }
        try {
            return obj instanceof String ? (String) obj : objectMapper.writeValueAsString(obj);
        } catch (IOException e) {
            log.warn("parse object to string error",e);
            return null;
        }
    }

    /**
     * 序列化方法，同上，只是输出的格式是美化的，便于测试
     * @param obj
     * @param <T>
     * @return
     */
    public static <T> String obj2StringPretty(T obj){
        if(obj == null){
            return null;
        }
        try {
            return obj instanceof String ? (String) obj : objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(obj);
        } catch (IOException e) {
            log.warn("parse object to string error",e);
            return null;
        }
    }

    /**
     * 比较简单的反序列化的方法，将字符串转为单个对象
     * @param str
     * @param clazz
     * @param <T>
     * @return
     */
    public static <T> T String2Obj(String str,Class<T> clazz){
        if(StringUtils.isEmpty(str) || clazz == null){
            return null;
        }
        try {
            return clazz.equals(String.class)?(T)str:objectMapper.readValue(str,clazz);
        } catch (IOException e) {
            log.warn("parse string to obj error",e);
            return null;
        }
    }

    /**
     * 复杂对象的反序列化（通用）
     * @param str
     * @param typeReference
     * @param <T>
     * @return
     */
    public static <T> T Str2Obj(String str, TypeReference typeReference){
        if(StringUtils.isEmpty(str) || typeReference == null){
            return null;
        }
        try {
            return (T) (typeReference.getType().equals(String.class)?str:objectMapper.readValue(str,typeReference));
        } catch (IOException e) {
            log.warn("parse string to obj error",e);
            return null;
        }
    }

    /**
     * 第二种方式实现复杂对象的反序列化
     * @param str
     * @param collectionClass
     * @param elementClasses
     * @param <T>
     * @return
     */
    public static <T> T Str2Obj(String str,Class<?> collectionClass,Class<?>... elementClasses){
        JavaType javaType = objectMapper.getTypeFactory().constructParametricType(collectionClass,elementClasses);
        try {
            return objectMapper.readValue(str,javaType);
        } catch (IOException e) {
            log.warn("parse string to obj error",e);
            return null;
        }
    }
}
