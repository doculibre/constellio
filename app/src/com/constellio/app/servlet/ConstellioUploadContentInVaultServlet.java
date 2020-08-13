package com.constellio.app.servlet;

import com.constellio.app.api.HttpServletRequestAuthenticator;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.services.factories.ConstellioFactories;
import com.constellio.data.io.services.facades.FileService;
import com.constellio.model.entities.security.global.UserCredential;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.users.SystemWideUserInfos;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.jetbrains.annotations.NotNull;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

public class ConstellioUploadContentInVaultServlet extends HttpServlet {

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws IOException {
		HttpServletRequestAuthenticator authenticator = new HttpServletRequestAuthenticator(modelLayerFactory());
		SystemWideUserInfos user = authenticator.authenticate(request);
		if (user == null) {
			response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
		} else {
			try (ObjectOutputStream responseOutputStream = new ObjectOutputStream(response.getOutputStream())) {
				responseOutputStream.writeObject(handle(request));
			}
		}


	}

	@NotNull
	private Map<String, Object> handle(HttpServletRequest request) {
		Map<String, Object> outParams = new HashMap<>();
		try (InputStream inputStream = request.getInputStream()) {
			FileService fileService = ConstellioFactories.getInstance().getIoServicesFactory().newFileService();
			String filename = request.getHeader("fileName");
			File tempFile = fileService.newTemporaryFile("ConstellioUploadContentInVaultServlet.tempFile", filename);
			try {
				try (OutputStream outputStream = new BufferedOutputStream(new FileOutputStream(tempFile))) {
					IOUtils.copy(inputStream, outputStream);
				}
				outParams.put("result", respond(tempFile));

			} finally {
				fileService.deleteQuietly(tempFile);
			}

		} catch (Throwable throwable) {
			throwable.printStackTrace();
			outParams = toThrowableParams(throwable);

		}
		return outParams;
	}

	@NotNull
	private Map<String, Object> toThrowableParams(Throwable throwable) {
		Map<String, Object> outParams = new HashMap<>();
		String exceptionClassName = throwable.getClass().getName();
		String exceptionMessage = throwable.getMessage();
		String exceptionStackTrace = ExceptionUtils.getFullStackTrace(throwable);
		outParams.put("throwableClassName", exceptionClassName);
		outParams.put("throwableMessage", exceptionMessage);
		outParams.put("throwableStackTrace", exceptionStackTrace);
		return outParams;
	}

	protected ModelLayerFactory modelLayerFactory() {
		return ConstellioFactories.getInstance().getModelLayerFactory();
	}

	protected Map<String, Object> respond(File file)
			throws Exception {
		AppLayerFactory appLayerFactory = ConstellioFactories.getInstance().getAppLayerFactory();

		ConstellioUploadContentInVaultService.UploadContentServiceInput input = parseParams(file);
		String hash = new ConstellioUploadContentInVaultService(appLayerFactory, input).uploadContentAndGetHash();

		Map<String, Object> hashResponse = new HashMap<>();
		hashResponse.put("hash", hash);

		return hashResponse;
	}

	private ConstellioUploadContentInVaultService.UploadContentServiceInput parseParams(File file) {
		ConstellioUploadContentInVaultService.UploadContentServiceInput uploadDocumentInput = new ConstellioUploadContentInVaultService.UploadContentServiceInput();
		uploadDocumentInput.setFile(file);

		return uploadDocumentInput;
	}
}
