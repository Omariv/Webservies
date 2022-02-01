/**
 *
 */
package com.alstom.plm4aproxy.crypto;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.AlgorithmParameterSpec;
import java.util.Arrays;
import java.util.Base64;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * This class expose method to manage encryption for PLM4AProxy
 * @author Accenture
 * @version 1.0
 */
public class CryptoUtils {

	/**
	 * AES/GCM/NoPadding encryption scheme
	 */
	public static final String AES_GCM_NOPADDING_ENCRYPTION_SCHEME = "AES/GCM/NoPadding";

	/**
	 * AES algorithm
	 */
	public static final String AES_ALGORITHM = "AES";

	/**
	 * This method generate a SecretKey
	 * @param algorithm				Algorithm used to generate SecretKey
	 * @return SecretKey
	 */
	public static SecretKey generateSecretKey(String algorithm) {
		SecureRandom secureRandom = new SecureRandom();
		byte[] key = new byte[16];
		secureRandom.nextBytes(key);
		SecretKey secretKey = new SecretKeySpec(key, algorithm);
		Arrays.fill(key,(byte) 0); //overwrite the content of key with zeros
		return secretKey;
	}

	/**
	 * This method return an encrypted plain text, encoded in base64, using a SecretKey 
	 * @param unencryptedString		Plain text data to encrypt
	 * @param secretKey				SecretKey
	 * @param encryptionScheme		Encryption Scheme
	 * @return Encrypted data, base64 encoded
	 * @throws InvalidKeyException If exception
	 * @throws InvalidAlgorithmParameterException If exception
	 * @throws NoSuchAlgorithmException If exception
	 * @throws NoSuchPaddingException If exception
	 * @throws UnsupportedEncodingException If exception
	 * @throws IllegalBlockSizeException If exception
	 * @throws BadPaddingException If exception
	 */
	public static String encript(String unencryptedString, SecretKey secretKey, String encryptionScheme) throws InvalidKeyException, InvalidAlgorithmParameterException, NoSuchAlgorithmException, NoSuchPaddingException, UnsupportedEncodingException, IllegalBlockSizeException, BadPaddingException {
		SecureRandom secureRandom = new SecureRandom();

		byte[] iv = new byte[12]; //NEVER REUSE THIS IV WITH SAME KEY
		secureRandom.nextBytes(iv);

		final Cipher cipher = Cipher.getInstance(encryptionScheme);
		GCMParameterSpec parameterSpec = new GCMParameterSpec(128, iv); //128 bit auth tag length
		cipher.init(Cipher.ENCRYPT_MODE, secretKey, parameterSpec);

		byte[] plainText = unencryptedString.getBytes(StandardCharsets.UTF_8);
		byte[] cipherText = cipher.doFinal(plainText);
		ByteBuffer byteBuffer = ByteBuffer.allocate(iv.length + cipherText.length);
		byteBuffer.put(iv);
		byteBuffer.put(cipherText);
		byte[] cipherMessage = byteBuffer.array();

		Arrays.fill(iv,(byte) 0); //overwrite the content of iv with zeros
		return base64Encode(cipherMessage);
	}

	/**
	 * This method return a decrypted base64 encoded binary message
	 * @param encryptedString		Encrypted data to decrypt, base64 encoded
	 * @param secretKey				SecretKey
	 * @param encryptionScheme		Encryption Scheme
	 * @return Decoded and decrypted plain text data.
	 * @throws InvalidKeyException If exception
	 * @throws InvalidAlgorithmParameterException If exception
	 * @throws NoSuchAlgorithmException If exception
	 * @throws NoSuchPaddingException If exception
	 * @throws IllegalBlockSizeException If exception
	 * @throws BadPaddingException If exception
	 */
	public static String decrypt(String encryptedString, SecretKey secretKey, String encryptionScheme) throws InvalidKeyException, InvalidAlgorithmParameterException, NoSuchAlgorithmException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException {
		final Cipher cipher = Cipher.getInstance(encryptionScheme);

		byte[] cipherMessage = base64Decode(encryptedString);
		//use first 12 bytes for iv
		AlgorithmParameterSpec gcmIv = new GCMParameterSpec(128, cipherMessage, 0, 12);
		cipher.init(Cipher.DECRYPT_MODE, secretKey, gcmIv);

		//use everything from 12 bytes on as ciphertext
		byte[] plainText = cipher.doFinal(cipherMessage, 12, cipherMessage.length - 12);

		return new String(plainText);
	}

	/**
	 * This method encode binary data into base64 plain text 
	 * @param toEncode				Binary data to encode
	 * @return Base64 encoded data
	 */
	public static String base64Encode(byte[] toEncode) {
		return Base64.getUrlEncoder().encodeToString(toEncode);
	}

	/**
	 * This method decode a base 64 plain text into binary data
	 * @param toDecode				Base64 encoded data
	 * @return Binary decoded data
	 */
	public static byte[] base64Decode(String toDecode) {
		return Base64.getUrlDecoder().decode(toDecode);
	}

	/**
	 * This method will generate a SecretKey, and store it in a temporary file in TEMP folder
	 * @param args				Not used
	 */
	public static void main(String[] args) {
		String defaultBaseDir = System.getProperty("java.io.tmpdir");
		Path customBaseDir = FileSystems.getDefault().getPath(defaultBaseDir);
		String customFilePrefix = "secret_";
		String customFileSuffix = ".key";

		Path tmpFile = null;

		try {
			tmpFile = Files.createTempFile(
					customBaseDir, 
					customFilePrefix, 
					customFileSuffix
					);

			try (BufferedWriter bw = Files.newBufferedWriter(tmpFile, StandardCharsets.UTF_8)) {
				bw.append(base64Encode(generateSecretKey(AES_ALGORITHM).getEncoded()));
				bw.flush();
				System.out.println("SecretKey successfully generated in temporary file: ");
				System.out.printf("%-25s : %25s%n", "SecretKey file", tmpFile.toAbsolutePath());

			} catch (IOException e) {
				System.err.println("Unable to write in temporary file: "+e.getMessage());
			}

		} catch (IOException e) {
			System.err.println("Unable to create temporary file: "+e.getMessage());
		}
	}

}
