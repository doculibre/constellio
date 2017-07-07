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
import com.constellio.model.frameworks.validation.ValidationException;
import com.vaadin.data.Buffered;
import com.vaadin.data.Validator;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.Field;
import org.apache.commons.lang.StringUtils;

import java.util.Map;

import static com.constellio.app.ui.i18n.i18n.$;

/**
 * Created by Marco on 2017-07-07.
 */
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
        @Override
        public Field<?> build(RecordVO recordVO, MetadataVO metadataVO) {
            return metadataVO.getCode().equals(PrintableReport.SCHEMA_NAME + "_" + PrintableReport.REPORT_TYPE) ? createComboBox(metadataVO) : new MetadataFieldFactory().build(metadataVO);
        }

        public ComboBox createComboBox(MetadataVO metadataVO) {
            ComboBox comboBox = new BaseComboBox();
            Object folderValue = PrintableReportListPossibleView.FOLDER;
            Object documentValue = PrintableReportListPossibleView.DOCUMENT;
            Object taskValue = PrintableReportListPossibleView.TASK;
            comboBox.addItems(folderValue, documentValue, taskValue);
            comboBox.setItemCaption(folderValue, PrintableReportListPossibleView.FOLDER.getLabel());
            comboBox.setItemCaption(documentValue, PrintableReportListPossibleView.DOCUMENT.getLabel());
            comboBox.setItemCaption(taskValue, PrintableReportListPossibleView.TASK.getLabel());
            comboBox.setTextInputAllowed(false);
            comboBox.setCaption(metadataVO.getLabel(i18n.getLocale()));
            comboBox.setConverter(new PrintableReportListToStringConverter());
            return comboBox;
        }
    }
}
