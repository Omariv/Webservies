/**
 * 
 */
package com.alstom.plm4aproxy.crypto.sample;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import com.alstom.plm4aproxy.crypto.CryptoUtils;

/**
 * Sample file download class
 * @author Accenture
 * @version 1.0
 *
 */
public class EncryptData {

	/**
	 * Sample method to encrypt data
	 * @param args With args[0]: the string to encode, args[1]: the base64 encoded secretkey
	 */
	public static void main(String[] args) {
		try {
			// Build SecretKey object from base64 encoded key
			byte[] decodedKey = CryptoUtils.base64Decode(args[1]);
			SecretKey originalKey = new SecretKeySpec(decodedKey, 0, decodedKey.length, CryptoUtils.AES_ALGORITHM);
			
			String encryptedData = CryptoUtils.encript(args[0], originalKey, CryptoUtils.AES_GCM_NOPADDING_ENCRYPTION_SCHEME);
			System.out.printf("Encrypted data: %s",encryptedData);
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

}
