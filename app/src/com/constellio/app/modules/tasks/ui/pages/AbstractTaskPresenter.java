package com.constellio.app.modules.tasks.ui.pages;

import com.constellio.app.modules.es.model.connectors.smb.ConnectorSmbDocument;
import com.constellio.app.modules.rm.RMConfigs;
import com.constellio.app.modules.rm.navigation.RMNavigationConfiguration;
import com.constellio.app.modules.rm.navigation.RMViews;
import com.constellio.app.modules.rm.services.EmailParsingServices;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.ui.builders.DocumentToVOBuilder;
import com.constellio.app.modules.rm.ui.components.content.ConstellioAgentClickHandler;
import com.constellio.app.modules.rm.ui.entities.DocumentVO;
import com.constellio.app.modules.rm.ui.pages.folder.DisplayFolderPresenter;
import com.constellio.app.modules.rm.ui.util.ConstellioAgentUtils;
import com.constellio.app.modules.rm.wrappers.ContainerRecord;
import com.constellio.app.modules.rm.wrappers.Document;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.modules.rm.wrappers.RMTask;
import com.constellio.app.modules.rm.wrappers.structures.Comment;
import com.constellio.app.modules.tasks.data.trees.TaskFoldersTreeNodesDataProvider;
import com.constellio.app.modules.tasks.model.wrappers.Task;
import com.constellio.app.modules.tasks.ui.components.TaskTable.TaskPresenter;
import com.constellio.app.services.factories.ConstellioFactories;
import com.constellio.app.ui.entities.ContentVersionVO;
import com.constellio.app.ui.entities.ContentVersionVO.InputStreamProvider;
import com.constellio.app.ui.entities.MetadataVO;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.entities.RecordVO.VIEW_MODE;
import com.constellio.app.ui.framework.builders.RecordToVOBuilder;
import com.constellio.app.ui.framework.data.BaseRecordTreeDataProvider;
import com.constellio.app.ui.pages.base.BaseView;
import com.constellio.app.ui.pages.base.SchemaPresenterUtils;
import com.constellio.app.ui.pages.base.SessionContext;
import com.constellio.app.ui.pages.base.SingleSchemaBasePresenter;
import com.constellio.data.dao.dto.records.RecordsFlushing;
import com.constellio.model.entities.records.Content;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.entities.schemas.MetadataSchemaType;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.extensions.ModelLayerCollectionExtensions;
import com.constellio.model.services.configs.SystemConfigurationsManager;
import com.constellio.model.services.contents.ContentFactory;
import com.constellio.model.services.contents.icap.IcapException;
import com.constellio.model.services.migrations.ConstellioEIMConfigs;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.records.RecordServicesException;
import com.constellio.model.services.records.RecordServicesRuntimeException.NoSuchRecordWithId;
import com.constellio.model.services.schemas.SchemaUtils;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators;
import com.constellio.model.services.search.query.logical.condition.LogicalSearchCondition;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.LocalDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static com.constellio.app.ui.i18n.i18n.$;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;

public abstract class AbstractTaskPresenter<T extends BaseView> extends SingleSchemaBasePresenter<T> implements TaskPresenter {

	private static Logger LOGGER = LoggerFactory.getLogger(AbstractTaskPresenter.class);

	public AbstractTaskPresenter(T view) {
		super(view);
	}

	public AbstractTaskPresenter(T view, String schemaCode) {
		super(view, schemaCode);
	}

	public AbstractTaskPresenter(T view, String schemaCode, ConstellioFactories constellioFactories,
			SessionContext sessionContext) {
		super(view, schemaCode, constellioFactories, sessionContext);
	}
	
	@Override
	public RecordVO reloadRequested(RecordVO taskVO) {
		Record taskRecord = schemaPresenterUtils.getRecord(taskVO.getId());
		return new RecordToVOBuilder().build(taskRecord, VIEW_MODE.DISPLAY, view.getSessionContext());
	}
	
	@Override
	public BaseRecordTreeDataProvider getTaskFoldersTreeDataProvider(RecordVO taskVO) {
		SessionContext sessionContext = view.getSessionContext();
		TaskFoldersTreeNodesDataProvider taskFoldersDataProvider = new TaskFoldersTreeNodesDataProvider(taskVO.getRecord(), appLayerFactory, sessionContext);
		return new BaseRecordTreeDataProvider(taskFoldersDataProvider);
	}
	
	@Override
	public boolean taskFolderOrDocumentClicked(RecordVO taskVO, String id) {
		boolean navigating = false;
		if (id != null && !id.startsWith("dummy")) {
			try {
				RecordServices recordServices = modelLayerFactory.newRecordServices();
				
				Record record = recordServices.getDocumentById(id);
				String collection = record.getCollection();
				MetadataSchemaTypes types = modelLayerFactory.getMetadataSchemasManager().getSchemaTypes(collection);
				String schemaCode = record.getSchemaCode();
				String schemaTypeCode = SchemaUtils.getSchemaTypeCode(schemaCode);
				if (Folder.SCHEMA_TYPE.equals(schemaTypeCode)) {
					view.navigate().to(RMViews.class).displayFolder(id);
					navigating = true;
				} else if (Document.SCHEMA_TYPE.equals(schemaTypeCode)) {
					SessionContext sessionContext = view.getSessionContext();
					RecordVO documentVO = new RecordToVOBuilder().build(record, VIEW_MODE.DISPLAY, sessionContext);
					ContentVersionVO contentVersionVO = documentVO.get(Document.CONTENT);
					if (contentVersionVO == null) {
						view.navigate().to(RMViews.class).displayDocument(id);
						navigating = true;
					}
					String agentURL = ConstellioAgentUtils.getAgentURL(documentVO, contentVersionVO);
					if (agentURL != null) {
						//			view.openAgentURL(agentURL);
						new ConstellioAgentClickHandler().handleClick(agentURL, documentVO, contentVersionVO, new HashMap<String, String>());
						navigating = false;
					} else {
						view.navigate().to(RMViews.class).displayDocument(id);
						navigating = true;
					}
				} else if (ContainerRecord.SCHEMA_TYPE.equals(schemaTypeCode)) {
					view.navigate().to(RMViews.class).displayContainer(id);
					navigating = true;
				} else if (ConstellioAgentUtils.isAgentSupported()) {
					String smbMetadataCode;
					if (ConnectorSmbDocument.SCHEMA_TYPE.equals(schemaTypeCode)) {
						smbMetadataCode = ConnectorSmbDocument.URL;
						//					} else if (ConnectorSmbFolder.SCHEMA_TYPE.equals(schemaTypeCode)) {
						//						smbMetadataCode = ConnectorSmbFolder.URL;
					} else {
						smbMetadataCode = null;
					}
					if (smbMetadataCode != null) {
						SystemConfigurationsManager systemConfigurationsManager = modelLayerFactory
								.getSystemConfigurationsManager();
						RMConfigs rmConfigs = new RMConfigs(systemConfigurationsManager);
						if (rmConfigs.isAgentEnabled()) {
							RecordVO recordVO = new RecordToVOBuilder().build(record, VIEW_MODE.DISPLAY, view.getSessionContext());
							MetadataVO smbPathMetadata = recordVO.getMetadata(schemaTypeCode + "_default_" + smbMetadataCode);
							String agentSmbPath = ConstellioAgentUtils.getAgentSmbURL(recordVO, smbPathMetadata);
							view.openURL(agentSmbPath);
						} else {
							Metadata smbUrlMetadata = types.getMetadata(schemaTypeCode + "_default_" + smbMetadataCode);
							String smbPath = record.get(smbUrlMetadata);
							String path = smbPath;
							if (StringUtils.startsWith(path, "smb://")) {
								path = "file://" + StringUtils.removeStart(path, "smb://");
							}
							view.openURL(path);
						}
						navigating = true;
					}
				}
			} catch (NoSuchRecordWithId e) {
				view.showErrorMessage($("TaskTable.noSuchRecord"));
				LOGGER.warn("Error while clicking on record id " + id, e);
				navigating = false;
			}
		}
		return navigating;
	}
	
	@Override
	public boolean taskCommentAdded(RecordVO taskVO, Comment newComment) {
		boolean added;
		SessionContext sessionContext = view.getSessionContext();
		RecordServices recordServices = modelLayerFactory.newRecordServices();
		String collection = sessionContext.getCurrentCollection();
		RMSchemasRecordsServices rm = new RMSchemasRecordsServices(collection, appLayerFactory);
		User currentUser = rm.getUser(sessionContext.getCurrentUser().getId());
		
		newComment.setDateTime(new LocalDateTime());
		newComment.setUser(currentUser);
		
		Task task = rm.getRMTask(taskVO.getId());
		List<Comment> newComments = new ArrayList<>(task.getComments());
		newComments.add(newComment);
		task.setComments(newComments);
		try {
			recordServices.update(task.getWrappedRecord());
			added = true;
		} catch (RecordServicesException e) {
			added = false;
			LOGGER.error("Error while adding a comment", e);
			view.showErrorMessage(e.getMessage());
		}
		return added;
	}

	@Override
	public RecordVO getDocumentVO(String linkedDocumentId) {
		Record record = getRecord(linkedDocumentId);
		return new RecordToVOBuilder().build(record, VIEW_MODE.DISPLAY, view.getSessionContext());
	}

	public boolean userHasPermissionOn(RecordVO recordVO) {
		return getCurrentUser().hasReadAccess().on(recordVO.getRecord());
	}

	private boolean documentExists(String fileName, Folder folder) {
		RMSchemasRecordsServices rm = new RMSchemasRecordsServices(collection, appLayerFactory);
		Record record = folder.getWrappedRecord();

		MetadataSchemaType documentsSchemaType = rm.documentSchemaType();
		MetadataSchema documentsSchema = rm.defaultDocumentSchema();
		Metadata folderMetadata = documentsSchema.getMetadata(Document.FOLDER);
		Metadata titleMetadata = documentsSchema.getMetadata(Schemas.TITLE.getCode());
		LogicalSearchQuery query = new LogicalSearchQuery();
		LogicalSearchCondition parentCondition = from(documentsSchemaType).where(folderMetadata).is(record)
				.andWhere(Schemas.LOGICALLY_DELETED_STATUS).isFalseOrNull();
		query.setCondition(parentCondition.andWhere(titleMetadata).is(fileName));

		SearchServices searchServices = modelLayerFactory.newSearchServices();
		return searchServices.query(query).getNumFound() > 0;
	}

	protected Object contentVersionUploaded(RecordVO taskVO, ContentVersionVO uploadedContentVO, String folderId) {
		Object result;
		
		RMSchemasRecordsServices rm = new RMSchemasRecordsServices(collection, appLayerFactory);
		ModelLayerCollectionExtensions extensions = modelLayerFactory.getExtensions().forCollection(collection);
		try {
			if (folderId != null) {
				Folder folder = rm.getFolder(folderId);
				String fileName = uploadedContentVO.getFileName();
				if (!documentExists(fileName, folder) && !extensions.isModifyBlocked(folder.getWrappedRecord(), getCurrentUser())) {
					if (Boolean.TRUE.equals(uploadedContentVO.hasFoundDuplicate())) {
						LogicalSearchQuery duplicateDocumentsQuery = new LogicalSearchQuery()
								.setCondition(LogicalSearchQueryOperators.from(rm.documentSchemaType())
										.where(rm.document.content()).is(ContentFactory.isHash(uploadedContentVO.getDuplicatedHash()))
										.andWhere(Schemas.LOGICALLY_DELETED_STATUS).isFalseOrNull()
								)
								.filteredWithUser(getCurrentUser());
						List<Document> duplicateDocuments = rm.searchDocuments(duplicateDocumentsQuery);
						if (duplicateDocuments.size() > 0) {
							StringBuilder message = new StringBuilder(
									$("ContentManager.hasFoundDuplicateWithConfirmation", StringUtils.defaultIfBlank(fileName, "")));
							message.append("<br>");
							for (Document document : duplicateDocuments) {
								message.append("<br>-");
								message.append(document.getTitle());
								message.append(": ");
								message.append(generateDisplayLink(document));
							}
							view.showClickableMessage(message.toString());
						}
					}
					uploadedContentVO.setMajorVersion(true);
					Record newRecord;
					if (rm.isEmail(fileName)) {
						InputStreamProvider inputStreamProvider = uploadedContentVO.getInputStreamProvider();
						InputStream in = inputStreamProvider.getInputStream(DisplayFolderPresenter.class + ".contentVersionUploaded");
						Document document = new EmailParsingServices(rm).newEmail(fileName, in);
						newRecord = document.getWrappedRecord();
					} else {
						Document document = rm.newDocument();
						newRecord = document.getWrappedRecord();
					}
					DocumentVO documentVO = new DocumentToVOBuilder(modelLayerFactory).build(newRecord, VIEW_MODE.FORM, view.getSessionContext());
					documentVO.setFolder(folderId);
					documentVO.setTitle(fileName);
					documentVO.setContent(uploadedContentVO);
					
					String schemaCode = newRecord.getSchemaCode();
					ConstellioFactories constellioFactories = view.getConstellioFactories();
					SessionContext sessionContext = view.getSessionContext();
					SchemaPresenterUtils documentPresenterUtils = new SchemaPresenterUtils(schemaCode, constellioFactories,
							sessionContext);
					newRecord = documentPresenterUtils.toRecord(documentVO);

					documentPresenterUtils.addOrUpdate(newRecord);
					//				view.selectFolderContentTab();
					result = newRecord;
				} else {
					result = null;
				}
			} else {
				result = toContent(null, null, uploadedContentVO);
			}
		} catch (final IcapException e) {
			view.showErrorMessage(e.getMessage());
			result = null;
		} catch (Exception e) {
			LOGGER.error(e.getMessage(), e);
			result = null;
		}
		return result;
	}

	String generateDisplayLink(Document document) {
		ConstellioEIMConfigs eimConfigs = new ConstellioEIMConfigs(modelLayerFactory.getSystemConfigurationsManager());
		String constellioUrl = eimConfigs.getConstellioUrl();
		String displayURL = RMNavigationConfiguration.DISPLAY_DOCUMENT;
		String url = constellioUrl + "#!" + displayURL + "/" + document.getId();
		return "<a href=\"" + url + "\">" + url + "</a>";
	}

	@Override
	public List<String> addDocumentsButtonClicked(RecordVO taskVO, List<ContentVersionVO> contentVersionVOs, String folderId /*, LazyTreeDataProvider<String> treeDataProvider*/) {
		List<String> newDocumentRecordIds = new ArrayList<>();
		List<Content> newContents = new ArrayList<>();
		
		for (ContentVersionVO contentVersionVO : contentVersionVOs) {
			Object uploadResult = contentVersionUploaded(taskVO, contentVersionVO, folderId);
			if (uploadResult instanceof Record) {
				newDocumentRecordIds.add(((Record) uploadResult).getId());
			} else if (uploadResult instanceof Content) {
				newContents.add((Content) uploadResult);
			}
		}
		
		Task task = (Task) getTask(taskVO);
		Metadata linkedDocumentsMetadata = getMetadata(RMTask.LINKED_DOCUMENTS);
		Metadata contentsMetadata = getMetadata(RMTask.CONTENTS);
		
		List<Object> linkedDocumentIds = new ArrayList<>(task.getWrappedRecord().getList(linkedDocumentsMetadata));
		for (String newDocumentRecordId : newDocumentRecordIds) {
			linkedDocumentIds.add(newDocumentRecordId);
		}
		List<Object> contents = new ArrayList<>(task.getWrappedRecord().getList(contentsMetadata));
		contents.addAll(newContents);
		
		task.set(linkedDocumentsMetadata, linkedDocumentIds);
		task.set(contentsMetadata, contents);
		addOrUpdate(task.getWrappedRecord(), RecordsFlushing.NOW);
//		treeDataProvider.fireDataRefreshEvent();
		return newDocumentRecordIds;
	}

}
