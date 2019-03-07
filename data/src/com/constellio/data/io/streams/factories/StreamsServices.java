package com.constellio.data.io.streams.factories;

import com.constellio.data.io.services.facades.FileService;
import com.constellio.data.io.services.facades.OpenedResourcesWatcher;
import com.constellio.data.io.streamFactories.StreamFactory;
import com.constellio.data.io.streams.factories.StreamsServicesRuntimeException.StreamsServicesRuntimeException_CannotWriteInFile;
import com.constellio.data.io.streams.factories.StreamsServicesRuntimeException.StreamsServicesRuntimeException_FileNotFound;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.input.ReaderInputStream;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.StringReader;
import java.io.Writer;
import java.util.Scanner;

public class StreamsServices {

	private FileService fileService;

	public StreamsServices(FileService fileService) {
		this.fileService = fileService;
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
		return OpenedResourcesWatcher.onOpen(new BufferedOutputStream(outputStream, 65536) {

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
		return OpenedResourcesWatcher.onOpen(new BufferedInputStream(inputStream, 65536) {

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
		return OpenedResourcesWatcher.onOpen(new BufferedReader(reader, 65536) {

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

	public BufferedWriter newBufferedWriter(Writer writer, final String name) {
		return OpenedResourcesWatcher.onOpen(new BufferedWriter(writer, 65536) {

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
		return newFileInputStream(file, name, false);
	}

	public InputStream newFileInputStream(final File file, final String name, final boolean deleteFileOnclose)
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
				if (deleteFileOnclose) {
					fileService.deleteQuietly(file);
				}
				super.close();
			}
		});

		return newBufferedInputStream(fileInputStream, name);
	}

	public InputStream newBufferedFileInputStreamWithFileClosingAction(final File file, final String name,
																	   final Runnable runnable)
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
				runnable.run();
			}
		});

		return newBufferedInputStream(fileInputStream, name);
	}

	public OutputStream newBufferedFileOutputStreamWithFileClosingAction(final File file, final String name,
																		 final Runnable runnable)
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
				runnable.run();
			}
		});

		return newBufferedOutputStream(fileOutputStream, name);
	}


	public BufferedWriter newBufferedFileWriterWithFileClosingAction(final File file, final String name,
																	 final Runnable runnable)
			throws IOException {

		FileWriter fileWriter = OpenedResourcesWatcher.onOpen(new FileWriter(file) {

			@Override
			public String toString() {
				return name + "[" + file.getPath() + "]";
			}

			@Override
			public void close()
					throws IOException {
				OpenedResourcesWatcher.onClose(this);
				super.close();
				runnable.run();
			}
		});

		return newBufferedWriter(fileWriter, name);
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

	public BufferedWriter newFileWriter(final File file, final String name) {

		try {

			FileWriter fileWriter = OpenedResourcesWatcher.onOpen(new FileWriter(file) {

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
			return newBufferedWriter(fileWriter, name);
		} catch (IOException e) {
			throw new StreamsServicesRuntimeException_CannotWriteInFile(file, e);
		}

	}

	public BufferedInputStream newBufferedFileInputStream(File file, final String name)
			throws FileNotFoundException {
		return (BufferedInputStream) newFileInputStream(file, name);
	}

	public BufferedInputStream newBufferedFileInputStreamWithFileDeleteOnClose(File file, final String name)
			throws FileNotFoundException {
		return (BufferedInputStream) newFileInputStream(file, name, true);
	}

	public OutputStream newFileOutputStream(final File file, final String name, boolean appendToFile)
			throws FileNotFoundException {
		FileOutputStream fileOutputStream = OpenedResourcesWatcher.onOpen(new FileOutputStream(file, appendToFile) {

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

	public OutputStream newFileOutputStream(final File file, final String name)
			throws FileNotFoundException {
		return newFileOutputStream(file, name, false);
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

	public Scanner newFileScanner(File file) {
		try {
			return new Scanner(file);
		} catch (FileNotFoundException e) {
			throw new RuntimeException(e);
		}
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