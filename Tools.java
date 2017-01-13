package com.demo;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

public class Tools {
	/*
	 * 检索出含有特定字符串的Method对象，并以List形式返回
	 */
	public List<Method> getMethodList(Method[] methods, String contains) {
		List<Method> method_list = new ArrayList<Method>();
		for (Method m : methods) {
			if (m.getName().contains("getClass")) {
				continue;
			}

			if (m.getName().contains(contains)) {
				method_list.add(m);
			}
		}

		return method_list;
	}

	/*
	 * 返回Class对应的数据库表明 默认类名与数据库表同名 如果，不同名，则类定义，需加MyTable注解
	 */
	public String getTableName(Class cls) {
		String table = "";
		if (cls.isAnnotationPresent(MyTable.class)) {
			MyTable anno = (MyTable) cls.getAnnotation(MyTable.class);
			table = anno.name();
		} else {
			table = cls.getName();
			table = table.substring(table.lastIndexOf(".") + 1);
		}

		return table;
	}

	/*
	 * 获取主键值——bean。getId()的返回值
	 */
	public int getKeyId(Object obj) {
		Class cls = obj.getClass();
		int ret = 0;
		try {
			Method m = cls.getMethod("getId", new Class[0]);
			ret = (int) m.invoke(obj);
		} catch (NoSuchMethodException | SecurityException
				| IllegalAccessException | IllegalArgumentException
				| InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return ret;
	}

	/*
	 * 返回属性名对应的set方法名
	 */
	public String name2Set(String name) {
		StringBuilder sb = new StringBuilder(name);
		sb.setCharAt(0, Character.toUpperCase(sb.charAt(0)));
		return "set"+ sb.toString();
	}
	
	/*
	 * 返回属性名对应的get方法名
	 */
	public String name2Get(String name) {
		StringBuilder sb = new StringBuilder(name);
		sb.setCharAt(0, Character.toUpperCase(sb.charAt(0)));
		return "get"+ sb.toString();
	}
	
	/*
	 * 根据函数名，检索并返回Method对象
	 */
	public Method getMethod(Class cls, String method_name) {
		Method m = null;
		Boolean isFind = true;
		Class[] clss = new Class[] { String.class, int.class };

		for (int i = 0; i < clss.length; i++) {
			try {
				m = cls.getMethod(method_name, clss[i]);
			} catch (SecurityException e) {
				isFind = false;
			} catch (NoSuchMethodException e) {
				isFind = false;
			}

			if (isFind) {
				return m;
			}
		}

		return m;
	}
	
	/*
	 * 根据MyBeans注解，获取Class所包含的所有bean，并为每一个bean生成一个Object实例，压入List，返回
	 * 
	 * 返回值：存放一个cls对象实例的、所有bean成员实例的list容器
	 */
	public List<Object> getInnerBeans(Class cls) {
		List<Object> list = new ArrayList<Object>();
		if (cls.isAnnotationPresent(MyBean.class)) {
			MyBean anno = (MyBean) cls.getAnnotation(MyBean.class);
			Class[] clses = anno.classes();
			
			for (Class c : clses)
			{
				try {
					Object o = c.newInstance();
					list.add(o);
				} catch (InstantiationException | IllegalAccessException e) {
					e.printStackTrace();
				}
			}
		}
		
		return list;
	}
	
	/*
	 * T obj: 接收List<T>数据的一个T对象
	 * List<T> inner_bean_list:为一个T对象里所有bean成员实例的list容器
	 */
	public <T> void setObj(T obj, List<T> obj_T_members_list) {
		Class cls = obj.getClass();
		
		for (Object o : obj_T_members_list) {
			String name = o.getClass().getName();
			name = name.substring(name.lastIndexOf(".")+1);
			name = name2Set(name);
			
			try {
				Method m = cls.getMethod(name, o.getClass());
				m.invoke((Object)obj, o);
			} catch (NoSuchMethodException | SecurityException 
					| IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
				e.printStackTrace();
			}
		}
	}
	
	/*
	 * 执行一个Method方法
	 */
	public void runMethod(Method m, Object owner_obj, Object para_obj) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException
	{
		if (para_obj != null)
		{
			m.invoke(owner_obj, para_obj);
		}
	}
}
