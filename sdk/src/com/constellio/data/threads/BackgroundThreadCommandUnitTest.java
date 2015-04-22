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
import static org.junit.Assert.fail;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

import org.joda.time.Duration;
import org.joda.time.LocalDateTime;
import org.joda.time.LocalTime;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;

import com.constellio.sdk.tests.ConstellioTest;

public class BackgroundThreadCommandUnitTest extends ConstellioTest {

	TestRunnable nestedCommand;
	String zeId = "zeId";
	BackgroundThreadConfiguration configuration;
	BackgroundThreadCommand command;

	@Before
	public void setUp()
			throws Exception {

		nestedCommand = spy(new TestRunnable());

		configuration = spy(
				BackgroundThreadConfiguration.repeatingAction(zeId, nestedCommand).executedEvery(Duration.standardSeconds(42)));
		command = spy(new BackgroundThreadCommand(configuration));
	}

	@Test
	public void givenNoTimeIntervalWhenRunningThenLogAndRunNestedCommand()
			throws Exception {

		doNothing().when(command).runAndHandleException();

		command.run();

		verify(command).runAndHandleException();

	}

	@Test
	public void givenInsideTimeIntervalWhenRunningThenLogAndRunNestedCommand()
			throws Exception {

		configuration.between(new LocalTime(11, 45, 00), new LocalTime(12, 45, 00));
		givenTimeIs(new LocalDateTime(2010, 02, 02, 11, 45, 01));
		doNothing().when(command).runAndHandleException();

		command.run();

		verify(command).runAndHandleException();

	}

	@Test
	public void givenBeforeTimeIntervalWhenRunningThenLogAndRunNestedCommand()
			throws Exception {

		configuration.between(new LocalTime(11, 45, 00), new LocalTime(12, 45, 00));
		givenTimeIs(new LocalDateTime(2010, 02, 02, 11, 44, 59));
		doNothing().when(command).runAndHandleException();

		command.run();

		verify(command, never()).runAndHandleException();

	}

	@Test
	public void givenAfterTimeIntervalWhenRunningThenLogAndRunNestedCommand()
			throws Exception {

		configuration.between(new LocalTime(11, 45, 00), new LocalTime(12, 45, 00));
		givenTimeIs(new LocalDateTime(2010, 02, 02, 12, 45, 01));
		doNothing().when(command).runAndHandleException();

		command.run();

		verify(command, never()).runAndHandleException();

	}

	@Test
	public void givenInsideTimeIntervalOnTwoDayWhenRunningThenLogAndRunNestedCommand()
			throws Exception {

		configuration.between(new LocalTime(20, 45, 00), new LocalTime(2, 45, 00));
		givenTimeIs(new LocalDateTime(2010, 02, 02, 20, 45, 01));
		doNothing().when(command).runAndHandleException();

		command.run();

		verify(command).runAndHandleException();

	}

	@Test
	public void givenBeforeTimeIntervalOnTwoDayWhenRunningThenLogAndRunNestedCommand()
			throws Exception {

		configuration.between(new LocalTime(20, 45, 00), new LocalTime(2, 45, 00));
		givenTimeIs(new LocalDateTime(2010, 02, 02, 20, 44, 59));
		doNothing().when(command).runAndHandleException();

		command.run();

		verify(command, never()).runAndHandleException();

	}

	@Test
	public void givenAfterTimeIntervalOnTwoDayWhenRunningThenLogAndRunNestedCommand()
			throws Exception {

		configuration.between(new LocalTime(20, 45, 00), new LocalTime(2, 45, 00));
		givenTimeIs(new LocalDateTime(2010, 02, 02, 2, 45, 01));
		doNothing().when(command).runAndHandleException();

		command.run();

		verify(command, never()).runAndHandleException();

	}

	@Test
	public void whenRunHandlingExceptionThenLogAndSetThreadName()
			throws Exception {

		String threadName = zeId + " (" + TestRunnable.class.getName() + ")";

		command.run();

		InOrder inOrder = inOrder(command, nestedCommand);
		inOrder.verify(command).setCurrentThreadName();
		inOrder.verify(command).logCommandCall();
		inOrder.verify(nestedCommand).run();
		inOrder.verify(command).logCommandCallEnd();

	}

	@Test
	public void givenStoppingOnExceptionWhenAnExceptionOccurThenLogAndRethrow()
			throws Exception {

		configuration.handlingExceptionWith(BackgroundThreadExceptionHandling.STOP);

		RuntimeException e = new RuntimeException();
		doThrow(e).when(nestedCommand).run();

		try {
			command.run();
			fail("Exception expected");
		} catch (RuntimeException e2) {
			assertThat(e2).isEqualTo(e);
		}

		String threadName = zeId + " (" + TestRunnable.class.getName() + ")";
		InOrder inOrder = inOrder(command, nestedCommand);
		inOrder.verify(command).setCurrentThreadName();
		inOrder.verify(command).logCommandCall();
		inOrder.verify(nestedCommand).run();
		inOrder.verify(command).logCommandCallEndedWithException(e);

	}

	@Test
	public void givenContinuingOnExceptionWhenAnExceptionOccurThenLogAndReturnNormally()
			throws Exception {

		configuration.handlingExceptionWith(BackgroundThreadExceptionHandling.CONTINUE);

		RuntimeException e = new RuntimeException();
		doThrow(e).when(nestedCommand).run();

		command.run();

		String threadName = zeId + " (" + TestRunnable.class.getName() + ")";
		InOrder inOrder = inOrder(command, nestedCommand);
		inOrder.verify(command).setCurrentThreadName();
		inOrder.verify(command).logCommandCall();
		inOrder.verify(nestedCommand).run();
		inOrder.verify(command).logCommandCallEndedWithException(e);

	}

	public static class TestRunnable implements Runnable {

		@Override
		public void run() {

		}
	}
}
