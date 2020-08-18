package com.constellio.sdk.tests;

import com.constellio.data.io.services.facades.FileService;
import com.constellio.data.io.services.facades.IOServices;
import com.constellio.data.io.services.zip.ZipService;
import com.constellio.data.io.services.zip.ZipServiceException;
import com.constellio.data.utils.Octets;
import com.constellio.data.utils.TenantUtils;
import com.constellio.sdk.tests.annotations.PreserveState;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;

import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Random;

import static com.constellio.sdk.tests.TestUtils.aString;

public class FileSystemTestFeatures {

	private static long sequence = 1;
	private final File tempFolder;
	private final File testClassFolder;

	private final List<File> foldersToDeleteAfterTest = new ArrayList<File>();
	private Class<? extends AbstractConstellioTest> testClass;

	public FileSystemTestFeatures(String tempFolderName, Map<String, String> sdkProperties,
								  Class<? extends AbstractConstellioTest> testClass) {
		this.testClass = testClass;
		String tempFoldersLocation = sdkProperties.get("tempFoldersLocation");
		if (tempFoldersLocation == null) {
			tempFolder = new File(tempFolderName);
		} else {
			File tempFolders = new File(tempFoldersLocation);
			tempFolders.mkdirs();
			tempFolder = new File(tempFolders, tempFolderName);
		}
		testClassFolder = new File(tempFolder, testClass.getSimpleName());
		testClassFolder.mkdirs();

		boolean clearTempFolder = true;
		String lastPreservedState = getLastPreservedState();
		if (lastPreservedState != null) {
			PreserveState preserveStateAnnotation = testClass.getAnnotation(PreserveState.class);
			if (preserveStateAnnotation != null) {
				clearTempFolder = !lastPreservedState.equals(testClass.getName() + "-" + preserveStateAnnotation.state());
			}
		}

		if (clearTempFolder && testClassFolder.exists()) {
			try {
				FileUtils.deleteDirectory(testClassFolder);
			} catch (IOException e) {
				Error error = new Error("Cannot deleteLogically temp test directory, it is impossible to start tests in a clean state.", e);
				writeStackTrace(error);
				throw error;
			}
		}
	}

	void setPreservedState(String state) {
		File stateFile = new File(testClassFolder, "state.txt");
		try {
			FileUtils.write(stateFile, state);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	String getLastPreservedState() {
		File stateFile = new File(testClassFolder, "state.txt");
		if (stateFile.exists()) {
			try {
				return FileUtils.readFileToString(stateFile);
			} catch (IOException e) {
				return null;
			}
		} else {
			return null;
		}
	}

	public void addFolderToDeleteAfterTest(File tempFolder, File folderToDelete) {
		this.foldersToDeleteAfterTest.add(folderToDelete);
	}

	public void close() {
		try {
			for (File folderToDelete : foldersToDeleteAfterTest) {
				try {
					FileUtils.deleteDirectory(folderToDelete);
				} catch (IOException e) {
					throw new RuntimeException("Cannot delete logically temp directory '" + folderToDelete
											   + "' after test execution. It probably means that a resource has not been closed", e);
				}
				folderToDelete = null;

			}

			if (testClassFolder.exists()) {
				try {
					FileUtils.forceDelete(testClassFolder);
				} catch (IOException e) {
					throw new RuntimeException("Cannot delete temp directory '" + testClassFolder
											   + "' after test execution. It probably means that a resource has not been closed", e);
				}
			}
		} catch (RuntimeException e) {
			writeStackTrace(e);
			throw e;
		}
	}

	private void writeStackTrace(Throwable t) {
		String stackTrace = ExceptionUtils.getStackTrace(t);
		String dateTimeStr = new SimpleDateFormat("yyyy-MM-dd_HH_mm_ss").format(new Date());
		String stackTraceFileName = "stack-trace-" + dateTimeStr + ".txt";
		File stackTraceFile = new File(testClassFolder, stackTraceFileName);
		try {
			FileUtils.write(stackTraceFile, stackTrace);
		} catch (IOException e) {
			throw new RuntimeException("Unable to write stack trace to file " + stackTraceFile.getAbsolutePath() + " : " + stackTrace, e);
		}
	}

	public File createTempCopy(File originalFile) {
		if (!originalFile.exists()) {
			throw new RuntimeException("The original file '" + originalFile.getAbsolutePath() + "' does not exist");

		}

		File tempFolder = newTempFolder();
		File destination = new File(tempFolder, originalFile.getName());
		FileService fileService = new FileService(null);
		if (originalFile.isDirectory()) {
			destination.mkdir();
			fileService.copyDirectoryWithoutExpectableIOException(originalFile, destination);
		} else {
			fileService.copyFileWithoutExpectableIOException(originalFile, destination);
		}

		return destination;
	}

	public File newTempFileWithContent(String content) {
		return newTempFileWithContent(newTempFolder(), "file.txt", content);
	}

	public File newTempFileWithContent(File tempFolder, String fileName, String content) {
		File tempFile = new File(tempFolder, fileName);
		try {
			FileUtils.writeStringToFile(tempFile, content);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		if (!tempFile.exists()) {
			throw new RuntimeException("File was not created");
		}
		return tempFile;
	}

	public File newTempFolder() {
		File testFolder = new File(testClassFolder, "test_" + testClass.getSimpleName() + "_" + (++sequence));
		testFolder.mkdirs();
		foldersToDeleteAfterTest.add(testFolder);
		return testFolder;
	}

	public File newTempFolderWithName(String name) {
		name = appendTenant(name);
		File testFolder = new File(testClassFolder, "test_" + testClass.getSimpleName() + "_" + name);
		testFolder.mkdirs();
		foldersToDeleteAfterTest.add(testFolder);
		return testFolder;
	}

	public File createRandomTextFilesInTempFolder(int numberOfFiles, int charactersPerFile) {
		File tempFolder = newTempFolder();

		Random random = new Random();
		String characters = "abcdefghijklmnopqrstuvwxyz  ABCDEFGHIJKLMNOPQRSTUVWXYZ   1234567890-=!/$%?&*()    ";
		try {
			for (int i = 0; i < numberOfFiles; i++) {

				BufferedWriter b = new BufferedWriter(new FileWriter(new File(tempFolder, "" + i + ".txt")));
				for (int j = 0; j < charactersPerFile; j++) {
					int randomPosition = random.nextInt(characters.length());
					String c = characters.substring(randomPosition, randomPosition + 1);
					b.append(c);
				}
				b.close();
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		return tempFolder;
	}

	private IOServices getIOServices() {
		return new IOServices(newTempFolder());
	}

	private ZipService getZipServices() {
		return new ZipService(getIOServices());
	}

	public File createJarWithClassesOfPackages(String jarFileName, String... packages) {

		File tempJarCreationFolder = newTempFolder();

		//		for (String aPackage : packages) {
		//			String packageAsPath = aPackage.replace(".", File.separator);
		//			File classes = new File(new SDKFoldersLocator(null, null, null).getSDKClassesFolder(), packageAsPath);
		//			File destinationInTempDirectory = new File(tempJarCreationFolder, packageAsPath);
		//			destinationInTempDirectory.mkdirs();
		//			try {
		//				FileUtils.copyDirectory(classes, destinationInTempDirectory);
		//			} catch (IOException e) {
		//				throw new RuntimeException(e);
		//			}
		//		}

		//		File jarFile = new File(newTempFolder(), jarFileName);
		//		try {
		//			getZipServices().zip(jarFile, Arrays.asList(tempJarCreationFolder.listFiles()));
		//		} catch (Exception e) {
		//			//throw new RuntimeException(e);
		//		}
		return null;
	}

	public File creatingBinaryFileWithSize(Octets octets)
			throws IOException {
		return creatingBinaryFileWithSizeInTempFolder(newTempFolder(), octets);
	}

	public File creatingBinaryFileWithSizeInTempFolder(File tempFolder, Octets octets) {
		File tempFile = new File(tempFolder, aString() + ".bin");
		writeRandomBytesToFile(tempFile, octets);
		return tempFile;
	}

	public void writeRandomBytesToFile(File file, Octets octets) {

		OutputStream out = null;
		try {
			Random random = new Random();
			out = new BufferedOutputStream(new FileOutputStream(file));
			long sizeToWrite = octets.getOctets();
			byte[] bytes = new byte[1024];
			while (sizeToWrite > 1024) {
				random.nextBytes(bytes);
				out.write(bytes);
				sizeToWrite -= 1024;
			}
			if (sizeToWrite > 0) {
				random.nextBytes(bytes);
				out.write(bytes, 0, (int) sizeToWrite);
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		} finally {
			IOUtils.closeQuietly(out);
		}
	}

	public File newFileInFolder(File folder, String fileName) {
		File afile = new File(folder, fileName);
		return newFileWithContent(afile);
	}

	public File newFileWithContent(File file) {
		File aFile = file.getAbsoluteFile();
		try {
			new FileService(null).replaceFileContent(aFile, "test");
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		return aFile;
	}

	public File newTempFileInNewTempFolder(String fileName) {
		File aFile = new File(newTempFolder(), fileName);
		return newFileWithContent(aFile);
	}

	public File givenUnzipedFileInTempFolder(File zipFile) {
		File tempFolder = newTempFolder();
		try {
			getZipServices().unzip(zipFile, tempFolder);
		} catch (ZipServiceException e) {
			throw new RuntimeException(e);
		}

		return tempFolder;
	}

	private String appendTenant(String path) {
		String tenantId = TenantUtils.getTenantId();
		if (tenantId != null) {
			return path.concat(File.separator).concat("tenant").concat(tenantId);
		}
		return path;
	}
}
