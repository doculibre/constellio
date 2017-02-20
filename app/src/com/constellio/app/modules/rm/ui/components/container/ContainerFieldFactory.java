package com.constellio.app.modules.rm.ui.components.container;

import com.constellio.app.modules.rm.ui.components.RMRecordFieldFactory;
import com.constellio.app.modules.rm.ui.components.container.fields.ContainerStorageSpaceLookupField;
import com.constellio.app.modules.rm.ui.components.folder.fields.CustomFolderField;
import com.constellio.app.modules.rm.ui.pages.containers.edit.AddEditContainerPresenter;
import com.constellio.app.modules.rm.wrappers.ContainerRecord;
import com.constellio.app.ui.entities.MetadataVO;
import com.constellio.app.ui.entities.RecordVO;
import com.vaadin.ui.Field;

public class ContainerFieldFactory extends RMRecordFieldFactory {

	private String containerRecordType;

	private AddEditContainerPresenter presenter;

	public ContainerFieldFactory(String containerRecordType, AddEditContainerPresenter presenter) {
		this.containerRecordType = containerRecordType;
		this.presenter = presenter;
	}

	@Override
	public Field<?> build(RecordVO recordVO, MetadataVO metadataVO) {
		Field<?> field;
		switch (metadataVO.getLocalCode()) {
			case ContainerRecord.STORAGE_SPACE:
				field = new ContainerStorageSpaceLookupField(containerRecordType, presenter);
				break;
			default:
				field = super.build(recordVO, metadataVO);
		}

		if (field instanceof CustomFolderField) {
			postBuild(field, recordVO, metadataVO);
		}

		return field;
	}

}
