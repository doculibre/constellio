/*Constellio Enterprise Information Management

Copyright (c) 2015 "Constellio inc."

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as
published by the Free Software Foundation, either version 3 of the
License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program. If not, see <http://www.gnu.org/licenses/>.
*/
package com.constellio.data.io;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.junit.runners.MethodSorters;

import com.carrotsearch.junitbenchmarks.BenchmarkRule;
import com.constellio.sdk.tests.ConstellioTestWithGlobalContext;
import com.constellio.sdk.tests.TestUtils;
import com.constellio.sdk.tests.annotations.PerformanceTest;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@PerformanceTest
public class EncodingServicePerformanceTest extends ConstellioTestWithGlobalContext {

	static byte[] bytes1ko;
	static byte[] bytes10ko;
	static byte[] bytes100ko;
	static byte[] bytes1mo;
	static byte[] bytes10mo;
	static byte[] bytes100mo;
	static byte[] bytes500mo;
	@Rule public TestRule benchmarkRun = new BenchmarkRule();
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
