package com.constellio.model.services.users;

import com.constellio.model.services.encrypt.EncryptionServices;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

public class FakeEncryptionServicesUtils extends EncryptionServices {
	public FakeEncryptionServicesUtils()
			throws NoSuchAlgorithmException, InvalidKeySpecException, IOException {
		super(true);
	}

	@Override
	public Object encryptWithAppKey(Object toEncrypt) {
		return "$" + toEncrypt;
	}

	@Override
	public Object decryptWithAppKey(Object encryptedText) {
		return ((String) encryptedText).substring(1);
	}

	public static EncryptionServices create() {
		EncryptionServices encryptionServices = Mockito.mock(EncryptionServices.class);
		when(encryptionServices.decryptWithAppKey(anyString())).thenAnswer(new Answer<String>() {
			@Override
			public String answer(InvocationOnMock invocation)
					throws Throwable {
				String encryptedText = (String) invocation.getArguments()[0];
				return encryptedText.substring(1);
			}
		});

		when(encryptionServices.encryptWithAppKey(anyString())).thenAnswer(new Answer<String>() {
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
