package com.constellio.model.services.encrypt;

import com.constellio.data.utils.ImpossibleRuntimeException;
import com.constellio.data.utils.dev.Toggle;
import com.constellio.model.services.encrypt.EncryptionServicesRuntimeException.EncryptionServicesRuntimeException_InvalidKey;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.KeyGenerator;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.Key;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.ArrayList;
import java.util.List;

public class EncryptionServices {
	private static final String OLD_DEFAULT_ENCRYPTION_ALGORITHM = "AES/CBC/PKCS5Padding";

	byte[] key = new byte[16];
	@Deprecated
	byte[] iv = new byte[16];
	boolean initialized = false;
	boolean lostPreviousKey;

	public static EncryptionServices create(boolean lostPreviousKey, Key key) {
		try {
			return new EncryptionServices(lostPreviousKey).withKey(key);
		} catch (InvalidKeySpecException e) {
			throw new EncryptionServicesRuntimeException_InvalidKey(e);
		} catch (NoSuchAlgorithmException | IOException e) {
			throw new ImpossibleRuntimeException(e);
		}
	}

	public EncryptionServices(boolean lostPreviousKey) {
		this.lostPreviousKey = lostPreviousKey;
	}

	public boolean isInitialized() {
		return initialized;
	}

	public EncryptionServices withKey(Key key)
			throws NoSuchAlgorithmException, InvalidKeySpecException, IOException {
		if (initialized) {
			throw new RuntimeException("Already intialized");
		}
		System.arraycopy(key.getEncoded(), 16, this.key, 0, 16);
		initialized = true;
		return this;
	}

	@Deprecated
	public EncryptionServices withKeyAndIV(Key key)
			throws NoSuchAlgorithmException, InvalidKeySpecException, IOException {
		if (initialized) {
			throw new RuntimeException("Already intialized");
		}
		System.arraycopy(key.getEncoded(), 0, this.iv, 0, 16);
		System.arraycopy(key.getEncoded(), 16, this.key, 0, 16);
		initialized = true;
		return this;
	}

	//
	// AES
	//

	private byte[] generateIv() {
		SecureRandom rand = new SecureRandom();
		byte[] iv = new byte[16];
		rand.nextBytes(iv);
		return iv;
	}

	public Key generateAESKey() {
		try {
			SecureRandom rand = new SecureRandom();
			KeyGenerator generator = KeyGenerator.getInstance("AES");
			generator.init(128, rand);
			return generator.generateKey();
		} catch (Exception e) {
			throw new RuntimeException("Cannot generate key", e);
		}
	}

	//
	// Encrypt String or List<String>
	//

	public Object encryptWithAppKey(Object toEncrypt) {
		return encrypt(toEncrypt, key);
	}

	public Object encrypt(Object toEncrypt, Object key) {
		if (toEncrypt == null) {
			return null;
		}

		if (toEncrypt instanceof List) {
			List<Object> list = (List<Object>) toEncrypt;
			List<Object> encryptedValues = new ArrayList<>();
			for (Object item : list) {
				encryptedValues.add(encrypt(item, key));
			}
			return encryptedValues;
		} else if (toEncrypt instanceof String) {
			return encrypt((String) toEncrypt, key);
		}

		throw new IllegalArgumentException("Unsupported element of class '" + toEncrypt.getClass().getName() + "'");
	}

	private String encrypt(String toEncrypt, Object key) {
		try {
			byte[] iv = generateIv();

			Cipher cipher = Cipher.getInstance(EncryptionAlgorithm.AES.getAlgorithm());
			Key cipherKey = key instanceof byte[] ? new SecretKeySpec((byte[]) key, "AES") : (Key) key;
			cipher.init(Cipher.ENCRYPT_MODE, cipherKey, new IvParameterSpec(iv));

			byte[] encryptedText = cipher.doFinal(toEncrypt.getBytes());

			byte[] result = new byte[iv.length + encryptedText.length];
			System.arraycopy(iv, 0, result, 0, iv.length);
			System.arraycopy(encryptedText, 0, result, iv.length, encryptedText.length);

			return Base64.encodeBase64String(result);
		} catch (Exception e) {
			throw new RuntimeException("Cannot encrypt '" + toEncrypt + "'", e);
		}
	}

	//
	// Encrypt File
	//

	public File encryptWithAppKey(File toEncrypt, File encryptedFile) {
		return encrypt(toEncrypt, encryptedFile, key);
	}

	public File encrypt(File toEncrypt, File encryptedFile, Object key) {
		if (toEncrypt == null) {
			return null;
		}

		try {
			byte[] iv = generateIv();

			Cipher cipher = Cipher.getInstance(EncryptionAlgorithm.AES.getAlgorithm());
			Key cipherKey = key instanceof byte[] ? new SecretKeySpec((byte[]) key, "AES") : (Key) key;
			cipher.init(Cipher.ENCRYPT_MODE, cipherKey, new IvParameterSpec(iv));

			try (InputStream in = new BufferedInputStream(new FileInputStream(toEncrypt));
				 OutputStream out = new BufferedOutputStream(new FileOutputStream(encryptedFile));
				 OutputStream cipherOut = new BufferedOutputStream(new CipherOutputStream(out, cipher))) {

				out.write(iv);
				IOUtils.copy(in, cipherOut);
			} catch (Exception e) {
				throw new RuntimeException("Cannot encrypt '" + toEncrypt.getName() + "'", e);
			}
		} catch (Exception e) {
			throw new RuntimeException("Cannot encrypt '" + toEncrypt.getName() + "'", e);
		}

		return encryptedFile;
	}

	//
	// Decrypt old String or List<String>
	//

	public String decryptWithOldWayAppKey(String encryptedText) {
		return decryptWithOldWayAppKey(encryptedText, OLD_DEFAULT_ENCRYPTION_ALGORITHM);
	}

	public Object decryptWithOldWayAppKey(Object encryptedText) {
		if (encryptedText instanceof String) {
			return decryptWithOldWayAppKey((String) encryptedText);

		} else if (encryptedText instanceof List) {
			List<Object> list = (List<Object>) encryptedText;
			List<Object> decryptedValues = new ArrayList<>();

			for (Object item : list) {
				decryptedValues.add(decryptWithOldWayAppKey(item));
			}

			return decryptedValues;

		} else {
			throw new IllegalArgumentException("Unsupported element of class '" + encryptedText.getClass().getName() + "'");
		}

	}

	public String decryptWithOldWayAppKey(String encryptedBase64, String algorithm) {
		try {
			Cipher cipher = Cipher.getInstance(algorithm);
			cipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(key, "AES"), new IvParameterSpec(iv));
			byte[] decryptedText = cipher.doFinal(Base64.decodeBase64(encryptedBase64));
			return new String(decryptedText);
		} catch (Exception e) {
			if (lostPreviousKey || Toggle.LOST_PRIVATE_KEY.isEnabled()) {
				return encryptedBase64;
			}
			throw new RuntimeException("Cannot decrypt '" + encryptedBase64 + "'", e);
		}
	}

	//
	// Decrypt String or List<String>
	//

	public Object decryptWithAppKey(Object toDecrypt) {
		return decrypt(toDecrypt, key);
	}

	public Object decrypt(Object toDecrypt, Object key) {
		if (toDecrypt == null) {
			return null;
		}

		if (toDecrypt instanceof List) {
			List<Object> list = (List<Object>) toDecrypt;
			List<Object> decryptedValues = new ArrayList<>();
			for (Object item : list) {
				decryptedValues.add(decrypt(item, key));
			}
			return decryptedValues;
		} else if (toDecrypt instanceof String) {
			return decrypt((String) toDecrypt, key);
		}

		throw new IllegalArgumentException("Unsupported element of class '" + toDecrypt.getClass().getName() + "'");
	}

	private String decrypt(String toDecrypt, Object key) {
		try {
			byte[] iv = new byte[16];
			System.arraycopy(toDecrypt.getBytes(), 0, iv, 0, iv.length);

			Cipher cipher = Cipher.getInstance(EncryptionAlgorithm.AES.getAlgorithm());
			Key cipherKey = key instanceof byte[] ? new SecretKeySpec((byte[]) key, "AES") : (Key) key;
			cipher.init(Cipher.DECRYPT_MODE, cipherKey, new IvParameterSpec(iv));

			byte[] decryptedText = cipher.doFinal(Base64.decodeBase64(toDecrypt));

			byte[] result = new byte[decryptedText.length - 16];
			System.arraycopy(decryptedText, 16, result, 0, result.length);
			return new String(result);
		} catch (Exception e) {
			throw new RuntimeException("Cannot decrypt '" + toDecrypt + "'", e);
		}
	}

	//
	// Decrypt File
	//

	public File decryptWithAppKey(File toDecrypt, File decryptedFile) {
		return decrypt(toDecrypt, decryptedFile, key);
	}

	public File decrypt(File toDecrypt, File decryptedFile, Object key) {
		if (toDecrypt == null) {
			return null;
		}

		try (InputStream in = new BufferedInputStream(new FileInputStream(toDecrypt))) {
			byte[] iv = new byte[16];
			in.read(iv);

			Cipher cipher = Cipher.getInstance(EncryptionAlgorithm.AES.getAlgorithm());
			Key cipherKey = key instanceof byte[] ? new SecretKeySpec((byte[]) key, "AES") : (Key) key;
			cipher.init(Cipher.DECRYPT_MODE, cipherKey, new IvParameterSpec(iv));

			try (InputStream cipherIn = new BufferedInputStream(new CipherInputStream(in, cipher));
				 OutputStream out = new BufferedOutputStream(new FileOutputStream(decryptedFile))) {

				IOUtils.copy(cipherIn, out);
			} catch (Exception e) {
				throw new RuntimeException("Cannot decrypt '" + toDecrypt.getName() + "'", e);
			}
		} catch (Exception e) {
			throw new RuntimeException("Cannot decrypt '" + toDecrypt.getName() + "'", e);
		}

		return decryptedFile;
	}

	//
	// RSA
	//

	public String encryptKey(Key toEncrypt, PublicKey key) {
		if (toEncrypt == null) {
			return null;
		}

		try {
			Cipher cipher = Cipher.getInstance(EncryptionAlgorithm.RSA.getAlgorithm());
			cipher.init(Cipher.ENCRYPT_MODE, key);

			byte[] encryptedKey = cipher.doFinal(toEncrypt.getEncoded());
			return Base64.encodeBase64String(encryptedKey);
		} catch (Exception e) {
			throw new RuntimeException("Cannot encrypt key.", e);
		}
	}

	public Key decryptKey(String toDecrypt, PrivateKey key) {
		if (toDecrypt == null) {
			return null;
		}

		try {
			Cipher cipher = Cipher.getInstance(EncryptionAlgorithm.RSA.getAlgorithm());
			cipher.init(Cipher.DECRYPT_MODE, key);

			byte[] decryptedKey = cipher.doFinal(Base64.decodeBase64(toDecrypt.getBytes()));
			return new SecretKeySpec(decryptedKey, "AES");
		} catch (Exception e) {
			throw new RuntimeException("Cannot decrypt key.", e);
		}
	}

	public PrivateKey createPrivateKeyFromFile(File key) {
		PrivateKey privateKey;
		try {
			KeyFactory keyFactory = KeyFactory.getInstance("RSA");
			privateKey = keyFactory.generatePrivate(new PKCS8EncodedKeySpec(readKeyFile(key)));
		} catch (Exception e) {
			throw new RuntimeException("Cannot retrieve private key.", e);
		}

		return privateKey;
	}

	public PublicKey createPublicKeyFromFile(File key) {
		PublicKey publicKey;
		try {
			KeyFactory keyFactory = KeyFactory.getInstance("RSA");
			publicKey = keyFactory.generatePublic(new X509EncodedKeySpec(readKeyFile(key)));
		} catch (Exception e) {
			throw new RuntimeException("Cannot retrieve public key.", e);
		}

		return publicKey;
	}

	private byte[] readKeyFile(File key) {
		try {
			String data = new String(FileUtils.readFileToByteArray(key));
			data = data.replaceAll("\\n", "")
					.replaceAll("-----BEGIN PUBLIC KEY-----", "")
					.replaceAll("-----END PUBLIC KEY-----", "")
					.replaceAll("-----BEGIN PRIVATE KEY-----", "")
					.replaceAll("-----END PRIVATE KEY-----", "");
			return Base64.decodeBase64(data);
		} catch (Exception e) {
			throw new RuntimeException("Cannot read key file.", e);
		}
	}
}