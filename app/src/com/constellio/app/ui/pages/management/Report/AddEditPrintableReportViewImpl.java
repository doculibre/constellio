package com.constellio.app.ui.pages.management.Report;

import com.constellio.app.modules.rm.wrappers.PrintableReport;
import com.constellio.app.services.factories.ConstellioFactories;
import com.constellio.app.ui.application.ConstellioUI;
import com.constellio.app.ui.entities.MetadataVO;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.framework.builders.RecordToVOBuilder;
import com.constellio.app.ui.framework.components.MetadataFieldFactory;
import com.constellio.app.ui.framework.components.RecordFieldFactory;
import com.constellio.app.ui.framework.components.RecordForm;
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
import com.vaadin.data.util.converter.Converter;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.Field;
import org.apache.commons.lang.StringUtils;

import java.util.Map;

import static com.constellio.app.ui.i18n.i18n.$;

public class AddEditPrintableReportViewImpl extends BaseViewImpl implements AddEditPrintableReportView {
    private AddEditPrintableReportPresenter presenter = new AddEditPrintableReportPresenter(this);
    private RecordVO recordVO;
    private PrintableReportFormImpl recordForm;
    private boolean isEdit;

    @Override
    protected void initBeforeCreateComponents(ViewChangeListener.ViewChangeEvent event) {
        if (StringUtils.isNotEmpty(event.getParameters())) {
            Map<String, String> paramsMap = ParamUtils.getParamsMap(event.getParameters());
            recordVO = presenter.getRecordVO(paramsMap.get("id"), RecordVO.VIEW_MODE.FORM);
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
        return $( isEdit ? "AddLabelView.title" : "EditLabelView.title");
    }

    private PrintableReportFormImpl newForm() {
        return recordForm = new PrintableReportFormImpl(recordVO, new PrintableReportRecordFieldFactory());
    }

    private class PrintableReportFormImpl  extends RecordForm implements PrintableReportFrom {
        public PrintableReportFormImpl(RecordVO recordVO) {
            super(recordVO);
        }

        public PrintableReportFormImpl(RecordVO recordVO, RecordFieldFactory recordFieldFactory) {
            super(recordVO, recordFieldFactory);
        }

        @Override
        public void reload() {
            recordForm = newForm();
            replaceComponent(this, recordForm);
        }
        @Override
        public void commit(){
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
            return (CustomLabelField<?>)getField(metadataCode);
        }

        @Override
        protected void saveButtonClick(RecordVO viewObject) throws ValidationException {
            try{
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
        public Field<?> build(RecordVO recordVO, MetadataVO metadataVO) {
            Field<?> field = null;
            if(metadataVO.getCode().equals(PrintableReport.SCHEMA_NAME + "_" + PrintableReport.RECORD_TYPE)) {
                field = createComboBox(metadataVO);
            } else if(metadataVO.getCode().equals(PrintableReport.SCHEMA_NAME + "_" + PrintableReport.RECORD_SCHEMA)) {
                field = createComboBoxForSchemaType(metadataVO);
            } else {
                field = new MetadataFieldFactory().build(metadataVO);
            }
            return field;
        }

        public ComboBox createComboBox(MetadataVO metadataVO) {
            typeCombobox = new BaseComboBox();
            Object folderValue = PrintableReportListPossibleView.FOLDER;
            Object documentValue = PrintableReportListPossibleView.DOCUMENT;
            Object taskValue = PrintableReportListPossibleView.TASK;
            typeCombobox.addItems(folderValue, documentValue, taskValue);
            typeCombobox.setItemCaption(folderValue, PrintableReportListPossibleView.FOLDER.getLabel());
            typeCombobox.setItemCaption(documentValue, PrintableReportListPossibleView.DOCUMENT.getLabel());
            typeCombobox.setItemCaption(taskValue, PrintableReportListPossibleView.TASK.getLabel());
            typeCombobox.setTextInputAllowed(false);
            typeCombobox.setCaption(metadataVO.getLabel(i18n.getLocale()));
            typeCombobox.setConverter(new PrintableReportListToStringConverter());
            typeCombobox.addValueChangeListener(new Property.ValueChangeListener() {
                @Override
                public void valueChange(Property.ValueChangeEvent event) {
                    presenter.setCurrentType((PrintableReportListPossibleView) event.getProperty().getValue());
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
            return schemaCombobox;
        }

        private ComboBox fillComboBox(ComboBox comboBox) {
            comboBox.removeAllItems();
            for(MetadataSchema metadataSchema : presenter.getSchemasForCurrentType()) {
                comboBox.addItem(metadataSchema.getCode());
                comboBox.setItemCaption(metadataSchema.getCode(), metadataSchema.getFrenchLabel());
            }
            return comboBox;
        }
    }
}
