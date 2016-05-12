package com.constellio.sdk;

import java.io.IOException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

import com.constellio.model.services.encrypt.EncryptionKeyFactory;
import com.constellio.model.services.encrypt.EncryptionServices;

/**
 * Created by dakota on 11/9/15.
 */
public class FakeEncryptionServices extends EncryptionServices {

	@Override
	public EncryptionServices withKey(Key key)
			throws NoSuchAlgorithmException, InvalidKeySpecException, IOException {
		super.withKey(EncryptionKeyFactory.newApplicationKey("test", "test"));
		return this;
	}

	@Override
	public String encrypt(String toEncrypt) {
		return "crypted:" + toEncrypt;
	}

	@Override
	public String decrypt(String encryptedBase64, String algorithm) {
		if (encryptedBase64 == null) {
			return null;
		} else {
			return encryptedBase64.replace("crypted:", "");
		}
	}

}