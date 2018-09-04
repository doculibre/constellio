package com.constellio.app.modules.restapi.core.util;

import org.junit.Test;

import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;

public class HashingUtilsTest {

	private static final String data = "This is a test";
	private static final String key = "secret key";
	private static final String expectedHmacSha256Base64UrlHash = "JQbGgA-RpmlT2GDh-S9MbOuMtdEPbTpz54-Pr6r-89k";

	private static final String dataUTF8 = "This is a Ω test";
	private static final String expectedMd5Hash = "633a5f7d19b613c9a4cab058db0c12d0";

	@Test
	public void testHmacSha256Base64UrlEncoded() throws Exception {
		String hash = HashingUtils.hmacSha256Base64UrlEncoded(key, data);

		assertThat(hash).doesNotContain("+").doesNotContain("/").isEqualTo(expectedHmacSha256Base64UrlHash);
		assertThat(hash.endsWith("=")).isFalse();
	}

	@Test
	public void testMd5WithString() throws Exception {
		String hash = HashingUtils.md5(dataUTF8);

		assertThat(hash).isEqualTo(expectedMd5Hash);
	}

	@Test
	public void testMd5WithBytes() throws Exception {
		String hash = HashingUtils.md5(dataUTF8.getBytes(StandardCharsets.UTF_8));

		assertThat(hash).isEqualTo(expectedMd5Hash);
	}

}
