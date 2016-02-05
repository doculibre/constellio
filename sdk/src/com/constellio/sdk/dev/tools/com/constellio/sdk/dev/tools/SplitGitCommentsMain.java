package com.constellio.sdk.dev.tools.com.constellio.sdk.dev.tools;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
import org.joda.time.LocalTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

public class SplitGitCommentsMain {

	private static File gitLog = new File("/Users/francisbaril/Workspaces/rsde/log.csv");
	private static File gitLogSplittedFolder = new File("/Users/francisbaril/Workspaces/rsde/splitted/");

	private static List<Sprint> sprints = new ArrayList<>();

	private static DateTimeFormatter formatter = DateTimeFormat.forPattern("EEE, dd MMM yyyy HH:mm:ss Z");

	static {
		sprints.add(new Sprint("Sprint 1-22", new LocalDate(2014, 1, 1), new LocalDate(2015, 1, 31)));
		sprints.add(new Sprint("Sprint 23", new LocalDate(2015, 2, 1), new LocalDate(2015, 2, 14)));
		sprints.add(new Sprint("Sprint 24", new LocalDate(2015, 2, 15), new LocalDate(2015, 2, 28)));
		sprints.add(new Sprint("Sprint 25", new LocalDate(2015, 3, 1), new LocalDate(2015, 3, 14)));
		sprints.add(new Sprint("Sprint 26", new LocalDate(2015, 3, 15), new LocalDate(2015, 3, 28)));
		sprints.add(new Sprint("Sprint 5.0.2", new LocalDate(2015, 3, 29), new LocalDate(2015, 4, 18)));
		sprints.add(new Sprint("Sprint 5.0.3", new LocalDate(2015, 4, 19), new LocalDate(2015, 5, 2)));
		sprints.add(new Sprint("Sprint 5.0.4", new LocalDate(2015, 5, 3), new LocalDate(2015, 5, 30)));
		sprints.add(new Sprint("Sprint 5.0.5", new LocalDate(2015, 5, 31), new LocalDate(2015, 6, 13)));
		sprints.add(new Sprint("Sprint 5.0.6", new LocalDate(2015, 6, 14), new LocalDate(2015, 6, 27)));
		sprints.add(new Sprint("Sprint 5.0.7 partie 1", new LocalDate(2015, 6, 28), new LocalDate(2015, 7, 11)));
		sprints.add(new Sprint("Sprint 5.0.7 partie 2", new LocalDate(2015, 7, 12), new LocalDate(2015, 7, 25)));
		sprints.add(new Sprint("Sprint 5.0.7 partie 3", new LocalDate(2015, 7, 26), new LocalDate(2015, 8, 8)));
		sprints.add(new Sprint("Sprint 5.0.7 partie 4", new LocalDate(2015, 8, 9), new LocalDate(2015, 8, 22)));
		sprints.add(new Sprint("Sprint 5.1", new LocalDate(2015, 8, 23), new LocalDate(2015, 8, 29)));
		sprints.add(new Sprint("Sprint 5.1.1", new LocalDate(2015, 8, 30), new LocalDate(2015, 9, 12)));
		sprints.add(new Sprint("Sprint 5.1.2", new LocalDate(2015, 9, 13), new LocalDate(2015, 10, 17)));
		sprints.add(new Sprint("Sprint 5.1.3", new LocalDate(2015, 10, 18), new LocalDate(2015, 10, 31)));
		sprints.add(new Sprint("Sprint 5.1.4", new LocalDate(2015, 11, 1), new LocalDate(2015, 11, 14)));
		sprints.add(new Sprint("Sprint 5.1.5", new LocalDate(2015, 11, 15), new LocalDate(2015, 11, 28)));
		sprints.add(new Sprint("Sprint 5.1.6", new LocalDate(2015, 11, 29), new LocalDate(2015, 12, 7)));
		sprints.add(new Sprint("Sprint 5.1.7", new LocalDate(2015, 12, 8), new LocalDate(2015, 12, 12)));
		sprints.add(new Sprint("Sprint 5.1.8", new LocalDate(2015, 12, 13), new LocalDate(2016, 1, 2)));

	}

	public static void main(String argv[])
			throws Exception {
		Iterator<String> iterator = FileUtils.lineIterator(gitLog);

		FileUtils.deleteDirectory(gitLogSplittedFolder);
		gitLogSplittedFolder.mkdirs();

		while (iterator.hasNext()) {
			String line = iterator.next();
			String sprintName = detectSprintOfLine(line);
			String author = detectAuthorOfLine(line);

			File authorFile = new File(gitLogSplittedFolder, author + ".csv");
			File sprintDir = new File(gitLogSplittedFolder, sprintName);
			sprintDir.mkdirs();

			File all = new File(sprintDir, "all.csv");
			File authorInSprintFile = new File(sprintDir, author + ".csv");

			FileUtils.write(all, line + "\n", true);
			FileUtils.write(authorFile, line.replace(author, sprintName) + "\n", true);
			FileUtils.write(authorInSprintFile, line + "\n", true);
		}
	}

	private static String detectSprintOfLine(String line) {
		String dateTimeStr = line.split("\",\"")[2].replace("\"", "");
		LocalDateTime dateTime = LocalDateTime.parse(dateTimeStr, formatter);

		for (Sprint sprint : sprints) {
			if (sprint.beginDate.isBefore(dateTime) && sprint.endDate.isAfter(dateTime)) {
				return sprint.name;
			}
		}

		throw new RuntimeException("Cannot detect sprint of '" + line + "'");
	}

	private static String detectAuthorOfLine(String line) {
		String author = line.split(",")[1].replace("\"", "");
		if ("Maxime Cote".equals(author)) {
			author = "Maxime Côté";
		}

		return author;
	}

	private static class Sprint {

		String name;

		LocalDateTime beginDate;

		LocalDateTime endDate;

		public Sprint(String name, LocalDate beginDate, LocalDate endDate) {
			this.name = name;
			this.beginDate = beginDate.toLocalDateTime(LocalTime.MIDNIGHT);
			this.endDate = endDate.toLocalDateTime(LocalTime.MIDNIGHT).plusDays(1);
		}
	}

	private static LocalDate from(int year, int month, int day) {
		return new LocalDate(year, month, day);
	}

	private static LocalDate to(int year, int month, int day) {
		return new LocalDate(year, month, day);
	}

}
