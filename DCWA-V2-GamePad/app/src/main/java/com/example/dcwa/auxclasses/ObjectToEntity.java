package com.example.dcwa.auxclasses;

import java.io.UnsupportedEncodingException;

import org.apache.http.HttpEntity;
import org.apache.http.entity.StringEntity;

import com.google.gson.Gson;

public class ObjectToEntity {

	private Gson gson = new Gson();
	String strObject;
	
	public HttpEntity ToJsonHttpEntity(ISendableAction object)
	{
		strObject = gson.toJson(object);
		try
		{
			return new StringEntity(strObject, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			return null;
		}
	}
	
}
