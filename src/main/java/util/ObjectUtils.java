package util;

import java.lang.reflect.Field;
import org.apache.commons.lang3.reflect.FieldUtils;

public class ObjectUtils extends org.apache.commons.lang3.ObjectUtils {

    public static <T> T loadField(Object object, String fieldName) {
        try {
            Class<?> clazz = object.getClass();
            Field field = null;
            while(field == null && clazz != null){
                field = FieldUtils.getDeclaredField(clazz, fieldName, true);
                clazz = clazz.getSuperclass();
            }
            field.setAccessible(true);
            return (T) field.get(object);
        } catch (IllegalArgumentException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public static void writeField(Object object, String fieldName, Object fieldValue) {
        try {
            Class<?> clazz = object.getClass();
            Field field = null;
            while(field == null && clazz != null){
                field = FieldUtils.getDeclaredField(clazz, fieldName, true);
                clazz = clazz.getSuperclass();
            }
            field.setAccessible(true);
            field.set(object, fieldValue);
        } catch (IllegalArgumentException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public static <T extends Comparable<? super T>> T minInTwo(T a, T b) {
        return compare(a, b) <= 0 ? a : b;
    }

    public static <T extends Comparable<? super T>> T maxInTwo(T a, T b) {
    	return compare(a, b) >= 0 ? a : b;
    }

    public static <T extends Comparable<? super T>> Pair<T,T> minMax(T a, T b) {
        if (compare(a, b) <= 0) {
            return new Pair<>(a, b);
        }
        return new Pair<>(b, a);
    }

    public static <T extends Enum<?>> T getEnumValue(Class<T> clazz, String name, boolean throwExceptionNotFound) {
        for(T enumConstant :clazz.getEnumConstants()){
            if(enumConstant.name().equals(name)){
                return enumConstant;
            }
        }
        if(throwExceptionNotFound)
        	throw new IllegalArgumentException("enum type "+name+" not found in" +clazz.getName());
        return null;
    }
	public static <T extends Enum<?>> T getEnumValue(Class<T> clazz, String name){
		return getEnumValue(clazz, name, false);
	}
}
