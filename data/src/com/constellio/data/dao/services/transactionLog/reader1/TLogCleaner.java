package com.constellio.data.dao.services.transactionLog.reader1;

import java.io.*;

public class TLogCleaner {

	public static final String INPUT_TLOG = "/Users/francisbaril/Downloads/savestate-mcc/content/tlogs";
	public static final String OUTPUT_TLOG = "/Users/francisbaril/Downloads/savestate-mcc/content/cleaned-tlogs";

	public static void main(String argv[]) {

		File inputFolder = new File(INPUT_TLOG);
		File outputFolder = new File(OUTPUT_TLOG);
		outputFolder.mkdirs();

		for (File file : inputFolder.listFiles()) {
			if (file.getName().endsWith(".tlog")) {
				File outputFile = new File(outputFolder, file.getName());

				try {
					BufferedReader reader = new BufferedReader(new FileReader(file));
					BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile));

					String line;
					while ((line = reader.readLine()) != null) {
						if (!line.startsWith("emailContent_t")) {
							writer.append(line);
							writer.newLine();
						}
					}
					reader.close();
					writer.close();

				} catch (Exception e) {
					throw new RuntimeException(e);
				}

			}
		}

	}

}
