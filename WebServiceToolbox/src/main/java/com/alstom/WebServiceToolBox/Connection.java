/**
 * 
 */
package com.alstom.WebServiceToolBox;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Properties;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;

/**
 * This class handle connection to 3DExperience server 
 * @author Accenture
 * @version 1.0
 *
 */
public class Connection extends ITBWebService {
	static final Logger logger = LogManager.getLogger(Connection.class);


	private String urlPassport;
	private String url3dpaceEnv;
	private String batchName;
	private String batchKey;

	private String user;
	private String securityContext;

	private String csrfTicket;

	private boolean open = false;


	/**
	 * Class constructor.
	 * @param props Connection Properties
	 * @param user 3DExperience User 
	 * @param securityContext 3DExperience Security Context
	 * @throws Exception If the operation fails
	 */
	public Connection(Properties props, String user, String securityContext) throws Exception {
		this.urlPassport  =  props.getProperty("PASSPORT_URL").trim();
		this.url3dpaceEnv  =  props.getProperty("3DX_URL").trim();
		this.batchName  =  props.getProperty("BatchName").trim();
		this.batchKey  =  props.getProperty("BatchKey").trim();
		this.user= user;
		this.securityContext=securityContext;

		CookieManager cookiemanager = new CookieManager();
		CookieHandler.setDefault(cookiemanager);

		int responseCode = setConnection();
		if(responseCode == HttpURLConnection.HTTP_OK){
			open=renewCSRFTokent();
		}

	}


	/**
	 * This method is used to open 3DExperience connection
	 * @return HTTP response code
	 * @throws Exception If the operation fails
	 */
	public  int setConnection() throws Exception {

		int ResponseNewCode = 0;

		String str3dpaceURL = url3dpaceEnv + ITBWebService.SERVICE_3DSPACE;
		str3dpaceURL = URLEncoder.encode(str3dpaceURL, "UTF-8");
		String strUrl = urlPassport+ITBWebService.SERVICE_PASSPORT+ITBWebService.PASSPORT_API_BATCH_TICKET+"?identifier="+user+"&service="+str3dpaceURL;

		URL urlPassport = new URL(strUrl);
		HttpURLConnection connection = (HttpURLConnection)urlPassport.openConnection();


		connection.setRequestProperty("DS-Service-Name",batchName);
		connection.setRequestProperty("DS-Service-Secret", batchKey);

		ResponseNewCode = connection.getResponseCode();
		
		URL urlx3ds_reauth = new URL("http://test.com");
		if(200 == ResponseNewCode){
			String stResponse = getResponseBody(connection);
			logger.log(Level.DEBUG, stResponse);
			JSONObject jsonObj = new JSONObject(stResponse.toString());
			String stX3ds_reauth_url = jsonObj.getString("x3ds_reauth_url");
			urlx3ds_reauth = new URL(stX3ds_reauth_url);
			HttpURLConnection connectionx3ds_reauth = (HttpURLConnection)urlx3ds_reauth.openConnection();
			ResponseNewCode = connectionx3ds_reauth.getResponseCode();


		} else {
			throw new Exception(connection.getResponseMessage());
		}

		return ResponseNewCode;
	}

	/**
	 * This method is used to obtain a CSRF ticket
	 * @return <b>true</b> if CSRF renewed successfully
	 * @throws Exception If the operation fails
	 */
	public boolean renewCSRFTokent() throws Exception {
		boolean success = false;
		StringBuffer sbCSRFUrl = new StringBuffer();
		sbCSRFUrl.append(url3dpaceEnv);
		sbCSRFUrl.append(ITBWebService.SERVICE_3DSPACE);
		sbCSRFUrl.append(ITBWebService.SPACE_CSRF);

		String strCSRFContent = getResultFromWebService(sbCSRFUrl.toString(), "GET",  "");

		logger.log(Level.DEBUG, strCSRFContent);

		JSONObject jsonTokenObj = new JSONObject(strCSRFContent);
		if(jsonTokenObj.getBoolean("success")) {
			JSONObject jsonObjToken = (JSONObject)jsonTokenObj.get("csrf");
			this.csrfTicket = jsonObjToken.getString("value");
			success = true;
		} else {
			throw new Exception(jsonTokenObj.getString("error"));
		}

		return success;
	}

	/**
	 * Return a String containing result of called Web Service
	 * @param webServiceURL URL of Web Service to call
	 * @param method HTTP Method
	 * @param inputData Input data to add to Web Service call
	 * @return Response text from Web Service
	 * @throws Exception If the operation fail
	 */
	public String getResultFromWebService(String webServiceURL, String method, String inputData) throws Exception{
		
		StringBuffer sbBuffer = new StringBuffer();

		URL urlWebServiceURL = new URL(webServiceURL);
		HttpURLConnection connectionWebURL = (HttpURLConnection)urlWebServiceURL.openConnection();

		connectionWebURL.setRequestProperty("SecurityContext", securityContext);
		connectionWebURL.setRequestProperty("accept", "application/json");
		connectionWebURL.setRequestProperty("Content-Type", "application/json");
		if(csrfTicket !=null && !"".equals(csrfTicket)){
			connectionWebURL.setRequestProperty("ENO_CSRF_TOKEN", csrfTicket);
		}

		connectionWebURL.setRequestMethod(method);
		connectionWebURL.setUseCaches(false);


		if(method !=null && ("POST".equalsIgnoreCase(method) || "PUT".equalsIgnoreCase(method))){
			connectionWebURL.setDoOutput(true);
			DataOutputStream wr = new DataOutputStream(connectionWebURL.getOutputStream());
			wr.writeBytes(inputData);
			wr.flush();
			wr.close();
		}

		sbBuffer.append(getResponseBody(connectionWebURL));
		//System.out.println("retour: "+sbBuffer.toString());
		return sbBuffer.toString();
	}

	/**
	 * 
	 * @param connection HTTP connection that handle the Web Service call
	 * @return String containing the response body
	 * @throws Exception If the operation fail
	 */
	public String getResponseBody(HttpURLConnection connection) throws Exception {

		StringBuffer sbBuffer = new StringBuffer();

		int iResponseCode = connection.getResponseCode();

		BufferedReader brReader = null;
		if(iResponseCode == HttpURLConnection.HTTP_OK){
			// Reading response from input Stream
			brReader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
		} else{
			brReader = new BufferedReader(new InputStreamReader(connection.getErrorStream()));
		}

		String strOutput;
		while ((strOutput = brReader.readLine()) != null) {
			sbBuffer.append(strOutput);
		}
		brReader.close();
		
		return sbBuffer.toString();
	}

	/**
	 * Get URL of 3DPassport
	 * @return URL of 3DPassport
	 */
	public String getUrlPassport() {
		return urlPassport;
	}

	/**
	 * Set URL of 3DPassport
	 * @param urlPassport URL of 3DPassport
	 */
	public void setUrlPassport(String urlPassport) {
		this.urlPassport = urlPassport;
	}

	/**
	 * Get URL of 3DSpace
	 * @return URL of 3DSpace
	 */
	public String getUrl3dpaceEnv() {
		return url3dpaceEnv;
	}

	/**
	 * Set URL of 3DSpace
	 * @param url3dpaceEnv URL of 3DSpace
	 */
	public void setUrl3dpaceEnv(String url3dpaceEnv) {
		this.url3dpaceEnv = url3dpaceEnv;
	}

	/**
	 * Get Batch Service Name
	 * @return Batch Service Name
	 */
	public String getBatchName() {
		return batchName;
	}

	/**
	 * Set Batch Service Name
	 * @param batchName Batch Service Name
	 */
	public void setBatchName(String batchName) {
		this.batchName = batchName;
	}

	/**
	 * Get Batch Service Key
	 * @return Batch Service Key
	 */
	public String getBatchKey() {
		return batchKey;
	}

	/**
	 * Set Batch Service Key
	 * @param batchKey Batch Service Key
	 */
	public void setBatchKey(String batchKey) {
		this.batchKey = batchKey;
	}

	/**
	 * Get User
	 * @return User
	 */
	public String getUser() {
		return user;
	}

	/**
	 * Set User
	 * @param user User
	 */
	public void setUser(String user) {
		this.user = user;
	}

	/**
	 * Get Security Context
	 * @return  Security Context
	 */
	public String getSecurityContext() {
		return securityContext;
	}

	/**
	 * Set Security Context
	 * @param securityContext Security Context
	 */
	public void setSecurityContext(String securityContext) {
		this.securityContext = securityContext;
	}

	/**
	 * Get CSRF ticket
	 * @return CSRF ticket
	 */
	public String getCSRFTicket() {
		return csrfTicket;
	}

	/**
	 * Is connection open
	 * @return <b>true</b> if connection open
	 */
	public boolean isOpen() {
		return open;
	}
}
