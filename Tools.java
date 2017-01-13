package com.demo;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

public class Tools {
	/*
	 * �����������ض��ַ�����Method���󣬲���List��ʽ����
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
	 * ����Class��Ӧ�����ݿ���� Ĭ�����������ݿ��ͬ�� �������ͬ�������ඨ�壬���MyTableע��
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
	 * ��ȡ����ֵ����bean��getId()�ķ���ֵ
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
	 * ������������Ӧ��set������
	 */
	public String name2Set(String name) {
		StringBuilder sb = new StringBuilder(name);
		sb.setCharAt(0, Character.toUpperCase(sb.charAt(0)));
		return "set"+ sb.toString();
	}
	
	/*
	 * ������������Ӧ��get������
	 */
	public String name2Get(String name) {
		StringBuilder sb = new StringBuilder(name);
		sb.setCharAt(0, Character.toUpperCase(sb.charAt(0)));
		return "get"+ sb.toString();
	}
	
	/*
	 * ���ݺ�����������������Method����
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
	 * ����MyBeansע�⣬��ȡClass������������bean����Ϊÿһ��bean����һ��Objectʵ����ѹ��List������
	 * 
	 * ����ֵ�����һ��cls����ʵ���ġ�����bean��Աʵ����list����
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
	 * T obj: ����List<T>���ݵ�һ��T����
	 * List<T> inner_bean_list:Ϊһ��T����������bean��Աʵ����list����
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
	 * ִ��һ��Method����
	 */
	public void runMethod(Method m, Object owner_obj, Object para_obj) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException
	{
		if (para_obj != null)
		{
			m.invoke(owner_obj, para_obj);
		}
	}
}
