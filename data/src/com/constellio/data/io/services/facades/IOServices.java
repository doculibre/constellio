package com.constellio.data.io.services.facades;

import com.constellio.data.dao.services.idGenerator.UUIDV1Generator;
import com.constellio.data.io.streamFactories.CloseableStreamFactory;
import com.constellio.data.io.streamFactories.StreamFactory;
import com.constellio.data.io.streamFactories.impl.CopyInputStreamFactory;
import com.constellio.data.io.streamFactories.impl.CopyInputStreamFactoryRuntimeException;
import com.constellio.data.io.streamFactories.services.StreamFactoriesServices;
import com.constellio.data.io.streamFactories.services.one.StreamOperation;
import com.constellio.data.io.streamFactories.services.one.StreamOperationReturningValue;
import com.constellio.data.io.streamFactories.services.one.StreamOperationReturningValueOrThrowingException;
import com.constellio.data.io.streamFactories.services.one.StreamOperationThrowingException;
import com.constellio.data.io.streamFactories.services.two.TwoStreamsOperation;
import com.constellio.data.io.streamFactories.services.two.TwoStreamsOperationReturningValue;
import com.constellio.data.io.streamFactories.services.two.TwoStreamsOperationReturningValueOrThrowingException;
import com.constellio.data.io.streamFactories.services.two.TwoStreamsOperationThrowingException;
import com.constellio.data.io.streams.factories.StreamsServices;
import com.constellio.data.utils.ImpossibleRuntimeException;
import com.constellio.data.utils.Octets;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.filefilter.IOFileFilter;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.util.Collection;
import java.util.List;
import java.util.Scanner;

public class IOServices {

	private static final String REPLACE_FILE_CONTENT_TEMP_FILE = "IOServices-replaceFileContentTempFile";

	private final File tempFolder;
	private final FileService fileServices;
	private final StreamsServices streamsServices;
	private final StreamFactoriesServices streamFactoriesServices;

	public IOServices(File tempFolder) {
		super();
		this.tempFolder = tempFolder;
		this.fileServices = new FileService(tempFolder);
		this.streamsServices = new StreamsServices(fileServices);
		this.streamFactoriesServices = new StreamFactoriesServices(streamsServices);
	}

	public byte[] readBytes(InputStream inputStream)
			throws IOException {
		return streamsServices.readBytes(inputStream);
	}

	public void copyAndClose(InputStream inputStream, OutputStream outputStream)
			throws IOException {

		try {
			copy(inputStream, outputStream);

		} finally {
			closeQuietly(inputStream);
			closeQuietly(outputStream);
		}
	}

	public void copy(InputStream inputStream, OutputStream outputStream)
			throws IOException {
		streamsServices.copy(inputStream, outputStream);
	}

	public void copyLarge(InputStream inputStream, OutputStream outputStream)
			throws IOException {
		streamsServices.copyLarge(inputStream, outputStream);
	}

	public void deleteQuietly(File file) {
		fileServices.deleteQuietly(file);
	}

	public void closeQuietly(Closeable closeable) {
		streamsServices.closeQuietly(closeable);
	}

	public InputStream newByteInputStream(byte[] theContentBytes, String name) {
		return streamsServices.newByteArrayInputStream(theContentBytes, uniqueIdWith(name));
	}

	public InputStream newBufferedInputStream(InputStream inputStream, String name) {
		return streamsServices.newBufferedInputStream(inputStream, uniqueIdWith(name));
	}

	public BufferedReader newBufferedReader(Reader reader, String name) {
		return streamsServices.newBufferedReader(reader, uniqueIdWith(name));
	}

	public BufferedReader newBufferedFileReader(File file, String name) {
		return streamsServices.newFileReader(file, uniqueIdWith(name));
	}


	public BufferedWriter newBufferedFileWriter(File file, String name) {
		return streamsServices.newFileWriter(file, uniqueIdWith(name));
	}

	public BufferedReader newFileReader(File file, String name) {
		return streamsServices.newFileReader(file, uniqueIdWith(name));
	}

	public StreamFactory<Reader> newFileReaderFactory(File file)
			throws FileNotFoundException {
		return streamsServices.newFileReaderFactory(file);
	}

	public Scanner newFileScanner(File file) {
		return streamsServices.newFileScanner(file);
	}

	public InputStream newFileInputStream(File file, String name)
			throws FileNotFoundException {
		return streamsServices.newFileInputStream(file, uniqueIdWith(name));
	}

	public InputStream newBufferedByteArrayInputStream(byte[] byteArray, String name) {
		return streamsServices.newBufferedByteArrayInputStream(byteArray, uniqueIdWith(name));
	}

	public InputStream newBufferedFileInputStream(File file, String name)
			throws FileNotFoundException {
		return streamsServices.newBufferedFileInputStream(file, uniqueIdWith(name));
	}

	public InputStream newBufferedFileInputStreamWithFileDeleteOnClose(File file, String name)
			throws FileNotFoundException {
		return streamsServices.newBufferedFileInputStreamWithFileDeleteOnClose(file, uniqueIdWith(name));
	}


	public InputStream newBufferedFileInputStreamWithFileClosingAction(File file, String name,
																	   Runnable runnable)
			throws FileNotFoundException {
		return streamsServices.newBufferedFileInputStreamWithFileClosingAction(file, uniqueIdWith(name), runnable);
	}

	public OutputStream newBufferedFileOutputStreamWithFileClosingAction(File file, String name,
																		 Runnable runnable)
			throws FileNotFoundException {
		return streamsServices.newBufferedFileOutputStreamWithFileClosingAction(file, uniqueIdWith(name), runnable);
	}

	public BufferedWriter newBufferedFileWriterWithFileClosingAction(File file, String name,
																	 Runnable runnable)
			throws IOException {
		return streamsServices.newBufferedFileWriterWithFileClosingAction(file, uniqueIdWith(name), runnable);
	}


	public BufferedInputStream newBufferedFileInputStreamWithoutExpectableFileNotFoundException(File file,
																								String name) {
		try {
			return streamsServices.newBufferedFileInputStream(file, uniqueIdWith(name));
		} catch (FileNotFoundException e) {
			throw new ImpossibleRuntimeException("File does not exist: " + file.getAbsolutePath(), e);
		}
	}

	public ByteArrayInputStream newByteArrayInputStream(byte[] byteArray, String name) {
		return streamsServices.newByteArrayInputStream(byteArray, uniqueIdWith(name));
	}

	public OutputStream newBufferedOutputStream(OutputStream outputStream, String name) {
		return streamsServices.newBufferedOutputStream(outputStream, uniqueIdWith(name));
	}

	public OutputStream newBufferedFileOutputStream(File file, String name)
			throws FileNotFoundException {
		return streamsServices.newBufferedFileOutputStream(file, uniqueIdWith(name));
	}

	public OutputStream newBufferedFileOutputStreamWithoutExpectableFileNotFoundException(File file, String name) {
		try {
			return streamsServices.newBufferedFileOutputStream(file, uniqueIdWith(name));
		} catch (FileNotFoundException e) {
			throw new ImpossibleRuntimeException(e);
		}
	}

	public OutputStream newFileOutputStream(File file, String name, boolean append)
			throws FileNotFoundException {
		return streamsServices.newFileOutputStream(file, uniqueIdWith(name), append);
	}

	public OutputStream newFileOutputStream(File file, String name)
			throws FileNotFoundException {
		return streamsServices.newFileOutputStream(file, uniqueIdWith(name));
	}

	public ByteArrayOutputStream newByteArrayOutputStream(String name) {
		return streamsServices.newByteArrayOutputStream(uniqueIdWith(name));
	}

	public StreamFactory<InputStream> newByteArrayStreamFactory(final byte[] bytes, String name) {
		return streamsServices.newByteArrayStreamFactory(bytes, uniqueIdWith(name));
	}

	public <F extends Closeable> void execute(StreamOperation<F> operation, StreamFactory<F> closeableStreamFactory)
			throws IOException {
		streamFactoriesServices.execute(operation, closeableStreamFactory);
	}

	public <F extends Closeable, R> R execute(StreamOperationReturningValue<F, R> operation,
											  StreamFactory<F> closeableStreamFactory)
			throws IOException {
		return streamFactoriesServices.execute(operation, closeableStreamFactory);
	}

	public <F extends Closeable, R, E extends Exception> R execute(
			StreamOperationReturningValueOrThrowingException<F, R, E> operation,
			StreamFactory<F> closeableStreamFactory)
			throws E, IOException {
		return streamFactoriesServices.execute(operation, closeableStreamFactory);
	}

	public <F extends Closeable, E extends Exception> void execute(StreamOperationThrowingException<F, E> operation,
																   StreamFactory<F> closeableStreamFactory)
			throws E, IOException {
		streamFactoriesServices.execute(operation, closeableStreamFactory);
	}

	public <F extends Closeable, S extends Closeable> void execute(TwoStreamsOperation<F, S> operation,
																   StreamFactory<F> firstCloseableStreamFactory,
																   StreamFactory<S> secondCloseableStreamFactory)
			throws IOException {
		streamFactoriesServices.execute(operation, firstCloseableStreamFactory, secondCloseableStreamFactory);
	}

	public <F extends Closeable, S extends Closeable, R> R execute(TwoStreamsOperationReturningValue<F, S, R> operation,
																   StreamFactory<F> firstCloseableStreamFactory,
																   StreamFactory<S> secondCloseableStreamFactory)
			throws IOException {
		return streamFactoriesServices.execute(operation, firstCloseableStreamFactory, secondCloseableStreamFactory);
	}

	public <F extends Closeable, S extends Closeable, R, E extends Exception> R execute(
			TwoStreamsOperationReturningValueOrThrowingException<F, S, R, E> operation,
			StreamFactory<F> firstCloseableStreamFactory, StreamFactory<S> secondCloseableStreamFactory)
			throws E, IOException {
		return streamFactoriesServices.execute(operation, firstCloseableStreamFactory, secondCloseableStreamFactory);
	}

	public <F extends Closeable, S extends Closeable, E extends Exception> void execute(
			TwoStreamsOperationThrowingException<F, S, E> operation, StreamFactory<F> firstCloseableStreamFactory,
			StreamFactory<S> secondCloseableStreamFactory)
			throws E, IOException {
		streamFactoriesServices.execute(operation, firstCloseableStreamFactory, secondCloseableStreamFactory);
	}

	public void closeQuietly(CloseableStreamFactory<InputStream> inputStreamFactory) {
		streamFactoriesServices.closeQuietly(inputStreamFactory);
	}

	public CopyInputStreamFactory copyToReusableStreamFactory(InputStream inputStream, String filename)
			throws CopyInputStreamFactoryRuntimeException {
		CopyInputStreamFactory copyInputStreamFactory = new CopyInputStreamFactory(this, Octets.megaoctets(10));
		try {
			copyInputStreamFactory.saveInputStreamContent(inputStream, filename);
		} finally {
			IOUtils.closeQuietly(inputStream);
		}

		return copyInputStreamFactory;
	}

	public void copyDirectory(File srcDir, File destDir)
			throws IOException {
		fileServices.copyDirectory(srcDir, destDir);
	}

	public void copyDirectoryWithoutExpectableIOException(File srcDir, File destDir) {
		fileServices.copyDirectoryWithoutExpectableIOException(srcDir, destDir);
	}

	public void copyFile(File srcFile, File destFile)
			throws IOException {
		fileServices.copyFile(srcFile, destFile);
	}

	public void copyFileWithoutExpectableIOException(File srcFile, File destFile) {
		fileServices.copyFileWithoutExpectableIOException(srcFile, destFile);
	}

	public String readFileToString(File file)
			throws IOException {
		return fileServices.readFileToString(file);
	}

	public String readFileToStringWithoutExpectableIOException(File file) {
		return fileServices.readFileToStringWithoutExpectableIOException(file);
	}

	public List<String> readFileToLinesWithoutExpectableIOException(File file) {
		return fileServices.readFileToLinesWithoutExpectableIOException(file);
	}

	public void replaceFileContent(File file, String data)
			throws IOException {
		fileServices.replaceFileContent(file, data);
	}

	public void appendFileContent(File file, String data)
			throws IOException {
		fileServices.appendFileContent(file, data);
	}

	public void ensureWritePermissions(File file)
			throws IOException {
		fileServices.ensureWritePermissions(file);
	}

	public Collection<File> listFiles(File directory, IOFileFilter fileFilter, IOFileFilter dirFilter) {
		return fileServices.listFiles(directory, fileFilter, dirFilter);
	}

	public Collection<File> listRecursiveFiles(File directory) {
		return fileServices.listRecursiveFiles(directory);
	}

	public Collection<File> listRecursiveFiles(File directory, IOFileFilter fileFilter) {
		return fileServices.listRecursiveFiles(directory, fileFilter);
	}

	public Collection<File> listRecursiveFilesWithName(File directory, String name) {
		return fileServices.listRecursiveFilesWithName(directory, name);
	}

	public void deleteDirectory(File directory)
			throws IOException {
		fileServices.deleteDirectory(directory);
	}

	public void deleteDirectoryWithoutExpectableIOException(File directory) {
		fileServices.deleteDirectoryWithoutExpectableIOException(directory);
	}

	public File newTemporaryFolder(String resourceName) {
		return fileServices.newTemporaryFolder(resourceName);
	}

	public File newTemporaryFolderWithoutExpectableIOException(String resourceName) {
		return fileServices.newTemporaryFolder(resourceName);
	}

	public String readStreamToStringWithoutExpectableIOException(InputStream inputStream) {
		return fileServices.readStreamToStringWithoutExpectableIOException(inputStream);
	}

	public String readStreamToString(InputStream inputStream)
			throws IOException {
		return fileServices.readStreamToString(inputStream);
	}

	public List<String> readStreamToLines(InputStream inputStream)
			throws IOException {
		return fileServices.readStreamToLines(inputStream);
	}

	public StreamFactory<InputStream> newInputStreamFactory(File file, String name) {
		return streamsServices.newInputStreamFactory(file, uniqueIdWith(name));
	}

	public StreamFactory<OutputStream> newOutputStreamFactory(File file, String name) {
		return streamsServices.newOutputStreamFactory(file, uniqueIdWith(name));
	}

	public StreamFactory<InputStream> newInputStreamFactory(String string) {
		return streamsServices.newInputStreamFactory(string);
	}

	private String uniqueIdWith(String name) {
		return name + "-" + UUIDV1Generator.newRandomId();
	}

	public File newTemporaryFile(String resourceName) {
		return fileServices.newTemporaryFile(resourceName);
	}

	public File newTemporaryFileWithoutGuid(String resourceName) {
		return fileServices.newTemporaryFileWithoutGuid(resourceName);
	}

	public File newTemporaryFile(String resourceName, String extension) {
		return fileServices.newTemporaryFile(resourceName, extension);
	}

	public void touch(File file) {
		try {
			FileUtils.touch(file);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public File getAtomicWriteTempFileFor(File propertiesFile) {
		return fileServices.getAtomicWriteTempFileFor(propertiesFile);
	}

	public void moveFolder(File src, File dest) {
		fileServices.moveFolder(src, dest);
	}

	public void moveFile(File src, File dest) {
		fileServices.moveFile(src, dest);
	}

	public void replaceFileContent(File file, byte[] bytes)
			throws IOException {
		InputStream byteArrayInputStream = new ByteArrayInputStream(bytes);
		try {
			replaceFileContent(file, byteArrayInputStream);
		} finally {
			byteArrayInputStream.close();
		}
	}

	public void replaceFileContent(File file, InputStream inputStream)
			throws IOException {
		File tempFile = getAtomicWriteTempFileFor(file);

		OutputStream outputStream = newFileOutputStream(tempFile, REPLACE_FILE_CONTENT_TEMP_FILE);
		try {
			copy(inputStream, outputStream);

		} catch (IOException e) {
			tempFile.delete();
			throw e;

		} finally {
			closeQuietly(outputStream);
		}

		moveFile(tempFile, file);
	}

	public void deleteEmptyDirectoriesExceptThisOneIn(File folder) {
		deleteEmptyDirectoriesIn(folder, true);
	}

	public void deleteEmptyDirectoriesIn(File folder, boolean exceptThisDirectory) {
		boolean hasFile = false;
		File[] files = folder.listFiles();
		if (files != null) {
			for (File file : files) {
				if (file.isDirectory()) {
					deleteEmptyDirectoriesIn(file, false);
					if (file.exists()) {
						hasFile = true;
					}

				} else {
					hasFile = true;
				}
			}
		}

		if (!hasFile && !exceptThisDirectory) {
			try {
				deleteDirectory(folder);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
	}
}