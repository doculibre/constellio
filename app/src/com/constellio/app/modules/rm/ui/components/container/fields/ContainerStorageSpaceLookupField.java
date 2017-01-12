package com.constellio.app.modules.rm.ui.components.container.fields;

import com.constellio.app.modules.rm.ui.components.folder.fields.CustomFolderField;
import com.constellio.app.modules.rm.wrappers.StorageSpace;
import com.constellio.app.services.factories.ConstellioFactories;
import com.constellio.app.ui.application.ConstellioUI;
import com.constellio.app.ui.entities.UserVO;
import com.constellio.app.ui.framework.buttons.WindowButton;
import com.constellio.app.ui.framework.components.converters.RecordIdToCaptionConverter;
import com.constellio.app.ui.framework.components.fields.lookup.LookupRecordField;
import com.constellio.app.ui.framework.data.ObjectsResponse;
import com.constellio.app.ui.framework.data.RecordLookupTreeDataProvider;
import com.constellio.app.ui.framework.data.RecordTextInputDataProvider;
import com.constellio.app.ui.pages.base.SessionContext;
import com.constellio.model.entities.Taxonomy;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.schemas.MetadataSchemasManager;
import com.constellio.model.services.taxonomies.TaxonomiesManager;
import com.constellio.model.services.users.UserServices;
import com.vaadin.data.Property;
import com.vaadin.server.ThemeResource;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.themes.ValoTheme;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

import static com.constellio.app.services.factories.ConstellioFactories.getInstance;
import static com.constellio.app.ui.application.ConstellioUI.getCurrentSessionContext;
import static com.constellio.app.ui.i18n.i18n.$;
import static java.util.Arrays.asList;

/**
 * Created by Constellio on 2017-01-11.
 */
public class ContainerStorageSpaceLookupField extends LookupRecordField implements CustomFolderField<String> {

    private String containerRecordType;

    public ContainerStorageSpaceLookupField(String containerRecordType) {
        this(StorageSpace.SCHEMA_TYPE, null, containerRecordType);

    }

    private ContainerStorageSpaceLookupField(String schemaTypeCode, String schemaCode, String containerRecordType) {
        this(schemaTypeCode, schemaCode, false, containerRecordType);
    }

    private ContainerStorageSpaceLookupField(String schemaTypeCode, String schemaCode, boolean writeAccess, String containerRecordType) {
        super(new RecordTextInputDataProvider(getInstance(), getCurrentSessionContext(), schemaTypeCode, schemaCode, writeAccess),
                getTreeDataProvider(schemaTypeCode, schemaCode, writeAccess, false, containerRecordType));
        setItemConverter(new RecordIdToCaptionConverter());
        this.containerRecordType = containerRecordType;
    }

    @Override
    protected Component initContent() {
        HorizontalLayout horizontalLayout = ((HorizontalLayout) super.initContent());
        horizontalLayout.addComponent(buildNewLookupButton(), 1);
        return horizontalLayout;
    }

    private Component buildNewLookupButton() {
        final WindowButton lookupButton = new WindowButton(null, $("asd")) {
            @Override
            protected Component buildWindowContent() {
                return new LookupWindowContent(getWindow()) {
                    @Override
                    public List<LookupTreeDataProvider<String>> getLookupTreeDataProviders() {
                        return asList(getTreeDataProvider(StorageSpace.SCHEMA_TYPE, null, false, true, containerRecordType));
                    }
                };
            }
        };
        lookupButton.setIcon(new ThemeResource("images/icons/actions/view.png"));
        lookupButton.addStyleName(ValoTheme.BUTTON_ICON_ONLY);
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

    private static LookupTreeDataProvider<String>[] getTreeDataProvider(final String schemaTypeCode, final String schemaCode,
                                                                        boolean writeAccess, boolean onlySuggestions, final String containerRecordType) {
        SessionContext sessionContext = ConstellioUI.getCurrentSessionContext();
        final String collection = sessionContext.getCurrentCollection();
        UserVO currentUserVO = sessionContext.getCurrentUser();

        ConstellioFactories constellioFactories = ConstellioFactories.getInstance();
        final ModelLayerFactory modelLayerFactory = constellioFactories.getModelLayerFactory();
        UserServices userServices = modelLayerFactory.newUserServices();
        MetadataSchemasManager metadataSchemasManager = modelLayerFactory.getMetadataSchemasManager();
        TaxonomiesManager taxonomiesManager = modelLayerFactory.getTaxonomiesManager();

        User currentUser = userServices.getUserInCollection(currentUserVO.getUsername(), collection);
        List<Taxonomy> taxonomies;
        if (schemaTypeCode != null) {
            taxonomies = taxonomiesManager.getAvailableTaxonomiesForSelectionOfType(schemaTypeCode, currentUser, metadataSchemasManager);
        } else {
            taxonomies = taxonomiesManager.getAvailableTaxonomiesForSchema(schemaCode, currentUser, metadataSchemasManager);
        }
        List<RecordLookupTreeDataProvider> dataProviders = new ArrayList<>();
        for (Taxonomy taxonomy : taxonomies) {
            String taxonomyCode = taxonomy.getCode();
            if (StringUtils.isNotBlank(taxonomyCode)) {
                if(onlySuggestions) {
                    dataProviders.add(new RecordLookupTreeDataProvider(schemaTypeCode, taxonomyCode, writeAccess) {
                        @Override
                        public ObjectsResponse<String> getRootObjects(int start, int maxSize) {
                            return super.getRootObjects(start, maxSize);
                        }

                        @Override
                        public boolean isSelectable(String selection) {
                            return isLeaf(selection);
                        }
                    });
                } else {
                    dataProviders.add(new RecordLookupTreeDataProvider(schemaTypeCode, taxonomyCode, writeAccess) {
                        @Override
                        public boolean isSelectable(String selection) {
                            StorageSpace storageSpace = new StorageSpace(getRecord(modelLayerFactory, selection),
                                    modelLayerFactory.getMetadataSchemasManager().getSchemaTypes(collection));

                            return storageSpace.getContainerType() == null || storageSpace.getContainerType().isEmpty()
                                    || storageSpace.getContainerType().contains(containerRecordType);
                        }
                    });
                }
            }
        }
        return !dataProviders.isEmpty() ? dataProviders.toArray(new RecordLookupTreeDataProvider[0]) : null;
    }
}