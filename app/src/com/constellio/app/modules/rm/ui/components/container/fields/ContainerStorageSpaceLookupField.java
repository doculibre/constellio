package com.constellio.app.modules.rm.ui.components.container.fields;

import static com.constellio.app.ui.i18n.i18n.$;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.anyConditions;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.where;
import static java.util.Arrays.asList;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import com.constellio.app.modules.rm.ui.components.folder.fields.CustomFolderField;
import com.constellio.app.modules.rm.ui.pages.containers.edit.AddEditContainerPresenter;
import com.constellio.app.modules.rm.wrappers.StorageSpace;
import com.constellio.app.services.factories.ConstellioFactories;
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

/**
 * Created by Constellio on 2017-01-11.
 */
public class ContainerStorageSpaceLookupField extends LookupRecordField implements CustomFolderField<String> {

	private String containerRecordType;
	private Double containerCapacity;

	private AddEditContainerPresenter presenter;

	public ContainerStorageSpaceLookupField(String containerRecordType, Double containerCapacity,
			AddEditContainerPresenter presenter) {
		this(StorageSpace.SCHEMA_TYPE, null, containerRecordType, containerCapacity, presenter);
		this.containerRecordType = containerRecordType;
		this.containerCapacity = containerCapacity;
		this.presenter = presenter;
	}

	private ContainerStorageSpaceLookupField(String schemaTypeCode, String schemaCode, String containerRecordType,
			Double containerCapacity, AddEditContainerPresenter presenter) {
		this(schemaTypeCode, schemaCode, false, containerRecordType, containerCapacity, presenter);
	}

	private ContainerStorageSpaceLookupField(String schemaTypeCode, String schemaCode, boolean writeAccess,
			String containerRecordType, Double containerCapacity, AddEditContainerPresenter presenter) {
		super(new RecordTextInputDataProvider(presenter.getConstellioFactories(), presenter.getSessionContext(), schemaTypeCode,
						schemaCode, writeAccess),
				getTreeDataProvider(schemaTypeCode, schemaCode, writeAccess, false, containerRecordType, containerCapacity,
						presenter));
		setItemConverter(new RecordIdToCaptionConverter());
	}

	@Override
	protected Component initContent() {
		HorizontalLayout horizontalLayout = ((HorizontalLayout) super.initContent());
		horizontalLayout.addComponent(buildSuggestedButton(), 2);
		return horizontalLayout;
	}

	private Component buildSuggestedButton() {
		final Button suggestedButton = new Button($("ContainerStorageLookupField.suggested"));
		suggestedButton.addStyleName(OPEN_WINDOW_BUTTON_STYLE_NAME);

		suggestedButton.addClickListener(new Button.ClickListener() {
			@Override
			public void buttonClick(Button.ClickEvent event) {
				suggestedButtonClicked();
			}
		});

		return suggestedButton;
	}

	public void suggestedButtonClicked() {
		List<Record> recordList = presenter.getModelLayerFactory().newSearchServices()
				.search(buildQuery(presenter.getModelLayerFactory(), presenter.getSessionContext()));
		if (recordList != null && !recordList.isEmpty()) {
			ContainerStorageSpaceLookupField.this.setFieldValue(recordList.get(0).getId());
			presenter.setStorageSpaceTo(recordList.get(0).getId());
		}
	}

	private LogicalSearchQuery buildQuery(ModelLayerFactory modelLayerFactory, SessionContext sessionContext) {
		MetadataSchemaType storageSpaceType = modelLayerFactory.getMetadataSchemasManager()
				.getSchemaTypes(sessionContext.getCurrentCollection()).getSchemaType(StorageSpace.SCHEMA_TYPE);
		Double containerCapacity = this.containerCapacity == null ? 0.0 : this.containerCapacity;
		return new LogicalSearchQuery().setCondition(from(storageSpaceType).whereAllConditions(
				where(storageSpaceType.getDefaultSchema().get(StorageSpace.NUMBER_OF_CHILD)).isEqualTo(0),
				anyConditions(
						where(storageSpaceType.getDefaultSchema().get(StorageSpace.AVAILABLE_SIZE))
								.isGreaterOrEqualThan(containerCapacity),
						where(storageSpaceType.getDefaultSchema().get(StorageSpace.AVAILABLE_SIZE)).isNull()
				),
				anyConditions(
						where(storageSpaceType.getDefaultSchema().get(StorageSpace.CONTAINER_TYPE))
								.isContaining(asList(containerRecordType)),
						where(storageSpaceType.getDefaultSchema().get(StorageSpace.CONTAINER_TYPE)).isNull()
				)
		));
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
			boolean writeAccess, boolean onlySuggestions, final String containerRecordType,
			Double containerCapacity, AddEditContainerPresenter presenter) {
		SessionContext sessionContext = presenter.getSessionContext();
		final String collection = sessionContext.getCurrentCollection();
		UserVO currentUserVO = sessionContext.getCurrentUser();

		ConstellioFactories constellioFactories = presenter.getConstellioFactories();
		final ModelLayerFactory modelLayerFactory = constellioFactories.getModelLayerFactory();
		UserServices userServices = modelLayerFactory.newUserServices();
		MetadataSchemasManager metadataSchemasManager = modelLayerFactory.getMetadataSchemasManager();
		TaxonomiesManager taxonomiesManager = modelLayerFactory.getTaxonomiesManager();

		User currentUser = userServices.getUserInCollection(currentUserVO.getUsername(), collection);
		List<Taxonomy> taxonomies;
		if (schemaTypeCode != null) {
			taxonomies = taxonomiesManager
					.getAvailableTaxonomiesForSelectionOfType(schemaTypeCode, currentUser, metadataSchemasManager);
		} else {
			taxonomies = taxonomiesManager.getAvailableTaxonomiesForSchema(schemaCode, currentUser, metadataSchemasManager);
		}
		List<RecordLookupTreeDataProvider> dataProviders = new ArrayList<>();
		for (Taxonomy taxonomy : taxonomies) {
			String taxonomyCode = taxonomy.getCode();
			if (StringUtils.isNotBlank(taxonomyCode)) {
				dataProviders.add(new RecordLookupTreeDataProvider(schemaTypeCode, writeAccess,
						getDataProvider(taxonomyCode, containerRecordType, containerCapacity, presenter)));
			}
		}
		return !dataProviders.isEmpty() ? dataProviders.toArray(new RecordLookupTreeDataProvider[0]) : null;
	}

	static public LinkableRecordTreeNodesDataProvider getDataProvider(String taxonomyCode, String containerRecordType,
			Double containerCapacity, AddEditContainerPresenter presenter) {
		ConstellioFactories constellioFactories = presenter.getConstellioFactories();
		SessionContext sessionContext = presenter.getSessionContext();
		MetadataSchemaType storageSpaceType = constellioFactories.getModelLayerFactory().getMetadataSchemasManager()
				.getSchemaTypes(sessionContext.getCurrentCollection()).getSchemaType(StorageSpace.SCHEMA_TYPE);
		containerCapacity = containerCapacity == null ? 0.0 : containerCapacity;
		LogicalSearchCondition searchCondition = from(storageSpaceType).whereAllConditions(
				where(storageSpaceType.getDefaultSchema().get(StorageSpace.NUMBER_OF_CHILD)).isEqualTo(0),
				anyConditions(
						where(storageSpaceType.getDefaultSchema().get(StorageSpace.AVAILABLE_SIZE))
								.isGreaterOrEqualThan(containerCapacity),
						where(storageSpaceType.getDefaultSchema().get(StorageSpace.AVAILABLE_SIZE)).isNull()
				),
				anyConditions(
						where(storageSpaceType.getDefaultSchema().get(StorageSpace.CONTAINER_TYPE))
								.isContaining(asList(containerRecordType)),
						where(storageSpaceType.getDefaultSchema().get(StorageSpace.CONTAINER_TYPE)).isNull()
				));
		TaxonomiesSearchFilter taxonomiesSearchFilter = new TaxonomiesSearchFilter();
//		taxonomiesSearchFilter.setLinkableConceptsFilter(new LinkableConceptFilter() {
		//			@Override
		//			public boolean isLinkable(LinkableConceptFilterParams params) {
		//
		//				RMSchemasRecordsServices rm = new RMSchemasRecordsServices(params.getRecord().getCollection(),
		//						ConstellioFactories.getInstance().getAppLayerFactory());
		//
		//				Category category = rm.wrapCategory(params.getRecord());
		//				return LangUtils.isFalseOrNull(category.<Boolean>get(Category.DEACTIVATE));
		//			}
		//		});

		return new LinkableRecordTreeNodesDataProvider(taxonomyCode, StorageSpace.SCHEMA_TYPE, false, taxonomiesSearchFilter);
	}
}