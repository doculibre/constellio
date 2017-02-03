package com.constellio.app.modules.rm.ui.components.container.fields;

import com.constellio.app.modules.rm.ui.components.folder.fields.CustomFolderField;
import com.constellio.app.modules.rm.wrappers.StorageSpace;
import com.constellio.app.services.factories.ConstellioFactories;
import com.constellio.app.ui.application.ConstellioUI;
import com.constellio.app.ui.framework.buttons.WindowButton;
import com.constellio.app.ui.framework.components.fields.lookup.LookupRecordField;
import com.constellio.app.ui.framework.data.RecordTextInputDataProvider;
import com.constellio.app.ui.pages.base.SessionContext;
import com.constellio.model.entities.schemas.MetadataSchemaType;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators;
import com.vaadin.data.Property;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Window;

import java.util.List;

import static com.constellio.app.ui.i18n.i18n.$;
import static java.util.Arrays.asList;

/**
 * Created by Constellio on 2017-01-11.
 */
public class ContainerStorageSpaceLookupField extends LookupRecordField implements CustomFolderField<String> {

    private String containerRecordType;

    public ContainerStorageSpaceLookupField(String containerRecordType) {
        super(StorageSpace.SCHEMA_TYPE, null);
        this.containerRecordType =  containerRecordType;
    }

    @Override
    protected Component initContent() {
        HorizontalLayout horizontalLayout = ((HorizontalLayout) super.initContent());
        horizontalLayout.addComponent(buildNewLookupButton(), 2);
        return horizontalLayout;
    }

    private Component buildNewLookupButton() {
        final WindowButton lookupButton = new WindowButton($("ContainerStorageLookupField.suggested"), $("ContainerStorageLookupField.suggested")) {
            @Override
            protected Component buildWindowContent() {
                return new LookupWindowContent(getWindow()) {
                    @Override
                    public List<LookupTreeDataProvider<String>> getLookupTreeDataProviders() {
                        return asList();
                    }

                    @Override
                    public TextInputDataProvider geSuggestInputDataProvider() {
                        ConstellioFactories constellioFactories = ConstellioFactories.getInstance();
                        SessionContext sessionContext = ConstellioUI.getCurrentSessionContext();

                        return new RecordTextInputDataProvider(constellioFactories, sessionContext, StorageSpace.SCHEMA_TYPE, false) {
                            @Override
                            public List<String> getData(String text, int startIndex, int count) {
                                return getModelLayerFactory().newSearchServices().searchRecordIds(buildQuery().setStartRow(startIndex).setNumberOfRows(count));
                            }

                            @Override
                            public int size(String text) {
                                return (int) getModelLayerFactory().newSearchServices().getResultsCount(buildQuery());
                            }

                            private LogicalSearchQuery buildQuery() {
                                MetadataSchemaType type = getModelLayerFactory().getMetadataSchemasManager()
                                        .getSchemaTypes(getCurrentCollection()).getSchemaType(schemaTypeCode);
                                return new LogicalSearchQuery().setCondition(LogicalSearchQueryOperators.from(type).where(type.getDefaultSchema().get(StorageSpace.TITLE)).isStartingWithText("E"));
                            }
                        };
                    }
                };
            }


        };
        lookupButton.addStyleName(OPEN_WINDOW_BUTTON_STYLE_NAME);

        addValueChangeListener(new ValueChangeListener() {
            @Override
            public void valueChange(Property.ValueChangeEvent event) {
                if (lookupButton.getWindow() != null) {
                    Window lookupWindow = lookupButton.getWindow();
                    lookupWindow.close();
                }
            }
        });

        return lookupButton;
    }

    @Override
    public String getFieldValue() {
        return (String) getConvertedValue();
    }

    @Override
    public void setFieldValue(Object value) {
        setInternalValue((String) value);
    }
}