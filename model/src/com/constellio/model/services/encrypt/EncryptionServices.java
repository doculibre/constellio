package com.constellio.model.services.encrypt;

import java.io.IOException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.ArrayList;
import java.util.List;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.binary.Base64;

import com.constellio.data.utils.ImpossibleRuntimeException;
import com.constellio.model.services.encrypt.EncryptionServicesRuntimeException.EncryptionServicesRuntimeException_InvalidKey;

public class EncryptionServices {
	private static final String DEFAULT_ENCRYPTION_ALGORITHM = "AES/CBC/PKCS5Padding";
	byte[] key = new byte[16];
	byte[] iv = new byte[16];
	boolean initialized = false;

	public EncryptionServices withKey(Key key)
			throws NoSuchAlgorithmException, InvalidKeySpecException, IOException {
		if (initialized) {
			throw new RuntimeException("Already intialized");
		}
		System.arraycopy(key.getEncoded(), 0, this.iv, 0, 16);
		System.arraycopy(key.getEncoded(), 16, this.key, 0, 16);
		initialized = true;
		return this;
	}

	public String encrypt(String toEncrypt, String algorithm) {
		if (toEncrypt == null) {
			return null;
		}
		try {
			Cipher cipher = Cipher.getInstance(algorithm);
			cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(key, "AES"), new IvParameterSpec(iv));

			byte[] encrypted = cipher.doFinal(toEncrypt.getBytes());
			return Base64.encodeBase64String(encrypted);

		} catch (Exception e) {
			throw new RuntimeException("Cannot encrypt '" + toEncrypt + "'", e);
		}
	}

	public String encrypt(String toEncrypt) {
		return encrypt(toEncrypt, DEFAULT_ENCRYPTION_ALGORITHM);
	}

	public String decrypt(String encryptedBase64, String algorithm) {
		try {
			Cipher cipher = Cipher.getInstance(algorithm);
			cipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(key, "AES"), new IvParameterSpec(iv));
			byte[] decryptedText = cipher.doFinal(Base64.decodeBase64(encryptedBase64));
			return new String(decryptedText);
		} catch (Exception e) {
			throw new RuntimeException("Cannot decrypt '" + encryptedBase64 + "'", e);
		}
	}

	public Object decrypt(Object encryptedText) {
		if (encryptedText instanceof String) {
			return decrypt((String) encryptedText);

		} else if (encryptedText instanceof List) {
			List<Object> list = (List<Object>) encryptedText;
			List<Object> decryptedValues = new ArrayList<>();

			for (Object item : list) {
				decryptedValues.add(decrypt(item));
			}

			return decryptedValues;

		} else {
			throw new IllegalArgumentException("Unsupported element of class '" + encryptedText.getClass().getName() + "'");
		}

	}

	public Object encrypt(Object toEncrypt) {
		if (toEncrypt instanceof String) {
			return encrypt((String) toEncrypt);

		} else if (toEncrypt instanceof List) {
			List<Object> list = (List<Object>) toEncrypt;
			List<Object> encryptedValues = new ArrayList<>();

			for (Object item : list) {
				encryptedValues.add(encrypt(item));
			}

			return encryptedValues;

		} else {
			throw new IllegalArgumentException("Unsupported element of class '" + toEncrypt.getClass().getName() + "'");
		}

	}

	public String decrypt(String encryptedText) {
		return decrypt(encryptedText, DEFAULT_ENCRYPTION_ALGORITHM);
	}

	public static EncryptionServices create(Key key) {
		try {

			return new EncryptionServices().withKey(key);

		} catch (InvalidKeySpecException e) {
			throw new EncryptionServicesRuntimeException_InvalidKey(e);
		} catch (NoSuchAlgorithmException | IOException e) {
			throw new ImpossibleRuntimeException(e);
		}
	}

	public boolean isInitialized() {
		return initialized;
	}
}
