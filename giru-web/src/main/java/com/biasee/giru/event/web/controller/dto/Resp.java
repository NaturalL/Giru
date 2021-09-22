package com.biasee.giru.event.web.controller.dto;

import java.util.HashMap;
import java.util.Map;


public class Resp {
	int status = 200;
	String msg = "";
	Object data;
	Map<String, Object> map = new HashMap<String, Object>();


	public Resp(int code, Object data, String msg) {
		this.status = code;
		this.data = data;
		this.msg = msg;
	}

	public static Resp success() {
		return new Resp(200, null, "success");
	}

	public static Resp error(int status) {
		return new Resp(status, null, "failed");
	}

	public static Resp success(Object data) {
		return new Resp(200, data, "success");
	}

	public static Resp error(int status, Object data) {
		return new Resp(status, data, "failed");
	}

	public Resp put(String key, Object value) {
		map.put(key, value);
		return this;
	}

	public int getStatus() {
		return status;
	}

	public Resp status(int status) {
		this.status = status;
		return this;
	}

	public Object getData() {
		return data;
	}

	public Resp data(Object data) {
		this.data = data;
		return this;
	}

	public String getMsg() {
		return msg;
	}

	public Map<String, Object> getMap() {
		return map;
	}

	public Resp msg(String msg) {
		this.msg = msg;
		return this;
	}


}
