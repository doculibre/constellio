package com.constellio.app.ui.pages.search.batchProcessing;

import com.constellio.app.ui.entities.MetadataVO;
import com.constellio.app.ui.framework.buttons.WindowButton;
import com.constellio.app.ui.framework.components.MetadataFieldFactory;
import com.constellio.app.ui.pages.search.AdvancedSearchPresenter;
import com.constellio.app.ui.pages.search.AdvancedSearchView;
import com.vaadin.data.Property;
import com.vaadin.data.util.ObjectProperty;
import com.vaadin.ui.*;
import com.vaadin.ui.themes.ValoTheme;

import static com.constellio.app.ui.i18n.i18n.$;

public class BatchProcessingModifyingOneMetadataButton extends WindowButton {
    private MetadataFieldFactory factory;
    private HorizontalLayout valueArea;
    private ComboBox metadata;
    private Field value;
    private Button process;
    private AdvancedSearchPresenter presenter;
    private final AdvancedSearchView view;

    public BatchProcessingModifyingOneMetadataButton(AdvancedSearchPresenter presenter, AdvancedSearchView view) {
        super($("AdvancedSearchView.batchProcessing"), $("AdvancedSearchView.batchProcessing"));
        this.presenter = presenter;
        factory = new MetadataFieldFactory();
        this.view = view;
    }

    @Override
    protected Component buildWindowContent() {
        Label label = new Label($("AdvancedSearchView.batchProcessValue"));
        value = null;

        valueArea = new HorizontalLayout(label);
        valueArea.setSpacing(true);

        process = new Button($("AdvancedSearchView.batchProcessStart"), new ClickListener() {
            @Override
            public void buttonClick(ClickEvent event) {
                MetadataVO metadataVO = (MetadataVO) metadata.getValue();
                presenter.batchEditRequested(view.getSelectedRecordIds(), metadataVO.getCode(),
                        ((AbstractField) value).getConvertedValue());
                getWindow().close();
                view.showMessage($("AdvancedSearchView.batchProcessConfirm"));
            }
        });
        process.addStyleName(ValoTheme.BUTTON_PRIMARY);

        VerticalLayout layout = new VerticalLayout(buildMetadataComponent(), valueArea, process);
        layout.setComponentAlignment(process, Alignment.MIDDLE_RIGHT);
        layout.setSpacing(true);
        return layout;
    }

    private Component buildMetadataComponent() {
        Label label = new Label($("AdvancedSearchView.batchProcessField"));

        metadata = new ComboBox();
        metadata.setItemCaptionMode(AbstractSelect.ItemCaptionMode.EXPLICIT);
        metadata.setNullSelectionAllowed(false);
        for (MetadataVO metadata : presenter.getMetadataAllowedInBatchEdit()) {
            this.metadata.addItem(metadata);
            this.metadata.setItemCaption(metadata, metadata.getLabel());
        }
        metadata.addValueChangeListener(new Property.ValueChangeListener() {
            @Override
            public void valueChange(Property.ValueChangeEvent event) {
                if (value != null) {
                    valueArea.removeComponent(value);
                }
                value = buildValueField();
                valueArea.addComponent(value);
            }
        });

        HorizontalLayout layout = new HorizontalLayout(label, metadata);
        layout.setSpacing(true);
        return layout;
    }

    private Field buildValueField() {
        final Field field = factory.build((MetadataVO) metadata.getValue());
        field.setCaption(null);
        field.setWidthUndefined();
        field.setPropertyDataSource(new ObjectProperty<>(null, Object.class));
        return field;
    }
}
