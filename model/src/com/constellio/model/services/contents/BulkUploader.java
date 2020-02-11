package com.constellio.model.services.contents;

import com.constellio.data.io.services.facades.IOServices;
import com.constellio.data.io.streamFactories.StreamFactory;
import com.constellio.model.services.contents.ContentManager.ParseOptions;
import com.constellio.model.services.contents.ContentManager.UploadOptions;
import com.constellio.model.services.contents.ContentManagerRuntimeException.ContentManagerRuntimeException_CannotReadParsedContent;
import com.constellio.model.services.factories.ModelLayerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class BulkUploader {

	private static final Logger LOGGER = LoggerFactory.getLogger(BulkUploader.class);
	private static final String READ_STREAM_RESOURCE = "BatchContentsUploader-readStream";

	AtomicInteger progression = new AtomicInteger();
	AtomicInteger total = new AtomicInteger();

	Map<String, Throwable> exceptionsMap = new HashMap<>();
	Map<String, ContentVersionDataSummary> summariesMap = new HashMap<>();
	ForkJoinPool pool;
	IOServices ioServices;
	ContentManager contentManager;
	boolean handleDeletionOfUnreferencedHashes = true;

	public BulkUploader(ModelLayerFactory modelLayerFactory) {
		this(modelLayerFactory, 4);
	}

	public BulkUploader(ModelLayerFactory modelLayerFactory, int threads) {
		this.contentManager = modelLayerFactory.getContentManager();
		this.ioServices = modelLayerFactory.getDataLayerFactory().getIOServicesFactory().newIOServices();
		this.pool = new ForkJoinPool(threads);
	}

	public void setHandleDeletionOfUnreferencedHashes(boolean handleDeletionOfUnreferencedHashes) {
		this.handleDeletionOfUnreferencedHashes = handleDeletionOfUnreferencedHashes;
	}

	public void close() {
		pool.shutdown();
		try {
			pool.awaitTermination(2, TimeUnit.DAYS);
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
	}

	public void uploadAsync(final String key, File file) {
		uploadAsync(key, ioServices.newInputStreamFactory(file, "BulkUploader-input"), true, file.getName());
	}

	public void uploadAsync(final String key, final StreamFactory<InputStream> streamFactory) {
		uploadAsync(key, streamFactory, true, null);
	}

	public void uploadAsync(final String key, final StreamFactory<InputStream> streamFactory, String fileName) {
		uploadAsync(key, streamFactory, true, fileName);
	}

	public void uploadAsyncWithoutParsing(final String key, final StreamFactory<InputStream> streamFactory,
										  String fileName) {
		uploadAsync(key, streamFactory, false, fileName);
	}

	private void uploadAsync(final String key, final StreamFactory<InputStream> streamFactory, final boolean parse,
							 final String fileName) {

		total.incrementAndGet();
		pool.execute(new Runnable() {
			@Override
			public void run() {
				InputStream inputStream = null;
				try {

					inputStream = streamFactory.create(READ_STREAM_RESOURCE);

					UploadOptions options = new UploadOptions(fileName)
							.setHandleDeletionOfUnreferencedHashes(handleDeletionOfUnreferencedHashes);
					if (parse) {
						options.setParseOptions(new ParseOptions());
					}

					ContentVersionDataSummary summary = contentManager.upload(inputStream, options)
							.getContentVersionDataSummary();
					writeToMap(key, summary);

				} catch (ContentManagerRuntimeException_CannotReadParsedContent e) {
					//The file is probably being written by an other thread. The exception is logged and will be thrown if it's not the case.
					writeToMap(key, e);

				} catch (Throwable e) {
					new RuntimeException("Failed to upload " + key, e).printStackTrace();
					writeToMap(key, e);

				} finally {
					ioServices.closeQuietly(inputStream);
				}
				int currentProgression = progression.incrementAndGet();
				if (currentProgression % 100 == 0) {
					LOGGER.info("Bulk uploading " + currentProgression + "/" + total);
				}
			}
		});
	}

	private synchronized void writeToMap(String key, Throwable throwable) {
		exceptionsMap.put(key, throwable);
	}

	private synchronized void writeToMap(String key, ContentVersionDataSummary summary) {
		summariesMap.put(key, summary);
	}

	public ContentVersionDataSummary get(String key) {
		if (!summariesMap.containsKey(key)) {
			throw new BulkUploaderRuntimeException(key, exceptionsMap.get(key));
		}
		return summariesMap.get(key);
	}
}
