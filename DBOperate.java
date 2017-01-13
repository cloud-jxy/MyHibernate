package com.demo;
/*
 * 姜学云 2014.07.08
 * 仿Hibernate动态生成SQL代码，减少开发人员的编码量
 */

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.junit.Test;

import com.mysql.jdbc.EscapeTokenizer;
import com.mysql.jdbc.UpdatableResultSet;
import com.sunny.vod.util.DBUtil;

/*
 * 对外隐藏所有含有String table_name参数的API；强制要求类名与表明不一致时，用加注解解决
 * 
 * 几乎所有的API都必须注意如下2点：
 * 1. bean的类名需与数据表同名，如不同需加@MyTable注释
 * 2. bean的所有成员名，需与数据库表的类名完全相同（含大小写）
 */

public class DBOperate {
	private static Tools tools = new Tools();
/****************************************插入API*******************************************************************************/
	private static int add(Object bean, String table_name)
	{
		int key=0;
		Class cls = bean.getClass();
		Method[] methods = cls.getMethods();

		List<Method> get_method_list = tools.getMethodList(methods, "get");
		
		String sql="insert into "+table_name+"(";
		for(Method m : get_method_list) {
			sql += m.getName().substring(3)+", ";
		}
		
		sql = sql.substring(0, sql.length()-2)+") value(";
		
		for(Method m : get_method_list) {
			sql += "?, ";
		}
		
		sql = sql.substring(0, sql.length()-2)+");";
		
		printSQL(sql);
		
		DBUtil util = new DBUtil();
		Connection conn = util.getConnection();
		
		PreparedStatement pstmt = null;
		try {
			pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
			int i = 0;
			for(Method m : get_method_list) {
				i++;
				
				pstmt.setObject(i, m.invoke(bean));
			}
			
			pstmt.execute();
			
			ResultSet rs = pstmt.getGeneratedKeys();
			if (rs.next()) {
				key = rs.getInt(1);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		}finally{
			util.close(conn);
		}
		return key;
	}
	
	/*
	 * 使用前提是，javabean的类名与表明相同，或者类有加MyTable注释
	 */
	public static int add(Object bean) {
		return add(bean, tools.getTableName(bean.getClass()));
	}
/****************************************插入API结束*******************************************************************************/
/****************************************删除API**********************************************************************************/		
	/*
	 * sql delete操作
	 * 删除条件 where id = bean.getId()
	 */
	private static void delete(Object bean, String table_name)
	{
		Class cls = bean.getClass();
		
		DBUtil util = new DBUtil();
		Connection conn = util.getConnection();
		PreparedStatement pstmt = null;
		
		try {
			
			String sql = "delete from "+table_name+" where id=?";
			
			printSQL(sql);
			
			pstmt = conn.prepareStatement(sql);
			pstmt.setInt(1, tools.getKeyId(bean));
			pstmt.execute();
			
		} catch (SQLException | IllegalArgumentException e) {
			e.printStackTrace();
		}  finally{
			util.close(conn);
		}
	}

	/*
	 * 使用delete方法的前提:
	 * 1.javabean的类名与表明相同，或者类有加MyTable注释
	 * 2.Id属性不能为空
	 * 
	 * 删除依据：Id属性
	 */
	public static void delete(Object bean)
	{
		Class cls = bean.getClass();
		String name = tools.getTableName(cls);
		
		delete(bean, name);
	}
/****************************************删除API结束*******************************************************************************/
/****************************************查询API**********************************************************************************/
	/*
	 * 使用前提：javabean中的所有属性名和数据库表完全相同
	 * 如果命名不同而导致使报错，建议使用Eclipse IDE重命名;如有特殊情况，联系我（姜学云）加自定义注释解决
	 * select *
	 */
	public static <T> List<T> getBeans(ResultSet rs, Class cls) {
		
		List<Method> list = tools.getMethodList(cls.getMethods(), "set");
		List<T> ret = new ArrayList<T>();
		
		try {
			while (rs.next()) {
				T obj = (T) cls.newInstance();
				
				for (Method m :list) {
					//去掉set前缀
					//SQL语句不区分大小写，故没做大小写转换
					tools.runMethod(m, obj, rs.getObject(m.getName().substring(3)));
				}
				ret.add(obj);
			}
		} catch (SQLException | InstantiationException | IllegalAccessException 
				| IllegalArgumentException | InvocationTargetException e) {
			e.printStackTrace();
		}
		
		return ret;
	}
	
	/*
	 * 特定列查询的
	 * select name ，age 
	 * 注意：javabean属性名与数据库表列名。大小统一
	 */
	public static <T> List<T> getBeans2(ResultSet rs, Class cls)  {
		
		List<Method> list = tools.getMethodList(cls.getMethods(), "set");
		List<T> ret = new ArrayList<T>();
		
		try {
			while (rs.next()) {
				T obj = (T) cls.newInstance();
				
				ResultSetMetaData rmd = rs.getMetaData();
				for (int i = 1; i <= rmd.getColumnCount(); i++) {
					rmd.getCatalogName(1);
					Method m = tools.getMethod(cls, tools.name2Set(rmd.getColumnName(i)));
					tools.runMethod(m, obj, rs.getObject(i));
				}
				ret.add(obj);
			}
		} catch (SQLException | InstantiationException | IllegalAccessException 
				| IllegalArgumentException | InvocationTargetException e) {
			e.printStackTrace();
		}
		
		return ret;
	}
	
	/*
	 * 用于多表查询联合的API
	 * 注意：
	 * 1.T类必须使用@MyBeans注解——设置T有哪些bean成员
	 * 2.每一个成员bean，需与数据库表同名，如不同需加@MyTable注释
	 * 3。每一个bean的成员对象，与数据库表中的列同名，包括大小写
	 * 4.bean成员名最好为类名首字母小写（建议，不是也不会出错）；但set/get方法名一定要是此名（类名首字母小写）默认的IDE生成名
	 * 如：
		@MyBean(classes = {Testbean.class, User.class})
		public class UniteBean {
			
			private User user;
			
			private Testbean testbean;
			
			public User getUser() {
				return user;
			}
			public void setUser(User user) {
				this.user = user;
			}
			public Testbean getTestbean() {
				return testbean;
			}
			public void setTestbean(Testbean testbean) {
				this.testbean = testbean;
			}
		}
	 */
	public static<T> List<T> getBeans3(ResultSet rs, Class cls) throws IllegalArgumentException, SQLException {
		List<T> list = new ArrayList<T>();
		
		while (rs.next()) {
			//obj_members_list：包含了T对象的所有成员的List容器
			List<Object> obj_T_members_list = (List<Object>) tools.getInnerBeans(cls);
			T obj_T = (T)null;
			try {
				obj_T = (T)cls.newInstance();
			} catch (InstantiationException | IllegalAccessException e) {
				e.printStackTrace();
			}
			
			ResultSetMetaData rmd = rs.getMetaData();
			for (int i = 1; i <= rmd.getColumnCount(); i++) {
				
				for (Object obj_member : obj_T_members_list) {
					//根据查询结果的table属性，从inner_bean_list中找出table对应的bean
					//能正确创建bean实例的条件：满足注释中的注意事项2：
					Class c = obj_member.getClass();
					
					if (tools.getTableName(c).equals(rmd.getTableName(i)))
					{
						//找到bean对象中，利用反射机制，调用对象的set方法赋值
						//正确调用set方法的条件：满足注意事项3.
						String method_name = tools.name2Set(rmd.getColumnName(i));
						Method m = tools.getMethod(c, method_name);
						try {
							tools.runMethod(m, obj_member, rs.getObject(i));
						} catch (IllegalAccessException
								| InvocationTargetException e) {
							e.printStackTrace();
						}
						break;
					}
				}
			}
			
			//反射调用obj中的set方法赋值
			//能正确执行的条件，满足注意事项4.
			tools.setObj(obj_T, obj_T_members_list);
			list.add(obj_T);
		}
		return list;
	}
	/*
	 * 按固定id查询
	 */
	private static <T> List<T> select(int id, Class cls, String table_name) {
		List<T> list = new ArrayList<T>();
		
		DBUtil util = new DBUtil();
		Connection conn = util.getConnection();
		PreparedStatement pstmt = null;
		String sql = "select * from "+table_name+" where id=?";
		ResultSet rs = null;
		
		printSQL(sql);
		
		try {
			pstmt = conn.prepareStatement(sql);
			pstmt.setInt(1, id);
			rs = pstmt.executeQuery();
			
			list = getBeans(rs, cls);
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			util.close(conn);
		}
		
		return list;
	}
	public static <T> List<T> select(int id, Class cls) {
		return select(id, cls, tools.getTableName(cls));
	}
	
/****************************************查询API结束*****************************************************************************/	
	
/****************************************更新API*******************************************************************************/
	/*
	 * sql update提交操作
	 * 更新条件 where id=bean.getId()
	 * 
	 * 后面存在一组缺省函数
	 * 1.table_name的缺省，略；
	 * 2.isUpdateEmpty缺省为false——空值不更新
	 */
	private static void Update(Object bean, String table_name, Boolean isUpdataEmpty) {
		Class cls = bean.getClass();
		List<Method> set_list = tools.getMethodList(cls.getMethods(), "get");
		List<Method> update_list = new ArrayList<Method>();
		
		String sql="Update "+table_name+" set ";
		
		for (Method m : set_list) {
			//SQL Update时，不可对主键进行更新
			if (m.getName().equals("getId"))
			{
				continue;
			}
			
			if (isUpdataEmpty)
			{
				update_list.add(m);
				continue;
			}
			
//			String type = m.getReturnType().getName();
			Object o = null;
			try {
				o = m.invoke(bean);
			} catch (IllegalAccessException | IllegalArgumentException
					| InvocationTargetException e) {
				e.printStackTrace();
			}
//			Boolean isEmptyValue = false;
//			isEmptyValue = isEmptyValue 
//					|| (o instanceof Integer && ((int)o) == 0)
//					//如果字符串为null，由于null本身不是对象，也不是Objcet的实例，所以判断结果为false
//					|| (type.equals("java.lang.String") && ((String)o) == null)
//					;
//			if (!isEmptyValue)
			if (o != null)
			{
				update_list.add(m);
			}
		}
		
		for (Method m: update_list) {
			sql+=m.getName().substring(3)+"=?, ";
		}
		
		sql = sql.substring(0, sql.length()-2)+" where id=?";
		
		printSQL(sql);

		DBUtil util = new DBUtil();
		Connection conn = util.getConnection();
		PreparedStatement pstmt = null;
		
		try {
			pstmt = conn.prepareStatement(sql);
			int i=0;
			for (Method m : update_list)
			{	
				i++;
				try {
					pstmt.setObject(i, m.invoke(bean));
				} catch (IllegalAccessException | IllegalArgumentException
						| InvocationTargetException e) {
					e.printStackTrace();
				}
			}
			
			pstmt.setInt(i+1, tools.getKeyId(bean));
			pstmt.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		} finally{
			util.close(conn);
		}
	}

	/*
	 * 缺省数据库表名的方法，注意条件同上，略
	 */
	public static void Update(Object bean, Boolean isUpdataEmpty) {
		Update(bean, tools.getTableName(bean.getClass()), isUpdataEmpty);
	}
	
	/*
	 * Update(Object obj, Boolean isUpdataEmpty)的缺省
	 * 缺省值为isUpdataEmpty=true
	 */
	public static void Update(Object bean) {
		Update(bean, false);
	}
	
	/*
	 * Update(Object obj, String table_name， Boolean isUpdataEmpty)的缺省
	 * 缺省值为isUpdataEmpty=true
	 */
	private static void Update(Object bean, String table_name)
	{
		Update(bean, table_name, false);
	}
/****************************************更新API结束***************************************************************************/
	private static void printSQL(String sql)
	{
		System.out.println("DBOperate SQL: "+sql);
	}
}
