package com.constellio.app.modules.rm.ui.menuBar;

import com.constellio.app.modules.rm.ConstellioRMModule;
import com.constellio.app.modules.rm.extensions.api.RMModuleExtensions;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.ui.builders.DocumentToVOBuilder;
import com.constellio.app.modules.rm.ui.entities.DocumentVO;
import com.constellio.app.modules.rm.wrappers.ContainerRecord;
import com.constellio.app.modules.rm.wrappers.Document;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.ui.application.ConstellioUI;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.entities.RecordVO.VIEW_MODE;
import com.constellio.app.ui.framework.components.menuBar.AbstractRecordMenuBarHandler;
import com.constellio.app.ui.framework.components.menuBar.RecordVOMenuBar;
import com.constellio.app.ui.pages.base.SessionContext;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.Event;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.records.RecordServicesRuntimeException;
import com.constellio.model.services.schemas.SchemaUtils;
import com.vaadin.ui.MenuBar;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class RMRecordMenuBarHandler extends AbstractRecordMenuBarHandler {

	private DocumentToVOBuilder documentToVOBuilder;

	public RMRecordMenuBarHandler(AppLayerFactory appLayerFactory) {
		super(appLayerFactory);
		initTransientObjects();
	}

	private void readObject(java.io.ObjectInputStream stream)
			throws IOException, ClassNotFoundException {
		stream.defaultReadObject();
		initTransientObjects();
	}

	private void initTransientObjects() {
		ModelLayerFactory modelLayerFactory = appLayerFactory.getModelLayerFactory();
		documentToVOBuilder = new DocumentToVOBuilder(modelLayerFactory);
	}

	@Override
	public boolean isMenuBarForSchemaTypeCode(String schemaTypeCode) {
		boolean menuBarForSchemaTypeCode;
		if (Document.SCHEMA_TYPE.equals(schemaTypeCode) || Event.SCHEMA_TYPE.equals(schemaTypeCode)) {
			menuBarForSchemaTypeCode = true;
		} else {
			menuBarForSchemaTypeCode = false;
		}
		return menuBarForSchemaTypeCode;
	}

	@Override
	public MenuBar get(RecordVO recordVO) {
		String schemaTypeCode = recordVO.getSchema().getTypeCode();
		if (Document.SCHEMA_TYPE.equals(schemaTypeCode) || Folder.SCHEMA_TYPE.equals(schemaTypeCode) || ContainerRecord.SCHEMA_TYPE.equals(schemaTypeCode)) {
			RMModuleExtensions rmModuleExtensions = appLayerFactory.getExtensions()
					.forCollection(recordVO.getRecord().getCollection())
					.forModule(ConstellioRMModule.ID);
			List<String> filteredActions = new ArrayList<String>() {{
				addAll(rmModuleExtensions.getFilteredActionsForContainers());
				addAll(rmModuleExtensions.getFilteredActionsForFolders());
			}};
			return new RecordVOMenuBar(recordVO, filteredActions, appLayerFactory);
		} else {
			return null;
		}
	}

	public RecordVO getDocumentVO(RecordVO recordVO) {
		RecordVO documentVO = null;

		String schemaTypeCode = recordVO.getSchema().getTypeCode();
		if (recordVO instanceof DocumentVO) {
			documentVO = (DocumentVO) recordVO;
		} else {
			SessionContext sessionContext = ConstellioUI.getCurrentSessionContext();
			VIEW_MODE viewMode = recordVO.getViewMode();
			String id = recordVO.getId();
			String collection = recordVO.getSchema().getCollection();
			RMSchemasRecordsServices rm = new RMSchemasRecordsServices(collection, appLayerFactory);
			Document document;
			if (schemaTypeCode.equals(Document.SCHEMA_TYPE)) {
				Record record = recordVO.getRecord();
				if (record != null) {
					document = rm.wrapDocument(record);
				} else {
					document = rm.getDocument(id);
				}
			} else {
				Event event = rm.getEvent(id);
				try {
					Record eventRecord = rm.get(event.getRecordId());
					String eventRecordSchemaType = SchemaUtils.getSchemaTypeCode(eventRecord.getSchemaCode());
					if (Document.SCHEMA_TYPE.equals(eventRecordSchemaType)) {
						document = rm.getDocument(event.getRecordId());
					} else {
						document = null;
					}
				} catch (RecordServicesRuntimeException.NoSuchRecordWithId e) {
					document = null;
					//Event of a deleted record, normal
				}
			}
			if (document != null) {
				documentVO = documentToVOBuilder.build(document.getWrappedRecord(), viewMode, sessionContext);
			} else {
				documentVO = null;
			}
		}

		return documentVO;
	}

}
