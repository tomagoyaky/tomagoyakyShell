package com.tomagoyaky.common;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Field;

public class RefInvokeUtil {
	/**
	 * 反射执行类的静�?�函�?(public)
	 * @param class_name	类名
	 * @param method_name	函数�?
	 * @param pareTyple		函数的参数类�?
	 * @param pareVaules	调用函数时传入的参数
	 * @return
	 */
	public static  Object invokeStaticMethod(String class_name, String method_name, Class[] pareTyple, Object[] pareVaules){
		
		try {
			Class obj_class = Class.forName(class_name);
			Method method = obj_class.getMethod(method_name,pareTyple);
			
			return method.invoke(null, pareVaules);
			
		} catch (SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}  catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
		
	}
	
	/**
	 * 反射执行类的函数（public�?
	 * @param class_name
	 * @param method_name
	 * @param obj
	 * @param pareTyple
	 * @param pareVaules
	 * @return
	 */
	public static  Object invokeMethod(String class_name, String method_name, Object obj ,Class[] pareTyple, Object[] pareVaules){
		
		try {
			Class obj_class = Class.forName(class_name);
			Method method = obj_class.getMethod(method_name,pareTyple);
			return method.invoke(obj, pareVaules);
		} catch (SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}  catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
		
	}
	
	/**
	 * 反射得到类的属�?�（包括私有和保护）
	 * @param class_name
	 * @param obj
	 * @param filedName
	 * @return
	 */
	public static Object getFieldObject(String class_name,Object obj, String filedName){
		try {
			Class obj_class = Class.forName(class_name);
			Field field = obj_class.getDeclaredField(filedName);
			field.setAccessible(true);
			return field.get(obj);
		} catch (SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchFieldException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
		
	}
	
	/**
	 * 反射得到类的静�?�属性（包括私有和保护）
	 * @param class_name
	 * @param filedName
	 * @return
	 */
	public static Object getStaticFieldOjbect(String class_name, String filedName){
		
		try {
			Class obj_class = Class.forName(class_name);
			Field field = obj_class.getDeclaredField(filedName);
			field.setAccessible(true);
			return field.get(null);
		} catch (SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchFieldException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
		
	}
	
	/**
	 * 设置类的属�?�（包括私有和保护）
	 * @param classname
	 * @param filedName
	 * @param obj
	 * @param filedVaule
	 */
	public static void setFieldObject(String classname, String filedName, Object obj, Object filedVaule){
		try {
			Class<?> obj_class = Class.forName(classname);
			Field field = obj_class.getDeclaredField(filedName);
			field.setAccessible(true);
			field.set(obj, filedVaule);
		} catch (SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchFieldException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}	
	}
	
	/**
	 * 设置类的静�?�属性（包括私有和保护）
	 * @param class_name
	 * @param filedName
	 * @param filedVaule
	 */
	public static void setStaticOjbect(String class_name, String filedName, Object filedVaule){
		try {
			Class obj_class = Class.forName(class_name);
			Field field = obj_class.getDeclaredField(filedName);
			field.setAccessible(true);
			field.set(null, filedVaule);
		} catch (SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchFieldException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
	}

}
