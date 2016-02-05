package com.constellio.data.utils;

import static java.util.Arrays.asList;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import com.constellio.data.io.services.facades.IOServices;

public class BigFileFolderIterator extends LazyIterator<BigFileEntry> {

	private String resourceName;
	private Iterator<File> filesIterator;
	private BigFileIterator currentIterator;
	private InputStream currentIteratorInputStream;
	private IOServices ioServices;

	public BigFileFolderIterator(List<File> files, IOServices ioServices, String resourceName) {
		this.filesIterator = files.iterator();
		this.resourceName = resourceName;
		this.ioServices = ioServices;
	}

	public BigFileFolderIterator(File folder, IOServices ioServices, String resourceName) {
		this(getBigFilesIn(folder), ioServices, resourceName);
	}

	private static List<File> getBigFilesIn(File folder) {
		List<File> files = new ArrayList<>();
		List<String> filenames = asList(folder.list());
		Collections.sort(filenames);
		for (String filename : filenames) {
			if (filename.endsWith(".bigf")) {
				files.add(new File(folder, filename));
			}
		}
		return files;
	}

	@Override
	protected synchronized BigFileEntry getNextOrNull() {
		if (currentIterator == null || !currentIterator.hasNext()) {
			if (!filesIterator.hasNext()) {
				return null;
			} else {
				if (currentIteratorInputStream != null) {
					ioServices.closeQuietly(currentIteratorInputStream);
				}
				File nextFile = filesIterator.next();
				System.out.println("Reading bigfile '" + nextFile.getName() + "'");
				try {
					currentIteratorInputStream = ioServices.newBufferedFileInputStream(nextFile, resourceName);
					currentIterator = new BigFileIterator(currentIteratorInputStream);
				} catch (FileNotFoundException e) {
					throw new RuntimeException(e);
				}

				return getNextOrNull();
			}
		} else {
			return currentIterator.next();
		}

	}

	public void close() {
		if (currentIteratorInputStream != null) {
			ioServices.closeQuietly(currentIteratorInputStream);
		}
	}
}
