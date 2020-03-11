package com.constellio.data.utils;

import com.constellio.sdk.tests.ConstellioTest;
import org.joda.time.Duration;
import org.joda.time.LocalDateTime;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class AsyncTaskRegrouperAcceptanceTest extends ConstellioTest {


	@Test
	public void whenAddingThenRegroupedWhen80PctReached() throws Exception {

		List<Object> callArgs = new ArrayList<>();
		try (AsyncTaskRegrouper<Integer> regrouper = new AsyncTaskRegrouper(Duration.standardSeconds(2), (l) -> {
			callArgs.add(l);
		})) {

			regrouper.setQueueCapacity(10);
			regrouper.setSleepTime(0);
			regrouper.start();

			givenTimeIs(new LocalDateTime());

			regrouper.addAsync(1, null);
			regrouper.addAsync(2, null);
			regrouper.addAsync(3, null);
			regrouper.addAsync(4, null);
			regrouper.addAsync(5, null);
			regrouper.addAsync(6, null);
			regrouper.addAsync(7, null);
			Thread.sleep(10);

			regrouper.addAsync(8, null);
			Thread.sleep(10);

			regrouper.addAsync(9, null);
			regrouper.addAsync(10, null);
			regrouper.addAsync(11, null);
			regrouper.addAsync(12, null);
			regrouper.addAsync(13, null);
			regrouper.addAsync(14, null);
			regrouper.addAsync(15, null);
			Thread.sleep(10);

			regrouper.addAsync(16, null);
			Thread.sleep(10);

			assertThat(callArgs).containsOnly(
					Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8),
					Arrays.asList(9, 10, 11, 12, 13, 14, 15, 16)
			);
		}
	}

	@Test
	public void whenAddingThenRegroupedWhenTimeLimitReached() throws Exception {

		List<Object> callArgs = new ArrayList<>();
		List<Object> callbacksArgs = new ArrayList<>();
		try (AsyncTaskRegrouper<Integer> regrouper = new AsyncTaskRegrouper(Duration.standardSeconds(2), (l) -> {
			callArgs.add(l);
		})) {

			regrouper.setQueueCapacity(10);
			regrouper.setSleepTime(0);
			regrouper.start();

			LocalDateTime now = new LocalDateTime();
			givenTimeIs(now);

			System.out.println(now.toDate().getTime());

			regrouper.addAsync(1, () -> {
				callbacksArgs.add("A");
			});
			regrouper.addAsync(2, null);
			regrouper.addAsync(3, null);
			regrouper.addAsync(4, () -> {
				callbacksArgs.add("B");
			});
			regrouper.addAsync(5, null);
			regrouper.addAsync(6, null);
			regrouper.addAsync(7, () -> {
				callbacksArgs.add("C");
			});
			Thread.sleep(10);
			assertThat(callArgs).isEmpty();
			givenTimeIs(now.plusSeconds(1));
			Thread.sleep(10);
			assertThat(callArgs).isEmpty();
			assertThat(callbacksArgs).isEmpty();

			givenTimeIs(now.plusSeconds(2));
			Thread.sleep(10);
			assertThat(callArgs).containsOnly(
					Arrays.asList(1, 2, 3, 4, 5, 6, 7)
			);
			assertThat(callbacksArgs).containsOnly(
					"A", "B", "C"
			);
		}
	}

}
