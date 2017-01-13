package com.demo;

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
