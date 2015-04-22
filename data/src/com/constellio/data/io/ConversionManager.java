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
package com.constellio.data.io;

import static java.io.File.createTempFile;
import static java.util.concurrent.Executors.newFixedThreadPool;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.artofsolving.jodconverter.OfficeDocumentConverter;
import org.artofsolving.jodconverter.document.DocumentFamily;
import org.artofsolving.jodconverter.document.DocumentFormat;
import org.artofsolving.jodconverter.office.DefaultOfficeManagerConfiguration;
import org.artofsolving.jodconverter.office.OfficeException;
import org.artofsolving.jodconverter.office.OfficeManager;

import com.constellio.data.io.services.facades.IOServices;

public class ConversionManager implements AutoCloseable {
	public static int BASE_PORT = 2002;

	private final IOServices ioServices;
	private final OfficeManager processPool;
	private final ExecutorService executor;
	private final File workingFolder;

	public ConversionManager(IOServices ioServices, int numberOfProcesses, File workingFolder) {
		this.ioServices = ioServices;
		this.workingFolder = workingFolder;
		executor = newFixedThreadPool(numberOfProcesses);
		processPool = getOfficeManagerConfiguration(numberOfProcesses).buildOfficeManager();
		processPool.start();
	}

	public Future<File> convertToPDFAsync(final InputStream inputStream, final String originalName) {
		return executor.submit(new Callable<File>() {
			@Override
			public File call() {
				return convertToPDF(inputStream, originalName);
			}
		});
	}

	public File convertToPDF(InputStream inputStream, String originalName) {
		File input = null;
		File output = null;
		try {
			input = createTempFile("original", originalName, workingFolder);
			save(inputStream, input);
			output = createTempFile("converted", originalName + ".pdf", workingFolder);
			convertToPDF(input, output);
			return output;
		} catch (IOException | OfficeException e) {
			ioServices.deleteQuietly(output);
			throw new RuntimeException(e);
		} finally {
			ioServices.deleteQuietly(input);
		}
	}

	public void close() {
		executor.shutdown();
		try {
			if (!executor.awaitTermination(15, TimeUnit.SECONDS)) {
				executor.shutdownNow();
			}
		} catch (InterruptedException e) {
			executor.shutdownNow();
		}
		processPool.stop();
	}

	private DefaultOfficeManagerConfiguration getOfficeManagerConfiguration(int numberOfProcesses) {
		int[] ports = new int[numberOfProcesses];
		for (int i = 0; i < numberOfProcesses; i++) {
			ports[i] = BASE_PORT + i;
		}
		return new DefaultOfficeManagerConfiguration().setPortNumbers(ports);
	}

	private void save(InputStream inputStream, File file)
			throws IOException {
		try (OutputStream outputStream = new FileOutputStream(file)) {
			ioServices.copyLarge(inputStream, outputStream);
		} finally {
			ioServices.closeQuietly(inputStream);
		}
	}

	private void convertToPDF(File input, File output) {
		OfficeDocumentConverter converter = new OfficeDocumentConverter(processPool);
		converter.convert(input, output, toPDFa());
	}

	private DocumentFormat toPDFa() {
		Map<String, Object> filterData = new HashMap<>();
		filterData.put("SelectPdfVersion", 1);

		Map<String, Object> properties = new HashMap<>();
		properties.put("FilterName", "writer_pdf_Export");
		properties.put("FilterData", filterData);

		DocumentFormat format = new DocumentFormat("PDF/A", "pdf", "application/pdf");
		format.setStoreProperties(DocumentFamily.TEXT, properties);
		format.setStoreProperties(DocumentFamily.DRAWING, properties);

		return format;
	}
}
