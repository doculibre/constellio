package com.constellio.app.modules.rm.reports.model.decommissioning;

import com.constellio.app.extensions.AppLayerCollectionExtensions;
import com.constellio.app.extensions.AppLayerSystemExtensions;
import com.constellio.app.modules.rm.reports.builders.decommissioning.DecommissioningListXLSDetailedReportParameters;
import com.constellio.app.modules.rm.reports.model.excel.BaseExcelReportPresenter;
import com.constellio.app.modules.rm.reports.model.search.NoSuchReportRuntimeException;
import com.constellio.app.modules.rm.reports.model.search.UnsupportedReportException;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.wrappers.DecommissioningList;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.modules.rm.wrappers.structures.Comment;
import com.constellio.app.modules.rm.wrappers.structures.DecomListFolderDetail;
import com.constellio.app.modules.rm.wrappers.structures.DecomListValidation;
import com.constellio.app.modules.rm.wrappers.structures.FolderDetailWithType;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.model.entities.Language;
import com.constellio.model.entities.records.wrappers.Report;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.records.wrappers.structure.ReportedMetadata;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchemaType;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.reports.ReportServices;
import com.constellio.model.services.schemas.MetadataList;
import com.constellio.model.services.schemas.MetadataSchemasManager;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

import static com.constellio.app.ui.i18n.i18n.$;

public class DecommissioningListXLSDetailedReportPresenter extends BaseExcelReportPresenter {
	private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(DecommissioningListXLSDetailedReportPresenter.class);

	protected transient ModelLayerFactory modelLayerFactory;
	protected transient AppLayerCollectionExtensions appCollectionExtentions;
	protected transient AppLayerSystemExtensions appSystemExtentions;
	private String decommissioningListId;
	private String reportTitle;
	private String collection;
	private String username;
	private String schemaTypeCode;
	private User userInCollection;

	public DecommissioningListXLSDetailedReportPresenter(AppLayerFactory appLayerFactory, Locale locale, String collection,
														 DecommissioningListXLSDetailedReportParameters parameters) {
		super(appLayerFactory, locale, collection);
		this.modelLayerFactory = appLayerFactory.getModelLayerFactory();
		this.appCollectionExtentions = appLayerFactory.getExtensions().forCollection(collection);
		this.appSystemExtentions = appLayerFactory.getExtensions().getSystemWideExtensions();
		this.decommissioningListId = parameters.getDecommissioningListId();
		this.reportTitle = parameters.getReportTitle();
		this.collection = parameters.getCollection();
		this.username = parameters.getUsername();
		this.schemaTypeCode = parameters.getSchemaType();
		this.userInCollection = appLayerFactory.getModelLayerFactory().newUserServices().getUserInCollection(username, collection);
	}

	public DecommissioningListXLSDetailedReportModel build() {
		DecommissioningListXLSDetailedReportModel model = new DecommissioningListXLSDetailedReportModel();

		buildHeader(model);
		buildComments(model);
		buildValidations(model);
		buildFolders(model);

		return model;
	}

	private void buildHeader(DecommissioningListXLSDetailedReportModel model) {
		RMSchemasRecordsServices rmSchemasRecordsServices = new RMSchemasRecordsServices(collection, appLayerFactory);

		List<Metadata> requestedMetadataList = new ArrayList<>();
		requestedMetadataList.add(rmSchemasRecordsServices.decommissioningList.title());
		requestedMetadataList.add(rmSchemasRecordsServices.decommissioningList.type());
		requestedMetadataList.add(rmSchemasRecordsServices.decommissioningList.description());
		requestedMetadataList.add(rmSchemasRecordsServices.decommissioningList.administrativeUnit());
		requestedMetadataList.add(rmSchemasRecordsServices.decommissioningList.createdOn());
		requestedMetadataList.add(rmSchemasRecordsServices.decommissioningList.createdBy());
		requestedMetadataList.add(rmSchemasRecordsServices.decommissioningList.modifiedOn());
		requestedMetadataList.add(rmSchemasRecordsServices.decommissioningList.status());
		requestedMetadataList.add(rmSchemasRecordsServices.decommissioningList.approvalDate());
		requestedMetadataList.add(rmSchemasRecordsServices.decommissioningList.approvalUser());

		List<String> metadataCodes = requestedMetadataList.stream().map(Metadata::getLocalCode).collect(Collectors.toList());
		List<Metadata> headerMetadataList = getMetadataList(metadataCodes, schemaTypeCode);
		for (Metadata metadata : headerMetadataList) {
			model.addHeaderTitle(metadata.getLabel(Language.withCode(locale.getLanguage())));
		}

		DecommissioningList decommissioningList = rmSchemasRecordsServices.getDecommissioningList(decommissioningListId);
		model.setHeader(getRecordLine(decommissioningList.getWrappedRecord(), headerMetadataList));
	}

	private void buildComments(DecommissioningListXLSDetailedReportModel model) {
		model.addCommentTitle($("DecommissioningListDetailedReport.commentAuthor"));
		model.addCommentTitle($("DecommissioningListDetailedReport.commentDate"));
		model.addCommentTitle($("DecommissioningListDetailedReport.commentMessage"));

		RMSchemasRecordsServices rmSchemasRecordsServices = new RMSchemasRecordsServices(collection, appLayerFactory);
		DecommissioningList decommissioningList = rmSchemasRecordsServices.getDecommissioningList(decommissioningListId);

		for (Comment comment : decommissioningList.getComments()) {
			User user = rmSchemasRecordsServices.getUser(comment.getUserId());
			model.addComment(Arrays.asList(user.getTitle(), comment.getDateTime(), comment.getMessage()));
		}
	}

	private void buildValidations(DecommissioningListXLSDetailedReportModel model) {
		model.addValidationTitle($("DecommissioningListDetailedReport.validationUser"));
		model.addValidationTitle($("DecommissioningListDetailedReport.validationRequestDate"));
		model.addValidationTitle($("DecommissioningListDetailedReport.validationIsValidated"));
		model.addValidationTitle($("DecommissioningListDetailedReport.validationDate"));

		RMSchemasRecordsServices rmSchemasRecordsServices = new RMSchemasRecordsServices(collection, appLayerFactory);
		DecommissioningList decommissioningList = rmSchemasRecordsServices.getDecommissioningList(decommissioningListId);

		for (DecomListValidation validation : decommissioningList.getValidations()) {
			User user = rmSchemasRecordsServices.getUser(validation.getUserId());
			String isValidated = validation.isValidated() ? $("yes") : $("no");
			model.addValidation(Arrays.asList(user.getTitle(), validation.getRequestDate(), isValidated, validation.getValidationDate()));
		}
	}

	private void buildFolders(DecommissioningListXLSDetailedReportModel model) {
		RMSchemasRecordsServices rmSchemasRecordsServices = new RMSchemasRecordsServices(collection, appLayerFactory);

		ReportServices reportServices = new ReportServices(modelLayerFactory, collection);
		List<ReportedMetadata> requestedMetadataList = new ArrayList<>(getReportedMetadataList(reportServices));
		orderByPosition(requestedMetadataList);

		List<String> metadataCodes = requestedMetadataList.stream().map(ReportedMetadata::getMetadataLocaleCode).collect(Collectors.toList());
		List<Metadata> folderMetadataList = getMetadataList(metadataCodes, rmSchemasRecordsServices.folderSchemaType().getCode());
		for (Metadata metadata : folderMetadataList) {
			model.addFolderTitle(metadata.getLabel(Language.withCode(locale.getLanguage())));
		}

		DecommissioningList decommissioningList = rmSchemasRecordsServices.getDecommissioningList(decommissioningListId);

		List<String> includedFolders = new ArrayList<>();
		List<String> excludedFolders = new ArrayList<>();
		Map<String, FolderDetailWithType> folderDetails = new HashMap<>();

		for (FolderDetailWithType folder : decommissioningList.getFolderDetailsWithType()) {
			if (folder.isIncluded()) {
				folderDetails.put(folder.getFolderId(), folder);
				includedFolders.add(folder.getFolderId());
			} else {
				excludedFolders.add(folder.getFolderId());
			}
		}

		List<Folder> folders = new ArrayList<>(rmSchemasRecordsServices.getFolders(includedFolders));
		for (Folder folder : folders) {
			FolderDetailWithType folderDetailWithType = folderDetails.get(folder.getId());
			DecomListFolderDetail detail = folderDetailWithType.getDetail();

			folder.setLinearSize(detail.getFolderLinearSize());
			folder.setContainer(detail.getContainerRecordId());

			model.addFolder(getRecordLine(folder.getWrappedRecord(), folderMetadataList));
		}

		folders = new ArrayList<>(rmSchemasRecordsServices.getFolders(excludedFolders));
		for (Folder folder : folders) {
			model.addExclusion(getRecordLine(folder.getWrappedRecord(), folderMetadataList));
		}
	}

	private List<Metadata> getMetadataList(List<String> requestedMetadataCodes, String schemaTypeCode) {
		List<Metadata> returnList = new ArrayList<>();
		MetadataSchemasManager metadataSchemasManager = modelLayerFactory.getMetadataSchemasManager();
		MetadataSchemaType schemaType = metadataSchemasManager.getSchemaTypes(collection).getSchemaType(schemaTypeCode);
		MetadataList allMetadata = schemaType.getAllMetadatas().onlyAccessibleGloballyBy(userInCollection);

		for (String requestedMetadataCode : requestedMetadataCodes) {
			boolean found = false;
			for (Metadata metadata : allMetadata) {
				if (metadata.getLocalCode().equals(requestedMetadataCode)) {
					returnList.add(metadata);
					found = true;
					break;
				}
			}
			if (!found) {
				LOGGER.warn("Could not find requested metadata: " + requestedMetadataCode);
			}
		}

		return returnList;
	}

	private void orderByPosition(List<ReportedMetadata> reportedMetadataList) {
		Collections.sort(reportedMetadataList, new Comparator<ReportedMetadata>() {
			@Override
			public int compare(ReportedMetadata o1, ReportedMetadata o2) {
				return o1.getXPosition() - o2.getXPosition();
			}
		});
	}

	private List<ReportedMetadata> getReportedMetadataList(ReportServices reportServices) {
		Report report = reportServices.getUserReport(username, schemaTypeCode, reportTitle);
		if (report == null) {
			report = reportServices.getReport(Folder.SCHEMA_TYPE, reportTitle);
		}
		if (report == null) {
			String username = null;
			if (this.username != null) {
				username = this.username;
			}
			throw new NoSuchReportRuntimeException(username, Folder.SCHEMA_TYPE, reportTitle);
		}
		if (report.getLinesCount() != 1) {
			throw new UnsupportedReportException();
		}
		return report.getReportedMetadata();
	}

	public Locale getLocale() {
		return locale;
	}
}
