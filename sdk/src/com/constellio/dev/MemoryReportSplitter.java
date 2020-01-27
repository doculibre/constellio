package com.constellio.dev;

import com.constellio.data.utils.KeyIntMap;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.util.List;

public class MemoryReportSplitter {

	public static void main(String[] args) throws Exception {
		File file = new File("/Users/francisbaril/pmap-memory-report7.txt");

		List<String> lines = FileUtils.readLines(file);

		KeyIntMap<String> stats = new KeyIntMap<>();

		lines.stream().forEach((s) -> {
			try {
				String name = s.substring(32);
				String count = s.substring(17);
				count = count.substring(0, count.indexOf("K"));

				Integer size = Integer.valueOf(count.trim());
				stats.increment(name.trim(), size);

				//System.out.println(name + ">" + count);
			} catch (Exception e) {

			}
		});


		System.out.println("\n-----\n");
		stats.entriesSortedByDescValue().stream().forEach((e) -> {
			if (e.getValue() > 100_000) {
				System.out.println(e.getKey() + "=" + e.getValue());
			}

		});
	}

}
