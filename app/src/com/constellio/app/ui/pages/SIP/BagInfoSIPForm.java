package com.constellio.app.ui.pages.SIP;

import com.constellio.app.ui.entities.BagInfoVO;
import com.constellio.app.ui.entities.MetadataVO;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.framework.components.MetadataFieldFactory;
import com.constellio.app.ui.framework.components.RecordFieldFactory;
import com.constellio.app.ui.framework.components.RecordForm;
import com.constellio.app.ui.framework.components.fields.BaseComboBox;
import com.constellio.app.ui.pages.base.BaseViewImpl;
import com.constellio.model.frameworks.validation.ValidationException;
import com.vaadin.data.Property;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.Field;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ValoTheme;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static com.constellio.app.ui.framework.components.RecordForm.STYLE_FIELD;
import static com.constellio.app.ui.i18n.i18n.$;
import static java.util.Arrays.asList;

public class BagInfoSIPForm extends BaseViewImpl {

	private BagInfoSIPPresenter presenter;

	private BagInfoRecordForm recordForm;

	private CheckBox deleteFilesCheckBox;

	private CheckBox limitSizeCheckbox;

	private boolean showDeleteButton;

	public BagInfoSIPForm(boolean showDeleteButton) {
		this.showDeleteButton = showDeleteButton;
	}

	MetadataFieldFactory factory = new MetadataFieldFactory() {
		@Override
		public Field<?> build(MetadataVO metadata, String recordId, Locale locale) {
			if (metadata.getLocalCode().equals("title")) {
				return null;
			}
			return super.build(metadata, recordId, locale);
		}
	};

	@Override
	protected void initBeforeCreateComponents(ViewChangeListener.ViewChangeEvent event) {
		presenter = new BagInfoSIPPresenter(this);
	}

	@Override
	protected boolean isBreadcrumbsVisible() {
		return false;
	}

	private BagInfoRecordForm newForm(BagInfoVO bagInfoVO) {
		return new BagInfoRecordForm(bagInfoVO, factory) {
			@Override
			protected void saveButtonClick(RecordVO viewObject) throws ValidationException {
				BagInfoSIPForm.this.saveButtonClick((BagInfoVO) viewObject);
			}

			@Override
			protected void cancelButtonClick(RecordVO viewObject) {
				presenter.cancelButtonClicked();
			}
		};
	}

	@Override
	protected Component buildMainComponent(ViewChangeListener.ViewChangeEvent event) {
		List<BagInfoVO> bagInfoVOList = presenter.getAllBagInfo();
		final VerticalLayout mainLayout = new VerticalLayout();
		ComboBox cb = new BaseComboBox($("SIPButton.predefinedBagInfo"));
		for (BagInfoVO bagInfoVO : bagInfoVOList) {
			cb.addItem(bagInfoVO);
			cb.setItemCaption(bagInfoVO, bagInfoVO.getTitle());
		}
		cb.addValueChangeListener(new Property.ValueChangeListener() {
			@Override
			public void valueChange(Property.ValueChangeEvent event) {
				BagInfoVO newValue = (BagInfoVO) event.getProperty().getValue();
				if (newValue == null) {
					newValue = presenter.newRecord();
				}
				BagInfoRecordForm newForm = newForm(newValue);
				mainLayout.replaceComponent(recordForm, newForm);
				recordForm = newForm;
			}
		});

		if (bagInfoVOList.isEmpty()) {
			cb.setVisible(false);
			cb.setEnabled(false);
		}

		limitSizeCheckbox = new CheckBox($("SIPButton.limitSize"));

		deleteFilesCheckBox = new CheckBox($("SIPButton.deleteFilesLabel"));

		recordForm = newForm(presenter.newRecord());
		VerticalLayout deleteLayout = new VerticalLayout();
		deleteLayout.setSpacing(true);
		final Label deleteWarning = new Label($("SIPButton.deleteFilesWarning"));
		deleteWarning.addStyleName(ValoTheme.LABEL_FAILURE);
		deleteWarning.setVisible(false);
		deleteFilesCheckBox.addValueChangeListener(new Property.ValueChangeListener() {
			@Override
			public void valueChange(Property.ValueChangeEvent event) {
				boolean isChecked = (boolean) event.getProperty().getValue();
				((BagInfoVO) recordForm.getViewObject()).setDeleteFiles(isChecked);
				deleteWarning.setVisible(isChecked);
			}
		});
		deleteLayout.addComponents(deleteFilesCheckBox, deleteWarning);

		limitSizeCheckbox.addValueChangeListener(new Property.ValueChangeListener() {
			@Override
			public void valueChange(Property.ValueChangeEvent event) {
				((BagInfoVO) recordForm.getViewObject()).setLimitSize((boolean) event.getProperty().getValue());
			}
		});

		limitSizeCheckbox.addStyleName(STYLE_FIELD);
		deleteFilesCheckBox.addStyleName(STYLE_FIELD);
		deleteFilesCheckBox.setVisible(showDeleteButton);
		deleteLayout.setVisible(showDeleteButton);
		cb.setWidth("100%");
		mainLayout.addComponents(limitSizeCheckbox, deleteLayout, cb, new Hr(), recordForm);
		return mainLayout;
	}

	@SuppressWarnings("rawtypes")
	private void updateValue(BagInfoVO viewObject) {
		if (viewObject.getId().isEmpty()) {
			for (Field field : recordForm.getFields()) {
				field.setValue("");
			}
		} else {
			for (MetadataVO metadataVO : viewObject.getFormMetadatas()) {
				Field field = recordForm.getField(metadataVO.getCode());
				if (field != null) {
					field.setValue(viewObject.<String>get(metadataVO));
				}
			}
		}
	}

	protected void saveButtonClick(BagInfoVO viewObject) throws ValidationException {

	}

	static class BagInfoRecordForm extends RecordForm {
		public BagInfoRecordForm(BagInfoVO viewObject, MetadataFieldFactory metadataFactory,
								 FieldAndPropertyId... fields) {
			super(viewObject, metadataFactory);
		}

		@Override
		protected void saveButtonClick(RecordVO viewObject) throws ValidationException {

		}

		@Override
		protected void cancelButtonClick(RecordVO viewObject) {

		}

		private static List<FieldAndPropertyId> buildFields(BagInfoVO recordVO, RecordFieldFactory formFieldFactory,
															FieldAndPropertyId... fields) {
			List<FieldAndPropertyId> fieldsAndPropertyIds = buildInitialFields(fields);
			for (MetadataVO metadataVO : recordVO.getFormMetadatas()) {
				Field<?> field = formFieldFactory.build(recordVO, metadataVO);
				if (field != null) {
					field.addStyleName(STYLE_FIELD);
					field.addStyleName(STYLE_FIELD + "-" + metadataVO.getCode());
					fieldsAndPropertyIds.add(new FieldAndPropertyId(field, metadataVO));
				}
			}
			return fieldsAndPropertyIds;
		}

		@Override
		protected String getTabCaption(Field<?> field, Object propertyId) {
			return null;
		}

		private static List<FieldAndPropertyId> buildInitialFields(FieldAndPropertyId... fields) {
			return new ArrayList<>(asList(fields));
		}
	}

	private class Hr extends Label {
		Hr() {
			super("<hr/>", ContentMode.HTML);
		}
	}
}
