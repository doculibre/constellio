package com.constellio.data.io.concurrent.filesystem;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Arrays;
import java.util.List;

import com.constellio.sdk.tests.annotations.InDevelopmentTest;
import org.apache.zookeeper.server.NIOServerCnxnFactory;
import org.apache.zookeeper.server.ZooKeeperServer;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import com.constellio.data.io.IOServicesFactory;
import com.constellio.data.io.concurrent.data.DataWithVersion;
import com.constellio.data.io.concurrent.exception.FileNotFoundException;
import com.constellio.data.io.concurrent.exception.OptimisticLockingException;
import com.constellio.data.utils.hashing.HashingService;
import com.constellio.model.conf.FoldersLocator;

@InDevelopmentTest
@RunWith(value = Parameterized.class)
public class AtomicFileSystemsAcceptTest {
	//	private static NIOServerCnxnFactory serverFactory;
	//	private static ZooKeeperServer zks;
	//
	//	private static void setUpZookeeper(int port) throws IOException, InterruptedException{
	//		File zkTmpDir=File.createTempFile("zookeeper","test");
	//		zkTmpDir.delete();
	//		zkTmpDir.mkdir();
	//		zks = new ZooKeeperServer(zkTmpDir,zkTmpDir,2181);
	//		serverFactory = new NIOServerCnxnFactory();
	//		serverFactory.configure(new InetSocketAddress(port), 100);
	//		serverFactory.startup(zks);
	//	}
	//
	//	@Parameters(name = "{index}: Test {0}")
	//	public static Iterable<Object[]> setUpParameters() throws Exception {
	//		File baseFld = new FoldersLocator().getDefaultTempFolder();
	//		HashingService hashingService = new IOServicesFactory(null).newHashingService();
	//		AtomicFileSystem localFileSystem = new ChildAtomicFileSystem(new AtomicLocalFileSystem(hashingService), baseFld.getAbsolutePath());
	//
	//		int zkPort = 63210;
	//		setUpZookeeper(zkPort);
	//		AtomicFileSystem zookeeperAtomicFileSystem = new ChildAtomicFileSystem(new ZookeeperAtomicFileSystem("localhost:" + zkPort, 6000), "/tmp");
	//
	//		return Arrays.asList(new Object[][] {
	//			{ localFileSystem },
	//			{ zookeeperAtomicFileSystem}
	//		});
	//	}
	//
	//	@AfterClass
	//	public static void cleanup() throws IOException{
	////		zkc.close();
	//		if (serverFactory != null) {
	//			serverFactory.shutdown();
	//		}
	//		if (zks != null) {
	//			zks.shutdown();
	//		}
	//	}
	//
	//	private AtomicFileSystem fileSystemUnderTest;
	//	private String oldContent, newContent, path;
	//	private Object invalidVersion;
	//	private Object updatedVersion;
	//
	//	public AtomicFileSystemsAcceptTest(AtomicFileSystem fileSystemUnderTest) {
	//		this.fileSystemUnderTest = fileSystemUnderTest;
	//	}
	//
	//	@Before
	//	public void setUp(){
	//		oldContent = "it is a test";
	//		newContent = "it is an another data";
	//
	//		path = "/test.txt";
	//
	//		fileSystemUnderTest.delete("/", null);
	//		invalidVersion = fileSystemUnderTest.writeData(path, new DataWithVersion(oldContent.getBytes(), null)).getVersion();
	//		fileSystemUnderTest.writeData(path, new DataWithVersion(newContent.getBytes(), null));	//this will change the version of the file
	//
	//	}
	//
	//	@Test
	//	public void whenRemovingAllFilesInRootThenThereIsNoFileInTheRoot(){
	//		fileSystemUnderTest.delete("/", null);
	//
	//		assertThat(fileSystemUnderTest.list("/")).hasSize(0);
	//	}
	//
	//	@Test
	//	public void whenAddingAFileThenTheFileIsAdded(){
	//		assertThat(fileSystemUnderTest.exists(path)).isTrue();
	//	}
	//
	//	@Test
	//	public void whenDeletingAFileThenTheFileDoesNotExist(){
	//		fileSystemUnderTest.delete(path, updatedVersion);
	//		assertThat(fileSystemUnderTest.exists(path)).isFalse();
	//	}
	//
	//	@Test
	//	public void whenDeletingAFileThatDoesNotExistThenDoesNotThrowExceptio(){
	//		fileSystemUnderTest.delete(path, updatedVersion);
	//		fileSystemUnderTest.delete(path, null);
	//	}
	//
	//	@Test
	//	public void whenAddingAFileThenItIsAccissibleInListFiles(){
	//		List<String> rootFiles = fileSystemUnderTest.list("/");
	//		assertThat(rootFiles).containsOnly(path);
	//	}
	//
	//
	//	@Test (expected = OptimisticLockingException.class)
	//	public void whenDeletingDataWhileItsVersionHasBeenChangedThenThrowOptimisticLockingException(){
	//		//then
	//		fileSystemUnderTest.delete(path, invalidVersion);
	//	}
	//
	//	@Test (expected = OptimisticLockingException.class)
	//	public void whenWritingDataWhileItsVersionHasBeenChangedThenThrowOptimisticLockingException(){
	//		//then
	//		fileSystemUnderTest.writeData(path, new DataWithVersion(oldContent.getBytes(), invalidVersion));
	//	}
	//
	//	@Test
	//	public void givenAPathThatItsParentDoesNotExistWhenCreateAFileOnThatPathThenParentDirectoryIsCreated(){
	//		//given
	//		String path = "/test/notexist/test.txt";
	//		String parent = "/test";
	//		if (fileSystemUnderTest.exists(parent))
	//			fileSystemUnderTest.delete(parent, null);
	//		assertThat(fileSystemUnderTest.exists(parent)).isFalse();
	//
	//		//when
	//		fileSystemUnderTest.writeData(path, new DataWithVersion("it is a test".getBytes(), null));
	//
	//		//then
	//		assertThat(fileSystemUnderTest.exists(parent));
	//	}
	//
	//	@Test
	//	public void whenCreateADirectoryThenTheDirectoryExists(){
	//		//given
	//		String dirPath = "/test";
	//		if (fileSystemUnderTest.exists(dirPath))
	//			fileSystemUnderTest.delete(dirPath, null);
	//
	//		//when
	//		fileSystemUnderTest.mkdirs(dirPath);
	//
	//		//then
	//		assertThat(fileSystemUnderTest.exists(dirPath)).isTrue();
	//	}
	//
	//	@Test
	//	public void whenDeletingTheRootThenTheRootStillIsDirectory(){
	//		fileSystemUnderTest.delete("/", null);
	//		assertThat(fileSystemUnderTest.isDirectory("/")).isTrue();
	//		assertThat(fileSystemUnderTest.exists("/"));
	//	}
	//
	//
	//	@Test
	//	public void whenListingANonExistanceDirectoryThenReturnNull(){
	//		String aPath = "/this/is/a/random/path";
	//		assertThat(fileSystemUnderTest.list(aPath)).isNull();
	//	}
	//
	//	@Test
	//	public void givenAPathPointToAFileWhenListFilesInThePathThenReturnNull(){
	//		List<String> nullList = fileSystemUnderTest.list(path);
	//		assertThat(nullList).isNull();
	//	}
	//
	//	@Test (expected = FileNotFoundException.class)
	//	public void whenReadingFromNonExistedPathThenThrowsFileNotFoundException(){
	//		String aPath = "/this/is/a/random/path";
	//		assertThat(fileSystemUnderTest.readData(aPath));
	//	}
	//
	//	@Test
	//	public void whenReadingFromADirectoryThenReturnADataWithNullValue(){
	//		String aPath = "/aDirPath";
	//		if (fileSystemUnderTest.exists(aPath))
	//			fileSystemUnderTest.delete(aPath, null);
	//		fileSystemUnderTest.mkdirs(aPath);
	//
	//		assertThat(fileSystemUnderTest.readData(aPath).getData()).isNull();
	//	}
	//
	//	@Test
	//	public void whenCreatingAFileWithNoContentThenTheFileIsNotDirectory(){
	//		String aPath = "/aFileWithZeroContent";
	//		fileSystemUnderTest.writeData(aPath, new DataWithVersion(new byte[0], null));
	//
	//		assertThat(fileSystemUnderTest.isDirectory(aPath)).isFalse();
	//	}

}
