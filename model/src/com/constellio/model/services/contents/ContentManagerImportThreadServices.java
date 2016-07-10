package com.constellio.model.services.contents;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.TrueFileFilter;

import com.constellio.data.io.services.facades.IOServices;
import com.constellio.data.utils.BigFileEntry;
import com.constellio.data.utils.BigFileIterator;
import com.constellio.data.utils.PropertyFileUtils;
import com.constellio.data.utils.TimeProvider;
import com.constellio.model.services.factories.ModelLayerFactory;

public class ContentManagerImportThreadServices {

	private static final String READ_FILE_INPUTSTREAM = "ContentManagerImportThreadServices-ReadFileInputStream";
	private static final String BIGFILE_EXTRACT_TEMP_FOLDER = "ContentManagerImportThreadServices-BigFileExtractTempFolder";

	private ModelLayerFactory modelLayerFactory;
	private ContentManager contentManager;
	private File contentImportFolder;
	private File toImportFolder;
	private File errorsEmptyFolder;
	private File errorsUnparsableFolder;
	private File indexProperties;
	private File tempFolder;
	private IOServices ioServices;

	public ContentManagerImportThreadServices(ModelLayerFactory modelLayerFactory) {
		this.modelLayerFactory = modelLayerFactory;
		this.ioServices = modelLayerFactory.getIOServicesFactory().newIOServices();
		this.contentManager = modelLayerFactory.getContentManager();
		this.contentImportFolder = modelLayerFactory.getConfiguration().getContentImportThreadFolder();
		this.tempFolder = new File(contentImportFolder, "temp");
		this.toImportFolder = new File(contentImportFolder, "toImport");
		this.errorsEmptyFolder = new File(contentImportFolder, "errors-empty");
		this.errorsUnparsableFolder = new File(contentImportFolder, "errors-unparsable");
		this.indexProperties = new File(contentImportFolder, "filename-sha1-index.properties");
	}

	public void importFiles() {
		createFolders();

		List<File> files = getFilesReadyToImport();
		importFiles(files);
		ioServices.deleteEmptyDirectoriesExceptThisOneIn(toImportFolder);
		ioServices.deleteEmptyDirectoriesExceptThisOneIn(errorsEmptyFolder);
		ioServices.deleteEmptyDirectoriesExceptThisOneIn(errorsUnparsableFolder);
		ioServices.deleteEmptyDirectoriesExceptThisOneIn(tempFolder);
	}

	private void importFiles(List<File> files) {
		BulkUploader uploader = new BulkUploader(modelLayerFactory);
		uploader.setHandleDeletionOfUnreferencedHashes(false);

		List<File> extractedBigFileFolders = new ArrayList<>();

		Set<String> emptyFileKeys = new HashSet<>();

		for (File file : files) {
			if (file.getName().endsWith(".bigf")) {
				extractedBigFileFolders.add(extractBigFile(file));

			} else {
				String key = toKey(file);
				if (file.length() > 0) {
					uploader.uploadAsync(key, ioServices.newInputStreamFactory(file, READ_FILE_INPUTSTREAM));
				} else {
					emptyFileKeys.add(key);
					ioServices.moveFile(file, new File(errorsEmptyFolder, key.replace("/", File.separator)));
				}
			}
		}

		for (File extractedBigFileFolder : extractedBigFileFolders) {

			for (File file : allFilesRecursivelyIn(extractedBigFileFolder)) {
				String key = toBigFileKey(extractedBigFileFolder, file);
				if (file.length() > 0) {
					uploader.uploadAsync(key, ioServices.newInputStreamFactory(file, READ_FILE_INPUTSTREAM));
				} else {
					emptyFileKeys.add(key);
					ioServices.moveFile(file, new File(errorsEmptyFolder, key.replace("/", File.separator)));
				}
			}
		}

		uploader.close();

		Map<String, ContentVersionDataSummary> newEntriesInIndex = new HashMap<>();

		for (File extractedBigFileFolder : extractedBigFileFolders) {
			for (File file : allFilesRecursivelyIn(extractedBigFileFolder)) {
				String key = toBigFileKey(extractedBigFileFolder, file);

				ContentVersionDataSummary dataSummary = uploader.get(key);
				newEntriesInIndex.put(key, dataSummary);

				if (contentManager.getParsedContent(dataSummary.getHash()).getParsedContent().isEmpty()) {
					ioServices.moveFile(file, new File(errorsUnparsableFolder, key.replace("/", File.separator)));
				} else {
					ioServices.deleteQuietly(file);
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
						ioServices.moveFile(file, new File(errorsUnparsableFolder, key.replace("/", File.separator)));
					} else {
						ioServices.deleteQuietly(file);
					}
				}
			} else {
				ioServices.deleteQuietly(file);
			}
		}

		writeNewEntriesInIndex(newEntriesInIndex);

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

	private File extractBigFile(File bigFile) {
		File bigFileTempFolder = new File(tempFolder, toKey(bigFile));
		bigFileTempFolder.mkdirs();

		InputStream inputStream = ioServices.newBufferedFileInputStreamWithoutExpectableFileNotFoundException(
				bigFile, READ_FILE_INPUTSTREAM);
		try {
			BigFileIterator bigFileIterator = new BigFileIterator(inputStream);
			while (bigFileIterator.hasNext()) {
				BigFileEntry entry = bigFileIterator.next();
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

		return bigFileTempFolder;
	}

	private void uploadBigFile(File file, BulkUploader uploader) {

	}

	private List<File> getFilesReadyToImport() {
		List<File> filesReadyToImport = new ArrayList<>();
		for (File fileToImport : allFilesRecursivelyIn(toImportFolder)) {
			if (TimeProvider.getLocalDateTime().minusSeconds(10).toDate().getTime() >= fileToImport.lastModified()) {
				filesReadyToImport.add(fileToImport);
			}
		}

		return filesReadyToImport;
	}

	private Collection<File> allFilesRecursivelyIn(File folder) {
		return FileUtils.listFiles(folder, TrueFileFilter.INSTANCE, TrueFileFilter.INSTANCE);
	}

	private void createFolders() {
		System.out.println(errorsEmptyFolder.getAbsolutePath());
		tempFolder.mkdirs();
		toImportFolder.mkdirs();
		errorsEmptyFolder.mkdirs();
		errorsUnparsableFolder.mkdirs();
	}

	public Map<String, ContentVersionDataSummary> readFileNameSHA1Index() {
		Map<String, ContentVersionDataSummary> map = new HashMap<>();
		for (Map.Entry<String, String> entry : PropertyFileUtils.loadKeyValues(indexProperties).entrySet()) {
			map.put(entry.getKey(), toContentVersionDataSummary(entry.getValue()));
		}
		return map;
	}

	private ContentVersionDataSummary toContentVersionDataSummary(String value) {
		String[] parts = value.split(":");
		String mimetype = "null".equals(parts[2]) ? null : parts[2];
		return new ContentVersionDataSummary(parts[0], mimetype, Integer.valueOf(parts[1]));
	}

}
