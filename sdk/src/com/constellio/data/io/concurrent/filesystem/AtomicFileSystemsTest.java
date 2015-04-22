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

import java.io.File;
import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import com.constellio.data.io.IOServicesFactory;
import com.constellio.data.io.concurrent.data.DataWithVersion;
import com.constellio.data.io.concurrent.exception.OptimisticLockingException;
import com.constellio.data.utils.hashing.HashingService;
import com.constellio.model.conf.FoldersLocator;

@RunWith(value = Parameterized.class)
public class AtomicFileSystemsTest {
	@Parameters(name = "{index}: Test {0}")
	public static Iterable<Object[]> setUpParameters() {
		
		File baseFld = new FoldersLocator().getDefaultTempFolder();
		HashingService hashingService = new IOServicesFactory(null).newHashingService();
		AtomicLocalFileSystem localFileSystem = new AtomicLocalFileSystem(baseFld, hashingService);
		return Arrays.asList(new Object[][] { 
			{ localFileSystem }, 
		});
	}
	
	private AtomicFileSystem fileSystemUnderTest;
	private String oldContent, newContent, path;
	private Object invalidVersion;
	private Object updatedVersion;
	
	public AtomicFileSystemsTest(AtomicFileSystem fileSystemUnderTest) {
		this.fileSystemUnderTest = fileSystemUnderTest;
	}

	@Before
	public void setUp(){
		oldContent = "it is a test";
		newContent = "it is an another data";
		
		path = "/test.txt";
		fileSystemUnderTest.delete("/", null);
		invalidVersion = fileSystemUnderTest.writeData(path, new DataWithVersion(oldContent.getBytes(), null));
		fileSystemUnderTest.writeData(path, new DataWithVersion(newContent.getBytes(), null));	//this will change the version of the file

	}
	
	@Test
	public void whenRemovingAllFilesInRootThenThereIsNoFileInTheRoot(){
		for (String path: fileSystemUnderTest.list("/")){
			fileSystemUnderTest.delete(path, null);
		}
		
		assertThat(fileSystemUnderTest.list("/")).hasSize(0);
	}
	
	@Test
	public void whenAddingAFileThenTheFileIsAdded(){
		assertThat(fileSystemUnderTest.exists(path)).isTrue();
	}
	
	@Test
	public void whenDeletingAFileThenTheFileDoesNotExist(){
		fileSystemUnderTest.delete(path, updatedVersion);
		assertThat(fileSystemUnderTest.exists(path)).isFalse();
	}
	
	@Test
	public void whenDeletingAFileThatDoesNotExistThenDoesNotThrowExceptio(){
		fileSystemUnderTest.delete(path, updatedVersion);
		fileSystemUnderTest.delete(path, updatedVersion);
	}
	
	@Test
	public void whenAddingAFileThenItIsAccissibleInListFiles(){
		List<String> rootFiles = fileSystemUnderTest.list("/");
		assertThat(rootFiles).containsOnly(path);
	}
	
	
	@Test (expected = OptimisticLockingException.class)
	public void whenDeletingDataWhileItsVersionHasBeenChangedThenThrowOptimisticLockingException(){
		//then
		fileSystemUnderTest.delete(path, invalidVersion);
	}
	
	@Test (expected = OptimisticLockingException.class)
	public void whenWritingDataWhileItsVersionHasBeenChangedThenThrowOptimisticLockingException(){
		//then
		fileSystemUnderTest.writeData(path, new DataWithVersion(oldContent.getBytes(), invalidVersion));
	}
	
	@Test
	public void givenAPathThatItsParentDoesNotExistWhenCreateAFileOnThatPathThenParentDirectoryIsCreated(){
		//given
		String path = "/test/notexist/test.txt";
		String parent = "/test";
		if (fileSystemUnderTest.exists(parent))
			fileSystemUnderTest.delete(parent, null);
		assertThat(fileSystemUnderTest.exists(parent)).isFalse();
		
		//when
		fileSystemUnderTest.writeData(path, new DataWithVersion("it is a test".getBytes(), null));
		
		//then
		assertThat(fileSystemUnderTest.exists(parent));
	}
	
	@Test
	public void whenCreateADirectoryThenTheDirectoryExists(){
		//given
		String dirPath = "/test";
		if (fileSystemUnderTest.exists(dirPath))
			fileSystemUnderTest.delete(dirPath, null);
		
		//when
		fileSystemUnderTest.mkdirs(dirPath);
		
		//then
		assertThat(fileSystemUnderTest.exists(dirPath)).isTrue();
	}
	
	@Test
	public void whenDeletingTheRootThenTheRootStillIsDirectory(){
		fileSystemUnderTest.delete("/", null);
		assertThat(fileSystemUnderTest.isDirectory("/")).isTrue();
		assertThat(fileSystemUnderTest.exists("/"));
	}
	
	
	@Test
	public void whenListingANonExistanceDirectoryThenReturnNull(){
		fileSystemUnderTest.delete("/", null);
		String aPath = "/this/is/a/random/path";
		assertThat(fileSystemUnderTest.list(aPath)).isNull();
	}
	
	@Test
	public void givenAPathPointToAFileWhenListFilesInThePathThenReturnNull(){
		
		List<String> nullList = fileSystemUnderTest.list(path);
		assertThat(nullList).isNull();
	}
}
