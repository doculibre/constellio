package com.constellio.data.threads;

import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.annotations.SlowTest;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;

@SlowTest
public class ConstellioJobManagerAcceptTest extends ConstellioTest {

	static AtomicInteger counter = new AtomicInteger();
	static AtomicInteger counter2 = new AtomicInteger();

	ConstellioJobManager backgroundThreadsManager;

	@Before
	public void setUp()
			throws Exception {

		givenBackgroundThreadsEnabled();
		backgroundThreadsManager = getDataLayerFactory().getConstellioJobManager();
		backgroundThreadsManager.initialize();
		backgroundThreadsManager.onSystemStarted();
		counter.set(0);
		counter2.set(0);
	}

	@After
	public void tearDown() {
		if (backgroundThreadsManager != null) {
			backgroundThreadsManager.close();
		}
	}

	@Test
	public void givenSystemIsNotYetStartedThenWaitUntilStartedBeforeExecuting()
			throws Exception {
		backgroundThreadsManager.systemStarted.set(false);
		backgroundThreadsManager.addJob(new Job1(), true);

		Thread.sleep(5000);
		assertThat(counter.get()).isZero();

		backgroundThreadsManager.onSystemStarted();
		Thread.sleep(5000);
		assertThat(counter.get()).isGreaterThan(1);

		backgroundThreadsManager.close();

	}

	@Test
	public void whenStartedAndClosedMultipleTimesThenOk()
			throws Exception {
		backgroundThreadsManager.addJob(new ConstellioJobManagerAcceptTest.Job2(), false);
		backgroundThreadsManager.close();
		backgroundThreadsManager.initialize();
		backgroundThreadsManager.onSystemStarted();
		backgroundThreadsManager.addJob(new ConstellioJobManagerAcceptTest.Job2(), false);
		backgroundThreadsManager.close();
		backgroundThreadsManager.initialize();
		backgroundThreadsManager.onSystemStarted();
		backgroundThreadsManager.addJob(new ConstellioJobManagerAcceptTest.Job2(), false);

	}

	@Test
	public void whenConfiguringAThreadToExecuteAnActionOf2SecondsEvery3SecondsThenWait1SecondsBetweenRuns()
			throws Exception {

		backgroundThreadsManager.addJob(new Job1(), false);

		Thread.sleep(5000);

		assertThat(counter.get()).isBetween(4, 6);
		backgroundThreadsManager.close();

		Thread.sleep(2000);
		int counter1 = counter.get();
		Thread.sleep(3000);
		int counter2 = counter.get();

		assertThat(counter1).isEqualTo(counter2);

	}

	@Test
	public void whenConfiguringAThreadToExecuteTwoActionOf2SecondsEvery3SecondsThenWait1SecondsBetweenRuns()
			throws Exception {

		backgroundThreadsManager.addJob(new Job1(), false);

		backgroundThreadsManager.addJob(new Job2(), false);

		Thread.sleep(5000);

		assertThat(counter.get()).isBetween(4, 6);
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

	@Test
	public void givenBackgroundThreadConfiguredToContinueOnExceptionThenContinueUntilAnErrorOccur()
			throws InterruptedException {
		final Runnable runnable = mock(Runnable.class);

		doAnswer(increaseAndThrowException()).doAnswer(increaseAndThrowException()).doAnswer(increaseAndThrowError())
				.doAnswer(increase()).when(runnable).run();

		backgroundThreadsManager.addJob(new Job1(), false);

		Thread.sleep(10000);

		assertThat(counter.get()).isBetween(9, 11);

	}

	@Test
	public void givenBackgroundThreadConfiguredToStopOnExceptionThenStopAtTheFirstRuntimeException()
			throws InterruptedException {
		final Runnable runnable = mock(Runnable.class);

		backgroundThreadsManager.addJob(new Job1StoppingOnException(), false);

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

	public static class Job1 extends ConstellioJob {
		@Override
		protected String name() {
			return "action1";
		}

		@Override
		protected Runnable action() {
			return new ConstellioJobManagerAcceptTest.TestSleepingRunnable(50, counter);
		}

		@Override
		protected boolean unscheduleOnException() {
			return false;
		}

		@Override
		protected Set<Integer> intervals() {
			return new HashSet<>(asList(1));
		}

		@Override
		protected Set<String> cronExpressions() {
			return new HashSet<>();
		}
	}

	public static class Job1StoppingOnException extends ConstellioJob {
		@Override
		protected String name() {
			return "action1";
		}

		@Override
		protected Runnable action() {
			return new Runnable() {
				@Override
				public void run() {
					counter.incrementAndGet();
					throw new RuntimeException("Something wrong");
				}
			};
		}

		@Override
		protected boolean unscheduleOnException() {
			return true;
		}

		@Override
		protected Set<Integer> intervals() {
			return new HashSet<>(asList(2));
		}

		@Override
		protected Set<String> cronExpressions() {
			return new HashSet<>();
		}
	}

	public static class Job2 extends ConstellioJob {
		@Override
		protected String name() {
			return "action2";
		}

		@Override
		protected Runnable action() {
			return new ConstellioJobManagerAcceptTest.TestSleepingRunnable(2000, counter2);
		}

		@Override
		protected boolean unscheduleOnException() {
			return false;
		}

		@Override
		protected Set<Integer> intervals() {
			return new HashSet<>(asList(1));
		}

		@Override
		protected Set<String> cronExpressions() {
			return new HashSet<>();
		}
	}
}
