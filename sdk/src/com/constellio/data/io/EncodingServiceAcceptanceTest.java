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
	public void givenEncodedValueWhenDecodingThenRawValueObtained() {

		String encoded1Ko = encodingService.encodeToBase64(bytes1ko);
		String encoded10ko = encodingService.encodeToBase64(bytes10ko);
		String encoded100ko = encodingService.encodeToBase64(bytes100ko);

		assertThat(encodingService.decodeStringToBase64Bytes(encoded1Ko)).isEqualTo(bytes1ko);
		assertThat(encodingService.decodeStringToBase64Bytes(encoded10ko)).isEqualTo(bytes10ko);
		assertThat(encodingService.decodeStringToBase64Bytes(encoded100ko)).isEqualTo(bytes100ko);
	}

}
