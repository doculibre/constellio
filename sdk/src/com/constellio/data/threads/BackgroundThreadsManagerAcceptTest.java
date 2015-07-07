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
package com.constellio.data.threads;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.joda.time.Duration;
import org.joda.time.LocalTime;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.annotations.SlowTest;

public class BackgroundThreadsManagerAcceptTest extends ConstellioTest {

	AtomicInteger counter = new AtomicInteger();

	List<LocalTime> action1ThreadActionCallsTime = new ArrayList<>();
	BackgroundThreadsManager backgroundThreadsManager;

	@Before
	public void setUp()
			throws Exception {
		backgroundThreadsManager = getDataLayerFactory().getBackgroundThreadsManager();
		backgroundThreadsManager.initialize();
	}

	@After
	public void tearDown() {
		backgroundThreadsManager.close();
	}

	@SlowTest
	@Test
	public void givenSystemIsNotYetStartedThenWaitUntilStartedBeforeExecuting()
			throws Exception {
		backgroundThreadsManager.systemStarted.set(false);
		Runnable threadAction = spy(new TestSleepingRunnable(50, counter));
		backgroundThreadsManager.configure(BackgroundThreadConfiguration.repeatingAction("action1", threadAction).executedEvery(
				Duration.standardSeconds(1)));

		Thread.sleep(5000);
		assertThat(counter.get()).isZero();

		backgroundThreadsManager.systemStarted.set(true);
		Thread.sleep(5000);
		assertThat(counter.get()).isGreaterThan(1);

		backgroundThreadsManager.close();

	}

	@SlowTest
	@Test
	public void whenConfiguringAThreadToExecuteAnActionOf2SecondsEvery3SecondsThenWait1SecondsBetweenRuns()
			throws Exception {

		Runnable threadAction = spy(new TestSleepingRunnable(50, counter));

		backgroundThreadsManager.configure(BackgroundThreadConfiguration.repeatingAction("action1", threadAction).executedEvery(
				Duration.standardSeconds(1)));

		Thread.sleep(5000);

		assertThat(counter.get()).isBetween(4, 6);
		backgroundThreadsManager.close();

		Thread.sleep(2000);
		int counter1 = counter.get();
		Thread.sleep(3000);
		int counter2 = counter.get();

		assertThat(counter1).isEqualTo(counter2);

	}

	@SlowTest
	@Test
	public void givenBackgroundThreadConfiguredToContinueOnExceptionThenContinueUntilAnErrorOccur()
			throws InterruptedException {
		Runnable runnable = mock(Runnable.class);

		doAnswer(increaseAndThrowException()).doAnswer(increaseAndThrowException()).doAnswer(increaseAndThrowError())
				.doAnswer(increase()).when(runnable).run();

		backgroundThreadsManager.configure(BackgroundThreadConfiguration.repeatingAction("action", runnable).executedEvery(
				Duration.standardSeconds(1)).handlingExceptionWith(BackgroundThreadExceptionHandling.CONTINUE));

		Thread.sleep(10000);

		assertThat(counter.get()).isEqualTo(3);

	}

	@SlowTest
	@Test
	public void givenBackgroundThreadConfiguredToStopOnExceptionThenStopAtTheFirstRuntimeException()
			throws InterruptedException {
		Runnable runnable = mock(Runnable.class);

		doAnswer(increaseAndThrowException()).doAnswer(increase()).when(runnable).run();

		backgroundThreadsManager.configure(BackgroundThreadConfiguration.repeatingAction("action", runnable).executedEvery(
				Duration.standardSeconds(1)).handlingExceptionWith(BackgroundThreadExceptionHandling.STOP));

		Thread.sleep(10000);

		assertThat(counter.get()).isEqualTo(1);

	}

	private Answer<Object> increaseAndThrowError() {
		return new Answer<Object>() {
			@Override
			public Object answer(InvocationOnMock invocation)
					throws Throwable {
				counter.incrementAndGet();
				throw new Error("** RuntimeException thrown by the test**");
			}
		};
	}

	private Answer<Object> increaseAndThrowException() {
		return new Answer<Object>() {
			@Override
			public Object answer(InvocationOnMock invocation)
					throws Throwable {
				counter.incrementAndGet();
				throw new RuntimeException("** RuntimeException thrown by the test**");
			}
		};
	}

	private Answer<Object> increase() {
		return new Answer<Object>() {
			@Override
			public Object answer(InvocationOnMock invocation)
					throws Throwable {
				counter.incrementAndGet();
				return null;
			}
		};
	}

	public static class TestSleepingRunnable implements Runnable {

		private AtomicInteger atomicInteger;

		private int ms;

		public TestSleepingRunnable(int ms, AtomicInteger atomicInteger) {
			this.ms = ms;
			this.atomicInteger = atomicInteger;
		}

		@Override
		public void run() {
			atomicInteger.incrementAndGet();
			try {
				Thread.sleep(ms);
			} catch (InterruptedException e) {
				throw new RuntimeException(e);
			}
		}
	}

}
