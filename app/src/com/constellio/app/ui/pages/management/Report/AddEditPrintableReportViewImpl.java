package com.constellio.app.ui.pages.management.Report;

import com.constellio.app.modules.rm.wrappers.Printable;
import com.constellio.app.modules.rm.wrappers.PrintableReport;
import com.constellio.app.services.factories.ConstellioFactories;
import com.constellio.app.ui.application.ConstellioUI;
import com.constellio.app.ui.application.Navigation;
import com.constellio.app.ui.entities.ContentVersionVO;
import com.constellio.app.ui.entities.MetadataVO;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.framework.builders.RecordToVOBuilder;
import com.constellio.app.ui.framework.components.MetadataFieldFactory;
import com.constellio.app.ui.framework.components.RecordFieldFactory;
import com.constellio.app.ui.framework.components.RecordForm;
import com.constellio.app.ui.framework.components.breadcrumb.BaseBreadcrumbTrail;
import com.constellio.app.ui.framework.components.breadcrumb.IntermediateBreadCrumbTailItem;
import com.constellio.app.ui.framework.components.breadcrumb.TitleBreadcrumbTrail;
import com.constellio.app.ui.framework.components.fields.BaseComboBox;
import com.constellio.app.ui.i18n.i18n;
import com.constellio.app.ui.pages.base.BaseViewImpl;
import com.constellio.app.ui.pages.base.SessionContext;
import com.constellio.app.ui.pages.management.labels.CustomLabelField;
import com.constellio.app.ui.params.ParamUtils;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.frameworks.validation.ValidationException;
import com.vaadin.data.Buffered;
import com.vaadin.data.Property;
import com.vaadin.data.Validator;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.Field;
import org.apache.commons.lang.StringUtils;

import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static com.constellio.app.ui.i18n.i18n.$;

public class AddEditPrintableReportViewImpl extends BaseViewImpl implements AddEditPrintableReportView {
	private AddEditPrintableReportPresenter presenter = new AddEditPrintableReportPresenter(this);
	private RecordVO recordVO;
	private PrintableReportFormImpl recordForm;
	private boolean isEdit;

	@Override
	protected BaseBreadcrumbTrail buildBreadcrumbTrail() {
		return new TitleBreadcrumbTrail(this, getTitle()) {
			@Override
			public List<? extends IntermediateBreadCrumbTailItem> getIntermediateItems() {
				return Collections.singletonList(new IntermediateBreadCrumbTailItem() {
					@Override
					public boolean isEnabled() {
						return true;
					}

					@Override
					public String getTitle() {
						return $("ViewGroup.PrintableViewGroup");
					}

					@Override
					public void activate(Navigation navigate) {
						navigate.to().viewReport();
					}
				});
			}
		};
	}

	@Override
	protected void initBeforeCreateComponents(ViewChangeListener.ViewChangeEvent event) {
		if (StringUtils.isNotEmpty(event.getParameters())) {
			Map<String, String> paramsMap = ParamUtils.getParamsMap(event.getParameters());
			recordVO = presenter.getRecordVO(paramsMap.get("id"));
			isEdit = true;
		} else {
			recordVO = new RecordToVOBuilder().build(presenter.newRecord(), RecordVO.VIEW_MODE.FORM, getSessionContext());
			isEdit = false;
		}
	}

	public void setRecord(RecordVO recordVO) {
		this.recordVO = recordVO;
	}


	@Override
	protected Component buildMainComponent(ViewChangeListener.ViewChangeEvent event) {
		return newForm();
	}

	@Override
	protected String getTitle() {
		return $(isEdit ? "PrintableReport.edit.title" : "PrintableReport.add.title");
	}

	private PrintableReportFormImpl newForm() {
		return recordForm = new PrintableReportFormImpl(recordVO, new PrintableReportRecordFieldFactory(), getConstellioFactories());
	}

	private class PrintableReportFormImpl extends RecordForm implements PrintableReportFrom {
		public PrintableReportFormImpl(RecordVO recordVO, ConstellioFactories constellioFactories) {
			super(recordVO, constellioFactories);
		}

		public PrintableReportFormImpl(RecordVO recordVO, RecordFieldFactory recordFieldFactory,
									   ConstellioFactories constellioFactories) {
			super(recordVO, recordFieldFactory, constellioFactories);
		}

		@Override
		public void reload() {
			recordForm = newForm();
			replaceComponent(this, recordForm);
		}

		@Override
		public void commit() {
			for (Field<?> field : fieldGroup.getFields()) {
				try {
					field.commit();
				} catch (Buffered.SourceException | Validator.InvalidValueException e) {
					e.printStackTrace();
				}
			}
		}

		@Override
		public ConstellioFactories getConstellioFactories() {
			return ConstellioFactories.getInstance();
		}

		@Override
		public SessionContext getSessionContext() {
			return ConstellioUI.getCurrentSessionContext();
		}

		@Override
		public CustomLabelField<?> getCustomField(String metadataCode) {
			return (CustomLabelField<?>) getField(metadataCode);
		}

		@Override
		protected void saveButtonClick(RecordVO viewObject) throws ValidationException {
			try {
				presenter.saveButtonClicked(recordVO);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		@Override
		protected void cancelButtonClick(RecordVO viewObject) {
			presenter.cancelButtonClicked();
		}
	}

	private class PrintableReportRecordFieldFactory extends RecordFieldFactory {
		private ComboBox typeCombobox, schemaCombobox;

		@Override
		public Field<?> build(RecordVO recordVO, MetadataVO metadataVO, Locale locale) {
			Field<?> field;
			switch (metadataVO.getCode()) {
				case PrintableReport.SCHEMA_NAME + "_" + PrintableReport.RECORD_TYPE:
					field = createComboBox(metadataVO);
					break;
				case PrintableReport.SCHEMA_NAME + "_" + PrintableReport.RECORD_SCHEMA:
					field = createComboBoxForSchemaType(metadataVO);
					break;
				default:
					field = new MetadataFieldFactory().build(metadataVO, recordVO != null ? recordVO.getId() : null, locale);
					if (metadataVO.codeMatches(Printable.JASPERFILE)) {
						field.addValidator(new Validator() {
							@Override
							public void validate(Object value) throws InvalidValueException {
								ContentVersionVO contentValue = (ContentVersionVO) value;
								if (contentValue != null && !contentValue.getFileName().endsWith(".jasper")) {
									throw new InvalidValueException($("PrintableReport.invalidFileType"));
								}
							}
						});
					}
					break;
			}
			return field;
		}

		public ComboBox createComboBox(MetadataVO metadataVO) {
			typeCombobox = new BaseComboBox();
			String folderValue = PrintableReportListPossibleType.FOLDER.getSchemaType();
			String documentValue = PrintableReportListPossibleType.DOCUMENT.getSchemaType();
			String taskValue = PrintableReportListPossibleType.TASK.getSchemaType();
			typeCombobox.addItems(folderValue, documentValue, taskValue);
			typeCombobox.setItemCaption(folderValue, presenter.getLabelForSchemaType(folderValue));
			typeCombobox.setItemCaption(documentValue, presenter.getLabelForSchemaType(documentValue));
			typeCombobox.setItemCaption(taskValue, presenter.getLabelForSchemaType(taskValue));
			typeCombobox.setTextInputAllowed(false);
			typeCombobox.setCaption(metadataVO.getLabel(i18n.getLocale()));
			//            typeCombobox.setConverter(new PrintableReportListToStringConverter());
			typeCombobox.setNullSelectionAllowed(false);
			typeCombobox.addValidator(new Validator() {
				@Override
				public void validate(Object value) throws InvalidValueException {
					if (value == null) {
						throw new InvalidValueException($("PrintableReport.addEdit.emptyType"));
					}
				}
			});
			typeCombobox.addValueChangeListener(new Property.ValueChangeListener() {
				@Override
				public void valueChange(Property.ValueChangeEvent event) {
					presenter.setCurrentType((String) event.getProperty().getValue());
					schemaCombobox = fillComboBox(schemaCombobox);
				}
			});
			return typeCombobox;
		}

		private ComboBox createComboBoxForSchemaType(MetadataVO metadataVO) {
			schemaCombobox = new BaseComboBox();
			schemaCombobox = fillComboBox(schemaCombobox);
			schemaCombobox.setTextInputAllowed(false);
			schemaCombobox.setCaption(metadataVO.getLabel(i18n.getLocale()));
			schemaCombobox.setNullSelectionAllowed(false);
			//schemaCombobox.setConverter(new CustomSchemaToStringConverter(getCollection(), getConstellioFactories().getAppLayerFactory()));
			schemaCombobox.addValidator(new Validator() {
				@Override
				public void validate(Object value) throws InvalidValueException {
					if (value == null) {
						throw new InvalidValueException($("PrintableReport.addEdit.emptySchema"));
					}
				}
			});
			return schemaCombobox;
		}

		private ComboBox fillComboBox(ComboBox comboBox) {
			comboBox.removeAllItems();
			for (MetadataSchema metadataSchema : presenter.getSchemasForCurrentType()) {
				comboBox.addItem(metadataSchema.getCode());
				comboBox.setItemCaption(metadataSchema.getCode(), metadataSchema.getFrenchLabel());
				if (comboBox.getItemIds().size() == 1) {
					comboBox.setValue(metadataSchema.getCode());
				}

			}
			return comboBox;
		}
	}
}
