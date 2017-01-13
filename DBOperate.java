package com.demo;
/*
 * ��ѧ�� 2014.07.08
 * ��Hibernate��̬����SQL���룬���ٿ�����Ա�ı�����
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
 * �����������к���String table_name������API��ǿ��Ҫ�������������һ��ʱ���ü�ע����
 * 
 * �������е�API������ע������2�㣺
 * 1. bean�������������ݱ�ͬ�����粻ͬ���@MyTableע��
 * 2. bean�����г�Ա�����������ݿ���������ȫ��ͬ������Сд��
 */

public class DBOperate {
	private static Tools tools = new Tools();
/****************************************����API*******************************************************************************/
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
	 * ʹ��ǰ���ǣ�javabean�������������ͬ���������м�MyTableע��
	 */
	public static int add(Object bean) {
		return add(bean, tools.getTableName(bean.getClass()));
	}
/****************************************����API����*******************************************************************************/
/****************************************ɾ��API**********************************************************************************/		
	/*
	 * sql delete����
	 * ɾ������ where id = bean.getId()
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
	 * ʹ��delete������ǰ��:
	 * 1.javabean�������������ͬ���������м�MyTableע��
	 * 2.Id���Բ���Ϊ��
	 * 
	 * ɾ�����ݣ�Id����
	 */
	public static void delete(Object bean)
	{
		Class cls = bean.getClass();
		String name = tools.getTableName(cls);
		
		delete(bean, name);
	}
/****************************************ɾ��API����*******************************************************************************/
/****************************************��ѯAPI**********************************************************************************/
	/*
	 * ʹ��ǰ�᣺javabean�е����������������ݿ����ȫ��ͬ
	 * ���������ͬ������ʹ��������ʹ��Eclipse IDE������;���������������ϵ�ң���ѧ�ƣ����Զ���ע�ͽ��
	 * select *
	 */
	public static <T> List<T> getBeans(ResultSet rs, Class cls) {
		
		List<Method> list = tools.getMethodList(cls.getMethods(), "set");
		List<T> ret = new ArrayList<T>();
		
		try {
			while (rs.next()) {
				T obj = (T) cls.newInstance();
				
				for (Method m :list) {
					//ȥ��setǰ׺
					//SQL��䲻���ִ�Сд����û����Сдת��
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
	 * �ض��в�ѯ��
	 * select name ��age 
	 * ע�⣺javabean�����������ݿ����������Сͳһ
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
	 * ���ڶ���ѯ���ϵ�API
	 * ע�⣺
	 * 1.T�����ʹ��@MyBeansע�⡪������T����Щbean��Ա
	 * 2.ÿһ����Աbean���������ݿ��ͬ�����粻ͬ���@MyTableע��
	 * 3��ÿһ��bean�ĳ�Ա���������ݿ���е���ͬ����������Сд
	 * 4.bean��Ա�����Ϊ��������ĸСд�����飬����Ҳ�����������set/get������һ��Ҫ�Ǵ�������������ĸСд��Ĭ�ϵ�IDE������
	 * �磺
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
			//obj_members_list��������T��������г�Ա��List����
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
					//���ݲ�ѯ�����table���ԣ���inner_bean_list���ҳ�table��Ӧ��bean
					//����ȷ����beanʵ��������������ע���е�ע������2��
					Class c = obj_member.getClass();
					
					if (tools.getTableName(c).equals(rmd.getTableName(i)))
					{
						//�ҵ�bean�����У����÷�����ƣ����ö����set������ֵ
						//��ȷ����set����������������ע������3.
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
			
			//�������obj�е�set������ֵ
			//����ȷִ�е�����������ע������4.
			tools.setObj(obj_T, obj_T_members_list);
			list.add(obj_T);
		}
		return list;
	}
	/*
	 * ���̶�id��ѯ
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
	
/****************************************��ѯAPI����*****************************************************************************/	
	
/****************************************����API*******************************************************************************/
	/*
	 * sql update�ύ����
	 * �������� where id=bean.getId()
	 * 
	 * �������һ��ȱʡ����
	 * 1.table_name��ȱʡ���ԣ�
	 * 2.isUpdateEmptyȱʡΪfalse������ֵ������
	 */
	private static void Update(Object bean, String table_name, Boolean isUpdataEmpty) {
		Class cls = bean.getClass();
		List<Method> set_list = tools.getMethodList(cls.getMethods(), "get");
		List<Method> update_list = new ArrayList<Method>();
		
		String sql="Update "+table_name+" set ";
		
		for (Method m : set_list) {
			//SQL Updateʱ�����ɶ��������и���
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
//					//����ַ���Ϊnull������null�����Ƕ���Ҳ����Objcet��ʵ���������жϽ��Ϊfalse
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
	 * ȱʡ���ݿ�����ķ�����ע������ͬ�ϣ���
	 */
	public static void Update(Object bean, Boolean isUpdataEmpty) {
		Update(bean, tools.getTableName(bean.getClass()), isUpdataEmpty);
	}
	
	/*
	 * Update(Object obj, Boolean isUpdataEmpty)��ȱʡ
	 * ȱʡֵΪisUpdataEmpty=true
	 */
	public static void Update(Object bean) {
		Update(bean, false);
	}
	
	/*
	 * Update(Object obj, String table_name�� Boolean isUpdataEmpty)��ȱʡ
	 * ȱʡֵΪisUpdataEmpty=true
	 */
	private static void Update(Object bean, String table_name)
	{
		Update(bean, table_name, false);
	}
/****************************************����API����***************************************************************************/
	private static void printSQL(String sql)
	{
		System.out.println("DBOperate SQL: "+sql);
	}
}
