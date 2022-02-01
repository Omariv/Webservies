package com.WebServiceToolBox;

import org.json.simple.JSONObject;

public class Utils {

	public class Response{
		public int responseCode;
		public String responseBody;
		public String Id;
		public String Name;
		public String Status;
		public JSONObject ob;
		
		
		public Response() {}
		
		public String toString() {	
			return String.format("Code : %d ; Body : %s",responseCode, responseBody);
			//return String.format("Code : %d ;Body : %s",responseCode,responseBody);
		}
		/*
		public org.json.JSONObject ob(org.json.JSONObject myResponse) {
			return myResponse;
		}*/
	}
}
