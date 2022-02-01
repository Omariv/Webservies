/**
 * 
 */
package com.alstom.PLM4ADocToolKit.sample;

import java.io.IOException;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.alstom.PLM4ADocToolKit.DocumentManager;

/**
 * Sample file download class
 * @author Accenture
 * @version 1.0
 *
 */
public class DownloadFile {
	static final Logger logger = LogManager.getLogger(DownloadFile.class);
	/**
	 * This method download file attached to a document into a temporary folder, display file path and deleting file after 10 seconds.
	 * <p>
	 * <b>Usage:</b>
	 * <ol>
	 * <li>Path to properties file</li>
	 * <li>3DExperience User</li>
	 * <li>3DExperience security context</li>
	 * <li>Physical ID of the Document</li>
	 * <li>Physical ID of the File</li>
	 * </ol>
	 * @param args Arguments to launch the file download
	 */
	public static void main(String[] args) {
		try {

			DocumentManager dm = new DocumentManager(args[0], args[1], args[2]);
			long time = System.currentTimeMillis();
			String defaultBaseDir = System.getProperty("java.io.tmpdir");

			Map<String, String> downloadTicket =  dm.getDownloadTicket(args[3], args[4]);

			Path customBaseDir = FileSystems.getDefault().getPath(defaultBaseDir);
			String customFilePrefix = "plm4a_";
			String customFileSuffix = ".proxy";
			Path tmpFile = null;
			try {
				tmpFile = Files.createTempFile(
						customBaseDir, customFilePrefix, customFileSuffix);
			} catch (IOException e) {
				e.printStackTrace();
			}


			try (FileChannel fc = FileChannel.open(tmpFile, StandardOpenOption.WRITE, StandardOpenOption.DELETE_ON_CLOSE)) {

				fc.transferFrom(
						Channels.newChannel(dm.getFileResultFromFCS(downloadTicket.get("ticketURL"))),
						0, 
						Long.MAX_VALUE
						);
				time= System.currentTimeMillis() - time;


				System.out.printf("%s : %s in %s ms", "File downloaded at", tmpFile.toAbsolutePath(), time);

				Thread.sleep(10000);
			} catch (Exception e) {
				logger.catching(e);
			}


		} catch (Exception e) {
			logger.catching(e);
		}

	}

}
