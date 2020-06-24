package com.constellio.data.dao.services.contents;

import com.constellio.data.dao.services.contents.ContentDaoException.ContentDaoException_NoSuchContent;
import com.constellio.data.io.services.facades.IOServices;
import com.constellio.data.io.streamFactories.CloseableStreamFactory;
import com.constellio.data.utils.ImpossibleRuntimeException;
import org.apache.commons.io.IOUtils;
import org.joda.time.LocalDateTime;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Predicate;
import java.util.stream.Stream;

public interface ContentDao {

	interface VaultProducer {
		void produce(File file) throws IOException;
	}

	interface DaoFileFunction<T> {
		T apply(File file) throws IOException;
	}

	interface DaoFileConsumer {
		void accept(File file) throws IOException;
	}

	enum MoveToVaultOption {
		ONLY_IF_INEXISTING
	}

	void moveFileToVault(String id, File file, MoveToVaultOption... options);

	/**
	 * Copy in a file
	 *
	 * @param id
	 * @param outputFile
	 */
	default void copyFileFromVault(String id, File outputFile) throws ContentDaoException_NoSuchContent {

		InputStream in = null;
		OutputStream out = null;
		try {
			in = getContentInputStream(id, "ContentDao-copyFileFromVault");
			out = new BufferedOutputStream(new FileOutputStream(outputFile));

			IOUtils.copy(in, out);

		} catch (IOException e) {
			throw new RuntimeException(e);

		} finally {
			getIOServices().closeQuietly(in);
			getIOServices().closeQuietly(out);
		}

	}


	/**
	 * Call the consumer's consume method with a file containing the desired content.
	 * <p>
	 * The consumer is expected to not altering the file during the consuming. An error will be thrown if the file
	 * is modified, since the file could be directly the file in the vault, all changes made to the file won't be
	 * cancellable. Also, the file could be temporary and deleted after consumption.
	 * <p>
	 * The consumer will only be called if the file exists
	 *
	 * @param id
	 * @param consumer
	 */
	default void readonlyConsumeIfExists(String id, DaoFileConsumer consumer) {
		try {
			readonlyConsume(id, consumer);
		} catch (ContentDaoException_NoSuchContent ignored) {
		}
	}

	/**
	 * Call the consumer's consume method with a file containing the desired content.
	 * <p>
	 * The consumer is expected to not altering the file during the consuming. An error will be thrown if the file
	 * is modified, since the file could be directly the file in the vault, all changes made to the file won't be
	 * cancellable. Also, the file could be temporary and deleted after consumption.
	 * <p>
	 * The consumer will only be called if the file exists, otherwise an ContentDaoException_NoSuchContent is thrown
	 *
	 * @param id
	 * @param consumer
	 */
	default void readonlyConsume(String id, DaoFileConsumer consumer)
			throws ContentDaoException.ContentDaoException_NoSuchContent {
		InputStream in = getContentInputStream(id, "ContentDao.readonlyConsume.inputstream");

		File tempFile = null;
		try {
			tempFile = getIOServices().newTemporaryFile("ContentDao.readonlyConsume.tempFile");

			OutputStream out = null;
			try {
				out = getIOServices().newBufferedFileOutputStream(tempFile, "ContentDao.readonlyConsume.tempFile.out");
				getIOServices().copy(in, out);

			} finally {
				getIOServices().closeQuietly(out);
			}
			long lastModifiedBefore = tempFile.lastModified();
			long lengthBefore = tempFile.lastModified();
			consumer.accept(tempFile);

			File tempFileAfterConsume = new File(tempFile.getAbsolutePath());
			long lastModifiedAfter = tempFileAfterConsume.exists() ? tempFileAfterConsume.lastModified() : -1;
			long lengthAfter = tempFileAfterConsume.lastModified();

			if (lastModifiedBefore != lastModifiedAfter || lengthBefore != lengthAfter) {
				//This message may seems overkill, but this method is expected to be redefined in FileSystemContentDao,
				// giving access directly on the file
				throw new Error("File '" + tempFile + "' was modified, the consumer was expected to be readonly.");
			}

		} catch (IOException e) {
			throw new RuntimeException(e);

		} finally {
			getIOServices().deleteQuietly(tempFile);
			getIOServices().closeQuietly(in);
		}
	}

	default <T> T readonlyFunction(String id, DaoFileFunction<T> function) throws ContentDaoException_NoSuchContent {
		AtomicReference<T> methodReturn = new AtomicReference<>();
		readonlyConsume(id, (f) -> methodReturn.set(function.apply(f)));
		return methodReturn.get();
	}

	/**
	 * Call the producer's produce method with an inexisting file, which is not the final destination of the data.
	 * <p>
	 * The consumer is expected to write the file, otherwise a ContentDaoRuntimeException_WriteCancelled will be thrown
	 *
	 * @param id
	 * @param producer
	 */
	default void produceAtVaultLocation(String id, VaultProducer producer) {
		File tempFile = getIOServices().newTemporaryFile("ContentDao.writeToVault.tempFile");
		try {
			producer.produce(tempFile);
			if (!tempFile.exists()) {
				throw new ContentDaoRuntimeException.ContentDaoRuntimeException_WriteCancelled("File was not created : " + id);
			}
			moveFileToVault(id, tempFile);

		} catch (IOException e) {
			//This should never occur
			throw new ImpossibleRuntimeException(e);

		} finally {
			getIOServices().deleteQuietly(tempFile);
		}
	}

	/**
	 * Copy the file in the vault
	 *
	 * @param id
	 * @param inputFile
	 */
	default void copyFileToVault(String id, File inputFile) {
		InputStream in = null;
		try {
			in = new BufferedInputStream(new FileInputStream(inputFile));
			add(id, in);

		} catch (IOException e) {
			throw new RuntimeException(e);

		} finally {
			getIOServices().closeQuietly(in);
		}

	}


	void deleteFileNameContaining(String contentId, String filter);

	LocalDateTime getLastModification(String contentId);


	String getLocalRelativePath(String id);

	IOServices getIOServices();

	void add(String id, InputStream newInputStream);

	void delete(List<String> ids);

	InputStream getContentInputStream(String id, String streamName)
			throws ContentDaoException_NoSuchContent;

	List<String> getFolderContents(String folderRelativePath);

	boolean isFolderExisting(String folderRelativePath);

	boolean isDocumentExisting(String id);

	long getContentLength(String id);

	void moveFolder(String srcFolderRelativePath, String destFolderRelativePath);

	void deleteFolder(String folderRelativePath);

	CloseableStreamFactory<InputStream> getContentInputStreamFactory(String id)
			throws ContentDaoException_NoSuchContent;


	@Deprecated
	File getFileOf(String id);

	DaoFile getFile(String id);

	void readLogsAndRepairs();

	Stream<Path> streamVaultContent(Predicate<? super Path> filter);
}