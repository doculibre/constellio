package com.constellio.data.io;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Before;
import org.junit.Test;

import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.TestUtils;

public class EncodingServiceAcceptanceTest extends ConstellioTest {

	EncodingService encodingService;

	byte[] bytes1ko;

	byte[] bytes10ko;

	byte[] bytes100ko;

	@Before
	public void setUp() {
		encodingService = new EncodingService();
		bytes1ko = TestUtils.aRandomByteArray(1024);
		bytes10ko = TestUtils.aRandomByteArray(10 * 1024);
		bytes100ko = TestUtils.aRandomByteArray(100 * 1024);
	}

	@Test
	public void givenEncodedValueInBase64WhenDecodingThenRawValueObtained() {

		String encoded1Ko = encodingService.encodeToBase64(bytes1ko);
		String encoded10ko = encodingService.encodeToBase64(bytes10ko);
		String encoded100ko = encodingService.encodeToBase64(bytes100ko);

		assertThat(encodingService.decodeStringToBase64Bytes(encoded1Ko)).isEqualTo(bytes1ko);
		assertThat(encodingService.decodeStringToBase64Bytes(encoded10ko)).isEqualTo(bytes10ko);
		assertThat(encodingService.decodeStringToBase64Bytes(encoded100ko)).isEqualTo(bytes100ko);
	}

	@Test
	public void givenEncodedValueInBase32WhenDecodingThenRawValueObtained() {

		String encoded1Ko = encodingService.encodeToBase32(bytes1ko);
		String encoded10ko = encodingService.encodeToBase32(bytes10ko);
		String encoded100ko = encodingService.encodeToBase32(bytes100ko);

		assertThat(encodingService.decodeStringToBase32Bytes(encoded1Ko)).isEqualTo(bytes1ko);
		assertThat(encodingService.decodeStringToBase32Bytes(encoded10ko)).isEqualTo(bytes10ko);
		assertThat(encodingService.decodeStringToBase32Bytes(encoded100ko)).isEqualTo(bytes100ko);
	}

}
