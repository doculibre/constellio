package com.constellio.model.services.encrypt;

import java.io.File;
import java.io.IOException;
import java.security.Key;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

import org.apache.commons.io.FileUtils;

import com.constellio.data.dao.services.bigVault.solr.BigVaultException;
import com.constellio.model.services.factories.ModelLayerFactory;

public class EncryptionKeyFactory {
	private static final String KEY_CLASS_PART = "constellio_class_key_part";

	public static Key getApplicationKey(ModelLayerFactory modelLayerFactory) {
		try {
			String salt = KEY_CLASS_PART + getSolrKey(modelLayerFactory);
			String password = getFileKey(modelLayerFactory);
			return newApplicationKey(password, salt);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public static Key newApplicationKey(String password, String salt) {
		try {
			SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
			PBEKeySpec pbeKeySpec = new PBEKeySpec(password.toCharArray(), salt.getBytes(), 1024, 256);
			return factory.generateSecret(pbeKeySpec);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private static byte[] getKeyBytes(ModelLayerFactory modelLayerFactory)
			throws IOException, BigVaultException {
		String key = getFileKey(modelLayerFactory) + getSolrKey(modelLayerFactory) + KEY_CLASS_PART;
		return key.getBytes();
	}

	private static String getSolrKey(ModelLayerFactory modelLayerFactory)
			throws BigVaultException {
		return modelLayerFactory.getDataLayerFactory().readEncryptionKey();
	}

	private static String getFileKey(ModelLayerFactory modelLayerFactory)
			throws IOException {
		File encryptionFile = modelLayerFactory.getConfiguration().getConstellioEncryptionFile();
		return FileUtils.readFileToString(encryptionFile);
	}

}
