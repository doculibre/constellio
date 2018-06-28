package com.constellio.app.modules.rm.extensions;

import com.constellio.app.api.extensions.SearchPageExtension;
import com.constellio.app.api.extensions.params.GetSearchResultSimpleTableWindowComponentParam;
import com.constellio.app.api.extensions.taxonomies.GetCustomResultDisplayParam;
import com.constellio.app.api.extensions.taxonomies.UserSearchEvent;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.ui.components.DocumentSearchResultDisplay;
import com.constellio.app.modules.rm.ui.pages.containers.DisplayContainerPresenter;
import com.constellio.app.modules.rm.ui.pages.containers.DisplayContainerViewImpl;
import com.constellio.app.modules.rm.ui.pages.document.DisplayDocumentViewImpl;
import com.constellio.app.modules.rm.ui.pages.folder.DisplayFolderViewImpl;
import com.constellio.app.modules.rm.wrappers.ContainerRecord;
import com.constellio.app.modules.rm.wrappers.Document;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.framework.components.SearchResultDisplay;
import com.constellio.model.services.records.RecordServices;
import com.vaadin.ui.Component;

public class RMSearchPageExtension extends SearchPageExtension {

	AppLayerFactory appLayerFactory;
	String collection;
	RMSchemasRecordsServices rm;

	public RMSearchPageExtension(String collection, AppLayerFactory appLayerFactory) {
		this.appLayerFactory = appLayerFactory;
		this.collection = collection;
		this.rm = new RMSchemasRecordsServices(collection, appLayerFactory);
	}

	@Override
	public SearchResultDisplay getCustomResultDisplayFor(GetCustomResultDisplayParam param) {
		if (param.getSchemaType().equals(Document.SCHEMA_TYPE)) {
			return new DocumentSearchResultDisplay(param.getSearchResultVO(), param.getComponentFactory(), appLayerFactory, param.getQuery());
		}
		return super.getCustomResultDisplayFor(param);
	}

	@Override
	public void notifyNewUserSearch(UserSearchEvent event) {
	}

	@Override
	public Component getSimpleTableWindowComponent(GetSearchResultSimpleTableWindowComponentParam param) {
		Component result;
		RecordVO recordVO = param.getRecordVO();
		String typeCode = param.getSchemaType();
		//TODO add event
		if (typeCode.equals(Document.SCHEMA_TYPE)) {
//			result = new RecordDisplay(recordVO, new RMMetadataDisplayFactory());
			DisplayDocumentViewImpl view = new DisplayDocumentViewImpl(recordVO, true);
			view.enter(null);
			result = view;
		} else 	if (typeCode.equals(Folder.SCHEMA_TYPE)) {
			DisplayFolderViewImpl view = new DisplayFolderViewImpl(recordVO, true);
			view.enter(null);
			result = view;
		} else {
			if (typeCode.equals(ContainerRecord.SCHEMA_TYPE) &&
                    DisplayContainerPresenter.hasRestrictedRecordAccess(rm, param.getUser(), recordVO.getRecord())) {
                DisplayContainerViewImpl view = new DisplayContainerViewImpl(recordVO, true);
                view.enter(null);
                result = view;
            } else {
                result = null;
            }
		}
		return result;
	}
}
