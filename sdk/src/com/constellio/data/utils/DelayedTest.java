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
