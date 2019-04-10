package com.bc.utils.project;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.databind.util.BeanUtil;
import org.springframework.beans.BeanUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by mrt on 2019/4/8 0008 下午 4:15
 */
public class MyBeanUtil {

    //对象间的属性的复制
    public static void copyProperties(Object source, Object target) {
        BeanUtils.copyProperties(source, target);
    }
    //对象间的属性的复制，忽略一些属性
    public static void copyProperties(Object source, Object target,String... ignoreProperties) {
        BeanUtils.copyProperties(source, target,ignoreProperties);
    }
    //对象转换成map
    public static Map<String,Object> copyToMap(Object bean){
        return com.baomidou.mybatisplus.core.toolkit.BeanUtils.beanToMap(bean);
    }
    //map转换成bean
    public static <T> T mapToBean(Map<String, Object> map, Class<T> clazz){
        return com.baomidou.mybatisplus.core.toolkit.BeanUtils.mapToBean(map, clazz);
    }
    //maps转换成List<bean>
    public static <T> List<T> mapsToBeans(List<Map<String, Object>> maps, Class<T> clazz) {
        return com.baomidou.mybatisplus.core.toolkit.BeanUtils.mapsToBeans(maps, clazz);
    }
    //List<bean>转换成maps
    public static <T> List<Map<String, Object>> beansToMaps(List<T> beans) {
        return com.baomidou.mybatisplus.core.toolkit.BeanUtils.beansToMaps(beans);
    }
    //List<bean1>转换成List<bean2>
    public static <V> List<V> copyListToList(List beans,Class<V> clazz) {
        List<V> list = new ArrayList<>();
        try {
            for (Object bean : beans) {
                V v = clazz.newInstance();
                copyProperties(bean, v);
                list.add(v);
            }
            return list;
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return null;
    }

    //复制page
    public static <T> Page<T> copyPageToPage(Page page, Class<T> clazz) {
        Page<T> cPage = new Page();
        cPage.setSize(page.getSize());
        cPage.setTotal(page.getTotal());
        cPage.setCurrent(page.getCurrent());
        cPage.setRecords(copyListToList(page.getRecords(), clazz));
        return cPage;
    }
}
