package com.constellio.app.servlet;

import com.constellio.app.api.HttpServletRequestAuthenticator;
import com.constellio.app.services.factories.ConstellioFactories;
import com.constellio.app.services.schemas.bulkImport.BulkImportParams;
import com.constellio.app.services.schemas.bulkImport.LoggerBulkImportProgressionListener;
import com.constellio.app.services.schemas.bulkImport.RecordsImportServices;
import com.constellio.app.services.schemas.bulkImport.data.ImportDataProvider;
import com.constellio.app.services.schemas.bulkImport.data.excel.Excel2003ImportDataProvider;
import com.constellio.app.services.schemas.bulkImport.data.excel.Excel2007ImportDataProvider;
import com.constellio.app.services.schemas.bulkImport.data.xml.XMLImportDataProvider;
import com.constellio.app.ui.i18n.i18n;
import com.constellio.data.io.services.facades.FileService;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.frameworks.validation.ValidationError;
import com.constellio.model.frameworks.validation.ValidationErrors;
import com.constellio.model.frameworks.validation.ValidationException;
import com.constellio.model.frameworks.validation.ValidationRuntimeException;
import com.constellio.model.services.factories.ModelLayerFactory;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.exception.ExceptionUtils;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.Locale;

public class ConstellioImportRecordsServlet extends HttpServlet {

	public static final String TEMP_FILE_RESSOURCE_NAME = "ConstellioImportRecordsServlet-tempFile";
	private static final String PARAM_DATA_TYPE = "dataType";
	private static final String PARAM_SIMULATE = "simulate";
	private static final String RESPONSE_MSG_UNAUTHORIZED = "UNAUTHORIZED";
	private static final String RESPONSE_MSG_SUCCESS = "SUCCESS";

	@SuppressWarnings("unchecked")
	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		PrintWriter writer = response.getWriter();
		ValidationErrors importErrors;
		Throwable throwable = null;
		HttpServletRequestAuthenticator authenticator = new HttpServletRequestAuthenticator(modelLayerFactory());
		try {
			InputStream inputStream = request.getInputStream();
			FileService fileService = modelLayerFactory().getDataLayerFactory().getIOServicesFactory().newFileService();
			File tempFile = fileService.newTemporaryFile(TEMP_FILE_RESSOURCE_NAME);
			String dataType = request.getHeader(PARAM_DATA_TYPE);
			Boolean isSimulation = Boolean.valueOf(request.getHeader(PARAM_SIMULATE));

			try {
				OutputStream fos = new BufferedOutputStream(new FileOutputStream(tempFile));
				User user = authenticator.authenticateInCollection(request);
				if (user == null) {
					response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
					System.out.println(RESPONSE_MSG_UNAUTHORIZED);
					writer.println(RESPONSE_MSG_UNAUTHORIZED);
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
					importErrors = respond(tempFile, dataType, user, isSimulation);

					if (!importErrors.isEmpty()) {
						response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
						for (ValidationError error : importErrors.getValidationErrors()) {
							String message = i18n.$(error, Locale.FRENCH);
							System.out.println(message);
							writer.println(message);
						}
					} else {
						response.setStatus(HttpServletResponse.SC_OK);
						System.out.println(RESPONSE_MSG_SUCCESS);
						writer.println(RESPONSE_MSG_SUCCESS);
					}

				}
			} finally {
				fileService.deleteQuietly(tempFile);
			}
		} catch (Throwable e) {
			e.printStackTrace();
			writer.println(ExceptionUtils.getFullStackTrace(throwable));
		} finally {
			IOUtils.closeQuietly(writer);
		}

	}

	protected ModelLayerFactory modelLayerFactory() {
		return ConstellioFactories.getInstance().getModelLayerFactory();
	}

	protected ValidationErrors respond(File file, String dataType, User user, boolean isSimulation)
			throws Exception {
		ImportDataProvider dataProvider = toDataProvider(file, dataType);

		RecordsImportServices recordsImportServices = new RecordsImportServices(modelLayerFactory());
		BulkImportParams params = new BulkImportParams().setAllowingReferencesToNonExistingUsers(false);
		if (isSimulation) {
			params.setSimulate(true);
		}
		try {
			recordsImportServices.bulkImport(dataProvider, new LoggerBulkImportProgressionListener(), user,
					params);

		} catch (ValidationException e) {
			return e.getValidationErrors();
		} catch (ValidationRuntimeException e) {
			return e.getValidationErrors();
		}

		return new ValidationErrors();

	}

	private ImportDataProvider toDataProvider(File file, String dataType) {
		switch (dataType) {
			case "xml":
				return XMLImportDataProvider.forSingleXMLFile(modelLayerFactory(), file);

			case "xls":
				return Excel2003ImportDataProvider.fromFile(file);

			case "xlsx":
				return Excel2007ImportDataProvider.fromFile(file);

			default:
				throw new IllegalArgumentException("Invalid data type : " + dataType);
		}
	}

}
