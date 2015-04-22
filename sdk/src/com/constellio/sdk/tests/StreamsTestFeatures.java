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

import com.constellio.data.io.services.facades.OpenedResourcesWatcher;
import com.constellio.data.io.streamFactories.StreamFactory;

public class StreamsTestFeatures {

	public static final String SDK_STREAM = "SDK Stream";
	List<Closeable> closeableThatMustBeClosed = new ArrayList<Closeable>();

	List<Closeable> closeablesToClose = new ArrayList<Closeable>();

	List<String> unClosedResources = new ArrayList<>();

	public void afterTest() {
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
						for (StackTraceElement element : thread.getStackTrace()) {
							message.append("\n\t" + element.toString());
						}
						System.out.println(message);
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
