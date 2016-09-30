package com.constellio.data.utils;

import static com.constellio.data.utils.ThreadUtils.iterateOverRunningTaskInParallel;
import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.junit.Test;

import com.constellio.data.utils.ThreadUtils.IteratorElementTask;

public class ThreadUtilsAcceptanceTest {

	@Test
	public void testName()
			throws Exception {

		List<String> elements = asList("1", "2", "3", "4", "5", "6", "7", "8", "9", "10");

		IteratorElementTask<String> task = spy(new IteratorElementTask<String>() {

			@Override
			public void executeTask(String value)
					throws Exception {

				if ("3".equals(value)) {
					throw new Exception("No 3");
				}

				if ("8".equals(value)) {
					throw new Exception("No 8");
				}

			}
		});

		try {
			iterateOverRunningTaskInParallel(elements.iterator(), 3, task);
		} catch (Exception e) {
			assertThat(e.getMessage()).isEqualTo("No 3");
		}

		verify(task).executeTask("1");
		verify(task).executeTask("2");
		verify(task).executeTask("3");
		verify(task).executeTask("4");
		verify(task).executeTask("5");
		verify(task).executeTask("6");
		verify(task).executeTask("7");
		verify(task).executeTask("8");
		verify(task).executeTask("9");
		verify(task).executeTask("10");

	}
}
