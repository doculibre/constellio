package com.constellio.model.services.users;

import com.constellio.data.io.services.facades.IOServices;
import com.constellio.data.io.services.zip.ZipService;
import com.constellio.data.io.streamFactories.StreamFactory;
import com.constellio.model.services.users.UserPhotosServicesRuntimeException.UserPhotosServicesRuntimeException_UserHasNoPhoto;
import com.constellio.sdk.tests.ConstellioTest;
import org.apache.commons.io.IOUtils;
import org.joda.time.LocalDateTime;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import static org.assertj.core.api.Assertions.assertThat;

public class UserPhotosServicesAcceptanceTest extends ConstellioTest {

	LocalDateTime shishOClock = new LocalDateTime();
	LocalDateTime hearthstoneOClock = shishOClock.plusMillis(15);

	IOServices ioServices;
	UserPhotosServices services;
	ZipService zipService;

	@Before
	public void setUp()
			throws Exception {
		ioServices = getIOLayerFactory().newIOServices();
		services = getModelLayerFactory().newUserPhotosServices();

		UserServices userServices = getModelLayerFactory().newUserServices();
		userServices.addUpdateUserCredential(userServices.addRequest("zeUser", "ze", "user", "ze.user@gmail.com"));

		zipService = getIOLayerFactory().newZipService();
	}

	@Test(expected = UserPhotosServicesRuntimeException_UserHasNoPhoto.class)
	public void givenUserWithoutPhotoWhenGetPhotoThenException()
			throws Exception {

		services.getPhotoInputStream("zeUser");

	}

	@Test
	public void givenUserWithoutPhotoWhenChangePhotoThenPhotoAvailable()
			throws Exception {

		services.changePhoto(firstPhotoInputStream(), "zeUser");
		InputStream theUserPhotoInputStream = services.getPhotoInputStream("zeUser").create(SDK_STREAM);

		assertThat(theUserPhotoInputStream).hasContentEqualTo(firstPhotoInputStream());

	}

	@Test
	public void givenUserWithAPhotoWhenChangePhotoThenNewPhotoAvailable()
			throws Exception {

		services.changePhoto(firstPhotoInputStream(), "zeUser");

		services.changePhoto(secondPhotoInputStream(), "zeUser");
		InputStream theUserPhotoInputStream = services.getPhotoInputStream("zeUser").create(SDK_STREAM);

		assertThat(theUserPhotoInputStream).hasContentEqualTo(secondPhotoInputStream());

	}

	@Test
	public void whenAddLogsThenCanRetrieveThemAndDeleteThem()
			throws Exception {

		givenTimeIs(shishOClock);
		services.addLogFile("zeUser", firstLogInputStream());

		givenTimeIs(hearthstoneOClock);
		services.addLogFile("zeUser", secondLogInputStream());
		services.addLogFile("anotherUser", thirdLogInputStream());

		assertThat(services.getUserLogs("zeUser")).containsOnly(shishOClock.toString(UserPhotosServices.DATE_PATTERN),
				hearthstoneOClock.toString(UserPhotosServices.DATE_PATTERN));
		assertThat(services.getUserLogs("anotherUser")).containsOnly(hearthstoneOClock.toString(UserPhotosServices.DATE_PATTERN));
		assertThat(services.newUserLogInputStream("zeUser", shishOClock.toString(UserPhotosServices.DATE_PATTERN), SDK_STREAM))
				.hasContentEqualTo(firstLogInputStream());
		assertThat(
				services.newUserLogInputStream("zeUser", hearthstoneOClock.toString(UserPhotosServices.DATE_PATTERN), SDK_STREAM))
				.hasContentEqualTo(secondLogInputStream());
		assertThat(services.newUserLogInputStream("anotherUser", hearthstoneOClock.toString(UserPhotosServices.DATE_PATTERN),
				SDK_STREAM))
				.hasContentEqualTo(thirdLogInputStream());

		services.deleteUserLog("zeUser", hearthstoneOClock.toString(UserPhotosServices.DATE_PATTERN));

		assertThat(services.getUserLogs("zeUser")).containsOnly(shishOClock.toString(UserPhotosServices.DATE_PATTERN));
		assertThat(services.getUserLogs("anotherUser")).containsOnly(hearthstoneOClock.toString(UserPhotosServices.DATE_PATTERN));

	}

	@Test
	public void givenSomeLogsWhenGetZipThenAllInZep()
			throws Exception {

		givenTimeIs(shishOClock);
		services.addLogFile("zeUser", firstLogInputStream());

		givenTimeIs(hearthstoneOClock);
		services.addLogFile("zeUser", secondLogInputStream());
		services.addLogFile("anotherUser", thirdLogInputStream());

		StreamFactory<InputStream> inputStreamFactory = services.getAllLogs("zeUser");
		OutputStream outputStream = null;
		InputStream inputStream = inputStreamFactory.create("aStreamThatTheTestMustClose");

		File zipFile;
		try {
			zipFile = new File(newTempFolder(), "logs.zip");
			outputStream = ioServices.newFileOutputStream(zipFile, SDK_STREAM);

			IOUtils.copy(inputStream, outputStream);

		} finally {
			ioServices.closeQuietly(inputStream);
			ioServices.closeQuietly(outputStream);
		}

		File tempUnzipFolder = newTempFolder();
		zipService.unzip(zipFile, tempUnzipFolder);

		assertThat(tempUnzipFolder.list()).containsOnly(
				shishOClock.toString(UserPhotosServices.DATE_PATTERN) + ".zip",
				hearthstoneOClock.toString(UserPhotosServices.DATE_PATTERN) + ".zip");
		assertThat(new File(tempUnzipFolder, shishOClock.toString(UserPhotosServices.DATE_PATTERN) + ".zip"))
				.hasContentEqualTo(firstLogFile());
		assertThat(new File(tempUnzipFolder, hearthstoneOClock.toString(UserPhotosServices.DATE_PATTERN) + ".zip"))
				.hasContentEqualTo(secondLogFile());

	}

	private File firstLogFile()
			throws IOException {
		return getTestResourceFile("zeFirst.log.zip");
	}

	private File secondLogFile()
			throws IOException {
		return getTestResourceFile("zeSecond.log.zip");
	}

	private File thirdLogFile()
			throws IOException {
		return getTestResourceFile("zeThird.log.zip");
	}

	private InputStream firstLogInputStream()
			throws IOException {
		return getTestResourceInputStream("zeFirst.log.zip");
	}

	private InputStream secondLogInputStream()
			throws IOException {
		return getTestResourceInputStream("zeSecond.log.zip");
	}

	private InputStream thirdLogInputStream()
			throws IOException {
		return getTestResourceInputStream("zeThird.log.zip");
	}

	private InputStream firstPhotoInputStream()
			throws IOException {
		return getTestResourceInputStreamFactory("firstPhoto.jpg").create(SDK_STREAM);
	}

	private InputStream secondPhotoInputStream()
			throws IOException {
		return getTestResourceInputStreamFactory("secondPhoto.jpg").create(SDK_STREAM);
	}
}
