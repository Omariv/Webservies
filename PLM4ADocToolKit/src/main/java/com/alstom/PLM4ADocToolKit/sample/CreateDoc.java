/**
 * 
 */
package com.alstom.PLM4ADocToolKit.sample;

import java.io.File;
import java.util.HashMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;

import com.alstom.PLM4ADocToolKit.DocumentManager;

/**
 * Sample document creation class 
 * @author Accenture
 * @version 1.0
 *
 */
public class CreateDoc {
	static final Logger logger = LogManager.getLogger(CreateDoc.class);
	/**
	 * This method create 5 documents, and display there name.
	 * <p>
	 * <b>Usage:</b>
	 * <ol>
	 * <li>Path to properties file</li>
	 * <li>3DExperience User</li>
	 * <li>3DExperience security context</li>
	 * <li>Path of the file to checkin</li>
	 * </ol>
	 * @param args Arguments to launch the document creation
	 */
	public static void main(String[] args) {
		try {

			DocumentManager dm = new DocumentManager(args[0], args[1], args[2]);
			File file = new File(args[3]);

			HashMap<String, String> attributes = new HashMap<String, String>();
			attributes.put("title", "TEST DOC EN");
			attributes.put("AT_TitleDoc_SP", "TEST DOC SP");
			attributes.put("AT_TitleDoc_IT", "TEST DOC IT");
			attributes.put("AT_TitleDoc_GE", "TEST DOC GE");
			attributes.put("AT_TitleDoc_FR", "TEST DOC FR");

			for(int i = 0; i<5; i++) {

				String docPhysId = dm.createDocWithFile(
						"Document",
						attributes,
						file.getName(),
						dm.checkinFile(
								dm.getCheckinTicket(null)
								,file)
						);

				String jsonResult =  dm.getDocuments(docPhysId);
				JSONObject json = new JSONObject(jsonResult);
				if(json.getBoolean("success")) {
					logger.info(
							"Name: %s, ID %s",json.getJSONArray("data")
							.getJSONObject(0).getJSONObject("dataelements").getString("name"),
							docPhysId
							);
				} else {
					logger.error("ERROR: "+json.getString("error"));
				}
			}
		} catch (Exception e) {
			logger.catching(e);
		}

	}

}
