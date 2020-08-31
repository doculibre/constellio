package com.constellio.model.services.contents;

import com.constellio.data.io.services.facades.IOServices;
import com.constellio.data.utils.BigFileEntry;
import com.constellio.data.utils.BigFileIterator;
import com.constellio.data.utils.PropertyFileUtils;
import com.constellio.data.utils.TimeProvider;
import com.constellio.model.services.contents.ContentManagerException.ContentManagerException_ContentNotParsed;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.migrations.ConstellioEIMConfigs;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ContentManagerImportThreadServices {

	private static final int DEFAULT_BATCH_SIZE = 1000;
	private static final int THREADS = 3;

	private static final Logger LOGGER = LoggerFactory.getLogger(ContentManagerImportThreadServices.class);
	private static final String READ_FILE_INPUTSTREAM = "ContentManagerImportThreadServices-ReadFileInputStream";
	private static final String BIGFILE_EXTRACT_TEMP_FOLDER = "ContentManagerImportThreadServices-BigFileExtractTempFolder";

	private ModelLayerFactory modelLayerFactory;
	private ContentManager contentManager;
	private File contentImportFolder;
	private File toImportFolder;
	private File errorsEmptyFolder;
	private File errorsUnparsableFolder;
	private File indexProperties;
	private File filesExceedingParsingSizeLimit;
	private File tempFolder;
	private IOServices ioServices;
	private int batchSize;
	private boolean deleteUnusedContentEnabled;

	public ContentManagerImportThreadServices(ModelLayerFactory modelLayerFactory) {
		this(modelLayerFactory, DEFAULT_BATCH_SIZE);
	}

	public ContentManagerImportThreadServices(ModelLayerFactory modelLayerFactory, int batchSize) {
		this.batchSize = batchSize;
		this.modelLayerFactory = modelLayerFactory;
		this.ioServices = modelLayerFactory.getIOServicesFactory().newIOServices();
		this.contentManager = modelLayerFactory.getContentManager();
		this.contentImportFolder = modelLayerFactory.getConfiguration().getContentImportThreadFolder();
		this.deleteUnusedContentEnabled = modelLayerFactory.getConfiguration().isDeleteUnusedContentEnabled();
		this.tempFolder = new File(contentImportFolder, "temp");
		this.toImportFolder = new File(contentImportFolder, "toImport");
		this.errorsEmptyFolder = new File(contentImportFolder, "errors-empty");
		this.errorsUnparsableFolder = new File(contentImportFolder, "errors-unparsable");
		this.indexProperties = new File(contentImportFolder, "filename-sha1-index.properties");
		this.filesExceedingParsingSizeLimit = new File(contentImportFolder, "files-exceeding-parsing-size-limit.txt");
	}

	public void importFiles() {

		createFolders();
		if (deleteUnusedContentEnabled) {
			LOGGER.warn("Content import thread requires that configuration 'content.delete.unused.enabled' is set to false");
		} else {

			List<File> files = getFilesReadyToImport();
			if (!files.isEmpty()) {
				importFiles(files);

				ioServices.deleteEmptyDirectoriesExceptThisOneIn(toImportFolder);
				ioServices.deleteEmptyDirectoriesExceptThisOneIn(errorsEmptyFolder);
				ioServices.deleteEmptyDirectoriesExceptThisOneIn(errorsUnparsableFolder);
				ioServices.deleteEmptyDirectoriesExceptThisOneIn(tempFolder);
			}
		}
	}

	private void importFiles(List<File> files) {
		LOGGER.info("importing files " + files + "");
		BulkUploader uploader = new BulkUploader(modelLayerFactory, THREADS);
		uploader.setHandleDeletionOfUnreferencedHashes(false);

		List<File> extractedBigFileFolders = new ArrayList<>();

		Set<String> emptyFileKeys = new HashSet<>();

		for (File file : files) {
			if (file.getName().endsWith(".bigf")) {
				File bigFileTempFolder = new File(tempFolder, toKey(file));
				extractedBigFileFolders.add(bigFileTempFolder);

			} else {
				String key = toKey(file);
				if (file.length() > 0) {
					uploader.uploadAsync(key, ioServices.newInputStreamFactory(file, READ_FILE_INPUTSTREAM), key);
				} else {
					emptyFileKeys.add(key);
					File dest = new File(errorsEmptyFolder, key.replace("/", File.separator));
					dest.getParentFile().mkdirs();
					try {
						ioServices.moveFile(file, dest);
					} catch (Exception e) {
						LOGGER.warn("Failed to move empty file, deleting it...", e);
						FileUtils.deleteQuietly(file);
					}
				}
			}
		}

		for (File extractedBigFileFolder : extractedBigFileFolders) {

			for (File file : allFilesRecursivelyIn(extractedBigFileFolder)) {
				String key = toBigFileKey(extractedBigFileFolder, file);
				if (file.length() > 0) {
					uploader.uploadAsync(key, ioServices.newInputStreamFactory(file, READ_FILE_INPUTSTREAM), key);
				} else {
					emptyFileKeys.add(key);

					File dest = new File(errorsEmptyFolder, key.replace("/", File.separator));
					dest.getParentFile().mkdirs();
					try {
						ioServices.moveFile(file, dest);
					} catch (Exception e) {
						LOGGER.warn("Failed to move empty file, deleting it...", e);
						FileUtils.deleteQuietly(file);
					}

				}
			}
		}

		uploader.close();

		Map<String, ContentVersionDataSummary> newEntriesInIndex = new HashMap<>();

		try {
			for (File extractedBigFileFolder : extractedBigFileFolders) {
				for (File file : allFilesRecursivelyIn(extractedBigFileFolder)) {
					String key = toBigFileKey(extractedBigFileFolder, file);

					ContentVersionDataSummary dataSummary = uploader.get(key);
					newEntriesInIndex.put(key, dataSummary);

					try {
						if (contentManager.getParsedContent(dataSummary.getHash()).getParsedContent().isEmpty()) {
							if (fileNotExceedingParsingLimit(file)) {
								ioServices.moveFile(file, new File(errorsUnparsableFolder, key.replace("/", File.separator)));
							} else {
								try {
									ioServices.appendFileContent(filesExceedingParsingSizeLimit, key + "\n");
								} catch (IOException e) {
									throw new RuntimeException(e);
								}
								ioServices.deleteQuietly(file);
							}
						} else {
							ioServices.deleteQuietly(file);
						}
					} catch (ContentManagerException_ContentNotParsed contentManagerException_contentNotParsed) {
						throw new RuntimeException(contentManagerException_contentNotParsed);
					}

					//uploader.uploadAsync(toKey(file), ioServices.newInputStreamFactory(file, READ_FILE_INPUTSTREAM));
				}
			}

			for (File file : files) {
				if (!file.getName().endsWith(".bigf")) {
					String key = toKey(file);
					if (!emptyFileKeys.contains(key)) {
						ContentVersionDataSummary dataSummary = uploader.get(key);
						newEntriesInIndex.put(key, dataSummary);

						if (contentManager.getParsedContent(dataSummary.getHash()).getParsedContent().isEmpty()) {
							if (fileNotExceedingParsingLimit(file)) {
								ioServices.moveFile(file, new File(errorsUnparsableFolder, key.replace("/", File.separator)));
							} else {
								try {
									ioServices.appendFileContent(filesExceedingParsingSizeLimit, key + "\n");
								} catch (IOException e) {
									throw new RuntimeException(e);
								}
								ioServices.deleteQuietly(file);
							}
						} else {
							ioServices.deleteQuietly(file);
						}
					}
				} else {
					ioServices.deleteQuietly(file);
				}
			}
		} catch (ContentManagerException_ContentNotParsed contentManagerException_contentNotParsed) {
			throw new RuntimeException(contentManagerException_contentNotParsed);

		} finally {
			writeNewEntriesInIndex(newEntriesInIndex);
		}

	}

	private boolean fileNotExceedingParsingLimit(File file) {
		long limit = (int) modelLayerFactory.getSystemConfigurationsManager()
				.getValue(ConstellioEIMConfigs.CONTENT_MAX_LENGTH_FOR_PARSING_IN_MEGAOCTETS) * 1024 * 1024;
		return file.length() <= limit;
	}

	private void writeNewEntriesInIndex(Map<String, ContentVersionDataSummary> newEntriesInIndex) {
		Map<String, String> map = indexProperties.exists() ?
								  new HashMap<>(PropertyFileUtils.loadKeyValues(indexProperties)) : new HashMap<String, String>();
		for (Map.Entry<String, ContentVersionDataSummary> entry : newEntriesInIndex.entrySet()) {
			map.put(entry.getKey(), toStringValue(entry.getValue()));
		}

		PropertyFileUtils.writeMap(indexProperties, map);
	}

	private String toStringValue(ContentVersionDataSummary value) {
		return value.getHash() + ":" + value.getLength() + ":" + value.getMimetype();
	}

	private String toBigFileKey(File extractedBigFileFolder, File file) {
		String bigFile = extractedBigFileFolder.getAbsolutePath()
				.replace(tempFolder.getAbsolutePath() + File.separator, "");

		String entryPath = file.getAbsolutePath().replace(extractedBigFileFolder.getAbsolutePath() + File.separator, "");

		return (bigFile + "/" + entryPath).replace("/", File.separator);
	}

	private String toKey(File file) {
		return file.getAbsolutePath().replace(toImportFolder.getAbsolutePath() + File.separator, "")
				.replace(File.separator, "/");
	}

	private int extractBigFile(File bigFile) {
		File bigFileTempFolder = new File(tempFolder, toKey(bigFile));
		bigFileTempFolder.mkdirs();

		int entriesCount = 0;
		InputStream inputStream = ioServices.newBufferedFileInputStreamWithoutExpectableFileNotFoundException(
				bigFile, READ_FILE_INPUTSTREAM);
		try {
			BigFileIterator bigFileIterator = new BigFileIterator(inputStream);
			while (bigFileIterator.hasNext()) {
				BigFileEntry entry = bigFileIterator.next();
				entriesCount++;
				File destFileForCopy = new File(bigFileTempFolder, entry.getFileName().replace("/", File.separator));
				try {
					FileUtils.forceMkdir(destFileForCopy.getParentFile());
					ioServices.replaceFileContent(destFileForCopy, entry.getBytes());

				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			}
		} finally {
			ioServices.closeQuietly(inputStream);
		}

		return entriesCount;
	}

	private void uploadBigFile(File file, BulkUploader uploader) {

	}

	private List<File> getFilesReadyToImport() {

		List<File> filesReadyToImport = new ArrayList<>();

		int currentBatchSize = 0;
		for (File fileToImport : allFilesRecursivelyIn(toImportFolder)) {
			if (currentBatchSize < batchSize
				&& TimeProvider.getLocalDateTime().minusSeconds(10).toDate().getTime() >= fileToImport.lastModified()) {
				if (fileToImport.getName().endsWith(".bigf")) {
					Integer size = extractBigFile(fileToImport);
					filesReadyToImport.add(fileToImport);
					currentBatchSize += size;
				} else {
					filesReadyToImport.add(fileToImport);
					currentBatchSize++;
				}
			}
		}
		if (filesReadyToImport.size() > batchSize) {
			filesReadyToImport = filesReadyToImport.subList(0, batchSize);
		}

		return filesReadyToImport;
	}

	private List<File> allFilesRecursivelyIn(File folder) {
		List<File> files = new ArrayList<>(FileUtils.listFiles(folder, TrueFileFilter.INSTANCE, TrueFileFilter.INSTANCE));
		Collections.sort(files, new Comparator<File>() {
			@Override
			public int compare(File o1, File o2) {
				return o1.getAbsolutePath().compareTo(o2.getAbsolutePath());
			}
		});
		return files;
	}

	private void createFolders() {
		tempFolder.mkdirs();
		toImportFolder.mkdirs();
		errorsEmptyFolder.mkdirs();
		errorsUnparsableFolder.mkdirs();
		try {
			FileUtils.touch(filesExceedingParsingSizeLimit);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public Map<String, String> readFileNameSHA1Index() {
		return readFileNameSHA1Index(indexProperties);
	}

	public Map<String, String> readFileNameSHA1Index(File sha1Properties) {
		if (!sha1Properties.exists()) {
			return Collections.emptyMap();
		}
		Map<String, String> map = new HashMap<>();

		for (Map.Entry<String, String> entry : PropertyFileUtils.loadKeyValues(sha1Properties).entrySet()) {
			map.put(entry.getKey(), StringUtils.substringBefore(entry.getValue(), ":"));
		}
		return map;
	}

	public static Map<String, String> buildSHA1Map(File file) {

		Map<String, String> map = new LinkedHashMap<>();

		for (Map.Entry<String, String> entry : PropertyFileUtils.loadKeyValues(file).entrySet()) {
			map.put(entry.getKey(), StringUtils.substringBefore(entry.getValue(), ":"));
		}
		return map;
	}

	public static class FilenameSha1PropertiesEntry {

		private String name;

		private ContentVersionDataSummary contentVersionDataSummary;

		public FilenameSha1PropertiesEntry(String name,
										   ContentVersionDataSummary contentVersionDataSummary) {
			this.name = name;
			this.contentVersionDataSummary = contentVersionDataSummary;
		}

		public String getName() {
			return name;
		}

		public ContentVersionDataSummary getContentVersionDataSummary() {
			return contentVersionDataSummary;
		}
	}

	private static ContentVersionDataSummary toContentVersionDataSummary(String value) {
		String[] parts = value.split(":");
		String mimetype = "null".equals(parts[2]) ? null : parts[2];
		return new ContentVersionDataSummary(parts[0], mimetype, Integer.valueOf(parts[1]));
	}

}
