package com.constellio.app.ui.pages.search.batchProcessing;

import com.constellio.app.api.extensions.params.RecordFieldFactoryExtensionParams;
import com.constellio.app.modules.rm.extensions.app.BatchProcessingRecordFactoryExtension;
import com.constellio.app.ui.entities.MetadataVO;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.framework.buttons.WindowButton;
import com.constellio.app.ui.framework.components.RecordFieldFactory;
import com.constellio.app.ui.framework.components.RecordForm;
import com.constellio.app.ui.framework.components.fields.lookup.LookupRecordField;
import com.constellio.app.ui.pages.search.AdvancedSearchPresenter;
import com.constellio.app.ui.pages.search.AdvancedSearchView;
import com.constellio.model.frameworks.validation.ValidationException;
import com.vaadin.data.Property;
import com.vaadin.ui.*;
import com.vaadin.ui.themes.ValoTheme;

import static com.constellio.app.ui.i18n.i18n.$;
import static com.constellio.model.entities.schemas.MetadataValueType.CONTENT;

public class BatchProcessingButton extends WindowButton {
    private AdvancedSearchPresenter presenter;
    private final AdvancedSearchView view;

    //fields
    LookupRecordField typeField;
    String currentSchema;
    BatchProcessingForm form;
    VerticalLayout vLayout;

    public BatchProcessingButton(AdvancedSearchPresenter presenter, AdvancedSearchView view) {
        super($("AdvancedSearchView.batchProcessing"), $("AdvancedSearchView.batchProcessing"),new WindowConfiguration(true, true,
                "75%", "75%"));
        this.presenter = presenter;
        this.view = view;
    }

    @Override
    protected Component buildWindowContent() {
        Panel panel = new Panel();
        vLayout = new VerticalLayout();
        String typeSchemaType = presenter.getTypeSchemaType(view.getSchemaType());
        typeField = new LookupRecordField(typeSchemaType);
        String originType = presenter.getOriginType(view.getSchemaType(), view.getSelectedRecordIds());
        if (originType != null) {
            typeField.setValue(originType);
        }
        typeField.addListener(new Property.ValueChangeListener() {
            @Override
            public void valueChange(Property.ValueChangeEvent event) {
                refreshForm();
            }
        });
        vLayout.addComponent(typeField);
        String originSchema = presenter.getSchema(view.getSchemaType(), originType);

        form = new BatchProcessingForm(presenter.newRecordVO(originSchema, view.getSessionContext()), newFieldFactory());
        vLayout.addComponent(form);

        panel.setContent(vLayout);
        panel.setSizeFull();
        return panel;
    }

    private void refreshForm() {
        RecordFieldFactory fieldFactory = newFieldFactory();
        String originSchema = presenter.getSchema(view.getSchemaType(), typeField.getValue());
        BatchProcessingForm newForm = new BatchProcessingForm(presenter.newRecordVO(originSchema , view.getSessionContext()), fieldFactory);
        vLayout.replaceComponent(form, newForm);
        form = newForm;
    }

    private RecordFieldFactory newFieldFactory() {
        RecordFieldFactory fieldFactory = presenter.getBatchProcessingExtension().newRecordFieldFactory
                (new RecordFieldFactoryExtensionParams(BatchProcessingRecordFactoryExtension.BATCH_PROCESSING_FIELD_FACTORY_KEY, null));
        return new RecordFieldFactoryWithNoTypeNoContent(fieldFactory);
    }


    public class BatchProcessingForm extends RecordForm {
        Button simulateButton, processButton;

        public BatchProcessingForm(RecordVO record, RecordFieldFactory recordFieldFactory) {
            super(record, recordFieldFactory);
            getWindow().setSizeFull();
            simulateButton = new Button($("simulate"));
            simulateButton.addClickListener(new ClickListener() {
                @Override
                public void buttonClick(ClickEvent event) {
                    presenter.simulateButtonClicked(viewObject);
                }
            });
            processButton = new Button($("process"));
            processButton.addStyleName(ValoTheme.BUTTON_PRIMARY);
            processButton.addClickListener(new ClickListener() {
                @Override
                public void buttonClick(ClickEvent event) {
                    presenter.saveButtonClicked(viewObject);
                }
            });
            buttonsLayout.addComponent(processButton);
            buttonsLayout.addComponentAsFirst(simulateButton);
            buttonsLayout.removeComponent(cancelButton);
            buttonsLayout.removeComponent(saveButton);
        }

        @Override
        protected void saveButtonClick(RecordVO viewObject)
                throws ValidationException {
        }

        @Override
        protected void cancelButtonClick(RecordVO viewObject) {
            getWindow().close();
        }

    }

    private class RecordFieldFactoryWithNoTypeNoContent extends RecordFieldFactory {
        final RecordFieldFactory fieldFactory;
        public RecordFieldFactoryWithNoTypeNoContent(RecordFieldFactory fieldFactory) {
            this.fieldFactory = fieldFactory;
        }

        @Override
                public Field<?> build(RecordVO recordVO, MetadataVO metadataVO) {
                    if(metadataVO.getType().equals(CONTENT) || metadataVO.getLocalCode().equals("type")){
                        return null;
                    }
                    if(fieldFactory != null) {
                        return fieldFactory.build(recordVO, metadataVO);
                    }
                    return super.build(recordVO, metadataVO);
                }
    }
}

