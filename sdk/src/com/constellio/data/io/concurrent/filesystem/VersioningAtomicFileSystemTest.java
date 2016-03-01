package com.constellio.data.io.concurrent.filesystem;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.constellio.data.io.EncodingService;
import com.constellio.data.io.concurrent.data.TextView;
import com.constellio.data.utils.hashing.HashingService;
import com.constellio.model.conf.FoldersLocator;

public class VersioningAtomicFileSystemTest {
	VersioningAtomicFileSystem versioningAtomicFileSystem;
	
	
	private File baseFld;
	private String path = "/a.txt";
	private String originalContent = "XXX";
	private String newContent = "YYY";
	
	@Before
	public void setup(){
		baseFld = new FoldersLocator().getDefaultTempFolder();
		AtomicLocalFileSystem fileSystem = new AtomicLocalFileSystem(HashingService.forSHA1(new EncodingService()));
		ChildAtomicFileSystem targetFileSystem = new ChildAtomicFileSystem(fileSystem, baseFld.getAbsolutePath());
		versioningAtomicFileSystem = new VersioningAtomicFileSystem(targetFileSystem);
		
		versioningAtomicFileSystem.writeData(path, new TextView().setData(originalContent));
		
		TextView textView = versioningAtomicFileSystem.readData(path, new TextView());
		textView.setData(newContent);
		versioningAtomicFileSystem.writeData(path, textView);
	}
	
	@After
	public void cleanup() throws IOException{
		FileUtils.deleteDirectory(baseFld);
	}
	
	
	@Test
	public void whenChangingAFileAndRecoverItTheContentOfTheFileIsTheSame(){
		assertThat(versioningAtomicFileSystem.readData(path).getView(new TextView()).getData()).isEqualTo(newContent);
		versioningAtomicFileSystem.forceToRecover(path);
		assertThat(versioningAtomicFileSystem.readData(path).getView(new TextView()).getData()).isEqualTo(originalContent);
	}
	
	@Test
	public void whenWritingToAFileTwiceThenOnlyOneBackupExists(){
		String superNewContent = "ZZZ";
		TextView newData = versioningAtomicFileSystem.readData(path, new TextView());
		versioningAtomicFileSystem.writeData(path, newData.setData(superNewContent));
		
		versioningAtomicFileSystem.setHideBackupFiles(false);
		List<String> list = versioningAtomicFileSystem.list("/");
		assertThat(list).hasSize(2);
		assertThat(list.remove(path)).isTrue();
		assertThat(list.get(0)).startsWith(path);
	}
	
	@Test
	public void whenChangingAFileTwiceTheTheBackupContainsTheOriginalContent(){
		String superNewContent = "ZZZ";
		TextView newData = versioningAtomicFileSystem.readData(path).getView(new TextView());
		versioningAtomicFileSystem.writeData(path, newData.setData(superNewContent));
		
		assertThat(versioningAtomicFileSystem.readData(path).getView(new TextView()).getData()).isEqualTo(superNewContent);
		versioningAtomicFileSystem.forceToRecover(path);
		assertThat(versioningAtomicFileSystem.readData(path).getView(new TextView()).getData()).isEqualTo(originalContent);

	}
	
	@Test
	public void whenRecoveringAllFilesThenAllFileAreRecovered(){
		String anotherFile = "/b.txt";
		String anotherFileOriginalContent = "ANOTHER";
		TextView theContent = versioningAtomicFileSystem.writeData(anotherFile, new TextView().setData(anotherFileOriginalContent));
		theContent.setData(newContent);
		versioningAtomicFileSystem.writeData(anotherFile, theContent);
		
		assertThat(versioningAtomicFileSystem.readData(anotherFile).getView(new TextView()).getData()).isEqualTo(newContent);
		
		versioningAtomicFileSystem.forceToRecoverAll();

		assertThat(versioningAtomicFileSystem.readData(path, new TextView()).getData()).isEqualTo(originalContent);
		assertThat(versioningAtomicFileSystem.readData(anotherFile, new TextView()).getData()).isEqualTo(anotherFileOriginalContent);
	}
	
	@Test
	public void whenRecoveringAFileTheLastContentIsStillAvailable(){
		versioningAtomicFileSystem.forceToRecover(path);
		assertThat(versioningAtomicFileSystem.getContentBeforeRecovering(path).getView(new TextView()).getData()).isEqualTo(newContent);
	}
}
