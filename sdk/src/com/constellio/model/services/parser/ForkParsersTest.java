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
package com.constellio.model.services.parser;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

import org.apache.tika.fork.ForkParser;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import com.constellio.sdk.tests.ConstellioTest;

public class ForkParsersTest extends ConstellioTest {

	@Mock ForkParser forkParser, anotherForkParser;
	int forkParserPoolSize = 7;
	ForkParsers forkParsers;

	@Before
	public void setUp() {
		forkParsers = spy(new ForkParsers(forkParserPoolSize));
	}

	@Test
	public void givenForkParsersInstanciatedThemNoForkParserInitialized()
			throws Exception {
		verify(forkParsers, never()).newForkParser();
	}

	@Test
	public void givenForkParsersNotInitializedWhenGetForkParserThenInitializedItWithPoolSize()
			throws Exception {
		doReturn(forkParser).when(forkParsers).newForkParser();

		assertThat(forkParsers.getForkParser()).isSameAs(forkParser);

		verify(forkParsers).newForkParser();
		verify(forkParser).setPoolSize(forkParserPoolSize);
	}

	@Test
	public void givenForkParsersInitializedNotInitializedWhenGetForkParserThenReturnPreviousForkParser()
			throws Exception {
		doReturn(forkParser).when(forkParsers).newForkParser();

		assertThat(forkParsers.getForkParser()).isSameAs(forkParser);
		assertThat(forkParsers.getForkParser()).isSameAs(forkParser);

		verify(forkParsers).newForkParser();
	}

	@Test
	public void givenForkParserInitializedWhenCloseThenCloseForkParser()
			throws Exception {
		doReturn(forkParser).when(forkParsers).newForkParser();

		assertThat(forkParsers.getForkParser()).isSameAs(forkParser);
		forkParsers.close();

		verify(forkParser).close();
	}

	@Test
	public void givenForkParserClosedWhenGettingForkParserThenAnotherForkParserInstanciatedWithPoolSize()
			throws Exception {
		doReturn(forkParser).doReturn(anotherForkParser).when(forkParsers).newForkParser();
		forkParsers.getForkParser();
		forkParsers.close();

		assertThat(forkParsers.getForkParser()).isSameAs(anotherForkParser);
		verify(anotherForkParser).setPoolSize(forkParserPoolSize);
	}

	@Test
	public void givenForkParsersNotInitializedWhenCloseThenNothingHappens() {
		forkParsers.close();

		verify(forkParser, never()).close();
	}

}
