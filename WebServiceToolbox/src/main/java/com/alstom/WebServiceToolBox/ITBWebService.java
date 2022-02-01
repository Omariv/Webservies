/**
 * 
 */
package com.alstom.WebServiceToolBox;

/**
 * List of ITB Web Service
 * @author Accenture
 * @version 1.0
 *
 */
public abstract class ITBWebService {
	public static final String SERVICE_3DSPACE="/3dspace";
    public static final String SERVICE_PASSPORT="/3dpassport";

    public static final String PASSPORT_API_BATCH_TICKET = "/api/v2/batch/ticket";

    public static final String SPACE_CSRF = "/resources/v1/application/CSRF";
    public static final String SPACE_DOCUMENT = "/resources/v1/modeler/documents/%s";
    public static final String SPACE_DOCUMENTS = "/resources/v1/modeler/documents";
    public static final String SPACE_DOCUMENTS_FILES = "/resources/v1/modeler/documents/%s/files";
    public static final String SPACE_DOCUMENTS_FILES_DOWNLOADTICKET = "/resources/v1/modeler/documents/%s/files/%s/DownloadTicket";

    public static final String SPACE_DOCUMENT_FILE_CHECKINTICKET = "/resources/v1/modeler/documents/%s/files/CheckinTicket";
    public static final String SPACE_DOCUMENTS_FILE_CHECKINTICKET = "/resources/v1/modeler/documents/files/CheckinTicket";
}