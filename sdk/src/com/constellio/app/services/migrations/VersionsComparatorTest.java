package com.constellio.app.services.migrations;

import com.constellio.sdk.tests.ConstellioTest;
import org.junit.Before;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class VersionsComparatorTest extends ConstellioTest {
	VersionsComparator versionsComparator;

	@Before
	public void setUp() {
		versionsComparator = new VersionsComparator();
	}

	@Test
	public void whenCompareTwoVersionsThenZeroIfEqualsOneIfGreaterNegativeOneIfLower() {

		assertThat(versionsComparator.compare("1", "4")).isEqualTo(-1);
		assertThat(versionsComparator.compare("1", "1.1.1")).isEqualTo(-1);
		assertThat(versionsComparator.compare("1.1.1", "4")).isEqualTo(-1);
		assertThat(versionsComparator.compare("1", "0.0.1")).isEqualTo(1);
		assertThat(versionsComparator.compare("0.0.1", "0.1.1")).isEqualTo(-1);
		assertThat(versionsComparator.compare("2.1.3", "2.5.4")).isEqualTo(-1);
		assertThat(versionsComparator.compare("1", "1")).isEqualTo(0);
		assertThat(versionsComparator.compare("1", "2.5.4")).isEqualTo(-1);
		assertThat(versionsComparator.compare("2.5.4", "1")).isEqualTo(1);

		assertThat(versionsComparator.compare("2.5.beta1", "2.5.beta.1")).isEqualTo(0);
		assertThat(versionsComparator.compare("2.5.beta1", "2.5.beta.2")).isEqualTo(-1);
		assertThat(versionsComparator.compare("2.5.beta2", "2.5.beta.1")).isEqualTo(1);
		assertThat(versionsComparator.compare("2.5.beta2", "2.5")).isEqualTo(-1);
		assertThat(versionsComparator.compare("2.5", "2.5.beta2")).isEqualTo(1);

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


	@Test
	public void whenComparingWithLTSKeywordThenNotImportant()
			throws Exception {
		assertThat(VersionsComparator.isFirstVersionBeforeOrEqualToSecond("9.2", "9.2 LTS")).isTrue();
		assertThat(VersionsComparator.isFirstVersionBeforeOrEqualToSecond("9.2", "9.2 (LTS)")).isTrue();
		assertThat(VersionsComparator.isFirstVersionBeforeOrEqualToSecond("9.2 LTS", "9.2")).isTrue();
		assertThat(VersionsComparator.isFirstVersionBeforeOrEqualToSecond("9.2 (LTS)", "9.2")).isTrue();
		assertThat(VersionsComparator.isFirstVersionBeforeOrEqualToSecond("9.2.1", "9.2 LTS")).isFalse();
		assertThat(VersionsComparator.isFirstVersionBeforeOrEqualToSecond("9.2.1", "9.2 (LTS)")).isFalse();

		assertThat(VersionsComparator.isFirstVersionBeforeOrEqualToSecond("9.2 LTS RC1", "9.2 (LTS)")).isTrue();
		assertThat(VersionsComparator.isFirstVersionBeforeOrEqualToSecond("9.2 (LTS)", "9.2 LTS RC1")).isFalse();

		assertThat(VersionsComparator.isFirstVersionBeforeOrEqualToSecond("9.2 LTS RC1", "9.2 RC2")).isTrue();
		assertThat(VersionsComparator.isFirstVersionBeforeOrEqualToSecond("9.2 RC2", "9.2 LTS RC1")).isTrue();

	}
}
