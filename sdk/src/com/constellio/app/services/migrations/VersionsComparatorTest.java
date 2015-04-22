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
package com.constellio.app.services.migrations;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Before;
import org.junit.Test;

import com.constellio.sdk.tests.ConstellioTest;

public class VersionsComparatorTest extends ConstellioTest {
	VersionsComparator versionsComparator;

	String version111 = "1.1.1";
	String version1 = "1";
	String version213 = "2.1.3";
	String version254 = "2.5.4";
	String version4 = "4";
	String version011 = "0.1.1";
	String version001 = "0.0.1";

	@Before
	public void setUp() {
		versionsComparator = new VersionsComparator();
	}

	@Test
	public void whenCompareTwoVersionsThenZeroIfEqualsOneIfGreaterNegativeOneIfLower() {

		assertThat(versionsComparator.compare(version1, version4)).isEqualTo(-1);
		assertThat(versionsComparator.compare(version1, version111)).isEqualTo(-1);
		assertThat(versionsComparator.compare(version111, version4)).isEqualTo(-1);
		assertThat(versionsComparator.compare(version1, version001)).isEqualTo(1);
		assertThat(versionsComparator.compare(version001, version011)).isEqualTo(-1);
		assertThat(versionsComparator.compare(version213, version254)).isEqualTo(-1);
		assertThat(versionsComparator.compare(version1, version1)).isEqualTo(0);
		assertThat(versionsComparator.compare(version1, version254)).isEqualTo(-1);
		assertThat(versionsComparator.compare(version254, version1)).isEqualTo(1);

	}

	@Test
	public void whenComparingTwoVersionsUsingStaticMethodThenCorrect()
			throws Exception {
		assertThat(VersionsComparator.isFirstVersionBeforeOrEqualToSecond("5.0.1", "5.1.1")).isTrue();
		assertThat(VersionsComparator.isFirstVersionBeforeOrEqualToSecond("5.0.1", "5.0.1")).isTrue();
		assertThat(VersionsComparator.isFirstVersionBeforeOrEqualToSecond("5.1.1", "5.0.1")).isFalse();
	}

}
