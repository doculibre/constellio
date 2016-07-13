package com.constellio.app.api.systemManagement.services;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.servlet.http.HttpServletRequest;

import com.constellio.data.conf.HashingEncoding;
import org.jdom2.Element;

import com.constellio.app.entities.modules.ProgressInfo;
import com.constellio.app.services.appManagement.AppManagementServiceException;
import com.constellio.app.services.factories.ConstellioFactories;
import com.constellio.data.io.EncodingService;
import com.constellio.data.io.services.facades.IOServices;
import com.constellio.data.utils.hashing.HashingService;
import com.constellio.data.utils.hashing.HashingServiceException;

public class UpdateWarSystemManagementWebService extends AdminSystemManagementWebService {

	public static final String TEMP_FILE = "UpdateWarSystemManagementWebService-TempFile";
	public static final String TEMP_FILE_INPUTSTREAM = "UpdateWarSystemManagementWebService-TempFileInputStream";
	public static final String WAR_OUTPUTSTREAM = "UpdateWarSystemManagementWebService-WarOutputStream";

	@Override
	protected void doService(HttpServletRequest req, Element responseDocumentRootElement) {
		String warUrl = getRequiredParameter(req, "warUrl");
		String hash = getRequiredParameter(req, "hash").replace("_", "/").replace("-", "+");

		IOServices ioServices = ioServices();
		HashingService hashingService = HashingService.forMD5(new EncodingService(), HashingEncoding.BASE64);
		File tempFile = ioServices.newTemporaryFile(TEMP_FILE);
		InputStream in = null;
		OutputStream out = null;
		try {
			downloadTo(warUrl, tempFile);

			String hashFromDownloadedWar = hashingService.getHashFromFile(tempFile);

			if (!hashFromDownloadedWar.equals(hash)) {
				throw new AdminHttpServletRuntimeException("Bad hash");
			}

			in = ioServices.newBufferedFileInputStream(tempFile, TEMP_FILE_INPUTSTREAM);
			out = appLayerFactory().newApplicationService().getWarFileDestination().create(WAR_OUTPUTSTREAM);
			ioServices.copy(in, out);

		} catch (IOException | HashingServiceException e) {
			throw new RuntimeException(e);
		} finally {
			ioServices.closeQuietly(in);
			ioServices.closeQuietly(out);
			ioServices.deleteQuietly(tempFile);
		}

		try {
			ConstellioFactories.getInstance().getAppLayerFactory().newApplicationService().update(new ProgressInfo());
		} catch (AppManagementServiceException e) {
			throw new RuntimeException(e);
		}

		responseDocumentRootElement.setText("success");
		new java.util.Timer().schedule(
				new java.util.TimerTask() {
					@Override
					public void run() {
						try {
							ConstellioFactories.getInstance().getAppLayerFactory().newApplicationService().restart();
						} catch (AppManagementServiceException e) {
							e.printStackTrace();
						}
					}
				},
				5000
		);

	}
}
