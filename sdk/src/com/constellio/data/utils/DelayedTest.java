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
package com.constellio.data.utils;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

import com.constellio.data.utils.DelayedRuntimeException.DelayedRuntimeException_AlreadyDefined;
import com.constellio.data.utils.DelayedRuntimeException.DelayedRuntimeException_NotYetDefined;
import com.constellio.sdk.tests.ConstellioTest;

public class DelayedTest extends ConstellioTest {

	Delayed<Integer> delayed = new Delayed<>();

	Delayed<Integer> delayedWithInitialValue = new Delayed<>(42);

	@Test(expected = DelayedRuntimeException_NotYetDefined.class)
	public void givenDelayedWithoutValueWhenGetValueThenException()
			throws Exception {

		delayed.get();
	}

	@Test
	public void givenDelayedWithoutValueWhenSetValueThenCanGetValue()
			throws Exception {
		delayed.set(23);

		assertThat(delayed.get()).isEqualTo(23);
	}

	@Test(expected = DelayedRuntimeException_AlreadyDefined.class)
	public void whenSetValueTwiceThenException()
			throws Exception {
		delayed.set(23);
		delayed.set(34);
	}

	@Test
	public void givenDelayedWithInitialValueThenCanGetValue()
			throws Exception {
		Delayed<Integer> delayedWithInitialValue = new Delayed<>(42);
		assertThat(delayedWithInitialValue.get()).isEqualTo(42);
	}
}
