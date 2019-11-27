package com.constellio.data.dao.services.replicationFactor;

import com.constellio.data.dao.services.contents.ContentDao;
import com.constellio.sdk.tests.ConstellioTest;
import com.google.common.collect.ImmutableSet;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import static com.constellio.data.dao.services.replicationFactor.ReplicationFactorTestUtils.FOLDER;
import static com.constellio.data.dao.services.replicationFactor.ReplicationFactorTestUtils.PREFIX;
import static com.constellio.data.dao.services.replicationFactor.ReplicationFactorTestUtils.getLocalLogFilePath;
import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;

public class ReplicationFactorLogServiceAcceptanceTest extends ConstellioTest {

	private ReplicationFactorLogService service;
	private ContentDao contentDao;

	private Path localLogFilePath;
	private Path mergedLogFilePath;

	private static final String LINE1 = "{\"id\":\"1\",\"timestamp\":\"1\"}";
	private static final String LINE2 = "{\"id\":\"2\",\"timestamp\":\"2\"}";
	private static final String LINE3 = "{\"id\":\"3\",\"timestamp\":\"3\"}";
	private static final String LINE4 = "{\"id\":\"4\",\"timestamp\":\"4\"}";

	@Before
	public void setUp() throws Exception {
		contentDao = getDataLayerFactory().getContentsDao();
		service = new ReplicationFactorLogService(contentDao);

		localLogFilePath = getLocalLogFilePath(service, contentDao);

		mergedLogFilePath = contentDao.getFileOf(FOLDER.concat("/").concat(PREFIX).concat(".tlog")).toPath();

		String content = LINE1.concat(System.lineSeparator()).concat(LINE2).concat(System.lineSeparator());
		Files.write(localLogFilePath, content.getBytes());
	}

	@Test
	public void givenMergeLogFilesThenAllLogFilesMerged() throws Exception {
		String logFile2 = FOLDER.concat("/").concat(PREFIX).concat("-2.tlog");
		String content2 = LINE3.concat(System.lineSeparator()).concat(LINE4).concat(System.lineSeparator());
		contentDao.add(logFile2, new ByteArrayInputStream(content2.getBytes()));

		File resultFile = service.mergeAllLogFiles();

		List<String> lines = Files.readAllLines(resultFile.toPath(), StandardCharsets.UTF_8);
		assertThat(lines).containsOnly(LINE1, LINE2, LINE3, LINE4);

		lines = Files.readAllLines(mergedLogFilePath, StandardCharsets.UTF_8);
		assertThat(lines).containsOnly(LINE1, LINE2, LINE3, LINE4);

		assertThat(localLogFilePath.toFile().exists()).isFalse();
		assertThat(contentDao.getFileOf(logFile2).exists()).isFalse();
	}

	@Test
	public void givenWriteThenLineAddedToLog() throws Exception {
		service.writeLineToLocalLog(LINE3);

		List<String> lines = Files.readAllLines(localLogFilePath, StandardCharsets.UTF_8);
		assertThat(lines).containsExactly(LINE1, LINE2, LINE3);
	}

	@Test
	public void givenLogFileDeletedAndWriteThenLogFileCreatedAndLineAdded() throws Exception {
		Files.deleteIfExists(localLogFilePath);

		service.writeLineToLocalLog(LINE3);

		List<String> lines = Files.readAllLines(localLogFilePath, StandardCharsets.UTF_8);
		assertThat(lines).containsExactly(LINE3);
	}

	@Test
	public void givenLogWith2LinesAndRemove2LinesThenLogIsEmpty() throws Exception {
		addLinesToMergedLog(asList(LINE1, LINE2));

		service.removeLinesFromMergedLog(2, ImmutableSet.of("1", "2"));

		List<String> lines = Files.readAllLines(mergedLogFilePath, StandardCharsets.UTF_8);
		assertThat(lines).isEmpty();
	}

	@Test
	public void givenLogWith4LinesAndRemove2LinesThenLogContains2Lines() throws Exception {
		addLinesToMergedLog(asList(LINE1, LINE2, LINE3, LINE4));

		service.removeLinesFromMergedLog(2, ImmutableSet.of("1", "2"));

		List<String> lines = Files.readAllLines(mergedLogFilePath, StandardCharsets.UTF_8);
		assertThat(lines).containsExactly(LINE3, LINE4);
	}

	@Test
	public void givenLogWith4LinesAndRemove2LinesAnd1LineReplayedThenLogContains3Lines() throws Exception {
		addLinesToMergedLog(asList(LINE1, LINE2, LINE3, LINE4));

		service.removeLinesFromMergedLog(2, ImmutableSet.of("2"));

		List<String> lines = Files.readAllLines(mergedLogFilePath, StandardCharsets.UTF_8);
		assertThat(lines).containsExactly(LINE1, LINE3, LINE4);
	}

	@Test
	public void givenConcurrentWritesThenWritesExecutedSequentially() throws Exception {
		Files.delete(localLogFilePath);

		ScheduledExecutorService executor = Executors.newScheduledThreadPool(4);
		executor.invokeAll(asList(buildWrite(LINE1), buildWrite(LINE2), buildWrite(LINE3), buildWrite(LINE4)));

		List<String> lines = Files.readAllLines(localLogFilePath, StandardCharsets.UTF_8);
		assertThat(lines).containsOnly(LINE1, LINE2, LINE3, LINE4);
	}

	private Callable<Boolean> buildWrite(final String content) {
		return new Callable<Boolean>() {
			public Boolean call() throws Exception {
				service.writeLineToLocalLog(content);
				return true;
			}
		};
	}

	private void addLinesToMergedLog(List<String> lines) throws Exception {
		String content = "";
		for (String line : lines) {
			content = content.concat(line).concat(System.lineSeparator());
		}
		Files.write(mergedLogFilePath, content.getBytes());
	}
}
