package com.constellio.data.io.factories;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.spy;

import java.io.File;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import com.constellio.data.io.EncodingService;
import com.constellio.data.io.IOServicesFactory;
import com.constellio.data.io.services.facades.FileService;
import com.constellio.data.io.services.facades.IOServices;
import com.constellio.data.io.services.zip.ZipService;
import com.constellio.sdk.tests.ConstellioTest;

public class IOServicesFactoryTest extends ConstellioTest {

	private IOServicesFactory factory;
	@Mock private File tempFolder;

	@Before
	public void setUp()
			throws Exception {
		factory = spy(new IOServicesFactory(tempFolder));
	}

	@Test
	public void whenGettingNewEncodingServiceThenEncodingServiceCreated() {
		EncodingService returnedService = factory.newEncodingService();
		assertTrue(returnedService instanceof EncodingService);
	}

	@Test
	public void whenGettingNewFileServiceThenFileServiceCreated() {
		FileService returnedService = factory.newFileService();
		assertTrue(returnedService instanceof FileService);
	}

	@Test
	public void whenGettingNewIOServicesThenIOServicesCreated() {
		IOServices returnedService = factory.newIOServices();
		assertTrue(returnedService instanceof IOServices);
	}

	@Test
	public void whenGettingNewZipServiceThenZipServiceCreated() {
		ZipService returnedService = factory.newZipService();
		assertTrue(returnedService instanceof ZipService);
	}

}
