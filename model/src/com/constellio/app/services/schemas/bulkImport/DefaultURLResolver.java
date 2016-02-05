package com.constellio.app.services.schemas.bulkImport;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

import com.constellio.data.io.services.facades.IOServices;
import com.constellio.data.io.streamFactories.StreamFactory;

public class DefaultURLResolver implements URLResolver {

	IOServices ioServices;

	public DefaultURLResolver(IOServices ioServices) {
		this.ioServices = ioServices;
	}

	@Override
	public StreamFactory<InputStream> resolve(final String value, final String fileName) {

		return new StreamFactory<InputStream>() {

			@Override
			public InputStream create(String resourceName)
					throws IOException {

				try {
					URL url = new URL(value);
					return readUrl(resourceName, url, fileName);
				} catch (MalformedURLException e) {

					return readFile(resourceName, value, fileName);
				}

			}
		};
	}

	protected InputStream readFile(String resourceName, String url, String fileName) {
		try {
			return ioServices.newBufferedFileInputStream(new File(url), resourceName);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	protected InputStream readUrl(String resourceName, URL url, String fileName) {
		try {
			return ioServices.newBufferedInputStream(url.openStream(), resourceName);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
}
