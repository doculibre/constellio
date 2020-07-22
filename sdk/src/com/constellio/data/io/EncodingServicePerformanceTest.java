package com.constellio.data.io;

import com.constellio.sdk.tests.ConstellioTestWithGlobalContext;
import com.constellio.sdk.tests.TestUtils;
import com.constellio.sdk.tests.annotations.PerformanceTest;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import static org.assertj.core.api.Assertions.assertThat;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@PerformanceTest
// Confirm @SlowTest
public class EncodingServicePerformanceTest extends ConstellioTestWithGlobalContext {

	static byte[] bytes1ko;
	static byte[] bytes10ko;
	static byte[] bytes100ko;
	static byte[] bytes1mo;
	static byte[] bytes10mo;
	static byte[] bytes100mo;
	static byte[] bytes500mo;
	//@Rule public TestRule benchmarkRun = new BenchmarkRule();
	EncodingService encodingService;

	@Test
	public void __prepareTests__() {
		bytes1ko = TestUtils.aRandomByteArray(1024);
		bytes10ko = TestUtils.aRandomByteArray(10 * 1024);
		bytes100ko = TestUtils.aRandomByteArray(100 * 1024);
		bytes1mo = TestUtils.aRandomByteArray(1024 * 1024);
		bytes10mo = TestUtils.aRandomByteArray(10 * 1024 * 1024);
		bytes100mo = TestUtils.aRandomByteArray(100 * 1024 * 1024);
		bytes500mo = TestUtils.aRandomByteArray(500 * 1024 * 1024);
	}

	@Before
	public void setUp() {
		encodingService = new EncodingService();
	}

	@Test
	public void givenEncodedValueWhenDecodingThenRawValueObtained() {

		String encoded1Ko = encodingService.encodeToBase64(bytes1ko);
		String encoded10ko = encodingService.encodeToBase64(bytes10ko);
		String encoded100ko = encodingService.encodeToBase64(bytes100ko);
		String encoded1mo = encodingService.encodeToBase64(bytes1mo);

		assertThat(encodingService.decodeStringToBase64Bytes(encoded1Ko)).isEqualTo(bytes1ko);
		assertThat(encodingService.decodeStringToBase64Bytes(encoded10ko)).isEqualTo(bytes10ko);
		assertThat(encodingService.decodeStringToBase64Bytes(encoded100ko)).isEqualTo(bytes100ko);
		assertThat(encodingService.decodeStringToBase64Bytes(encoded1mo)).isEqualTo(bytes1mo);
	}

	@Test
	public void whenEncoding1koThenFastEnough()
			throws Exception {
		encodingService.encodeToBase64(bytes1ko);
	}

	@Test
	public void whenEncoding10koThenFastEnough()
			throws Exception {
		encodingService.encodeToBase64(bytes10ko);
	}

	@Test
	public void whenEncoding100koThenFastEnough()
			throws Exception {
		encodingService.encodeToBase64(bytes100ko);
	}

	@Test
	public void whenEncoding1moThenFastEnough()
			throws Exception {
		encodingService.encodeToBase64(bytes1mo);
	}

	@Test
	public void whenEncoding10moThenFastEnough()
			throws Exception {
		encodingService.encodeToBase64(bytes10mo);
	}

}
