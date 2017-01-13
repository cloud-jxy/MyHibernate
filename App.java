package com.demo;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import com.sunny.vod.util.DBUtil;

public class App {

	/*
	 * 测试DBOperate.add
	 */
	@Test
	public void testAdd()
	{
		Testbean bean = new Testbean();
		bean.setAge(26);
		bean.setName("jxy");
		//bean.setId(1);
		
		//DBOperate.add((Object)bean, "test");
		DBOperate.add((Object)bean);
	}
	
	/*
	 * 测试DBOperate.getBeans
	 */
	@Test
	public void testGetBeans() {
		DBUtil util = new DBUtil();
		Connection conn = util.getConnection();
		PreparedStatement pstmt = null;
		
		String sql = "select * from test";
		List<Testbean> list = null;
		
		try {
			pstmt = conn.prepareStatement(sql);
			ResultSet rs = pstmt.executeQuery();
			//list = DBOperate.getBeans2(rs, Testbean.class);
			list = DBOperate.getBeans(rs, Testbean.class);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		for (Testbean o : list) {
			Testbean t = (Testbean) o;
			System.out.println("id :"+t.getId());
			System.out.println("name :"+t.getName());
			System.out.println("age :"+t.getAge());
			System.out.println("-----------------------");
		}
	}
	
	@Test
	public void testGetBeans2() {
		DBUtil util = new DBUtil();
		Connection conn = util.getConnection();
		PreparedStatement pstmt = null;
		
		String sql = "select * from test";
		List<Testbean> list = null;
		
		try {
			pstmt = conn.prepareStatement(sql);
			ResultSet rs = pstmt.executeQuery();
			list = DBOperate.getBeans2(rs, Testbean.class);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		for (Testbean o : list) {
			Testbean t = (Testbean) o;
			System.out.println("id :"+t.getId());
			System.out.println("name :"+t.getName());
			System.out.println("age :"+t.getAge());
			System.out.println("-----------------------");
		}
	}
	
	/*
	 * 测试DBOperate.delete
	 */
	@Test
	public void testDelete() {
		DBUtil util = new DBUtil();
		Connection conn = util.getConnection();
		PreparedStatement pstmt = null;
		
		String sql = "select * from test";
		List<Testbean> list = null;
		
		try {
			pstmt = conn.prepareStatement(sql);
			ResultSet rs = pstmt.executeQuery();
			list = DBOperate.getBeans(rs, Testbean.class);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		Testbean bean = (Testbean)list.get(0);
		
		System.out.println("id :"+bean.getId());
		System.out.println("name :"+bean.getName());
		System.out.println("age :"+bean.getAge());
		System.out.println("-----------------------");
		
		DBOperate.delete(bean);
	}
	
	/*
	 * 测试DBOperate.update
	 */
	@Test
	public void testUpdate() {
		DBUtil util = new DBUtil();
		Connection conn = util.getConnection();
		PreparedStatement pstmt = null;
		
		String sql = "select * from test";
		List<Testbean> list = null;
		
		try {
			pstmt = conn.prepareStatement(sql);
			ResultSet rs = pstmt.executeQuery();
			list = DBOperate.getBeans2(rs, Testbean.class);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		Testbean bean = list.get(0);
		
		System.out.println("id :"+bean.getId());
		System.out.println("name :"+bean.getName());
		System.out.println("age :"+bean.getAge());
		System.out.println("-----------------------");
		
		Testbean bean2 = new Testbean();
		bean2.setId(((Testbean)list.get(0)).getId());
		bean2.setAge(10);
		DBOperate.Update(bean2, false);
		
		Testbean bean3 = new Testbean();
		bean3.setId(((Testbean)list.get(1)).getId());
		bean3.setAge(10);
		DBOperate.Update(bean3, true);
	}
	
	@Test
	public void testSelect() {
		//Testbean bean = (Testbean) DBOperate.select(19, Testbean.class, "test").get(0);
		Testbean bean = (Testbean) DBOperate.select(5, Testbean.class).get(0);
		System.out.println(bean.getId());
		System.out.println(bean.getAge());
		System.out.println(bean.getName());
		System.out.println("----------------------");
	}
	
	@Test
	public void test4() throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
		String sql = "select test.id , user.id from test left join user on test.id = user.id";
		DBUtil util = new DBUtil();
		Connection conn = util.getConnection();

		PreparedStatement pstmt;
		ResultSet rs = null;

		try {
			pstmt = conn.prepareStatement(sql);

			rs = pstmt.executeQuery();

			if (rs.next()) {
				System.out.println("key: " + rs.getInt(1));
				ResultSetMetaData rmd = rs.getMetaData();
				for (int i = 1; i <= rmd.getColumnCount(); i++) {
					System.out.println("表名"+rmd.getTableName(i)+"属性名：" + rmd.getColumnName(i) + " 属性值："
							+ rs.getObject(i));
					rmd.getCatalogName(1);
				}

				System.out.println("------------------");
			}

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			/*
			 * 当插入时出现主键冲突时的错误返回，此时需要update而不是insert msg=Duplicate entry '1' for
			 * key 'PRIMARY' code=1062
			 */
			System.out.println("msg=" + e.getMessage());
			System.out.println("code=" + e.getErrorCode());
		} catch (SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@Test
	public void testGetBeans3()
	{
		Class cls = UniteBean.class;
		
		Method[] ms = cls.getMethods();
		
		List<UniteBean> list = new ArrayList<UniteBean>();
		
		
		String sql = "select 1, test.* , user.* from test left join user on test.id = user.id";
		DBUtil util = new DBUtil();
		Connection conn = util.getConnection();

		PreparedStatement pstmt;
		ResultSet rs = null;

		try {
			pstmt = conn.prepareStatement(sql);
			rs = pstmt.executeQuery();
			
			list = DBOperate.getBeans3(rs, UniteBean.class);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		for (UniteBean u : list)
		{
			System.out.println("--------------------------");
			System.out.println(u.getTestbean().getId());
			System.out.println(u.getTestbean().getAge());
			System.out.println(u.getTestbean().getName());
			System.out.println(u.getUser().getName());
			System.out.println(u.getUser().getPwd());
			System.out.println("--------------------------");
		}
	}
}
