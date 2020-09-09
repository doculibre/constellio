package com.constellio.model.services.encrypt;

import com.constellio.app.modules.restapi.core.util.HashingUtils;
import com.constellio.sdk.tests.ConstellioTest;
import org.apache.commons.io.IOUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.security.Key;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;

public class EncryptionServiceAcceptanceTest extends ConstellioTest {

	private File tempFolder;

	private EncryptionServices encryptionService;

	@Before
	public void setUp() throws Exception {
		prepareSystem(
				withZeCollection()
		);
		encryptionService = getModelLayerFactory().newEncryptionServices();

		tempFolder = newTempFolder();
	}

	@After
	public void tearDown() throws Exception {
		tempFolder.delete();
	}

	//
	// App key encryption
	//

	@Test
	public void whenEncryptingStringWithAppKey() {
		String original = " www.java2s.com ";

		String encrypted = (String) encryptionService.encryptWithAppKey(original);
		assertThat(encrypted).isNotEqualTo(original);

		String decrypted = (String) encryptionService.decryptWithAppKey(encrypted);
		assertThat(decrypted).isEqualTo(original);
	}

	@Test
	public void whenEncryptingListWithAppKey() {
		List<String> original = new ArrayList<>();
		original.add(" www.java2s.com ");
		original.add(" www.java3s.com ");
		original.add(" www.java4s.com ");

		List<String> encrypted = (List<String>) encryptionService.encryptWithAppKey(original);
		assertThat(encrypted).isNotEqualTo(original);

		List<String> decrypted = (List<String>) encryptionService.decryptWithAppKey(encrypted);
		assertThat(decrypted).isEqualTo(original);
	}

	@Test
	public void whenEncryptingFileWithAppKey() throws Exception {
		File original = getTestResourceFile("textFile.txt");


		File encrypted = encryptionService.encryptWithAppKey(original, new File(tempFolder.getPath() + "\\encrypted.txt"));
		assertThat(getFileChecksum(encrypted)).isNotEqualTo(getFileChecksum(original));

		File decrypted = encryptionService.decryptWithAppKey(encrypted, new File(tempFolder.getPath() + "\\decrypted.txt"));
		assertThat(getFileChecksum(decrypted)).isEqualTo(getFileChecksum(original));
	}

	@Test
	public void whenEncryptingNullContentWithAppKey() {
		Object encrypted = encryptionService.encryptWithAppKey(null);
		assertThat(encrypted).isNull();
	}

	@Test
	public void whenEncryptingNullContentAsFileWithAppKey() {
		Object encrypted = encryptionService.encryptWithAppKey(null, null);
		assertThat(encrypted).isNull();
	}

	@Test
	public void whenDecryptingNullContentWithAppKey() {
		Object decrypted = encryptionService.decryptWithAppKey(null);
		assertThat(decrypted).isNull();
	}

	@Test
	public void whenDecryptingNullContentAsFileWithAppKey() {
		Object decrypted = encryptionService.decryptWithAppKey(null, null);
		assertThat(decrypted).isNull();
	}

	@Test
	public void whenEncryptingWithAppKeyAndDecryptingWithAnotherKey() {
		String original = " www.java2s.com ";

		String encrypted = (String) encryptionService.encryptWithAppKey(original);
		assertThat(encrypted).isNotEqualTo(original);

		try {
			String decrypted = (String) encryptionService.decryptVersion2(encrypted, encryptionService.generateAESKey());
			assertThat(decrypted).isNotEqualTo(original);
		} catch (Exception ignored) {
		}
	}

	@Test
	public void whenEncryptingFileWithAppKeyAndDecryptingWithAnotherKey() throws Exception {
		File original = getTestResourceFile("textFile.txt");

		File encrypted = encryptionService.encryptWithAppKey(original, new File(tempFolder.getPath() + "\\encrypted.txt"));
		assertThat(getFileChecksum(encrypted)).isNotEqualTo(getFileChecksum(original));

		try {
			File decrypted = encryptionService.decryptVersion2(encrypted, new File(tempFolder.getPath() + "\\decrypted.txt"), encryptionService.generateAESKey());
			assertThat(getFileChecksum(decrypted)).isNotEqualTo(getFileChecksum(original));
		} catch (Exception ignored) {
		}
	}

	@Test
	public void whenEncryptingWithAnotherKeyAndDecryptingWithAppKey() {
		String original = " www.java2s.com ";

		String encrypted = (String) encryptionService.encrypt(original, encryptionService.generateAESKey());
		assertThat(encrypted).isNotEqualTo(original);

		try {
			String decrypted = (String) encryptionService.decryptWithAppKey(encrypted);
			assertThat(decrypted).isNotEqualTo(original);
		} catch (Exception ignored) {
		}
	}

	@Test
	public void whenEncryptingFileWithAnotherKeyAndDecryptingWithAppKey() throws Exception {
		File original = getTestResourceFile("textFile.txt");

		File encrypted = encryptionService.encrypt(original, new File(tempFolder.getPath() + "\\encrypted.txt"), encryptionService.generateAESKey());
		assertThat(getFileChecksum(encrypted)).isNotEqualTo(getFileChecksum(original));

		try {
			File decrypted = encryptionService.decryptWithAppKey(encrypted, new File(tempFolder.getPath() + "\\decrypted.txt"));
			assertThat(getFileChecksum(decrypted)).isNotEqualTo(getFileChecksum(original));
		} catch (Exception ignored) {
		}
	}

	//
	// AES encryption
	//

	@Test
	public void whenEncryptingStringWithAES() {
		Key key = encryptionService.generateAESKey();
		String original = " www.java2s.com ";

		String encrypted = (String) encryptionService.encrypt(original, key);
		assertThat(encrypted).isNotEqualTo(original);

		String decrypted = (String) encryptionService.decryptVersion2(encrypted, key);
		assertThat(decrypted).isEqualTo(original);
	}

	@Test
	public void whenEncryptingListWithAES() {
		Key key = encryptionService.generateAESKey();
		List<String> original = new ArrayList<>();
		original.add(" www.java2s.com ");
		original.add(" www.java3s.com ");
		original.add(" www.java4s.com ");

		List<String> encrypted = (List<String>) encryptionService.encrypt(original, key);
		assertThat(encrypted).isNotEqualTo(original);

		List<String> decrypted = (List<String>) encryptionService.decryptVersion2(encrypted, key);
		assertThat(decrypted).isEqualTo(original);
	}

	@Test
	public void whenEncryptingFileWithAES() throws Exception {
		Key key = encryptionService.generateAESKey();
		File original = getTestResourceFile("textFile.txt");

		File encrypted = encryptionService.encrypt(original, new File(tempFolder.getPath() + "\\encrypted.txt"), key);
		assertThat(getFileChecksum(encrypted)).isNotEqualTo(getFileChecksum(original));

		File decrypted = encryptionService.decryptVersion2(encrypted, new File(tempFolder.getPath() + "\\decrypted.txt"), key);
		assertThat(getFileChecksum(decrypted)).isEqualTo(getFileChecksum(original));
	}

	@Test
	public void whenEncryptingNullContentWithAES() {
		Key key = encryptionService.generateAESKey();

		Object encrypted = encryptionService.encrypt(null, key);
		assertThat(encrypted).isNull();
	}

	@Test
	public void whenEncryptingNullContentAsFileWithAES() {
		Key key = encryptionService.generateAESKey();

		Object encrypted = encryptionService.encrypt(null, null, key);
		assertThat(encrypted).isNull();
	}

	@Test
	public void whenEncryptingWithAESWithNullKey() {
		String original = " www.java2s.com ";

		try {
			encryptionService.encrypt(original, null);
			fail("whenEncryptingWithAESWithNullKey should throw an exception");
		} catch (Exception ignored) {
		}
	}

	@Test
	public void whenEncryptingFileWithAESWithNullKey() {
		File original = getTestResourceFile("textFile.txt");

		try {
			encryptionService.encrypt(original, null, null);
			fail("whenEncryptingFileWithAESWithNullKey should throw an exception");
		} catch (Exception ignored) {
		}
	}

	@Test
	public void whenDecryptingNullContentWithAES() {
		Key key = encryptionService.generateAESKey();

		Object decrypted = encryptionService.decryptVersion2(null, key);
		assertThat(decrypted).isNull();
	}

	@Test
	public void whenDecryptingNullContentAsFileWithAES() {
		Key key = encryptionService.generateAESKey();

		Object decrypted = encryptionService.decryptVersion2(null, null, key);
		assertThat(decrypted).isNull();
	}

	@Test
	public void whenDecryptingWithAESWithNullKey() {
		String original = " www.java2s.com ";

		try {
			encryptionService.decryptVersion2(original, null);
			fail("whenDecryptingWithAESWithNullKey should throw an exception");
		} catch (Exception ignored) {
		}
	}

	@Test
	public void whenDecryptingFileWithAESWithNullKey() {
		File original = getTestResourceFile("textFile.txt");

		try {
			encryptionService.decryptVersion2(original, null, null);
			fail("whenDecryptingFileWithAESWithNullKey should throw an exception");
		} catch (Exception ignored) {
		}
	}

	@Test
	public void whenEncryptingWithAESAndDecryptingWithAnotherKey() {
		String original = " www.java2s.com ";

		String encrypted = (String) encryptionService.encrypt(original, encryptionService.generateAESKey());
		assertThat(encrypted).isNotEqualTo(original);

		try {
			String decrypted = (String) encryptionService.decryptVersion2(encrypted, encryptionService.generateAESKey());
			assertThat(decrypted).isNotEqualTo(original);
		} catch (Exception ignored) {
		}
	}

	@Test
	public void whenEncryptingFileWithAESAndDecryptingWithAnotherKey() throws Exception {
		File original = getTestResourceFile("textFile.txt");

		File encrypted = encryptionService.encrypt(original, new File(tempFolder.getPath() + "\\encrypted.txt"), encryptionService.generateAESKey());
		assertThat(getFileChecksum(encrypted)).isNotEqualTo(getFileChecksum(original));

		try {
			File decrypted = encryptionService.decryptVersion2(encrypted, new File(tempFolder.getPath() + "\\decrypted.txt"), encryptionService.generateAESKey());
			assertThat(getFileChecksum(decrypted)).isNotEqualTo(getFileChecksum(original));
		} catch (Exception ignored) {
		}
	}

	//
	// RSA encryption
	//

	@Test
	public void whenEncryptingKeyWithRSA() {
		Key key = encryptionService.generateAESKey();

		File publicKeyFile = getTestResourceFile("public_key.pem");
		PublicKey publicKey = encryptionService.createPublicKeyFromFile(publicKeyFile);

		String encrypted = encryptionService.encryptKey(key, publicKey);
		assertThat(encrypted).isNotEqualTo(key.getEncoded());

		File privateKeyFile = getTestResourceFile("private_key_pkcs8.pem");
		PrivateKey privateKey = encryptionService.createPrivateKeyFromFile(privateKeyFile);

		Key decrypted = encryptionService.decryptKey(encrypted, privateKey);
		assertThat(decrypted.getEncoded()).isEqualTo(key.getEncoded());
	}

	@Test
	public void whenEncryptingNullContentWithRSA() {
		File publicKeyFile = getTestResourceFile("public_key.pem");
		PublicKey publicKey = encryptionService.createPublicKeyFromFile(publicKeyFile);

		String encrypted = encryptionService.encryptKey(null, publicKey);
		assertThat(encrypted).isNull();
	}

	@Test
	public void whenEncryptingWithRSAWithNullKey() {
		Key key = encryptionService.generateAESKey();

		try {
			encryptionService.encryptKey(key, null);
			fail("whenEncryptingWithRSAWithNullKey should throw an exception");
		} catch (Exception ignored) {
		}
	}

	@Test
	public void whenDecryptingNullContentWithRSA() {
		File privateKeyFile = getTestResourceFile("private_key_pkcs8.pem");
		PrivateKey privateKey = encryptionService.createPrivateKeyFromFile(privateKeyFile);

		Key decrypted = encryptionService.decryptKey(null, privateKey);
		assertThat(decrypted).isNull();
	}

	@Test
	public void whenDecryptingWithRSAWithNullKey() {
		try {
			encryptionService.decryptKey("key", null);
			fail("whenDecryptingWithRSAWithNullKey should throw an exception");
		} catch (Exception ignored) {
		}
	}

	@Test
	public void whenEncryptingWithRSAAndDecryptingWithInvalidKey() {
		Key key = encryptionService.generateAESKey();

		File publicKeyFile = getTestResourceFile("public_key.pem");
		PublicKey publicKey = encryptionService.createPublicKeyFromFile(publicKeyFile);

		String encrypted = encryptionService.encryptKey(key, publicKey);
		assertThat(encrypted).isNotEqualTo(key.getEncoded());

		File privateKeyFile = getTestResourceFile("other_private_key_pkcs8.pem");
		PrivateKey privateKey = encryptionService.createPrivateKeyFromFile(privateKeyFile);

		try {
			Key decrypted = encryptionService.decryptKey(encrypted, privateKey);
			assertThat(decrypted.getEncoded()).isNotEqualTo(key.getEncoded());
		} catch (Exception ignored) {
		}
	}

	//
	// Private methods
	//

	private String getFileChecksum(File file) throws Exception {
		try (InputStream in = new BufferedInputStream(new FileInputStream(file))) {
			byte[] fileData = IOUtils.toByteArray(in);
			return HashingUtils.md5(fileData);
		}
	}
}