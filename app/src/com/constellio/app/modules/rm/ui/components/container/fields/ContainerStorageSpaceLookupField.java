package com.constellio.app.modules.rm.ui.components.container.fields;

import com.constellio.app.modules.rm.ui.components.folder.fields.CustomFolderField;
import com.constellio.app.modules.rm.wrappers.StorageSpace;
import com.constellio.app.services.factories.ConstellioFactories;
import com.constellio.app.ui.application.ConstellioUI;
import com.constellio.app.ui.entities.UserVO;
import com.constellio.app.ui.framework.components.converters.RecordIdToCaptionConverter;
import com.constellio.app.ui.framework.components.fields.lookup.LookupRecordField;
import com.constellio.app.ui.framework.data.RecordLookupTreeDataProvider;
import com.constellio.app.ui.framework.data.RecordTextInputDataProvider;
import com.constellio.app.ui.framework.data.trees.LinkableRecordTreeNodesDataProvider;
import com.constellio.app.ui.pages.base.SessionContext;
import com.constellio.model.entities.Taxonomy;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.MetadataSchemaType;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.schemas.MetadataSchemasManager;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.constellio.model.services.search.query.logical.condition.LogicalSearchCondition;
import com.constellio.model.services.taxonomies.TaxonomiesManager;
import com.constellio.model.services.taxonomies.TaxonomiesSearchFilter;
import com.constellio.model.services.users.UserServices;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.List;

import static com.constellio.app.ui.i18n.i18n.$;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.*;
import static java.util.Arrays.asList;

/**
 * Created by Constellio on 2017-01-11.
 */
public class ContainerStorageSpaceLookupField extends LookupRecordField implements CustomFolderField<String> {

    private String containerRecordType;

    public ContainerStorageSpaceLookupField(String containerRecordType) {
        this(StorageSpace.SCHEMA_TYPE, null, containerRecordType);
        this.containerRecordType =  containerRecordType;

    }

    private ContainerStorageSpaceLookupField(String schemaTypeCode, String schemaCode, String containerRecordType) {
        this(schemaTypeCode, schemaCode, false, containerRecordType);
    }

    private ContainerStorageSpaceLookupField(String schemaTypeCode, String schemaCode, boolean writeAccess, String containerRecordType) {
        super(new RecordTextInputDataProvider(ConstellioFactories.getInstance(), ConstellioUI.getCurrentSessionContext(), schemaTypeCode, schemaCode, writeAccess),
                getTreeDataProvider(schemaTypeCode, schemaCode, writeAccess, false, containerRecordType));
        setItemConverter(new RecordIdToCaptionConverter());
    }

    @Override
    protected Component initContent() {
        HorizontalLayout horizontalLayout = ((HorizontalLayout) super.initContent());
        horizontalLayout.addComponent(buildNewLookupButton2(), 2);
        return horizontalLayout;
    }

    private Component buildNewLookupButton2() {
        final Button suggestedButton = new Button($("ContainerStorageLookupField.suggested"));
        suggestedButton.addStyleName(OPEN_WINDOW_BUTTON_STYLE_NAME);

        suggestedButton.addClickListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                ConstellioFactories constellioFactories = ConstellioFactories.getInstance();
                SessionContext sessionContext = ConstellioUI.getCurrentSessionContext();
                List<Record> recordList = constellioFactories.getModelLayerFactory().newSearchServices().search(buildQuery(constellioFactories.getModelLayerFactory(), sessionContext));
                if(recordList != null && !recordList.isEmpty()) {
                    ContainerStorageSpaceLookupField.this.setFieldValue(recordList.get(0).getId());
                }
            }

            private LogicalSearchQuery buildQuery(ModelLayerFactory modelLayerFactory, SessionContext sessionContext) {
                MetadataSchemaType storageSpaceType = modelLayerFactory.getMetadataSchemasManager()
                        .getSchemaTypes(sessionContext.getCurrentCollection()).getSchemaType(StorageSpace.SCHEMA_TYPE);
                return new LogicalSearchQuery().setCondition(from(storageSpaceType).whereAllConditions(
                        where(storageSpaceType.getDefaultSchema().get(StorageSpace.NUMBER_OF_CHILD)).isEqualTo(0),
                        where(storageSpaceType.getDefaultSchema().get(StorageSpace.AVAILABLE_SIZE)).isGreaterOrEqualThan(0),
                        anyConditions(
                                where(storageSpaceType.getDefaultSchema().get(StorageSpace.CONTAINER_TYPE)).isContaining(asList(containerRecordType)),
                                where(storageSpaceType.getDefaultSchema().get(StorageSpace.CONTAINER_TYPE)).isNull()
                        )
                ));
            }
        });

        return suggestedButton;
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
                dataProviders.add(new RecordLookupTreeDataProvider(schemaTypeCode, writeAccess, getDataProvider(taxonomyCode, containerRecordType)));
            }
        }
        return !dataProviders.isEmpty() ? dataProviders.toArray(new RecordLookupTreeDataProvider[0]) : null;
    }

    static public LinkableRecordTreeNodesDataProvider getDataProvider(String taxonomyCode, String containerRecordType) {
        ConstellioFactories constellioFactories = ConstellioFactories.getInstance();
        SessionContext sessionContext = ConstellioUI.getCurrentSessionContext();
        MetadataSchemaType storageSpaceType = constellioFactories.getModelLayerFactory().getMetadataSchemasManager()
                .getSchemaTypes(sessionContext.getCurrentCollection()).getSchemaType(StorageSpace.SCHEMA_TYPE);
        LogicalSearchCondition searchCondition = from(storageSpaceType).whereAllConditions(
                where(storageSpaceType.getDefaultSchema().get(StorageSpace.NUMBER_OF_CHILD)).isEqualTo(0),
                where(storageSpaceType.getDefaultSchema().get(StorageSpace.AVAILABLE_SIZE)).isGreaterOrEqualThan(0),
                anyConditions(
                        where(storageSpaceType.getDefaultSchema().get(StorageSpace.CONTAINER_TYPE)).isContaining(asList(containerRecordType)),
                        where(storageSpaceType.getDefaultSchema().get(StorageSpace.CONTAINER_TYPE)).isNull()
                ));
        TaxonomiesSearchFilter taxonomiesSearchFilter = new TaxonomiesSearchFilter();
        taxonomiesSearchFilter.setLinkableConceptsCondition(searchCondition);

        return new LinkableRecordTreeNodesDataProvider(taxonomyCode, StorageSpace.SCHEMA_TYPE, false, taxonomiesSearchFilter);
    }
}