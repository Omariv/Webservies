/**
 * 
 */
package com.alstom.plm4aproxy.services;

import java.io.IOException;
import java.net.MalformedURLException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Map;

import javax.activation.MimetypesFileTypeMap;
import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import javax.ws.rs.ForbiddenException;
import javax.ws.rs.GET;
import javax.ws.rs.InternalServerErrorException;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.alstom.PLM4ADocToolKit.DocumentManager;
import com.alstom.plm4aproxy.config.ConfigUtils;
import com.alstom.plm4aproxy.crypto.CryptoUtils;

/**
 * This class handle WebService for documents.
 * @author Accenture
 * @version 1.0
 */
@Path("/documents")
public class DocumentFile {

	
	static final Logger logger = LogManager.getLogger(DocumentFile.class);
	
	/**
	 * This WebService is use to recover a file associated to a document.
	 * 
	 * @param docid  Encrypted document ID
	 * @param fileid Encrypted file ID
	 * @return Stream of file
	 * @throws MalformedURLException
	 * @throws IOException
	 */
	@GET
	@Path("{docid}/files/{fileid}")
	@Produces(MediaType.APPLICATION_OCTET_STREAM)
	public Response getFileById(@PathParam("docid") String docid, @PathParam("fileid") String fileid)
			throws MalformedURLException, IOException {

		// Build SecretKey object from base64 encoded key
		byte[] decodedKey = CryptoUtils.base64Decode(ConfigUtils.getInstance().getProperty("Secretkey"));
		SecretKey originalKey = new SecretKeySpec(decodedKey, 0, decodedKey.length, CryptoUtils.AES_ALGORITHM);

		String decryptedDocId;
		String decryptedFileId;
		try {
			// Decrypt IDs
			decryptedDocId = CryptoUtils.decrypt(docid, originalKey, CryptoUtils.AES_GCM_NOPADDING_ENCRYPTION_SCHEME);
			decryptedFileId = CryptoUtils.decrypt(fileid, originalKey, CryptoUtils.AES_GCM_NOPADDING_ENCRYPTION_SCHEME);
		} catch (InvalidKeyException | InvalidAlgorithmParameterException | NoSuchAlgorithmException
				| NoSuchPaddingException | IllegalBlockSizeException | BadPaddingException e) {
			logger.catching(e);
			throw new ForbiddenException(e);
		}

		if (decryptedDocId == null || decryptedDocId.isBlank() || decryptedFileId == null || decryptedFileId.isBlank()) {
			logger.error("Document not found");
			throw new NotFoundException();
		}

		try {
			long time = System.currentTimeMillis();
			DocumentManager dm = new DocumentManager(
					ConfigUtils.getInstance().getProperties(),
					ConfigUtils.getInstance().getProperty("User"),
					ConfigUtils.getInstance().getProperty("Context")
					);

			Map<String, String> downloadTicket =  dm.getDownloadTicket(decryptedDocId, decryptedFileId);


			// Identify Mime Type associated to File
			MimetypesFileTypeMap fileTypeMap = new MimetypesFileTypeMap();
			String mimeType = fileTypeMap.getContentType(downloadTicket.get("fileName"));

			// Return File with proper MimeType with Content-Disposition as inline
			return Response.ok((StreamingOutput) output -> {
				try {
					dm.getFileResultFromFCS(downloadTicket.get("ticketURL")).transferTo(output);
					output.flush();
					long endtime = System.currentTimeMillis() - time;
					logger.info("File downloaded in "+endtime+" ms");

				} catch ( Exception e ) 
				{ 
					logger.catching(e);
					throw new InternalServerErrorException(e);
				}
			},  mimeType).header("Content-Disposition", "inline; filename=\"" + downloadTicket.get("fileName") + "\"")
					.build();

		} catch (Exception e) {
			e.printStackTrace();
			throw new InternalServerErrorException(e);
		}

	}
}
