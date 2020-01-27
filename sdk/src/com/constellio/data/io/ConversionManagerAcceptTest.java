package com.constellio.data.io;

import com.constellio.data.io.services.facades.IOServices;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.annotations.SlowTest;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import static org.assertj.core.api.Assertions.assertThat;

@SlowTest
public class ConversionManagerAcceptTest extends ConstellioTest {
	IOServices ioServices;
	ConversionManager manager;
	File tempFolder;

	@Before
	public void setUp() {
		ioServices = getIOLayerFactory().newIOServices();
		tempFolder = ioServices.newTemporaryFolder("ConversionManagerAcceptTest");
	}

	@After
	public void tearDown() {
		if (manager != null) {
			manager.close();
		}
	}

	@Test
	public void givenInputStreamThenCreatePDF() {
		manager = new ConversionManager(ioServices, 1, null, getDataLayerFactory().getExtensions().getSystemWideExtensions(), getDataLayerFactory().getDataLayerConfiguration());
		manager.initialize();
		for (int i = 1; i < 9; i++) {
			String originalName = "test" + i + ".odt";
			InputStream input = getTestResourceInputStream(originalName);
			File result = manager.convertToPDF(input, originalName, tempFolder);
			assertThat(result.isFile());
		}
	}

	@Test
	public void givenInputStreamThenCreatePDFAsync()
			throws ExecutionException, InterruptedException {
		manager = new ConversionManager(ioServices, 4, null, getDataLayerFactory().getExtensions().getSystemWideExtensions(), getDataLayerFactory().getDataLayerConfiguration());
		manager.initialize();
		List<Future<File>> results = new ArrayList<>();
		for (int i = 1; i < 9; i++) {
			String originalName = "test" + i + ".odt";
			InputStream input = getTestResourceInputStream(originalName);
			results.add(manager.convertToPDFAsync(input, originalName, tempFolder));
		}
		for (Future<File> result : results) {
			assertThat(result.get().isFile());
		}
	}
}
