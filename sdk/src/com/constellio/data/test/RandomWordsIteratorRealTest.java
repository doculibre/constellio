package com.constellio.data.test;

import com.constellio.data.utils.Octets;
import com.constellio.sdk.tests.ConstellioTest;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;

// Confirm @SlowTest
public class RandomWordsIteratorRealTest extends ConstellioTest {

	RandomWordsIterator randomFrenchWordsIterators, randomEnglishWordsIterators;

	@Before
	public void before() {
		File dictionaryFolder = getFoldersLocator().getDict();
		randomFrenchWordsIterators = RandomWordsIterator.createFor(new File(dictionaryFolder, "fr_FR_avec_accents.dic"));
		randomEnglishWordsIterators = RandomWordsIterator.createFor(new File(dictionaryFolder, "en_US.dic"));
	}

	@Test
	public void whenGetTextBySizeThenHasSizeNoMoreThan10PercentMore()
			throws IOException {

		long time = new Date().getTime();
		File file = newTempFileWithContent("test1.txt",
				randomFrenchWordsIterators.nextWordsOfLength(Octets.kilooctets(1)));
		assertThat(file.length()).isBetween(950L, 1100L);

		time = new Date().getTime();
		file = newTempFileWithContent("test2.txt",
				randomFrenchWordsIterators.nextWordsOfLength(Octets.kilooctets(100)));
		assertThat(file.length()).isBetween(95000L, 110000L);

		time = new Date().getTime();
		file = newTempFileWithContent("test3.txt", randomFrenchWordsIterators.nextWordsOfLength(Octets.megaoctets(10)));
		assertThat(file.length()).isBetween(9500000L, 11000000L);

	}

}
