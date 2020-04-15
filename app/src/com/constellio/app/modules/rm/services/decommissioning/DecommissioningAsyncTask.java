package com.constellio.app.modules.rm.services.decommissioning;

import com.constellio.app.modules.rm.RMEmailTemplateConstants;
import com.constellio.app.modules.rm.navigation.RMNavigationConfiguration;
import com.constellio.app.modules.rm.services.ExternalLinkServices;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.services.decommissioning.DecommissioningServiceException.DecommissioningServiceException_TooMuchOptimisticLockingWhileAttemptingToDecommission;
import com.constellio.app.modules.rm.wrappers.DecommissioningList;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.services.factories.ConstellioFactories;
import com.constellio.app.ui.util.MessageUtils;
import com.constellio.data.utils.TimeProvider;
import com.constellio.model.conf.FoldersLocator;
import com.constellio.model.entities.batchprocess.AsyncTask;
import com.constellio.model.entities.batchprocess.AsyncTaskExecutionParams;
import com.constellio.model.entities.batchprocess.BatchProcess;
import com.constellio.model.entities.records.Content;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.entities.records.wrappers.BatchProcessReport;
import com.constellio.model.entities.records.wrappers.EmailToSend;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.entities.structures.EmailAddress;
import com.constellio.model.services.contents.ContentManager;
import com.constellio.model.services.contents.ContentVersionDataSummary;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.migrations.ConstellioEIMConfigs;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.records.RecordServicesException;
import com.constellio.model.services.records.SchemasRecordsServices;
import com.constellio.model.services.schemas.MetadataSchemasManager;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.users.UserServices;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.log4j.Logger;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.constellio.app.ui.i18n.i18n.$;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;

@Slf4j
public class DecommissioningAsyncTask implements AsyncTask {
	private static final Logger LOGGER = Logger.getLogger(DecommissioningService.class);

	private AppLayerFactory appLayerFactory;
	private ModelLayerFactory modelLayerFactory;

	private ContentManager contentManager;
	private MetadataSchemasManager schemasManager;
	private SearchServices searchServices;
	private UserServices userServices;
	private RecordServices recordServices;
	private ExternalLinkServices externalLinkServices;

	private RMSchemasRecordsServices rm;
	private DecommissioningService decommissioningService;
	private SchemasRecordsServices schemas;

	private ConstellioEIMConfigs eimConfigs;
	private MetadataSchemaTypes schemaTypes;
	private User currentUser;

	private String collection;
	private String username;
	private String decommissioningListId;

	public DecommissioningAsyncTask(String collection, String username, String decommissioningListId) {
		this.collection = collection;
		this.username = username;
		this.decommissioningListId = decommissioningListId;

		appLayerFactory = ConstellioFactories.getInstance().getAppLayerFactory();
		modelLayerFactory = appLayerFactory.getModelLayerFactory();

		contentManager = modelLayerFactory.getContentManager();
		schemasManager = modelLayerFactory.getMetadataSchemasManager();
		searchServices = modelLayerFactory.newSearchServices();
		userServices = modelLayerFactory.newUserServices();
		recordServices = modelLayerFactory.newRecordServices();
		externalLinkServices = new ExternalLinkServices(collection, appLayerFactory);

		rm = new RMSchemasRecordsServices(collection, appLayerFactory);
		decommissioningService = new DecommissioningService(collection, appLayerFactory);
		schemas = new SchemasRecordsServices(collection, modelLayerFactory);

		eimConfigs = new ConstellioEIMConfigs(modelLayerFactory.getSystemConfigurationsManager());
		schemaTypes = schemasManager.getSchemaTypes(collection);
		currentUser = userServices.getUserInCollection(username, collection);
	}

	@Override
	public Object[] getInstanceParameters() {
		return new Object[]{collection, username, decommissioningListId};
	}

	@Override
	public void execute(AsyncTaskExecutionParams params) {
		try {
			externalLinkServices.beforeExternalLinkImport(username);
			process(params, 0);
			sendEndMail(null);
		} catch (Exception e) {
			writeErrorToReport(params, MessageUtils.toMessage(e) + "\n\n" + ExceptionUtils.getStackTrace(e));
			sendEndMail(MessageUtils.toMessage(e));
		}
	}

	private void process(AsyncTaskExecutionParams params, int attempt) throws Exception {
		DecommissioningList decommissioningList = rm.getDecommissioningList(decommissioningListId);
		Decommissioner decommissioner = Decommissioner.forList(decommissioningList, decommissioningService, appLayerFactory);

		int recordCount = 1;
		if (decommissioningList.getDecommissioningListType().isFolderList()) {
			recordCount += decommissioningList.getFolders().size();
		}

		if (attempt == 0) {
			params.setProgressionUpperLimit(recordCount);
		}

		try {
			if (decommissioningList.getDecommissioningListType().isFolderList()) {
				importExternalLinks(params, decommissioningList);
			}
			decommissioner.process(decommissioningList, currentUser, TimeProvider.getLocalDate());
			params.incrementProgression(1);
		} catch (RecordServicesException.OptimisticLocking e) {
			if (attempt < 3) {
				LOGGER.warn("Decommission failed, retrying...", e);
				process(params, attempt + 1);
			} else {
				throw new DecommissioningServiceException_TooMuchOptimisticLockingWhileAttemptingToDecommission();
			}
		}
	}

	private void importExternalLinks(AsyncTaskExecutionParams params, DecommissioningList decommissioningList)
			throws Exception {
		for (String folderId : decommissioningList.getFolders()) {
			Folder folder = rm.getFolder(folderId);
			List<String> externalLinks = folder.getExternalLinks();
			for (String externalLinkId : externalLinks) {
				externalLinkServices.importExternalLink(externalLinkId, folderId);
			}
			params.incrementProgression(1);
		}
	}

	private void sendEndMail(String error) {
		try {
			EmailToSend emailToSend = newEmailToSend();

			EmailAddress userAddress = new EmailAddress(currentUser.getTitle(), currentUser.getEmail());
			emailToSend.setTemplate(RMEmailTemplateConstants.ALERT_DECOMMISSIONING_ENDED);
			emailToSend.setTo(Arrays.asList(userAddress));
			emailToSend.setSendOn(TimeProvider.getLocalDateTime());
			emailToSend.setSubject(getMailTitle(StringUtils.isBlank(error)));
			emailToSend.setParameters(buildMailParameters(error));

			recordServices.add(emailToSend);
		} catch (RecordServicesException e) {
			log.error("DecommissioningAsyncTask.cannotSendEmail", e);
		}
	}

	private EmailToSend newEmailToSend() {
		MetadataSchema schema = schemaTypes.getSchemaType(EmailToSend.SCHEMA_TYPE).getDefaultSchema();
		Record emailToSendRecord = recordServices.newRecordWithSchema(schema);
		return new EmailToSend(emailToSendRecord, schemaTypes);
	}

	private List<String> buildMailParameters(String error) {
		List<String> params = new ArrayList<>();

		params.add("title" + EmailToSend.PARAMETER_SEPARATOR + getMailTitle(StringUtils.isBlank(error)));
		params.add("greetings" + EmailToSend.PARAMETER_SEPARATOR +
				   $("DecommissionningServices.decomMailGreetings", currentUser.getTitle()));
		params.add("message" + EmailToSend.PARAMETER_SEPARATOR + getMailMessage(StringUtils.isBlank(error)));
		params.add("error" + EmailToSend.PARAMETER_SEPARATOR + (error == null ? "" : error));
		params.add("closure" + EmailToSend.PARAMETER_SEPARATOR + $("DecommissionningServices.decomMailClosure"));
		params.add("view" + EmailToSend.PARAMETER_SEPARATOR + $("DecommissionningServices.decomMailView"));

		String constellioUrl = eimConfigs.getConstellioUrl();
		params.add("constellioURL" + EmailToSend.PARAMETER_SEPARATOR + constellioUrl);
		String viewPath = error == null ? getDecommissioningListDisplayPath() : "batchProcesses";
		params.add("recordURL" + EmailToSend.PARAMETER_SEPARATOR + constellioUrl + "#!" + viewPath);

		return params;
	}

	private String getMailTitle(boolean success) {
		String recordTitle = recordServices.getDocumentById(decommissioningListId).getTitle();
		String subject = success ? $("DecommissionningServices.decomMailTitleSuccess", recordTitle)
								 : $("DecommissionningServices.decomMailTitleFailure", recordTitle);
		return subject;
	}

	private String getMailMessage(boolean success) {
		Record decomList = recordServices.getDocumentById(decommissioningListId);
		String subject = success ? $("DecommissionningServices.decomMailMessageSuccess", decomList.getTitle(), decomList.getId())
								 : $("DecommissionningServices.decomMailMessageFailure", decomList.getTitle(), decomList.getId());
		return subject;
	}

	private String getDecommissioningListDisplayPath() {
		StringBuilder displayPath = new StringBuilder();

		DecommissioningList decommissioningList = rm.getDecommissioningList(decommissioningListId);
		if (decommissioningList.getDecommissioningListType().isFolderList()) {
			displayPath.append(RMNavigationConfiguration.DECOMMISSIONING_LIST_DISPLAY);
		} else {
			displayPath.append(RMNavigationConfiguration.DOCUMENT_DECOMMISSIONING_LIST_DISPLAY);
		}

		displayPath.append("/");
		displayPath.append(decommissioningListId);
		return displayPath.toString();
	}

	private void writeErrorToReport(AsyncTaskExecutionParams params, String message) {
		File txtReport = null;
		OutputStream txtOuputStream = null;
		try {
			txtReport = new File(new FoldersLocator().getWorkFolder(),
					params.getBatchProcess().getId() + File.separator + "batchProcessReport.txt");
			txtOuputStream = FileUtils.openOutputStream(txtReport, true);

			BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(txtOuputStream));
			writer.write(message);
			writer.flush();

			InputStream txtInputStream = new FileInputStream(txtReport);
			ContentVersionDataSummary contentVersion = contentManager.upload(txtInputStream, txtReport.getName()).getContentVersionDataSummary();
			Content content = contentManager.createMajor(currentUser, txtReport.getName(), contentVersion);

			BatchProcessReport report = getLinkedBatchProcessReport(params.getBatchProcess(), appLayerFactory);
			report.setContent(content);
			updateBatchProcessReport(report);
		} catch (IOException e) {
			throw new RuntimeException(e);
		} finally {
			IOUtils.closeQuietly(txtOuputStream);
			FileUtils.deleteQuietly(txtReport);
		}
	}

	private void updateBatchProcessReport(BatchProcessReport report) {
		try {
			Transaction transaction = new Transaction();
			transaction.addUpdate(report.getWrappedRecord());
			recordServices.execute(transaction);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private BatchProcessReport getLinkedBatchProcessReport(BatchProcess batchProcess, AppLayerFactory appLayerFactory) {
		BatchProcessReport report = null;
		String collection = batchProcess.getCollection();
		if (collection != null) {
			User user = userServices.getUserRecordInCollection(batchProcess.getUsername(), collection);
			String userId = user != null ? user.getId() : null;
			try {
				MetadataSchema batchProcessReportSchema = schemasManager.getSchemaTypes(collection)
						.getSchema(BatchProcessReport.FULL_SCHEMA);
				Record reportRecord = searchServices.searchSingleResult(from(batchProcessReportSchema)
						.where(batchProcessReportSchema.getMetadata(BatchProcessReport.LINKED_BATCH_PROCESS))
						.isEqualTo(batchProcess.getId()));
				if (reportRecord != null) {
					report = new BatchProcessReport(reportRecord, schemasManager.getSchemaTypes(collection));
				} else {
					report = schemas.newBatchProcessReport();
					report.setLinkedBatchProcess(batchProcess.getId());
					report.setCreatedBy(userId);
				}
			} catch (Exception e) {
				report = schemas.newBatchProcessReport();
				report.setLinkedBatchProcess(batchProcess.getId());
				report.setCreatedBy(userId);
			}
		}
		return report;
	}
}
