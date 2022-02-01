package com.alstom.PLM4ADocToolKit;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.InaccessibleObjectException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import com.alstom.WebServiceToolBox.Connection;
import com.alstom.WebServiceToolBox.HTTPMultipartFormData;

/**
 * This class handle document management through 3DExperience ITB Web Service
 * @author Accenture
 * @version 1.0
 *
 */
public class DocumentManager {
	static final Logger logger = LogManager.getLogger(DocumentManager.class);

	private Connection con;

	public DocumentManager(String pathToProperty, String user, String securityContext) throws Exception {
		Properties props = new Properties();
		try( FileInputStream inputStream = new FileInputStream(pathToProperty) ) {
			props.load(inputStream);
		} catch (Exception e) {
			logger.catching(e);
			throw e;
		} 
		this.con=new Connection(props,user,securityContext);

	}

	public DocumentManager(Properties props, String user, String securityContext) throws Exception {
		this.con=new Connection(props,user,securityContext);

	}

	public String getDocuments(String docPhysicalId) throws Exception {
		String resultDoc ="";
		if(con.isOpen()) {
			StringBuffer sbDocumentsFile = new StringBuffer();
			sbDocumentsFile.append(con.getUrl3dpaceEnv());
			sbDocumentsFile.append(Connection.SERVICE_3DSPACE);
			sbDocumentsFile.append(String.format(Connection.SPACE_DOCUMENT, docPhysicalId));

			resultDoc = con.getResultFromWebService(sbDocumentsFile.toString(), "GET", "");
			logger.log(Level.DEBUG, resultDoc);
		} 
		return resultDoc;
	}

	public HashMap<String, String> getCheckinTicket(String docPhysicalId) throws Exception {
		HashMap<String, String> fcsParams = new HashMap<>();
		if(con.isOpen()) {
			StringBuffer sbDocumentsFile = new StringBuffer();
			sbDocumentsFile.append(con.getUrl3dpaceEnv());
			sbDocumentsFile.append(Connection.SERVICE_3DSPACE);
			if(docPhysicalId!=null && !docPhysicalId.isBlank()) {
				sbDocumentsFile.append(String.format(Connection.SPACE_DOCUMENT_FILE_CHECKINTICKET, docPhysicalId));
			} else {
				sbDocumentsFile.append(Connection.SPACE_DOCUMENTS_FILE_CHECKINTICKET);

			}

			String resultDoc = con.getResultFromWebService(sbDocumentsFile.toString(), "PUT", "");

			logger.log(Level.DEBUG, resultDoc);
			JSONObject jsonTokenObj = new JSONObject(resultDoc);

			if(jsonTokenObj.getBoolean("success")) {

				JSONArray jaData = jsonTokenObj.getJSONArray("data");
				if (jaData != null && !jaData.isEmpty()) {
					JSONObject joDocument = (JSONObject) jaData.get(0);
					JSONObject joDataElements = joDocument.getJSONObject ("dataelements");

					fcsParams.put("ticketURL", joDataElements.getString("ticketURL"));
					fcsParams.put("ticketparamname", joDataElements.getString("ticketparamname"));
					fcsParams.put("ticket", joDataElements.getString("ticket"));

				}
			} else {
				throw new Exception(jsonTokenObj.getString("error"));
			}

		}

		return fcsParams;
	}

	public String checkinFile(Map<String, String> fcsParams, File fileToCheckin) throws InaccessibleObjectException, IOException {
		String response="";
		if(fcsParams!=null && !fcsParams.isEmpty() && con.isOpen()){
			Map<String, String> headers = new HashMap<>();
			headers.put("SecurityContext", con.getSecurityContext());
			headers.put("accept", "*/*");

			HTTPMultipartFormData multipart = new HTTPMultipartFormData(fcsParams.get("ticketURL"), "utf-8", headers);
			// Add form field
			multipart.addFormField(fcsParams.get("ticketparamname"), fcsParams.get("ticket"));
			// Add file
			multipart.addFilePart("file_0", fileToCheckin);
			// Print result
			response = multipart.finish();
		}
		return response;
	}

	public String createDocWithFile( String type, Map<String,String> dataElements, String fileName, String fcsTicket ) throws Exception {
		String docPhysicalId = "";

		if( con.isOpen() ) {

			JSONObject jsonRequest = new JSONObject();

			JSONArray jsonData = new JSONArray();
			jsonRequest.put("data", jsonData);

			JSONObject jsonDoc = new JSONObject();
			jsonData.put(jsonDoc);

			jsonDoc.put("type", type);

			JSONObject jsonDataElements = new JSONObject();
			jsonDoc.put("dataelements", jsonDataElements);

			for(Entry<String, String> dataElement : dataElements.entrySet()) {
				jsonDataElements.put(dataElement.getKey(), dataElement.getValue());
			}

			JSONObject jsonRelatedData = new JSONObject();
			jsonDoc.put("relateddata", jsonRelatedData);

			JSONArray jsonFiles = new JSONArray();
			jsonRelatedData.put("files", jsonFiles);

			JSONObject jsonFile = new JSONObject();
			jsonFiles.put(jsonFile);

			JSONObject jsonFileDataElements = new JSONObject();
			jsonFile.put("dataelements", jsonFileDataElements);

			jsonFileDataElements.put("title", fileName);
			jsonFileDataElements.put("receipt", fcsTicket);

			StringBuffer sbNewDocumentURL = new StringBuffer();
			sbNewDocumentURL.append(con.getUrl3dpaceEnv());
			sbNewDocumentURL.append(Connection.SERVICE_3DSPACE);
			sbNewDocumentURL.append(Connection.SPACE_DOCUMENTS);

//			System.out.println("----");
//			System.out.println("a="+sbNewDocumentURL.toString());
//			System.out.println("b="+jsonRequest.toString());
			
			String response = con.getResultFromWebService(
					sbNewDocumentURL.toString(), 
					"POST", 
					jsonRequest.toString()
					);
			logger.log(Level.DEBUG, response);
			System.out.println("response" + response);

			JSONObject jsonResponse = new JSONObject(response);

			if(jsonResponse.getBoolean("success")) {

				docPhysicalId = jsonResponse.getJSONArray("data")
						.getJSONObject(0)
						.getString("id");
			} else {
				throw new Exception(jsonResponse.getString("error"));
			}

		}


		return docPhysicalId;
	}


	public String updateDocuments(String docPhysicalId, Map<String,String> dataElements) throws Exception {
		String resultDoc = "";

		if( con.isOpen() ) {

			JSONObject jsonRequest = new JSONObject();

			JSONArray jsonData = new JSONArray();
			jsonRequest.put("data", jsonData);

			JSONObject jsonDoc = new JSONObject();
			jsonData.put(jsonDoc);

			JSONObject jsonDataElements = new JSONObject();
			jsonDoc.put("dataelements", jsonDataElements);

			for(Entry<String, String> dataElement : dataElements.entrySet()) {
				jsonDataElements.put(dataElement.getKey(), dataElement.getValue());
			}

			StringBuffer sbDocumentsFile = new StringBuffer();
			sbDocumentsFile.append(con.getUrl3dpaceEnv());
			sbDocumentsFile.append(Connection.SERVICE_3DSPACE);
			sbDocumentsFile.append(String.format(Connection.SPACE_DOCUMENT, docPhysicalId));

			String response = con.getResultFromWebService(
					sbDocumentsFile.toString(), 
					"PUT",
					jsonRequest.toString()
					);
			logger.log(Level.DEBUG, response);
			JSONObject jsonResponse = new JSONObject(response);

			if(jsonResponse.getBoolean("success")) {

				docPhysicalId = jsonResponse.getJSONArray("data")
						.getJSONObject(0)
						.getString("id");
			} else {
				throw new Exception(jsonResponse.getString("error"));
			}
		} 
		return resultDoc;
	}

	public Map<String, String> getDownloadTicket(String docPhysicalId, String filePhysicalId) throws Exception {
		HashMap<String, String> downloadTicket = new HashMap<String, String>();
		if (con.isOpen()) {
			StringBuffer sbDocumentsFile = new StringBuffer();
			sbDocumentsFile.append(con.getUrl3dpaceEnv());
			sbDocumentsFile.append(Connection.SERVICE_3DSPACE);
			sbDocumentsFile.append(String.format(Connection.SPACE_DOCUMENTS_FILES_DOWNLOADTICKET, docPhysicalId, filePhysicalId));

			String response = con.getResultFromWebService(
					sbDocumentsFile.toString(), 
					"PUT", 
					""
					);
			logger.log(Level.DEBUG, response);
			JSONObject jsonResponse = new JSONObject(response);

			if(jsonResponse.getBoolean("success")) {

				JSONObject data = jsonResponse.getJSONArray("data")
						.getJSONObject(0);

				JSONObject dataElements = data
						.optJSONObject("dataelements");

				if(dataElements!=null) {
					downloadTicket.put(
							"ticketURL", 
							dataElements.getString("ticketURL")
							);


					downloadTicket.put(
							"fileName", 
							dataElements.getString("fileName")
							);
				} else {
					String message = data.optString("updateMessage");
					if(message==null || message.isBlank()) {
						message = "Unexpected error has occured";
					}
					throw new Exception(message);
				}
			} else {
				throw new Exception(jsonResponse.getString("error"));
			}
		}

		return downloadTicket;
	}

	public InputStream  getFileResultFromFCS(String ticketURL) throws Exception{
		URL webServiceURL = new URL(ticketURL);
		HttpURLConnection connectionFCS = (HttpURLConnection)webServiceURL.openConnection();

		connectionFCS.setRequestProperty("accept", "*/*");

		connectionFCS.setRequestMethod("GET");
		connectionFCS.setUseCaches(false);

		int responseCode = connectionFCS.getResponseCode();

		if(responseCode == HttpURLConnection.HTTP_OK) {
			return connectionFCS.getInputStream();
		} else {
			BufferedReader brReader = new BufferedReader(
					new InputStreamReader(
							connectionFCS.getErrorStream()
							)
					);
			StringBuffer sbBuffer = new StringBuffer();
			String strOutput;
			while ((strOutput = brReader.readLine()) != null) {
				sbBuffer.append(strOutput);
			}
			brReader.close();
			throw new Exception(sbBuffer.toString());
		}
	}

}
