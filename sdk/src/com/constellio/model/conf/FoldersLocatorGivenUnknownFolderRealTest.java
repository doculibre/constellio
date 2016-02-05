package com.constellio.model.conf;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;

import java.io.File;

import org.junit.Before;
import org.junit.Test;

import com.constellio.sdk.tests.ConstellioTest;

public class FoldersLocatorGivenUnknownFolderRealTest extends ConstellioTest {

	File emptyFolder;

	FoldersLocator foldersLocator;

	@Before
	public void setUp() {
		FoldersLocator.invalidateCaches();
		foldersLocator = spy(new FoldersLocator());
		emptyFolder = newTempFolder();
	}

	@Test(expected = RuntimeException.class)
	public void givenUnknownFolderThenWhenGetWebappThenExceptionThrown()
			throws Exception {
		doReturn(emptyFolder).when(foldersLocator).getJavaRootFolder();

		foldersLocator.getConstellioWebappFolder();
	}

}
