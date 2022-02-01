package com.WS_Part;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import java.text.ParseException;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import com.WebServiceToolBox.Connection;
import com.WebServiceToolBox.Utils.Response;

public class PartManager {
	static final Logger logger = LogManager.getLogger(PartManager.class);
	Connection con;
	String inputFile;
	
	
	public PartManager(String user, String securityContext, String inputFile) throws Exception {
		try {
			this.con = new Connection(user, securityContext);
			this.inputFile = inputFile;
		} catch (Exception e) {
			logger.catching(e);
			throw e;
		}
	}
	
	
	/*
	 * CreatePart method
	 */
	public Response CreatePart() throws FileNotFoundException,
    IOException, ParseException {		
		StringBuffer sbNewDocumentURL = new StringBuffer();
		sbNewDocumentURL.append(con.getUrl3dpaceEnv());
		sbNewDocumentURL.append(Connection.SERVICE_3DSPACE);
		sbNewDocumentURL.append(Utils.SPACE_PART);
		Response response = null;
		
		JSONParser parser = new JSONParser();
		JSONArray jsonArray = new JSONArray();
		 try {
			jsonArray =  (JSONArray) parser.parse(new FileReader(inputFile));
		        for (Object o : jsonArray)
		        {
		            JSONObject obj = (JSONObject) o;		            
		    				response = con.getResultFromWebService(
		    				sbNewDocumentURL.toString(), 
		    				"POST", 
		    				obj.toString());
		    		System.out.println(response);		    		
		        }
		        logger.log(Level.DEBUG, response.toString());
		        
		 } catch (Exception e) {
				e.printStackTrace();
		}
		 
		return response;
	}

}
