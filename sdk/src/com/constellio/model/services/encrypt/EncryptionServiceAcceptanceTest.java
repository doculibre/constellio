package com.constellio.model.services.encrypt;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Before;
import org.junit.Test;

import com.constellio.sdk.tests.ConstellioTest;

public class EncryptionServiceAcceptanceTest extends ConstellioTest {
	EncryptionServices encryptionService;

	@Before
	public void setUp()
			throws Exception {
		prepareSystem(
				withZeCollection()
		);
		encryptionService = getModelLayerFactory().newEncryptionServices();
	}

	@Test
	public void givenEncryptedValueWhenDecryptThenOk()
			throws Exception {
		String text = " www.java2s.com ";

		String encryptedText = encryptionService.encrypt(text);

		assertThat(encryptionService.decrypt(encryptedText)).isEqualTo(text);
	}

	@Test
	public void givenEncryptedValueWhenDecryptThenOk2()
			throws Exception {
		String text = " www.java2s.com ";

		String encryptedText = encryptionService.encrypt(text);

		assertThat(encryptionService.decrypt(encryptedText)).isEqualTo(text);
	}

	@Test
	public void givenEncryptedValueWhenDecryptThenOk3()
			throws Exception {
		String text = " www.java2s.com ";

		String encryptedText = encryptionService.encrypt(text);

		assertThat(encryptionService.decrypt(encryptedText)).isEqualTo(text);
	}

	@Test
	public void givenEncryptedValueWhenDecryptThenOk4()
			throws Exception {
		String text = " www.java2s.com ";

		String encryptedText = encryptionService.encrypt(text);

		assertThat(encryptionService.decrypt(encryptedText)).isEqualTo(text);
	}

	@Test
	public void givenNullEncryptedValueWhenEncryptThenNullReturned()
			throws Exception {
		String text = null;

		String encryptedText = encryptionService.encrypt(text);

		assertThat(encryptedText).isNull();
	}
}
