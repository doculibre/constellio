package com.constellio.app.servlet;

import com.constellio.app.api.HttpServletRequestAuthenticator;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.services.factories.ConstellioFactories;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.users.SystemWideUserInfos;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.exception.ExceptionUtils;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

public class ConstellioUploadContentInVaultServlet extends HttpServlet {

	@SuppressWarnings("unchecked")
	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		OutputStream output = null;
		Map<String, Object> responseResult = null;
		Throwable throwable = null;
		HttpServletRequestAuthenticator authenticator = new HttpServletRequestAuthenticator(modelLayerFactory());
		try {
			InputStream inputStream = request.getInputStream();
			File file = new File(request.getHeader("fileName"));
			FileOutputStream fos = new FileOutputStream(file);
			SystemWideUserInfos user = authenticator.authenticateUsingUsername(request);
			if (user == null) {
				response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
			} else {
				byte[] buffer = new byte[4096];
				int bytesRead;

				try {
					while ((bytesRead = inputStream.read(buffer)) != -1) {
						fos.write(buffer, 0, bytesRead);
					}
				} finally {
					if (inputStream != null) {
						inputStream.close();
					}
					fos.close();
				}
				responseResult = respond(file);
				output = response.getOutputStream();
				Map<String, Object> outParams = new HashMap<>();
				ObjectOutputStream oos = new ObjectOutputStream(output);
				outParams.put("result", responseResult);
				oos.writeObject(outParams);
			}
		} catch (Throwable e) {
			e.printStackTrace();
			throwable = e;
		} finally {
			IOUtils.closeQuietly(output);
		}
		if (responseResult != null || throwable != null) {
			Map<String, Object> outParams = new HashMap<>();
			ObjectOutputStream oos = new ObjectOutputStream(output);
			if (responseResult != null) {
				outParams.put("result", responseResult);
			} else {
				String exceptionClassName = throwable.getClass().getName();
				String exceptionMessage = throwable.getMessage();
				String exceptionStackTrace = ExceptionUtils.getFullStackTrace(throwable);
				outParams.put("throwableClassName", exceptionClassName);
				outParams.put("throwableMessage", exceptionMessage);
				outParams.put("throwableStackTrace", exceptionStackTrace);
			}
			oos.writeObject(outParams);
		}
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
