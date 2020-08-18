package com.constellio.app.modules.rm.ui.components.container;

import com.constellio.app.modules.rm.ui.components.container.fields.ContainerStorageSpaceLookupField;
import com.constellio.app.modules.rm.ui.pages.containers.edit.AddEditContainerPresenter;
import com.constellio.app.modules.rm.wrappers.ContainerRecord;
import com.constellio.app.services.factories.ConstellioFactories;
import com.constellio.app.ui.entities.MetadataVO;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.framework.buttons.WindowButton;
import com.constellio.app.ui.framework.components.BaseForm;
import com.constellio.app.ui.framework.components.RecordFieldFactory;
import com.constellio.app.ui.framework.components.RecordForm;
import com.constellio.app.ui.framework.components.fields.number.BaseIntegerField;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.Field;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ValoTheme;

import java.util.ArrayList;
import java.util.List;

import static com.constellio.app.ui.i18n.i18n.$;

/**
 * Created by Constellio on 2017-01-11.
 */
public abstract class ContainerFormImpl extends RecordForm implements ContainerForm {

	private static String getTypeFromRecordIfAvalible(RecordVO recordVO) {
		MetadataVO metadataVOOrNull = recordVO.getMetadataOrNull(ContainerRecord.TYPE);
		if(metadataVOOrNull == null) {
			return null;
		} else {
			return recordVO.get(ContainerRecord.TYPE);
		}
	}

	private static Double getCapacityFromrecordVOIfAvalible(RecordVO recordVO) {
		MetadataVO metadataVOOrNull = recordVO.getMetadataOrNull(ContainerRecord.CAPACITY);

		if (metadataVOOrNull == null) {
			return null;
		} else {
			return recordVO.get(ContainerRecord.CAPACITY);
		}
	}

	public ContainerFormImpl(RecordVO record, final AddEditContainerPresenter presenter,
							 ConstellioFactories constellioFactories) {
		this(record, new ContainerFieldFactory(getTypeFromRecordIfAvalible(record),
				getCapacityFromrecordVOIfAvalible(record), presenter), constellioFactories);
		if (presenter.isMultipleMode()) {
			WindowButton newSaveButton = new WindowButton(saveButton.getCaption(), saveButton.getCaption()) {
				@Override
				public void buttonClick(ClickEvent event) {
					super.buttonClick(event);
				}

				@Override
				protected Component buildWindowContent() {
					VerticalLayout mainLayout = new VerticalLayout();
					mainLayout.setSpacing(true);

					final BaseIntegerField integerField = new BaseIntegerField($("AddEditContainerView.numberOfContainer"));
					integerField.setRequired(true);

					HorizontalLayout buttonLayout = new HorizontalLayout();
					buttonLayout.setSpacing(true);
					Button newLayoutSaveButton = new Button(saveButton.getCaption());
					newLayoutSaveButton.addClickListener(new ClickListener() {
						@Override
						public void buttonClick(ClickEvent event) {
							int numberOfContainer = integerField.getValue() != null && integerField.getValue().matches("^\\d+$") ?
													Integer.parseInt(integerField.getValue()) :
													0;
							presenter.setNumberOfContainer(numberOfContainer);
							callTrySave();
							getWindow().close();
						}
					});
					newLayoutSaveButton.addStyleName(ValoTheme.BUTTON_PRIMARY);
					Button newLayoutCancelButton = new Button(cancelButton.getCaption());
					newLayoutCancelButton.addClickListener(new ClickListener() {
						@Override
						public void buttonClick(ClickEvent event) {
							getWindow().close();
						}
					});
					buttonLayout.addComponents(newLayoutSaveButton, newLayoutCancelButton);

					mainLayout.addComponents(integerField, buttonLayout);
					return mainLayout;
				}
			};
			newSaveButton.addStyleName(ValoTheme.BUTTON_PRIMARY);
			buttonsLayout.replaceComponent(saveButton, newSaveButton);
		}
	}

	private ContainerFormImpl(final RecordVO recordVO, RecordFieldFactory formFieldFactory,
							  ConstellioFactories constellioFactories) {
		super(recordVO, buildFields(recordVO, formFieldFactory), formFieldFactory, constellioFactories);
	}

	private static List<FieldAndPropertyId> buildFields(RecordVO recordVO, RecordFieldFactory formFieldFactory) {
		List<FieldAndPropertyId> fieldsAndPropertyIds = new ArrayList<FieldAndPropertyId>();
		for (MetadataVO metadataVO : recordVO.getFormMetadatas()) {
			if (!recordVO.getMetadataCodes().contains(metadataVO.getCode())) {
				continue;
			}
			Field<?> field = formFieldFactory.build(recordVO, metadataVO);
			if (field != null) {
				field.addStyleName(STYLE_FIELD);
				field.addStyleName(STYLE_FIELD + "-" + metadataVO.getCode());
				fieldsAndPropertyIds.add(new FieldAndPropertyId(field, metadataVO));
			}
		}
		return fieldsAndPropertyIds;
	}

	private ContainerStorageSpaceLookupField storageSpaceField;
	private VerticalLayout storageSpaceLayout;

	@Override
	protected void addFieldToLayout(Field<?> field, VerticalLayout fieldLayout) {
		super.addFieldToLayout(field, fieldLayout);
		if (field instanceof ContainerStorageSpaceLookupField) {
			storageSpaceField = (ContainerStorageSpaceLookupField) field;
			storageSpaceLayout = fieldLayout;
		}
	}

	public void replaceStorageSpaceField(RecordVO containerVo, AddEditContainerPresenter presenter) {
		ContainerStorageSpaceLookupField newField = ((ContainerFieldFactory) getFormFieldFactory())
				.rebuildContainerStorageSpaceLookupField(containerVo, presenter);
		newField.setPropertyDataSource(storageSpaceField.getPropertyDataSource());
		newField.addStyleName(BaseForm.STYLE_FIELD);
		newField.addStyleName(STYLE_FIELD);
		MetadataVO metadata = containerVo.getMetadata(ContainerRecord.STORAGE_SPACE);
		newField.addStyleName(STYLE_FIELD + "-" + metadata.getCode());
		storageSpaceLayout.replaceComponent(storageSpaceField, newField);
		fields.remove(storageSpaceField);
		fieldGroup.unbind(storageSpaceField);
		storageSpaceField = newField;
		fields.add(storageSpaceField);
		fieldGroup.bind(storageSpaceField, metadata);
	}

	@SuppressWarnings("unchecked")
	public Field<String> getTypeField() {
		return (Field<String>) getField(ContainerRecord.TYPE);
	}

	@SuppressWarnings("unchecked")
	public Field<String> getCapacityField() {
		return (Field<String>) getField(ContainerRecord.CAPACITY);
	}

	@SuppressWarnings("unchecked")
	public Field<String> getDecommissioningTypeField() {
		return (Field<String>) getField(ContainerRecord.DECOMMISSIONING_TYPE);
	}

}