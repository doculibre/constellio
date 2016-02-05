package com.constellio.data.io;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.constellio.data.io.services.facades.IOServices;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.annotations.SlowTest;

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
		manager.close();
	}

	@Test
	public void givenInputStreamThenCreatePDF() {
		manager = new ConversionManager(ioServices, 1, tempFolder);
		for (int i = 1; i < 9; i++) {
			String originalName = "test" + i + ".odt";
			InputStream input = getTestResourceInputStream(originalName);
			File result = manager.convertToPDF(input, originalName);
			assertThat(result.isFile());
		}
	}

	@Test
	public void givenInputStreamThenCreatePDFAsync()
			throws ExecutionException, InterruptedException {
		manager = new ConversionManager(ioServices, 4, tempFolder);
		List<Future<File>> results = new ArrayList<>();
		for (int i = 1; i < 9; i++) {
			String originalName = "test" + i + ".odt";
			InputStream input = getTestResourceInputStream(originalName);
			results.add(manager.convertToPDFAsync(input, originalName));
		}
		for (Future<File> result : results) {
			assertThat(result.get().isFile());
		}
	}
}
