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
package com.constellio.data.io.concurrent.filesystem;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.io.File;

import org.junit.Before;
import org.junit.Test;

import com.constellio.data.io.IOServicesFactory;
import com.constellio.data.io.concurrent.data.DataWithVersion;
import com.constellio.data.utils.hashing.HashingService;
import com.constellio.model.conf.FoldersLocator;

public class AtomicFileSystemUtilsTest {

	private AtomicFileSystem master, slave;

	@Before
	public void setUp(){
		File baseFld = new FoldersLocator().getDefaultTempFolder();
		HashingService hashingService = new IOServicesFactory(null).newHashingService();
		master = new AtomicLocalFileSystem(new File(baseFld, "master"), hashingService);
		slave = spy(new AtomicLocalFileSystem(new File(baseFld, "slave"), hashingService));
		master.delete("/", null);
		slave.delete("/", null);
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
