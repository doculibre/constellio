package com.constellio.data.io.concurrent.filesystem;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.constellio.data.io.IOServicesFactory;
import com.constellio.data.io.concurrent.data.DataWithVersion;
import com.constellio.data.utils.hashing.HashingService;
import com.constellio.model.conf.FoldersLocator;

public class AtomicFileSystemUtilsTest {

	private AtomicFileSystem master, slave;
	private File baseFld;

	@Before
	public void setUp(){
		baseFld = new FoldersLocator().getDefaultTempFolder();
		HashingService hashingService = new IOServicesFactory(null).newHashingService();
		master = new ChildAtomicFileSystem(new AtomicLocalFileSystem(hashingService), new File(baseFld, "master").getAbsolutePath());
		slave = spy(new ChildAtomicFileSystem(new AtomicLocalFileSystem(hashingService), new File(baseFld, "slave").getAbsolutePath()));
		master.delete(File.separator, null);
		slave.delete(File.separator, null);
	}
	
	@After
	public void cleanup() throws IOException{
		FileUtils.deleteDirectory(baseFld);
	}

	@Test
	public void givenOneFileNotExistsInSlaveWhenSyncingThenTheFileIsCopied(){
		//given
		String filePath = "/test.txt";
		DataWithVersion dataWithVersion = new DataWithVersion("it is a test".getBytes(), null);
		master.writeData(filePath, dataWithVersion);

		//when
		AtomicFileSystemUtils.sync(master, slave);

		//then
		assertThat(slave.readData(filePath).getData()).isEqualTo(dataWithVersion.getData());
	}

	@Test
	public void givenAPathWithDifferentContentInMasterAndSlaveWhenSyncingThenTheContentOfTheFileIsEqualToMaster(){
		//given
		String filePath = "/test.txt";
		DataWithVersion dataWithVersion = new DataWithVersion("it is a test".getBytes(), null);
		master.writeData(filePath, dataWithVersion);
		slave.writeData(filePath, new DataWithVersion("an another text".getBytes(), null));
		
		//when
		AtomicFileSystemUtils.sync(master, slave);

		//then
		assertThat(slave.readData(filePath).getData()).isEqualTo(dataWithVersion.getData());
	}
	
	@Test
	public void giveADirectoryWhenSyncingThenAllItsContentIsCopiedToSlaved(){
		//given
		String dirPath = "/test";
		String file1 = dirPath + "/file1.txt";
		String file2 = dirPath + "/file2.txt";
		
		master.writeData(file1, new DataWithVersion(file1.getBytes(), null));
		master.writeData(file2, new DataWithVersion(file2.getBytes(), null));
		
		//when
		AtomicFileSystemUtils.sync(master, slave);
		
		//then
		assertThat(slave.isDirectory(dirPath)).isTrue();
		assertThat(slave.readData(file1).getData()).isEqualTo(file1.getBytes());
		assertThat(slave.readData(file2).getData()).isEqualTo(file2.getBytes());
	}

	@Test
	public void givenAFileInBothMasterAndSlaveWhenSyncingThenTheContentOfFileInSlaveIsNotUpdated(){
		//given
		String path = "/test.txt";
		
		master.writeData(path, new DataWithVersion(path.getBytes(), null));
		slave.writeData(path, new DataWithVersion(path.getBytes(), null));
		
		//when
		AtomicFileSystemUtils.sync(master, slave);
		
		//then
		//times(1) since slave was spied.
		verify(slave, times(1)).writeData(any(String.class), any(DataWithVersion.class));
	}

	
	@Test
	public void givenAnExraFileInADirectoryInSlaveWhenSyncingThenTheFileAndDirectoryAreRemoved(){
		//given
		String dir = "/test";
		String path = dir + "/test.txt";
		slave.writeData(path, new DataWithVersion(path.getBytes(), null));
		
		//when
		AtomicFileSystemUtils.sync(master, slave);

		//then
		assertThat(slave.exists(path)).isFalse();
		assertThat(slave.exists(dir)).isFalse();
	}
}
