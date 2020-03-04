package com.constellio.app.ui.pages.management.authorizations;

import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.wrappers.Document;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.ui.entities.MetadataSchemaVO;
import com.constellio.app.ui.entities.RecordVO.VIEW_MODE;
import com.constellio.app.ui.entities.UserVO;
import com.constellio.app.ui.framework.builders.MetadataSchemaToVOBuilder;
import com.constellio.app.ui.framework.builders.RecordToVOBuilder;
import com.constellio.app.ui.framework.data.RecordVODataProvider;
import com.constellio.app.ui.framework.data.ShareContentDataProvider;
import com.constellio.app.ui.pages.base.BasePresenter;
import com.constellio.model.entities.CorePermissions;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.Authorization;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.services.schemas.MetadataSchemasManager;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators;

public class ShareContentListPresenter extends BasePresenter<ShareContentListViewImpl> {

	private MetadataSchemasManager metadataSchemasManager;
	private RMSchemasRecordsServices schemasRecordsServices;


	public ShareContentListPresenter(ShareContentListViewImpl view) {
		super(view);

		this.metadataSchemasManager = modelLayerFactory.getMetadataSchemasManager();
		this.schemasRecordsServices = new RMSchemasRecordsServices(view.getCollection(), appLayerFactory);
	}


	@Override
	protected boolean hasPageAccess(String params, User user) {
		return user.has(CorePermissions.MANAGE_SHARE).globally() || user.has(CorePermissions.MANAGE_GLOBAL_LINKS).globally();
	}

	@Override
	protected boolean hasRestrictedRecordAccess(String params, User user, Record restrictedRecord) {
		return true;
	}

	protected UserVO getUserVO() {
		return view.getSessionContext().getCurrentUser();
	}

	public RecordVODataProvider getPublishedDocumentDataProvider() {
		MetadataSchemaVO documentMetadataSchemaVO = getMetadataSchemaVO(Document.DEFAULT_SCHEMA);

		return new RecordVODataProvider(documentMetadataSchemaVO, new RecordToVOBuilder(), modelLayerFactory, view.getSessionContext()) {
			@Override
			public LogicalSearchQuery getQuery() {
				return getPublishedDocument();
			}
		};
	}

	public RecordVODataProvider getSharedDocumentDataProvider() {

		MetadataSchemaVO documentMetadataSchemaVO = getMetadataSchemaVO(Document.DEFAULT_SCHEMA);

		return new ShareContentDataProvider(documentMetadataSchemaVO, modelLayerFactory, view.getSessionContext()) {
			@Override
			public LogicalSearchQuery getAuthorizationQuery() {
				return getSharedAuthorization(Document.SCHEMA_TYPE);
			}
		};
	}

	public RecordVODataProvider getSharedFolderDataProvider() {
		MetadataSchemaVO folderMetadataSchemaVO = getMetadataSchemaVO(Folder.DEFAULT_SCHEMA);

		return new ShareContentDataProvider(folderMetadataSchemaVO, modelLayerFactory, view.getSessionContext()) {
			@Override
			public LogicalSearchQuery getAuthorizationQuery() {
				return getSharedAuthorization(Folder.SCHEMA_TYPE);
			}
		};
	}

	private MetadataSchemaVO getMetadataSchemaVO(String schema) {
		MetadataSchema folderMetadataSchema = this.metadataSchemasManager.getSchemaTypes(collection).getSchema(schema);
		return new MetadataSchemaToVOBuilder().build(folderMetadataSchema, VIEW_MODE.TABLE, view.getSessionContext());
	}

	private LogicalSearchQuery getSharedAuthorization(String schemaType) {
		return new LogicalSearchQuery((LogicalSearchQueryOperators.from(schemasRecordsServices.authorizationDetails.schemaType())
				.where(schemasRecordsServices.authorizationDetails.schema().getMetadata(Authorization.SHARED_BY)).isNotNull()
				.andWhere(schemasRecordsServices.authorizationDetails.targetSchemaType()).isEqualTo(schemaType)));
	}

	private LogicalSearchQuery getPublishedDocument() {
		return new LogicalSearchQuery((LogicalSearchQueryOperators.from(schemasRecordsServices.document.schemaType()))
				.where(schemasRecordsServices.document.published()).isTrue());
	}
}