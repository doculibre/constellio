package com.constellio.model.services.users;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import com.constellio.model.services.encrypt.EncryptionServices;

public class FakeEncryptionServicesUtils extends EncryptionServices {
	public FakeEncryptionServicesUtils()
			throws NoSuchAlgorithmException, InvalidKeySpecException, IOException {
		super(true);
	}

	@Override
	public String encrypt(String toEncrypt) {
		return "$" + toEncrypt;
	}

	@Override
	public String decrypt(String encryptedText) {
		return encryptedText.substring(1);
	}

	public static EncryptionServices create() {
		EncryptionServices encryptionServices = Mockito.mock(EncryptionServices.class);
		when(encryptionServices.decrypt(anyString())).thenAnswer(new Answer<String>() {
			@Override
			public String answer(InvocationOnMock invocation)
					throws Throwable {
				String encryptedText = (String) invocation.getArguments()[0];
				return encryptedText.substring(1);
			}
		});

		when(encryptionServices.encrypt(anyString())).thenAnswer(new Answer<String>() {
			@Override
			public String answer(InvocationOnMock invocation)
					throws Throwable {
				String toEncrypt = (String) invocation.getArguments()[0];
				return "$" + toEncrypt;
			}
		});
		return encryptionServices;
	}
}
