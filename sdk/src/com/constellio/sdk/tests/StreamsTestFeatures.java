package com.constellio.sdk.tests;

import static org.junit.Assert.fail;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.constellio.data.io.services.facades.OpenedResourcesWatcher;
import com.constellio.data.io.streamFactories.StreamFactory;

public class StreamsTestFeatures {

	private static final Logger LOGGER = LoggerFactory.getLogger(StreamsTestFeatures.class);

	public static final String SDK_STREAM = "SDK Stream";
	List<Closeable> closeableThatMustBeClosed = new ArrayList<Closeable>();

	List<Closeable> closeablesToClose = new ArrayList<Closeable>();

	List<String> unClosedResources = new ArrayList<>();

	//	List<Long> openedThreadsAtStartup = new ArrayList<>();
	//
	//	public StreamsTestFeatures() {
	//		for (Thread thread : Thread.getAllStackTraces().keySet()) {
	//			openedThreadsAtStartup.add(thread.getId());
	//		}
	//	}

	public void beforeTest(SkipTestsRule skipTestsRule) {
		if (skipTestsRule == null || skipTestsRule.getCurrentTestClass() == null) {
			OpenedResourcesWatcher.openingStackHeader = "Where the resource was opened : ";
		} else {
			String testName = skipTestsRule.getCurrentTestClass().getSimpleName() + "." + skipTestsRule.getCurrentTestName();
			OpenedResourcesWatcher.openingStackHeader = "Where the resource was opened (in test '" + testName + "') : s";
		}
	}

	public void afterTest() {

		//		List<String> openedThreads = new ArrayList<>();
		//		for (Thread thread : Thread.getAllStackTraces().keySet()) {
		//			if (openedThreadsAtStartup.contains(thread.getId())) {
		//				openedThreads.add(thread.getId() + " - " + thread.getName());
		//			}
		//		}
		//		assertThat(openedThreads).isEmpty();

		//		for (Thread thread : Thread.getAllStackTraces().keySet()) {
		//			openedThreadsAtStartup.add(thread.getId());
		//		}

		for (Closeable closeable : closeablesToClose) {
			IOUtils.closeQuietly(closeable);
		}

		boolean someStreamFactoryStreamsNotClosed = false;
		for (Closeable closeable : closeableThatMustBeClosed) {
			try {
				verify(closeable).close();
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
		if (someStreamFactoryStreamsNotClosed) {
			fail("Some opened stream has not been closed");
		}

		synchronized (OpenedResourcesWatcher.class) {

			for (Map.Entry<String, Object> entry : OpenedResourcesWatcher.getOpenedResources().entrySet()) {
				Object value = entry.getValue();
				if (value instanceof Thread) {
					if (!entry.getKey().contains(SDK_STREAM)) {
						unClosedResources.add(entry.getKey());
					}
					Thread thread = (Thread) value;
					while (thread.isAlive()) {
						StringBuilder message = new StringBuilder();
						message.append("Waiting for thread '" + thread.toString() + "' / '" + thread.getName() + "' to stop");

						message.append("\n" + OpenedResourcesWatcher.getOpeningStackTraceOf(entry.getKey()));
						message.append("\n");
						message.append("Current thread stack trace : ");
						for (StackTraceElement element : thread.getStackTrace()) {
							message.append("\n\t" + element.toString());
						}
						LOGGER.info(message.toString());
						try {
							Thread.sleep(1000);
						} catch (InterruptedException e) {
							throw new RuntimeException(e);
						}
					}

				}
			}

			//Close all streams before deleting temp files
			for (Map.Entry<String, Object> entry : OpenedResourcesWatcher.getOpenedResources().entrySet()) {
				Object value = entry.getValue();
				if (value instanceof Closeable) {
					if (!entry.getKey().contains(SDK_STREAM)) {
						unClosedResources.add(entry.getKey());
					}

					IOUtils.closeQuietly((Closeable) entry.getValue());
				}
			}

			for (Map.Entry<String, Object> entry : OpenedResourcesWatcher.getOpenedResources().entrySet()) {
				Object value = entry.getValue();
				if (value instanceof File) {
					if (!entry.getKey().contains(SDK_STREAM)) {
						unClosedResources.add(entry.getKey());
					}

					OpenedResourcesWatcher.onClose(value);
					FileUtils.deleteQuietly((File) value);
				}
			}
		}

	}

	public List<String> getUnClosedResources() {
		return unClosedResources;
	}

	public <T extends Closeable> T closeAfterTest(T closeable) {
		closeablesToClose.add(closeable);
		T mockedStream = Mockito.spy(closeable);
		try {
			doThrow(new RuntimeException("The closeable must be closed at the same place it has been opened")).when(mockedStream)
					.close();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		return mockedStream;
	}

	public <T extends Closeable> StreamFactory<T> ensureAllCreatedCloseableAreClosed(final StreamFactory<T> nestedStreamFactory) {
		return new StreamFactory<T>() {

			@Override
			public T create(String name)
					throws IOException {
				return spy(nestedStreamFactory.create(name));
			}
		};
	}

}
