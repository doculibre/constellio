package com.constellio.sdk;

import com.constellio.model.services.encrypt.EncryptionKeyFactory;
import com.constellio.model.services.encrypt.EncryptionServices;

import java.io.IOException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.List;

import static java.util.stream.Collectors.toList;

/**
 * Created by dakota on 11/9/15.
 */
public class FakeEncryptionServices extends EncryptionServices {

	public FakeEncryptionServices() {
		super(true);
		try {
			withKey(EncryptionKeyFactory.newApplicationKey("test", "test"));
		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException(e);
		} catch (InvalidKeySpecException e) {
			throw new RuntimeException(e);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public EncryptionServices withKey(Key key)
			throws NoSuchAlgorithmException, InvalidKeySpecException, IOException {
		return this;
	}

	@Override
	public Object encryptWithAppKey(Object toEncrypt) {
		if (toEncrypt instanceof String) {
			return "crypted:" + toEncrypt;
		} else if (toEncrypt instanceof List) {
			return ((List) toEncrypt).stream().map(this::encryptWithAppKey).collect(toList());
		} else {
			return null;
		}
	}

	@Override
	public Object decryptWithAppKey(Object encryptedBase64) {
		if (encryptedBase64 == null) {
			return null;
		} else if (encryptedBase64 instanceof List) {
			return ((List) encryptedBase64).stream().map(this::decryptWithAppKey).collect(toList());
		} else {
			return ((String) encryptedBase64).replace("crypted:", "");
		}
	}

	@Override
	public boolean isInitialized() {
		return true;
	}
}