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
package com.constellio.data.test;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.io.IOException;
import java.util.Date;

import org.junit.Before;
import org.junit.Test;

import com.constellio.data.utils.Octets;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.annotations.SlowTest;

@SlowTest
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
