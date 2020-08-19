package com.constellio.app.modules.rm.ui.menuBar;

import com.constellio.app.modules.rm.ConstellioRMModule;
import com.constellio.app.modules.rm.extensions.api.RMModuleExtensions;
import com.constellio.app.modules.rm.ui.builders.DocumentToVOBuilder;
import com.constellio.app.modules.rm.wrappers.ContainerRecord;
import com.constellio.app.modules.rm.wrappers.Document;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.modules.rm.wrappers.StorageSpace;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.framework.components.menuBar.AbstractRecordMenuBarHandler;
import com.constellio.app.ui.framework.components.menuBar.RecordVOMenuBar;
import com.constellio.model.entities.records.wrappers.Event;
import com.constellio.model.services.factories.ModelLayerFactory;
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
		String schemaTypeCode = recordVO == null ? null : recordVO.getSchema().getTypeCode();
		if (recordVO != null && isSchemaTypeSupported(schemaTypeCode)) {
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

	private boolean isSchemaTypeSupported(String schemaTypeCode) {
		return Document.SCHEMA_TYPE.equals(schemaTypeCode)
			   || Folder.SCHEMA_TYPE.equals(schemaTypeCode)
			   || ContainerRecord.SCHEMA_TYPE.equals(schemaTypeCode)
			   || StorageSpace.SCHEMA_TYPE.equals(schemaTypeCode);
	}

}
