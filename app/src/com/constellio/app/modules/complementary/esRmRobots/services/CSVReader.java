package com.constellio.app.modules.complementary.esRmRobots.services;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOUtils;

import com.constellio.model.entities.records.Content;
import com.constellio.model.services.contents.ContentManager;
import org.apache.commons.lang3.StringUtils;

public class CSVReader {

	ContentManager contentManager;

	public CSVReader(ContentManager contentManager) {
		this.contentManager = contentManager;
	}

	public List<Map<String, String>> readCSVContent(Content csv) {
		List<Map<String, String>> entries = new ArrayList<>();

		//		BufferedLineReaderInputStream input = new BufferedLineReaderInputStream(contentManager.getContentInputStream(csv.getId(),"csvInputStream"),1024);

		String contentId = csv.getCurrentVersion().getHash();
		BufferedReader reader = new BufferedReader(
				new InputStreamReader(contentManager.getContentInputStream(contentId, "csvInputStream")));

		try {
			String line = reader.readLine();

			if (StringUtils.startsWithIgnoreCase(line, "sep=,")) {
				//Skip sep
				line = reader.readLine();
			}

			String[] headers = line.split(",");
			while ((line = reader.readLine()) != null) {
				Map<String, String> entry = new HashMap<>();
				String[] values = line.split(",");
				for (int i = 0; i < headers.length; i++) {
					if (values.length > i) {
						entry.put(headers[i], values[i]);
					} else {
						entry.put(headers[i], "");
					}
				}
				entries.add(entry);
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		} finally {
			IOUtils.closeQuietly(reader);
		}

		return entries;
	}
}
