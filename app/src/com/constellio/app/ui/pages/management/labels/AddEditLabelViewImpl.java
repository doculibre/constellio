package com.constellio.app.ui.pages.management.labels;

import com.constellio.app.modules.rm.services.reports.ReportXMLGenerator;
import com.constellio.app.modules.rm.ui.components.document.fields.CustomDocumentField;
import com.constellio.app.modules.rm.wrappers.ContainerRecord;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.modules.rm.wrappers.PrintableLabel;
import com.constellio.app.ui.entities.LabelVO;
import com.constellio.app.ui.entities.MetadataVO;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.framework.builders.RecordToVOBuilder;
import com.constellio.app.ui.framework.components.MetadataFieldFactory;
import com.constellio.app.ui.framework.components.RecordFieldFactory;
import com.constellio.app.ui.framework.components.RecordForm;
import com.constellio.app.ui.framework.components.fields.BaseComboBox;
import com.constellio.app.ui.i18n.i18n;
import com.constellio.app.ui.pages.base.BaseViewImpl;
import com.constellio.app.ui.params.ParamUtils;
import com.constellio.model.entities.records.Record;
import com.constellio.model.frameworks.validation.ValidationException;
import com.vaadin.data.Buffered;
import com.vaadin.data.Property;
import com.vaadin.data.Validator;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.ui.*;
import org.apache.commons.lang.StringUtils;

import java.util.List;
import java.util.Map;

import static com.constellio.app.ui.i18n.i18n.$;

/**
 * Created by Nicolas D'Amours & Charles Blanchette on 2017-01-20.
 */
public class AddEditLabelViewImpl extends BaseViewImpl implements AddEditLabelView {
    private List<LabelVO> listLabel;
    private TabSheet tabSheet;
    private AddEditLabelPresenter presenter = new AddEditLabelPresenter(this);
    private Component folderDisplay, containerDisplay;
    private VerticalLayout mainLayout;
    private ReportXMLGenerator reportXmlGenerator;
    private Button addLabelButton;
    private LabelFormImpl recordForm;
    private RecordVO recordVO;

    @Override
    public void setLabels(List<LabelVO> list) {
    }

    @Override

    public void addLabels(LabelVO... items) {
    }

    public void setRecord(RecordVO recordVO) {
        this.recordVO = recordVO;
    }

    @Override
    protected void initBeforeCreateComponents(ViewChangeListener.ViewChangeEvent event) {
        if (StringUtils.isNotEmpty(event.getParameters())) {
            Map<String, String> paramsMap = ParamUtils.getParamsMap(event.getParameters());
            recordVO = presenter.getRecordVO(paramsMap.get("id"), RecordVO.VIEW_MODE.FORM);
        }
    }

    @Override
    protected Component buildMainComponent(ViewChangeListener.ViewChangeEvent event) {
        return newForm();
    }

    private LabelFormImpl newForm() {
        if (this.recordVO == null) {
            Record r = presenter.newRecord();
            RecordToVOBuilder voBuilder = new RecordToVOBuilder();
            this.recordVO = voBuilder.build(r, RecordVO.VIEW_MODE.FORM, getSessionContext());
        }
        recordForm = new LabelFormImpl(recordVO, new LabelRecordFieldFactory()) {
            @Override
            protected void saveButtonClick(RecordVO viewObject)
                    throws ValidationException {
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

            @Override
            public void reload() {
                recordForm = newForm();
                AddEditLabelViewImpl.this.replaceComponent(this, recordForm);
            }

            @Override
            public void commit() {
                for (Field<?> field : fieldGroup.getFields()) {
                    try {
                        field.commit();
                    } catch (Buffered.SourceException | Validator.InvalidValueException e) {
                    }
                }
            }
        };

        for (final Field<?> field : recordForm.getFields()) {
            if (field instanceof CustomDocumentField) {
                field.addValueChangeListener(new Property.ValueChangeListener() {
                    @Override
                    public void valueChange(Property.ValueChangeEvent event) {
                        presenter.customFieldValueChanged((CustomDocumentField<?>) field);
                    }
                });
            }
        }


        return recordForm;
    }

    @Override
    protected String getTitle() {
        String title = recordVO == null ? "AddLabelView.title" : "EditLabelView.title";
        return $(title);
    }

    private void prepareTypeField(final Field<String> field) {
        field.addValueChangeListener(new Property.ValueChangeListener() {
            @Override
            public void valueChange(Property.ValueChangeEvent event) {
                presenter.typeSelected(field.getValue());
            }
        });
    }

    class LabelForm extends RecordForm {
        public LabelForm(RecordVO record) {
            super(record);
        }

        @SuppressWarnings("unchecked")
        public Field<String> getTypeField() {
            return (Field<String>) getField(PrintableLabel.TYPE_LABEL);
        }

        @Override
        protected void saveButtonClick(RecordVO viewObject) throws ValidationException {

        }

        @Override
        protected void cancelButtonClick(RecordVO viewObject) {

        }
    }

    class LabelRecordFieldFactory extends RecordFieldFactory {
        @Override
        public Field<?> build(RecordVO recordVO, MetadataVO metadataVO) {
            return metadataVO.getCode().equals(PrintableLabel.SCHEMA_NAME + "_" + PrintableLabel.TYPE_LABEL) ? createComboBox(metadataVO) : new MetadataFieldFactory().build(metadataVO);
        }

        public ComboBox createComboBox(MetadataVO metadataVO) {
            ComboBox monComboBox = new BaseComboBox();
            Object valeur1 = Folder.SCHEMA_TYPE;
            Object valeur2 = ContainerRecord.SCHEMA_TYPE;
            monComboBox.addItems(valeur1, valeur2);
            monComboBox.setItemCaption(valeur1, $("AddEditTaxonomyView.classifiedObject.folder"));
            monComboBox.setItemCaption(valeur2, $("DecommissioningListView.folderDetails.container"));
            monComboBox.setTextInputAllowed(false);
            monComboBox.setCaption(metadataVO.getLabel(i18n.getLocale()));
            return monComboBox;
        }
    }
}
