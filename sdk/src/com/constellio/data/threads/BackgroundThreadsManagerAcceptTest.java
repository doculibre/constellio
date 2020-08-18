package com.constellio.data.threads;

import com.constellio.app.modules.rm.RMTestRecords;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.annotations.SlowTest;
import com.constellio.sdk.tests.setups.Users;
import org.joda.time.Duration;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;

public class BackgroundThreadsManagerAcceptTest extends ConstellioTest {
	RMTestRecords records = new RMTestRecords(zeCollection);
	Users users = new Users();

	AtomicInteger counter = new AtomicInteger();
	AtomicInteger counter2 = new AtomicInteger();

	BackgroundThreadsManager backgroundThreadsManager;

	@Before
	public void setUp()
			throws Exception {

		givenBackgroundThreadsEnabled();
		backgroundThreadsManager = getDataLayerFactory().getBackgroundThreadsManager();
		backgroundThreadsManager.initialize();
	}

	@After
	public void tearDown() {
		if (backgroundThreadsManager != null) {
			backgroundThreadsManager.close();
		}
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
	public void whenConfiguringAThreadToExecuteTwoActionOf2SecondsEvery3SecondsThenWait2SecondsBetweenRuns()
			throws Exception {

		Runnable threadAction = spy(new TestSleepingRunnable(2000, counter));
		Runnable threadAction2 = spy(new TestSleepingRunnable(2000, counter2));

		backgroundThreadsManager.configure(BackgroundThreadConfiguration
				.repeatingAction("action1", threadAction).executedEvery(
						Duration.standardSeconds(1)));

		backgroundThreadsManager.configure(BackgroundThreadConfiguration
				.repeatingAction("action2", threadAction2).executedEvery(
						Duration.standardSeconds(2)));

		Thread.sleep(5000);

		assertThat(counter.get()).isBetween(2, 3);
		assertThat(counter2.get()).isBetween(2, 3);
		backgroundThreadsManager.close();

		Thread.sleep(2000);
		int counter1 = counter.get();
		int threadAction2counter1 = counter2.get();
		Thread.sleep(3000);
		int threadAction2Counter2 = counter2.get();
		int counter2 = counter.get();

		assertThat(counter1).isEqualTo(counter2);
		assertThat(threadAction2counter1).isEqualTo(threadAction2Counter2);
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

		assertThat(counter.get()).isBetween(9, 11);

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
			System.out.println("Run!");
			atomicInteger.incrementAndGet();
			try {
				Thread.sleep(ms);
			} catch (InterruptedException e) {
				throw new RuntimeException(e);
			}
		}
	}

	public static class TestCountRunnable implements Runnable {

		private AtomicInteger atomicInteger;

		private int ms;
		int to;

		public TestCountRunnable(int to, AtomicInteger atomicInteger) {
			this.ms = ms;
			this.atomicInteger = atomicInteger;
			this.to = to;
		}

		@Override
		public void run() {
			System.out.println("Run!");
			int currentNumber = 0;

			for (int i = 0; i < to; i++) {
				currentNumber++;
			}

			atomicInteger.incrementAndGet();
		}
	}
}

