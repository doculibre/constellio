package com.constellio.app.ui.pages.search.batchProcessing;

import com.constellio.app.ui.entities.MetadataVO;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.framework.buttons.WindowButton;
import com.constellio.app.ui.framework.components.RecordFieldFactory;
import com.constellio.app.ui.framework.components.RecordForm;
import com.constellio.app.ui.pages.search.AdvancedSearchPresenter;
import com.constellio.app.ui.pages.search.AdvancedSearchView;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.model.frameworks.validation.ValidationException;
import com.vaadin.data.Property;
import com.vaadin.ui.*;
import com.vaadin.ui.themes.ValoTheme;

import java.util.List;

import static com.constellio.app.ui.i18n.i18n.$;
import static com.constellio.model.entities.schemas.MetadataValueType.CONTENT;

public class BatchProcessingButton extends WindowButton {
    private AdvancedSearchPresenter presenter;
    private final AdvancedSearchView view;

    //fields
    ComboBox schemaField;
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
        String originSchema = presenter.getOriginSchema(view.getSchemaType(), view.getSelectedRecordIds());
        List<String> destinationSchema = presenter.getDestinationSchemata(view.getSchemaType());
        schemaField = new ComboBox($("AdvancedSearchView.schema"), destinationSchema);
        schemaField.setValue(originSchema);
        schemaField.setNullSelectionAllowed(false);
        schemaField.addListener(new Property.ValueChangeListener() {
            @Override
            public void valueChange(Property.ValueChangeEvent event) {
                refreshForm();
            }
        });
        vLayout.addComponent(schemaField);

        form = new BatchProcessingForm(presenter.newRecordVO(originSchema, view.getSessionContext()), new RecordFieldFactoryWithNoTypeNoContent());
        vLayout.addComponent(form);

        panel.setContent(vLayout);
        panel.setSizeFull();
        return panel;
    }

    private void refreshForm() {
        BatchProcessingForm newForm = new BatchProcessingForm(presenter.newRecordVO((String) schemaField.getValue(), view.getSessionContext()), new RecordFieldFactoryWithNoTypeNoContent());
        vLayout.replaceComponent(form, newForm);
        form = newForm;
    }


    public class BatchProcessingForm extends RecordForm {
        Button simulateButton, processButton;

        public BatchProcessingForm(RecordVO record, RecordFieldFactory recordFieldFactory) {
            super(record, recordFieldFactory);
            getWindow().setWidth("200px");
            getWindow().setHeight("300px");
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

    public static class RecordFieldFactoryWithNoTypeNoContent extends RecordFieldFactory {
        @Override
        public Field<?> build(RecordVO recordVO, MetadataVO metadataVO) {
            if(metadataVO.getLocalCode().equals("type") || metadataVO.getType().equals(CONTENT)){
                return null;
            } else {
                return super.build(recordVO, metadataVO);
            }
        }
    }
}

