package com.informationretrieval.document;

/*
 * POJO class to store the 2 attributes of each query
 * 
 * Each query has the following attributes: 
 * .I: The unique ID of the query.
 * .W: The text of the query.
 */

public class CranfieldQuery {
	private int id;
	private String text;
	
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getText() {
		return text;
	}
	public void setText(String text) {
		this.text = text;
	}
}
