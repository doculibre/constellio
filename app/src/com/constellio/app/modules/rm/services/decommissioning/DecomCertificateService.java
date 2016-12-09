package com.constellio.app.modules.rm.services.decommissioning;

import static com.constellio.app.ui.i18n.i18n.$;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.IOUtils;

import com.constellio.app.extensions.AppLayerCollectionExtensions;
import com.constellio.app.modules.rm.ConstellioRMModule;
import com.constellio.app.modules.rm.extensions.api.RMModuleExtensions;
import com.constellio.app.modules.rm.extensions.api.reports.RMReportBuilderFactories;
import com.constellio.app.modules.rm.reports.builders.decommissioning.builders.DocumentToDocumentCertificate;
import com.constellio.app.modules.rm.reports.builders.decommissioning.builders.FolderToFolderCertificate;
import com.constellio.app.modules.rm.reports.model.decommissioning.DocumentsCertificateReportModel;
import com.constellio.app.modules.rm.reports.model.decommissioning.DocumentsCertificateReportModel.DocumentsCertificateReportModel_Document;
import com.constellio.app.modules.rm.reports.model.decommissioning.FoldersCertificateReportModel;
import com.constellio.app.modules.rm.reports.model.decommissioning.FoldersCertificateReportModel.FoldersCertificateReportModel_Folder;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.wrappers.DecommissioningList;
import com.constellio.app.modules.rm.wrappers.Document;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.services.factories.ConstellioFactories;
import com.constellio.app.ui.framework.reports.NewReportWriterFactory;
import com.constellio.app.ui.framework.reports.ReportWriter;
import com.constellio.data.io.services.facades.FileService;
import com.constellio.data.utils.TimeProvider;
import com.constellio.model.entities.records.Content;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.services.contents.ContentManager;
import com.constellio.model.services.contents.ContentVersionDataSummary;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.constellio.model.services.search.query.logical.condition.LogicalSearchCondition;

public class DecomCertificateService {
	private static final String DOCUMENTS_CERTIFICATE = "DecomCertificateService.documentsCertificate";
	private static final String FOLDERS_CERTIFICATE = "DecomCertificateService.folderCertificate";
	final RMSchemasRecordsServices rm;
	final DecommissioningList decommissioningList;
	final ContentManager contentManager;
	final SearchServices searchServices;
	final FileService fileService;
	private final User user;
	boolean contentsProcessed = false;
	Content documentsContent, foldersContent;

	public DecomCertificateService(RMSchemasRecordsServices rm,
			SearchServices searchServices, ContentManager contentManager, FileService fileService,
			User user, DecommissioningList decommissioningList) {
		this.rm = rm;
		this.decommissioningList = decommissioningList;
		this.searchServices = searchServices;
		this.contentManager = contentManager;
		this.fileService = fileService;
		this.user = user;
	}

	public void computeContents() {
		if (!contentsProcessed) {
			contentsProcessed = true;
			DocumentsCertificateReportModel_Elements elements = computeListElements();
			this.documentsContent = buildDocumentsContent(elements.getDocuments());
			this.foldersContent = buildFoldersContent(elements.getFolders());
		}
	}

	AppLayerFactory appLayerFactory() {
		//TODO Remove this bad singleton!
		return ConstellioFactories.getInstance().getAppLayerFactory();
	}

	RMReportBuilderFactories reportBuilderFactories() {
		AppLayerCollectionExtensions extensions = appLayerFactory().getExtensions().forCollection(rm.getCollection());
		RMModuleExtensions rmModuleExtensions = extensions.forModule(ConstellioRMModule.ID);
		return rmModuleExtensions.getReportBuilderFactories();
	}

	Content buildFoldersContent(List<FoldersCertificateReportModel_Folder> folders) {
		FoldersCertificateReportModel reportModel = new FoldersCertificateReportModel();
		reportModel.setDate(TimeProvider.getLocalDate()).setFolders(folders);

		NewReportWriterFactory<FolderDecommissioningCertificateParams> factory = reportBuilderFactories().folderDecommissioningCertificateFactory
				.getValue();

		if (factory != null) {
			return getContent(factory.getReportBuilder(new FolderDecommissioningCertificateParams(reportModel)),
					$(FOLDERS_CERTIFICATE));
		} else {
			return null;
		}

	}

	Content buildDocumentsContent(List<DocumentsCertificateReportModel_Document> documents) {
		DocumentsCertificateReportModel reportModel = new DocumentsCertificateReportModel();
		reportModel.setDate(TimeProvider.getLocalDate()).setDocuments(documents);

		NewReportWriterFactory<DocumentDecommissioningCertificateParams> factory = reportBuilderFactories().documentDecommissioningCertificateFactory
				.getValue();

		if (factory != null) {
			return getContent(factory.getReportBuilder(new DocumentDecommissioningCertificateParams(reportModel)),
					$(DOCUMENTS_CERTIFICATE));
		} else {
			return null;
		}
	}

	Content getContent(ReportWriter builder, String filename) {
		File tempFile = fileService.newTemporaryFile(filename);
		OutputStream outputStream = null;
		InputStream inputStream = null;
		try {
			outputStream = new FileOutputStream(tempFile);
			builder.write(outputStream);
			inputStream = new FileInputStream(tempFile);
			ContentVersionDataSummary contentVersion = contentManager.upload(inputStream, filename);
			Content content = contentManager.createMajor(user, filename, contentVersion);
			fileService.deleteQuietly(tempFile);
			return content;
		} catch (IOException e) {
			throw new RuntimeException(e);
		} finally {
			IOUtils.closeQuietly(inputStream);
			IOUtils.closeQuietly(outputStream);
		}
	}

	//FIXME test it!
	DocumentsCertificateReportModel_Elements computeListElements() {
		if (decommissioningList.getFolders() != null && !decommissioningList.getFolders().isEmpty()) {
			return computeListElements(getFolders(decommissioningList.getFolders()));
		} else if (decommissioningList.getDocuments() != null) {
			return new DocumentsCertificateReportModel_Elements()
					.addAllDocuments(rm, getDocuments(decommissioningList.getDocuments()));
		} else {
			return new DocumentsCertificateReportModel_Elements();
		}
	}

	private List<Document> getDocuments(List<String> documents) {
		List<Record> foldersRecords = rm.get(documents);
		return rm.wrapDocuments(foldersRecords);
	}

	List<Folder> getFolders(List<String> foldersIds) {
		List<Record> foldersRecords = rm.get(foldersIds);
		return rm.wrapFolders(foldersRecords);
	}

	DocumentsCertificateReportModel_Elements computeListElements(List<Folder> folders) {
		DocumentsCertificateReportModel_Elements returnElements = new DocumentsCertificateReportModel_Elements();
		if (folders != null) {
			returnElements.addAllFolders(rm, folders);
			for (Folder folder : folders) {
				List<Document> currentDocuments = getDirectDocuments(folder);
				returnElements.addAllDocuments(rm, currentDocuments);
				List<Folder> subFolders = getDirectSubFolders(folder);
				DocumentsCertificateReportModel_Elements elements = computeListElements(subFolders);
				returnElements.addElementsOf(elements);
			}
		}
		return returnElements;
	}

	private List<Folder> getDirectSubFolders(Folder folder) {
		LogicalSearchCondition query = from(rm.folder.schemaType()).where(rm.folder.parentFolder()).isEqualTo(folder.getId());
		List<Record> records = searchServices.search(new LogicalSearchQuery(query));
		return rm.wrapFolders(records);
	}

	private List<Document> getDirectDocuments(Folder folder) {
		LogicalSearchCondition query = from(rm.documentSchemaType()).where(rm.documentFolder()).isEqualTo(folder.getId());
		List<Record> records = searchServices.search(new LogicalSearchQuery(query));
		return rm.wrapDocuments(records);
	}

	public Content getDocumentsContent() {
		if (!contentsProcessed) {
			computeContents();
		}
		return documentsContent;
	}

	public Content getFoldersContent() {
		if (!contentsProcessed) {
			computeContents();
		}
		return foldersContent;
	}

	public static class DocumentsCertificateReportModel_Elements {
		List<DocumentsCertificateReportModel_Document> documents = new ArrayList<>();
		List<FoldersCertificateReportModel_Folder> folders = new ArrayList<>();

		public DocumentsCertificateReportModel_Elements addAllDocuments(RMSchemasRecordsServices rm, List<Document> documents) {
			if (documents != null) {
				DocumentToDocumentCertificate builder = new DocumentToDocumentCertificate(rm);
				for (Document document : documents) {
					this.documents.add(builder.toReportDocument(document));
				}
			}
			return this;
		}

		public DocumentsCertificateReportModel_Elements addAllFolders(RMSchemasRecordsServices rm, List<Folder> folders) {
			if (folders != null) {
				FolderToFolderCertificate builder = new FolderToFolderCertificate(rm);
				for (Folder folder : folders) {
					this.folders.add(builder.toReportFolder(folder));
				}
			}
			return this;
		}

		public List<DocumentsCertificateReportModel_Document> getDocuments() {
			return documents;
		}

		public List<FoldersCertificateReportModel_Folder> getFolders() {
			return folders;
		}

		public void addElementsOf(DocumentsCertificateReportModel_Elements elements) {
			this.documents.addAll(elements.getDocuments());
			this.folders.addAll(elements.getFolders());
		}
	}
}
