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

		assertThat(versionsComparator.compare("5.1-1", "5.1")).isEqualTo(1);
		assertThat(versionsComparator.compare("5.1", "5.1-1")).isEqualTo(-1);
		assertThat(versionsComparator.compare("5.1-1", "5.1-1")).isEqualTo(0);
		assertThat(versionsComparator.compare("5.1-2", "5.1-3")).isEqualTo(-1);
		assertThat(versionsComparator.compare("5.1-3", "5.1-1")).isEqualTo(1);
	}

	@Test
	public void whenComparingTwoVersionsUsingStaticMethodThenCorrect()
			throws Exception {
		assertThat(VersionsComparator.isFirstVersionBeforeOrEqualToSecond("5.0.1", "5.1.1")).isTrue();
		assertThat(VersionsComparator.isFirstVersionBeforeOrEqualToSecond("5.0.1", "5.0.1")).isTrue();
		assertThat(VersionsComparator.isFirstVersionBeforeOrEqualToSecond("5.1.1", "5.0.1")).isFalse();

		assertThat(VersionsComparator.isFirstVersionBeforeOrEqualToSecond("5.0.2.2", "5.0.2.1")).isFalse();
		assertThat(VersionsComparator.isFirstVersionBeforeOrEqualToSecond("5.0.2.2", "5.0.2.2")).isTrue();
		assertThat(VersionsComparator.isFirstVersionBeforeOrEqualToSecond("5.0.2.2", "5.0.2.3")).isTrue();
	}

	@Test
	public void whenComparingVersionsWithDifferentNumberOfPartsThenOk()
			throws Exception {
		assertThat(VersionsComparator.isFirstVersionBeforeOrEqualToSecond("5.0.1", "5.1.1.0")).isTrue();
		assertThat(VersionsComparator.isFirstVersionBeforeOrEqualToSecond("5.0.1.0", "5.1.1")).isTrue();
		assertThat(VersionsComparator.isFirstVersionBeforeOrEqualToSecond("5.0.1.0", "5.1.1.0")).isTrue();

		assertThat(VersionsComparator.isFirstVersionBeforeOrEqualToSecond("5.0.1", "5.0.1.0")).isTrue();
		assertThat(VersionsComparator.isFirstVersionBeforeOrEqualToSecond("5.0.1.0", "5.0.1")).isTrue();
		assertThat(VersionsComparator.isFirstVersionBeforeOrEqualToSecond("5.0.1.0", "5.0.1.0")).isTrue();

		assertThat(VersionsComparator.isFirstVersionBeforeOrEqualToSecond("5.1.1", "5.0.1.0")).isFalse();
		assertThat(VersionsComparator.isFirstVersionBeforeOrEqualToSecond("5.1.1.0", "5.0.1")).isFalse();
		assertThat(VersionsComparator.isFirstVersionBeforeOrEqualToSecond("5.1.1.0", "5.0.1.0")).isFalse();

		assertThat(VersionsComparator.isFirstVersionBeforeOrEqualToSecond("5.1.0.1", "5.1.1.1.0")).isTrue();
		assertThat(VersionsComparator.isFirstVersionBeforeOrEqualToSecond("5.1.0.1.0", "5.1.1.1")).isTrue();
		assertThat(VersionsComparator.isFirstVersionBeforeOrEqualToSecond("5.1.0.1.0", "5.1.1.1.0")).isTrue();

		assertThat(VersionsComparator.isFirstVersionBeforeOrEqualToSecond("5.1.0.1", "5.1.0.1.0")).isTrue();
		assertThat(VersionsComparator.isFirstVersionBeforeOrEqualToSecond("5.1.0.1.0", "5.1.0.1")).isTrue();
		assertThat(VersionsComparator.isFirstVersionBeforeOrEqualToSecond("5.1.0.1.0", "5.1.0.1.0")).isTrue();

		assertThat(VersionsComparator.isFirstVersionBeforeOrEqualToSecond("5.1.1.1", "5.1.0.1.0")).isFalse();
		assertThat(VersionsComparator.isFirstVersionBeforeOrEqualToSecond("5.1.1.1.0", "5.1.0.1")).isFalse();
		assertThat(VersionsComparator.isFirstVersionBeforeOrEqualToSecond("5.1.1.1.0", "5.1.0.1.0")).isFalse();
	}
}
