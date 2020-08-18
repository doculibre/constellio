package com.constellio.sdk.dev.tools.com.constellio.sdk.dev.tools;

import org.apache.commons.io.FileUtils;
import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
import org.joda.time.LocalTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

public class SplitGitCommentsMain {

	//git log --since='2 years' --pretty=format:'"%h","%an","%ad","%s"' > ~/logConstellio.csv
	private static File constellioGitLog = new File("/Users/francisbaril/logConstellio.csv");
	private static File constellioPluginsGitLog = new File("/Users/francisbaril/logConstellioPlugins.csv");
	private static File gitLogSplittedFolder = new File("/Users/francisbaril/Downloads/rsde/splitted/");

	private static List<Sprint> sprints = new ArrayList<>();

	private static DateTimeFormatter formatter = DateTimeFormat.forPattern("EEE MMM dd HH:mm:ss yyyy Z");

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
		sprints.add(new Sprint("Janvier 2015", new LocalDate(2016, 1, 3), new LocalDate(2016, 1, 31)));
		sprints.add(new Sprint("Année 2016", new LocalDate(2016, 2, 1), new LocalDate(2017, 1, 31)));
		sprints.add(new Sprint("Année 2017", new LocalDate(2017, 2, 1), new LocalDate(2018, 1, 31)));
		sprints.add(new Sprint("Année 2018", new LocalDate(2018, 2, 1), new LocalDate(2019, 1, 31)));
		sprints.add(new Sprint("Année 2019", new LocalDate(2019, 2, 1), new LocalDate(2020, 1, 31)));
		sprints.add(new Sprint("Année 2020", new LocalDate(2020, 2, 1), new LocalDate(2021, 1, 31)));

	}

	public static void main(String argv[])
			throws Exception {
		Locale.setDefault(Locale.ENGLISH);
		List<String> commitsOfAllRepos = new ArrayList<String>();
		commitsOfAllRepos.addAll(FileUtils.readLines(constellioGitLog));
		commitsOfAllRepos.addAll(FileUtils.readLines(constellioPluginsGitLog));

		Collections.sort(commitsOfAllRepos, new Comparator<String>() {
			@Override
			public int compare(String o1, String o2) {
				String convertedO1 = o1.split("\",\"")[2].replace("\"", "");
				String convertedO2 = o2.split("\",\"")[2].replace("\"", "");

				LocalDateTime dateTime1 = LocalDateTime.parse(convertedO1, formatter);
				LocalDateTime dateTime2 = LocalDateTime.parse(convertedO2, formatter);

				return dateTime1.compareTo(dateTime2);
			}
		});

		FileUtils.deleteDirectory(gitLogSplittedFolder);
		gitLogSplittedFolder.mkdirs();

		for (String line : commitsOfAllRepos) {
			String sprintName = detectSprintOfLine(line);
			String author = detectAuthorOfLine(line);

			File authorFile = new File(gitLogSplittedFolder, author + ".csv");
			File sprintDir = new File(gitLogSplittedFolder, sprintName);
			sprintDir.mkdirs();

			File all = new File(sprintDir, "all.csv");
			File authorInSprintFile = new File(sprintDir, author + ".csv");

			if (!line.contains(",\"Merge remote-tracking")) {

				FileUtils.write(all, line + "\n", true);
				FileUtils.write(authorFile, line.replace(author, sprintName) + "\n", true);
				FileUtils.write(authorInSprintFile, line + "\n", true);
			}
		}
	}

	private static String detectSprintOfLine(String line) {
		System.out.println(line);
		String dateTimeStr = line.split("\",\"")[2].replace("\"", "");
		System.out.println(dateTimeStr);
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
		System.out.println(author);
		if ("Maxime Cote".equals(author)) {
			author = "Maxime Côté";
		}

		if ("jplamondon".equals(author) || "jplamondon".equals(author) || "jonathan plamondon".equals(author) || "Charles"
				.equals(author)) {
			author = "Jonathan Plamondon";
		}

		if ("nDamours".equals(author) || "Marco".equals(author)) {
			author = "Nicolas d'Amours";
		}

		if ("gabrielLefrancoisConstellio".equals(author)) {
			author = "Gabriel Lefrançois";
		}

		if ("rabab.moubine@doculibre.com".equals(author) || "rabab".equals(author)) {
			author = "Rabab Moubine";
		}


		if ("ericgiguere".equals(author) || "EricG".equals(author)) {
			author = "Éric Giguère";
		}

		if ("dakota.indien".equals(author) || "dakota-indien".equals(author) || "stopping1".equals(author) || "Martin"
				.equals(author)
			|| "Rodrigue Mouadeu".equals(author) || "PatrickPontbriand".equals(author) || "Nabil Benyas".equals(author)
			|| "bnouha1".equals(author) || "Nabil Benyas".equals(author) || "bennab".equals(author)
			|| "julbaril".equals(author) || "Majid Laali".equals(author) || "fatima92".equals(author)
			|| "Fatima".equals(author)) {
			author = "Autres";
		}

		if ("dakota.indien".equals(author) || "dakota-indien".equals(author) || "stopping1".equals(author)) {
			author = "Autres";
		}

		if ("michel-boutin".equals(author)) {
			author = "Michel Boutin";
		}

		if ("rababMoubine".equals(author)) {
			author = "Rabab Moubine";
		}

		if ("francisbaril".equals(author)) {
			author = "Francis Baril";
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
