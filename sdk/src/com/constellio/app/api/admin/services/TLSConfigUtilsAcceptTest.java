package com.constellio.app.api.admin.services;

import com.constellio.data.io.services.facades.FileService;
import com.constellio.sdk.tests.ConstellioTest;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.LineIterator;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import java.io.File;
import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class TLSConfigUtilsAcceptTest extends ConstellioTest {
	private @Mock FileService fileService;
	private File newTempFile;
	private File correctWrapper;
	private File missingWrapper;

	@Before
	public void setup() throws Exception {
		missingWrapper = getTestResourceFile("wrapper-missing.conf");
		correctWrapper = getTestResourceFile("wrapper-correct.conf");
		newTempFile = newTempFileWithContent("newWrapper.conf", "");

		when(fileService.newTemporaryFile("newWraper.conf")).thenReturn(newTempFile);

		doNothing().when(fileService).copyFile(newTempFile, correctWrapper);
		doNothing().when(fileService).copyFile(newTempFile, missingWrapper);
	}

	@Test
	public void whenSettingAdditional2TemporaryDirectory()
			throws IOException {
		TLSConfigUtils.setSettingAdditional2TemporaryDirectory(correctWrapper, fileService);
		verifyFilePositionAndContent("wrapper.java.additional.1", "wrapper.java.additional.2=-Djava.io.tmpdir=/opt/constellio_tmp");
	}

	@Test
	public void whenCorrectWrapperLineIsAddedInTheRightLine() throws Exception {
		TLSConfigUtils.setSettingAdditional2EphemeralDHKeySize(correctWrapper, fileService);
		verifyFilePositionAndContent("wrapper.java.additional.1","wrapper.java.additional.2=-Djdk.tls.ephemeralDHKeySize=2048");
	}

	private void verifyFilePositionAndContent(String lineBefore, String addedLine)
			throws IOException {
		LineIterator iterator = FileUtils.lineIterator(newTempFile, "UTF-8");

		String compare = "";
		while (iterator.hasNext()) {
			String line = iterator.nextLine();
			if (line.contains(lineBefore)) {
				compare = iterator.nextLine();
				break;
			}
		}

		assertThat(compare).isNotEmpty();
		assertThat(compare).isEqualTo(addedLine);
		verify(fileService, times(1)).copyFile(eq(correctWrapper), any(File.class));
		verify(fileService, times(1)).copyFile(newTempFile, correctWrapper);

		iterator.close();
	}

	@Test
	public void whenMissingLineNothingChanged() throws Exception {
		LineIterator originalIterator = FileUtils.lineIterator(missingWrapper, "UTF-8");

		TLSConfigUtils.setSettingAdditional2EphemeralDHKeySize(missingWrapper, fileService);
		LineIterator newIterator = FileUtils.lineIterator(newTempFile, "UTF-8");

		while (newIterator.hasNext()) {
			String original = originalIterator.nextLine();
			String changed = newIterator.nextLine();

			assertThat(original).isEqualTo(changed);
		}

		verify(fileService, times(1)).copyFile(eq(missingWrapper), any(File.class));
		verify(fileService, times(1)).copyFile(newTempFile, missingWrapper);

		newIterator.close();
		originalIterator.close();
	}
}
