package com.constellio.data.conf;

import com.constellio.sdk.tests.ConstellioTest;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.io.File;

public class FoldersLocatorGivenUnknownFolderRealTest extends ConstellioTest {

	File emptyFolder;

	FoldersLocator foldersLocator;

	@Before
	public void setUp() {
		FoldersLocator.invalidateCaches();
		foldersLocator = Mockito.spy(new FoldersLocator());
		emptyFolder = newTempFolder();
	}

	@Test(expected = RuntimeException.class)
	public void givenUnknownFolderThenWhenGetWebappThenExceptionThrown()
			throws Exception {
		Mockito.doReturn(emptyFolder).when(foldersLocator).getJavaRootFolder();

		foldersLocator.getConstellioWebappFolder();
	}

}
