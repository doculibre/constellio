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
package com.constellio.data.io.streams.factories;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.StringReader;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.input.ReaderInputStream;

import com.constellio.data.io.services.facades.OpenedResourcesWatcher;
import com.constellio.data.io.streamFactories.StreamFactory;
import com.constellio.data.io.streams.factories.StreamsServicesRuntimeException.StreamsServicesRuntimeException_FileNotFound;

public class StreamsServices {

	public StreamsServices() {
		super();
	}

	public byte[] readBytes(InputStream inputStream)
			throws IOException {
		return IOUtils.toByteArray(inputStream);
	}

	public void copy(InputStream inputStream, OutputStream outputStream)
			throws IOException {
		IOUtils.copy(inputStream, outputStream);
	}

	public void copyLarge(InputStream inputStream, OutputStream outputStream)
			throws IOException {
		IOUtils.copyLarge(inputStream, outputStream);
	}

	public void closeQuietly(Closeable closeable) {
		if (closeable != null) {
			IOUtils.closeQuietly(closeable);
		}
	}

	public OutputStream newBufferedOutputStream(OutputStream outputStream, final String name) {
		return OpenedResourcesWatcher.onOpen(new BufferedOutputStream(outputStream) {

			@Override
			public String toString() {
				return "Buffered " + name;
			}

			@Override
			public void close()
					throws IOException {
				OpenedResourcesWatcher.onClose(this);
				super.close();
			}
		});
	}

	public InputStream newBufferedInputStream(InputStream inputStream, final String name) {
		return OpenedResourcesWatcher.onOpen(new BufferedInputStream(inputStream) {

			@Override
			public String toString() {
				return "Buffered " + name;
			}

			@Override
			public void close()
					throws IOException {
				OpenedResourcesWatcher.onClose(this);
				super.close();
			}
		});
	}

	public BufferedReader newBufferedReader(Reader reader, final String name) {
		return OpenedResourcesWatcher.onOpen(new BufferedReader(reader) {

			@Override
			public String toString() {
				return "Buffered " + name;
			}

			@Override
			public void close()
					throws IOException {
				OpenedResourcesWatcher.onClose(this);
				super.close();
			}
		});
	}

	public ByteArrayInputStream newByteArrayInputStream(byte[] byteArray, final String name) {
		return OpenedResourcesWatcher.onOpen(new ByteArrayInputStream(byteArray) {

			@Override
			public String toString() {
				return name;
			}

			@Override
			public void close()
					throws IOException {
				OpenedResourcesWatcher.onClose(this);
				super.close();
			}
		});
	}

	public InputStream newBufferedByteArrayInputStream(byte[] byteArray, final String name) {
		return newBufferedInputStream(newByteArrayInputStream(byteArray, name), name);
	}

	public ByteArrayOutputStream newByteArrayOutputStream(final String name) {
		return OpenedResourcesWatcher.onOpen(new ByteArrayOutputStream() {
			@Override
			public String toString() {
				return name;
			}

			@Override
			public void close()
					throws IOException {
				OpenedResourcesWatcher.onClose(this);
				super.close();
			}
		});
	}

	public OutputStream newBufferedByteArrayOutputStream(String name) {
		return newBufferedOutputStream(newByteArrayOutputStream(name), name);
	}

	public InputStream newFileInputStream(final File file, final String name)
			throws FileNotFoundException {

		FileInputStream fileInputStream = OpenedResourcesWatcher.onOpen(new FileInputStream(file) {

			@Override
			public String toString() {
				return name + "[" + file.getPath() + "]";
			}

			@Override
			public void close()
					throws IOException {
				OpenedResourcesWatcher.onClose(this);
				super.close();
			}
		});

		return newBufferedInputStream(fileInputStream, name);
	}

	public BufferedReader newFileReader(final File file, final String name) {

		try {

			FileReader fileReader = OpenedResourcesWatcher.onOpen(new FileReader(file) {

				@Override
				public String toString() {
					return name + "[" + file.getPath() + "]";
				}

				@Override
				public void close()
						throws IOException {
					OpenedResourcesWatcher.onClose(this);
					super.close();
				}
			});
			return newBufferedReader(fileReader, name);
		} catch (FileNotFoundException e) {
			throw new StreamsServicesRuntimeException_FileNotFound(e);
		}

	}

	public BufferedInputStream newBufferedFileInputStream(File file, final String name)
			throws FileNotFoundException {
		return (BufferedInputStream) newFileInputStream(file, name);
	}

	public OutputStream newFileOutputStream(final File file, final String name)
			throws FileNotFoundException {
		FileOutputStream fileOutputStream = OpenedResourcesWatcher.onOpen(new FileOutputStream(file) {

			@Override
			public String toString() {
				return name + "[" + file.getPath() + "]";
			}

			@Override
			public void close()
					throws IOException {
				OpenedResourcesWatcher.onClose(this);
				super.close();
			}

		});

		return newBufferedOutputStream(fileOutputStream, name);
	}

	public OutputStream newBufferedFileOutputStream(File file, String name)
			throws FileNotFoundException {
		return newFileOutputStream(file, name);
	}

	public StreamFactory<InputStream> newInputStreamFactory(final File file, final String factoryName) {
		return new FileInputStreamFactory(file, factoryName + "[" + file.getPath() + "]");
	}

	public StreamFactory<OutputStream> newOutputStreamFactory(final File file, final String factoryName) {
		return new FileOutputStreamFactory(file, factoryName + "[" + file.getPath() + "]");
	}

	public StreamFactory<InputStream> newByteArrayStreamFactory(final byte[] bytes, final String factoryName) {
		return new ByteArrayStreamFactory(bytes, factoryName);
	}

	public StreamFactory<InputStream> newInputStreamFactory(final String text) {
		return new StreamFactory<InputStream>() {
			@Override
			public InputStream create(final String name)
					throws IOException {
				return OpenedResourcesWatcher.onOpen(new ReaderInputStream(new StringReader(text)) {
					@Override
					public String toString() {
						return name;
					}

					@Override
					public void close()
							throws IOException {
						OpenedResourcesWatcher.onClose(this);
						super.close();
					}
				});
			}
		};
	}

	public StreamFactory<Reader> newFileReaderFactory(final File file) {
		return new StreamFactory<Reader>() {
			@Override
			public Reader create(final String name)
					throws IOException {
				return OpenedResourcesWatcher.onOpen(new FileReader(file) {
					@Override
					public String toString() {
						return name;
					}

					@Override
					public void close()
							throws IOException {
						OpenedResourcesWatcher.onClose(this);
						super.close();
					}
				});
			}
		};
	}

	public class FileInputStreamFactory implements StreamFactory<InputStream> {

		private File file;

		private String factoryName;

		public FileInputStreamFactory(File file, String factoryName) {
			super();
			this.file = file;
			this.factoryName = factoryName;
		}

		@Override
		public InputStream create(String name)
				throws IOException {
			return newFileInputStream(file, factoryName + "__" + name);
		}

	}

	public class FileOutputStreamFactory implements StreamFactory<OutputStream> {

		private File file;

		private String factoryName;

		public FileOutputStreamFactory(File file, String factoryName) {
			super();
			this.file = file;
			this.factoryName = factoryName;
		}

		@Override
		public OutputStream create(String name)
				throws IOException {
			return newFileOutputStream(file, factoryName + "__" + name);
		}

	}

	public class ByteArrayStreamFactory implements StreamFactory<InputStream> {

		private String factoryName;
		private byte[] bytes;

		public ByteArrayStreamFactory(byte[] bytes, String factoryName) {
			super();
			this.bytes = bytes;
			this.factoryName = factoryName;
		}

		@Override
		public InputStream create(final String name)
				throws IOException {
			return newByteArrayInputStream(bytes, factoryName + "__" + name);
		}

	}

}
