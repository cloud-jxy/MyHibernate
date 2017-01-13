package com.demo;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.HashMap;

@Retention(RetentionPolicy.RUNTIME)
public @interface MyTable{
	String name();
}
