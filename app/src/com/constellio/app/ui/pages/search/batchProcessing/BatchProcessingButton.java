package com.constellio.app.ui.pages.search.batchProcessing;

import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.framework.buttons.WindowButton;
import com.constellio.app.ui.framework.components.MetadataFieldFactory;
import com.constellio.app.ui.framework.components.RecordForm;
import com.constellio.app.ui.pages.search.AdvancedSearchPresenter;
import com.constellio.app.ui.pages.search.AdvancedSearchView;
import com.constellio.model.frameworks.validation.ValidationException;
import com.vaadin.data.Property;
import com.vaadin.ui.*;
import com.vaadin.ui.themes.ValoTheme;

import java.util.List;

import static com.constellio.app.ui.i18n.i18n.$;

public class BatchProcessingButton extends WindowButton {
    private MetadataFieldFactory factory;
    private HorizontalLayout valueArea;
    private ComboBox metadata;
    private Field value;
    private Button process;
    private AdvancedSearchPresenter presenter;
    private final AdvancedSearchView view;

    //fields
    ComboBox schemaField;
    BatchProcessingForm form;
    VerticalLayout vLayout;

    public BatchProcessingButton(AdvancedSearchPresenter presenter, AdvancedSearchView view) {
        super($("AdvancedSearchView.batchProcessing"), $("AdvancedSearchView.batchProcessing"),new WindowConfiguration(false, false,
                "75%", "75%"));
        this.presenter = presenter;
        factory = new MetadataFieldFactory();
        this.view = view;
    }

    @Override
    protected Component buildWindowContent() {
        Panel panel = new Panel();
        vLayout = new VerticalLayout();
        String originSchema = presenter.getOriginSchema(view.getSchemaType(), view.getSelectedRecordIds());
        List<String> destinationSchema = presenter.getDestinationSchemata(view.getSchemaType());
        schemaField = new ComboBox("AdvancedSearchView.schema", destinationSchema);
        schemaField.setValue(originSchema);
        schemaField.setNullSelectionAllowed(false);
        schemaField.addListener(new Property.ValueChangeListener() {
            @Override
            public void valueChange(Property.ValueChangeEvent event) {
                refreshForm();
            }
        });
        vLayout.addComponent(schemaField);

        form = new BatchProcessingForm(presenter.newRecordVO(originSchema, view.getSessionContext()));
        vLayout.addComponent(form);

        panel.setContent(vLayout);
        panel.setSizeFull();
        return panel;
    }

    private void refreshForm() {
        BatchProcessingForm newForm = new BatchProcessingForm(presenter.newRecordVO((String) schemaField.getValue(), view.getSessionContext()));
        vLayout.replaceComponent(form, newForm);
        form = newForm;
    }


    public class BatchProcessingForm extends RecordForm {
        Button simulateButton, processButton;

        public BatchProcessingForm(RecordVO record) {
            super(record);
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

        @Override
        public Field<?> getField(String metadataCode) {
            if(metadataCode.endsWith("_type")){
                return null;
            }else{
                return super.getField(metadataCode);
            }
        }
    }
}

