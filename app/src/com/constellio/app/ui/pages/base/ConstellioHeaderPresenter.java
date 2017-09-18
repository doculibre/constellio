package com.constellio.app.ui.pages.base;

import com.constellio.app.api.extensions.params.AvailableActionsParam;
import com.constellio.app.entities.navigation.NavigationConfig;
import com.constellio.app.entities.navigation.NavigationItem;
import com.constellio.app.entities.schemasDisplay.MetadataDisplayConfig;
import com.constellio.app.entities.schemasDisplay.SchemaTypeDisplayConfig;
import com.constellio.app.extensions.AppLayerCollectionExtensions;
import com.constellio.app.modules.rm.constants.RMPermissionsTo;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.ui.builders.UserToVOBuilder;
import com.constellio.app.modules.rm.wrappers.*;
import com.constellio.app.services.extensions.ConstellioModulesManagerImpl;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.services.factories.ConstellioFactories;
import com.constellio.app.services.schemasDisplay.SchemasDisplayManager;
import com.constellio.app.ui.entities.*;
import com.constellio.app.ui.entities.RecordVO.VIEW_MODE;
import com.constellio.app.ui.framework.builders.MetadataSchemaToVOBuilder;
import com.constellio.app.ui.framework.builders.MetadataSchemaTypeToVOBuilder;
import com.constellio.app.ui.framework.builders.MetadataToVOBuilder;
import com.constellio.app.ui.framework.builders.RecordToVOBuilder;
import com.constellio.app.ui.framework.components.ComponentState;
import com.constellio.app.ui.framework.data.RecordVODataProvider;
import com.constellio.app.ui.i18n.i18n;
import com.constellio.app.ui.pages.search.AdvancedSearchCriteriaComponent.SearchCriteriaPresenter;
import com.constellio.app.ui.pages.search.criteria.Criterion;
import com.constellio.data.dao.dto.records.FacetValue;
import com.constellio.data.utils.ImpossibleRuntimeException;
import com.constellio.data.utils.TimeProvider;
import com.constellio.data.utils.dev.Toggle;
import com.constellio.model.entities.Language;
import com.constellio.model.entities.Taxonomy;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.*;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.records.RecordServicesException;
import com.constellio.model.services.records.RecordServicesRuntimeException.NoSuchRecordWithId;
import com.constellio.model.services.schemas.MetadataList;
import com.constellio.model.services.schemas.MetadataSchemasManager;
import com.constellio.model.services.schemas.SchemaUtils;
import com.constellio.model.services.schemas.builders.CommonMetadataBuilder;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators;
import com.constellio.model.services.users.UserServices;
import com.vaadin.server.Page;
import com.vaadin.server.Resource;
import com.vaadin.ui.Component;
import com.vaadin.ui.Notification;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.util.*;

import static com.constellio.app.ui.i18n.i18n.$;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;
import static java.util.Arrays.asList;

public class ConstellioHeaderPresenter implements SearchCriteriaPresenter {

	Boolean allItemsSelected = true;

	Boolean allItemsDeselected = false;

	private final ConstellioHeader header;
	private String schemaTypeCode;
	private transient AppLayerFactory appLayerFactory;
	private transient ModelLayerFactory modelLayerFactory;
	private transient SchemasDisplayManager schemasDisplayManager;
	private boolean advancedSearchFormVisible;

	private BasePresenterUtils presenterUtils;

	private UserToVOBuilder voBuilder = new UserToVOBuilder();

	private boolean refreshSelectionPanel;
	private Map<String, String> deselectedRecordsWithSchema;

	public ConstellioHeaderPresenter(ConstellioHeader header) {
		this.header = header;
		init();
	}

	public void searchRequested(String expression, String schemaTypeCode) {
		if (StringUtils.isNotBlank(schemaTypeCode)) {
			header.hideAdvancedSearchPopup().navigateTo().advancedSearch();
		} else if (StringUtils.isNotBlank(expression)) {
			header.hideAdvancedSearchPopup().navigateTo().simpleSearch(expression);
		}
	}

	public void addCriterionRequested() {
		header.addEmptyCriterion();
	}

	public void savedSearchPageRequested() {
		header.hideAdvancedSearchPopup().navigateTo().listSavedSearches();
	}

	public void schemaTypeSelected(String schemaTypeCode) {
		this.schemaTypeCode = schemaTypeCode;
		header.setAdvancedSearchSchemaType(schemaTypeCode);
	}

	public List<MetadataSchemaTypeVO> getSchemaTypes() {
		MetadataSchemaTypeToVOBuilder builder = new MetadataSchemaTypeToVOBuilder();

		List<MetadataSchemaTypeVO> result = new ArrayList<>();
		MetadataSchemaTypes types = types();
		if (types != null) {
			for (MetadataSchemaType type : types.getSchemaTypes()) {
				SchemaTypeDisplayConfig config = schemasDisplayManager().getType(header.getCollection(), type.getCode());
				if (config.isAdvancedSearch() && isVisibleForUser(type, getCurrentUser())) {
					result.add(builder.build(type));
				}
			}
		}
		return result;
	}

	private boolean isVisibleForUser(MetadataSchemaType type, User currentUser) {
		if (ContainerRecord.SCHEMA_TYPE.equals(type.getCode()) && !currentUser
				.hasAny(RMPermissionsTo.DISPLAY_CONTAINERS, RMPermissionsTo.MANAGE_CONTAINERS)
				.onSomething()) {
			return false;
		} else if (StorageSpace.SCHEMA_TYPE.equals(type.getCode()) && !currentUser.has(RMPermissionsTo.MANAGE_STORAGE_SPACES).globally()) {
			return false;
		}
		return true;
	}

	public String getSchemaType() {
		return schemaTypeCode;
	}

	public boolean isValidAdvancedSearchCriterionPresent() {
		return schemaTypeCode != null;
	}

	@Override
	public List<MetadataVO> getMetadataAllowedInCriteria() {
		MetadataSchemaType schemaType = types().getSchemaType(schemaTypeCode);
		List<FacetValue> schema_s = modelLayerFactory.newSearchServices().query(new LogicalSearchQuery()
				.setNumberOfRows(0)
				.setCondition(from(schemaType).returnAll()).addFieldFacet("schema_s").filteredWithUser(getCurrentUser()))
				.getFieldFacetValues("schema_s");
		Set<String> metadataCodes = new HashSet<>();

		if (Toggle.RESTRICT_METADATAS_TO_THOSE_OF_SCHEMAS_WITH_RECORDS.isEnabled()) {
			if (schema_s != null) {
				for (FacetValue facetValue : schema_s) {
					if (facetValue.getQuantity() > 0) {
						String schema = facetValue.getValue();
						for (Metadata metadata : types().getSchema(schema).getMetadatas()) {
							if (metadata.getInheritance() != null && metadata.isEnabled()) {
								metadataCodes.add(metadata.getInheritance().getCode());
							} else if (metadata.getInheritance() == null && metadata.isEnabled()) {
								metadataCodes.add(metadata.getCode());
							}
						}
					}
				}
			}
		} else {
			for (Metadata metadata : schemaType.getAllMetadatas()) {
				metadataCodes.add(metadata.getCode());
			}
		}

		MetadataToVOBuilder builder = new MetadataToVOBuilder();

		List<MetadataVO> result = new ArrayList<>();
//		result.add(builder.build(schemaType.getMetadataWithAtomicCode(CommonMetadataBuilder.PATH), header.getSessionContext()));
		MetadataList allMetadatas = schemaType.getAllMetadatas();
		for (Metadata metadata : allMetadatas) {
			if (!schemaType.hasSecurity() || (metadataCodes.contains(metadata.getCode()))) {
				boolean isTextOrString =
						metadata.getType() == MetadataValueType.STRING || metadata.getType() == MetadataValueType.TEXT;
				MetadataDisplayConfig config = schemasDisplayManager().getMetadata(header.getCollection(), metadata.getCode());
				if (config.isVisibleInAdvancedSearch() && isMetadataVisibleForUser(metadata, getCurrentUser()) && (!isTextOrString
						|| (isTextOrString && metadata.isSearchable()) || Schemas.PATH.getLocalCode().equals(metadata.getLocalCode()))) {
					result.add(builder.build(metadata, header.getSessionContext()));
				}
			}
		}
		sort(result);
		return result;
	}

	private boolean isMetadataVisibleForUser(Metadata metadata, User currentUser) {
		if (MetadataValueType.REFERENCE.equals(metadata.getType())) {
			String referencedSchemaType = metadata.getAllowedReferences().getTypeWithAllowedSchemas();
			Taxonomy taxonomy = appLayerFactory.getModelLayerFactory().getTaxonomiesManager()
					.getTaxonomyFor(header.getCollection(), referencedSchemaType);
			if (taxonomy != null) {
				List<String> taxonomyGroupIds = taxonomy.getGroupIds();
				List<String> taxonomyUserIds = taxonomy.getUserIds();
				List<String> userGroups = currentUser.getUserGroups();
				for (String group : taxonomyGroupIds) {
					for (String userGroup : userGroups) {
						if (userGroup.equals(group)) {
							return true;
						}
					}
				}
				return (taxonomyGroupIds.isEmpty() && taxonomyUserIds.isEmpty()) || taxonomyUserIds.contains(currentUser.getId());
			} else {
				return true;
			}
		}
		return true;
	}

	protected void sort(List<MetadataVO> metadataVOs) {
		Collections.sort(metadataVOs, new Comparator<MetadataVO>() {
			@Override
			public int compare(MetadataVO o1, MetadataVO o2) {
				return o1.getLabel().toLowerCase().compareTo(o2.getLabel().toLowerCase());
			}
		});
	}

	@Override
	public MetadataVO getMetadataVO(String metadataCode) {
		if (metadataCode != null) {
			MetadataToVOBuilder builder = new MetadataToVOBuilder();
			MetadataSchemaTypes types = types();
			Metadata metadata = types.getMetadata(metadataCode);
			return builder.build(metadata);
		} else {
			return null;
		}

	}

	@Override
	public Component getExtensionComponentForCriterion(Criterion criterion) {
		AppLayerCollectionExtensions extensions = appLayerFactory.getExtensions().forCollection(header.getCollection());
		return extensions.getComponentForCriterion(criterion);
	}

	private MetadataSchemaTypes types() {
		MetadataSchemasManager metadataSchemasManager = modelLayerFactory.getMetadataSchemasManager();
		return metadataSchemasManager.getSchemaTypes(header.getCollection());
	}

	private SchemasDisplayManager schemasDisplayManager() {
		if (schemasDisplayManager == null) {
			schemasDisplayManager = appLayerFactory.getMetadataSchemasDisplayManager();
		}
		return schemasDisplayManager;
	}

	private void readObject(java.io.ObjectInputStream stream)
			throws IOException, ClassNotFoundException {
		stream.defaultReadObject();
		init();
	}

	private void init() {
		ConstellioFactories constellioFactories = header.getConstellioFactories();
		SessionContext sessionContext = header.getSessionContext();
		appLayerFactory = constellioFactories.getAppLayerFactory();
		modelLayerFactory = constellioFactories.getModelLayerFactory();
		this.presenterUtils = new BasePresenterUtils(constellioFactories, sessionContext);
		this.deselectedRecordsWithSchema = new HashMap<>();

		UserServices userServices = modelLayerFactory.newUserServices();
		List<String> collections = userServices.getUser(getCurrentUser().getUsername()).getCollections();
		header.setCollections(collections);

		if (sessionContext.getSelectedRecordIds().isEmpty()) {
			header.setSelectionButtonEnabled(false);
		} else {
			header.setSelectionButtonEnabled(true);
		}
		updateSelectionCount();
	}

	/**
	 * FIXME Remove from presenter
	 */
	public Resource getUserLogoResource() {
		return LogoUtils.getUserLogoResource(modelLayerFactory);
	}

	public List<NavigationItem> getActionMenuItems() {
		return navigationConfig().getNavigation(ConstellioHeader.ACTION_MENU);
	}

	private NavigationConfig navigationConfig() {
		ConstellioModulesManagerImpl manager = (ConstellioModulesManagerImpl) appLayerFactory.getModulesManager();
		return manager.getNavigationConfig(header.getCollection());
	}

	public ComponentState getStateFor(NavigationItem item) {
		return item.getStateFor(getCurrentUser(), appLayerFactory);
	}

	protected User getCurrentUser() {
		return presenterUtils.getCurrentUser();
	}

	public void collectionClicked(String newCollection) {
		SessionContext sessionContext = header.getSessionContext();
		String currentCollection = sessionContext.getCurrentCollection();
		if (!currentCollection.equals(newCollection)) {
			sessionContext.clearSelectedRecordIds();
			String username = getCurrentUser().getUsername();
			UserServices userServices = modelLayerFactory.newUserServices();
			User newUser = userServices.getUserInCollection(username, newCollection);
			try {
				modelLayerFactory.newRecordServices().update(newUser
						.setLastLogin(TimeProvider.getLocalDateTime())
						.setLastIPAddress(sessionContext.getCurrentUserIPAddress()));
				Locale userLanguage = getSessionLanguage(newUser);
				sessionContext.setCurrentLocale(userLanguage);
				i18n.setLocale(userLanguage);

			} catch (RecordServicesException e) {
				throw new RuntimeException(e);
			}
			UserVO newUserVO = voBuilder.build(newUser.getWrappedRecord(), VIEW_MODE.DISPLAY, sessionContext);
			sessionContext.setCurrentCollection(newCollection);
			sessionContext.setCurrentUser(newUserVO);

			header.navigateTo().home();
			header.updateUIContent();
		}
	}

	//FIXME use service and remove redundant code in LoginPresenter
	Locale getSessionLanguage(User userInLastCollection) {
		String userPreferredLanguage = userInLastCollection.getLoginLanguageCode();
		String systemLanguage = modelLayerFactory.getConfiguration().getMainDataLanguage();
		if (StringUtils.isBlank(userPreferredLanguage)) {
			return getLocale(systemLanguage);
		} else {
			List<String> collectionLanguages = modelLayerFactory.getCollectionsListManager()
					.getCollectionLanguages(userInLastCollection.getCollection());
			if (collectionLanguages == null || collectionLanguages.isEmpty() || !collectionLanguages
					.contains(userPreferredLanguage)) {
				return getLocale(systemLanguage);
			} else {
				return getLocale(userPreferredLanguage);
			}
		}
	}

	private Locale getLocale(String languageCode) {
		i18n.getSupportedLanguages();
		for (Language language : Language.values()) {
			if (language.getCode().equals(languageCode)) {
				return new Locale(languageCode);
			}
		}
		throw new ImpossibleRuntimeException("Invalid language " + languageCode);
	}

	public void logoClicked() {
		header.navigateTo().home();
	}

	private long popupClosedTS = -1;

	void popupClosed() {
		popupClosedTS = System.currentTimeMillis();
		advancedSearchFormVisible = false;
	}

	private boolean isPopupClosedIgnored() {
		return System.currentTimeMillis() - popupClosedTS < 500;
	}

	void advancedSearchFormButtonClicked() {
		if (advancedSearchFormVisible) {
			advancedSearchFormVisible = false;
		} else {
			advancedSearchFormVisible = !isPopupClosedIgnored();
		}
		header.setAdvancedSearchFormVisible(advancedSearchFormVisible);
	}

	void selectionButtonClicked() {
		header.setSelectionPanelVisible(true, refreshSelectionPanel);
		refreshSelectionPanel = false;
	}

	private void updateSelectionButton() {
		SessionContext sessionContext = header.getSessionContext();
		if (sessionContext.getSelectedRecordIds().isEmpty()) {
			header.setSelectionButtonEnabled(false);
		} else {
			header.setSelectionButtonEnabled(true);
		}
		header.refreshSelectionPanel();
		updateSelectionCount();
	}

	public void selectedRecordsCleared() {
		allItemsSelected = false;
		allItemsDeselected = false;

		refreshSelectionPanel = true;
		updateSelectionButton();
	}

	public void selectedRecordIdRemoved(String recordId) {
		refreshSelectionPanel = true;
		updateSelectionButton();
	}

	public void selectedRecordIdAdded(String recordId) {
		refreshSelectionPanel = true;
		updateSelectionButton();
	}

	public void clearSelectionButtonClicked() {
		SessionContext sessionContext = header.getSessionContext();
		List<String> selectedRecordIds = new ArrayList<>(sessionContext.getSelectedRecordIds());
		for (String id : deselectedRecordsWithSchema.keySet()) {
			selectedRecordIds.remove(id);
		}

		SearchServices searchServices = modelLayerFactory.newSearchServices();
		String currentCollection = sessionContext.getCurrentCollection();
		for (String id : selectedRecordIds) {
			Record record = searchServices.searchSingleResult(LogicalSearchQueryOperators.fromAllSchemasIn(currentCollection)
					.where(Schemas.IDENTIFIER).isEqualTo(id));
			String schemaTypeCode = record == null ? null : record.getTypeCode();
			sessionContext.removeSelectedRecordId(id, schemaTypeCode);
		}

		header.removeRecordsFromPanel(selectedRecordIds);
		updateSelectionCount();
	}

	public boolean isSelected(String recordId) {
		return !allItemsDeselected && (allItemsSelected || !deselectedRecordsWithSchema.containsKey(recordId));
	}

	public void selectionChanged(String recordId, Boolean selected) {
		allItemsSelected = false;
		allItemsDeselected = false;

		//		refreshSelectionPanel = true;
		SessionContext sessionContext = header.getSessionContext();
		RecordServices recordServices = modelLayerFactory.newRecordServices();
		Record record;
		try {
			record = recordServices.getDocumentById(recordId);
		} catch (NoSuchRecordWithId e) {
			record = null;
		}
		String schemaTypeCode = record == null ? null : record.getTypeCode();
		if (selected) {
			sessionContext.addSelectedRecordId(recordId, schemaTypeCode);
			deselectedRecordsWithSchema.remove(recordId);
		} else {
			deselectedRecordsWithSchema.put(recordId, schemaTypeCode);
		}
	}

	private void updateSelectionCount() {
		SessionContext sessionContext = header.getSessionContext();
		int selectionCount = sessionContext.getSelectedRecordIds().size();
		header.setSelectionCount(selectionCount);
	}

	/**
	 * FIXME Remove Vaadin references from presenter
	 */
	public void buildSelectionPanelActionButtons(Component actionMenuLayout) {
		appLayerFactory.getExtensions().forCollection(header.getCollection())
				.addAvailableActions(buildAvailableActionsParam(actionMenuLayout));
	}

	public AvailableActionsParam buildAvailableActionsParam(Component actionMenuLayout) {
		List<String> selectedRecordIds = new ArrayList<>();
		Map<String, Long> selectedRecordSchemaTypeCodes = new HashMap<>();
		if (!allItemsDeselected) {
			selectedRecordIds.addAll(header.getSessionContext().getSelectedRecordIds());
			selectedRecordSchemaTypeCodes.putAll(header.getSessionContext().getSelectedRecordSchemaTypeCodes());

			if (!allItemsSelected) {
				Set<Map.Entry<String, String>> entries = deselectedRecordsWithSchema.entrySet();
				for (Iterator<Map.Entry<String, String>> it = entries.iterator(); it.hasNext(); ) {
					Map.Entry<String, String> entry = it.next();
					String deselectedRecordId = entry.getKey();
					String schemaCode = entry.getValue();
					if (selectedRecordIds.contains(deselectedRecordId)) {
						selectedRecordIds.remove(deselectedRecordId);
						if (selectedRecordSchemaTypeCodes.containsKey(schemaCode)) {
							if (selectedRecordSchemaTypeCodes.get(schemaCode) == 1L) {
								selectedRecordSchemaTypeCodes.remove(schemaCode);
							} else {
								selectedRecordSchemaTypeCodes
										.put(entry.getValue(), selectedRecordSchemaTypeCodes.get(schemaCode) - 1);
							}
						}
					} else {
						it.remove();
					}
				}
			}
		}

		return new AvailableActionsParam(selectedRecordIds, new ArrayList<>(selectedRecordSchemaTypeCodes.keySet()),
				getCurrentUser(), actionMenuLayout, header);
	}

	public void createNewCartAndAddToItRequested(List<String> recordIds, String title) {
		RMSchemasRecordsServices rm = new RMSchemasRecordsServices(header.getCollection(), appLayerFactory);
		Cart cart = rm.newCart();
		cart.setTitle(title);
		cart.setOwner(getCurrentUser());
		RecordServices recordServices = modelLayerFactory.newRecordServices();
		for (String record : recordIds) {
			switch (recordServices.getDocumentById(record).getTypeCode()) {
			case Folder.SCHEMA_TYPE:
				cart.addFolders(asList(record));
				break;
			case Document.SCHEMA_TYPE:
				cart.addDocuments(asList(record));
				break;
			case ContainerRecord.SCHEMA_TYPE:
				cart.addContainers(asList(record));
				break;
			}
		}

		try {
			modelLayerFactory.newRecordServices().execute(new Transaction(cart.getWrappedRecord()).setUser(getCurrentUser()));
			showMessage($("ConstellioHeader.selection.actions.actionCompleted", recordIds.size()));
			//			view.showMessage($("SearchView.addedToCart"));
		} catch (RecordServicesException e) {
			e.printStackTrace();
		}
	}

	public RecordVODataProvider getOwnedCartsDataProvider() {
		MetadataSchemaToVOBuilder schemaToVOBuilder = new MetadataSchemaToVOBuilder();
		final RMSchemasRecordsServices rm = new RMSchemasRecordsServices(header.getCollection(), appLayerFactory);
		final MetadataSchemaVO cartSchemaVO = schemaToVOBuilder
				.build(rm.cartSchema(), RecordVO.VIEW_MODE.TABLE, header.getSessionContext());
		return new RecordVODataProvider(cartSchemaVO, new RecordToVOBuilder(), modelLayerFactory, header.getSessionContext()) {
			@Override
			protected LogicalSearchQuery getQuery() {
				return new LogicalSearchQuery(from(rm.cartSchema()).where(rm.cartOwner())
						.isEqualTo(getCurrentUser().getId())).sortAsc(Schemas.TITLE);
			}
		};
	}

	public RecordVODataProvider getSharedCartsDataProvider() {
		MetadataSchemaToVOBuilder schemaToVOBuilder = new MetadataSchemaToVOBuilder();
		final RMSchemasRecordsServices rm = new RMSchemasRecordsServices(header.getCollection(), appLayerFactory);
		final MetadataSchemaVO cartSchemaVO = schemaToVOBuilder
				.build(rm.cartSchema(), RecordVO.VIEW_MODE.TABLE, header.getSessionContext());
		return new RecordVODataProvider(cartSchemaVO, new RecordToVOBuilder(), modelLayerFactory, header.getSessionContext()) {
			@Override
			protected LogicalSearchQuery getQuery() {
				return new LogicalSearchQuery(from(rm.cartSchema()).where(rm.cartSharedWithUsers())
						.isContaining(asList(getCurrentUser().getId()))).sortAsc(Schemas.TITLE);
			}
		};
	}

	public void addToCartRequested(List<String> recordIds, RecordVO cartVO) {
		// TODO: Create an extension for this
		RMSchemasRecordsServices rm = new RMSchemasRecordsServices(header.getCollection(), appLayerFactory);
		Cart cart = rm.getOrCreateCart(getCurrentUser(), cartVO.getId());
		RecordServices recordServices = modelLayerFactory.newRecordServices();
		for (String record : recordIds) {
			switch (recordServices.getDocumentById(record).getTypeCode()) {
			case Folder.SCHEMA_TYPE:
				cart.addFolders(asList(record));
				break;
			case Document.SCHEMA_TYPE:
				cart.addDocuments(asList(record));
				break;
			case ContainerRecord.SCHEMA_TYPE:
				cart.addContainers(asList(record));
				break;
			}
		}
		try {
			modelLayerFactory.newRecordServices().add(cart);
			showMessage($("ConstellioHeader.selection.actions.actionCompleted", recordIds.size()));
		} catch (RecordServicesException e) {
			showMessage($(e));
		}
	}

	public void showMessage(String errorMessage) {
		Notification notification = new Notification(errorMessage + "<br/><br/>" + $("clickToClose"),
				Notification.Type.WARNING_MESSAGE);
		notification.setHtmlContentAllowed(true);
		notification.show(Page.getCurrent());
	}

	void computeAllItemsSelected() {
		SessionContext sessionContext = header.getSessionContext();
		List<String> selectedRecordIds = sessionContext.getSelectedRecordIds();
		for (String selectedRecordId : selectedRecordIds) {
			if (deselectedRecordsWithSchema.containsKey(selectedRecordId)) {
				allItemsSelected = false;
				return;
			}
		}
		allItemsSelected = true;
		allItemsDeselected = false;
	}

	boolean isAllItemsSelected() {
		return allItemsSelected;
	}

	boolean isAllItemsDeselected() {
		return allItemsDeselected;
	}

	void selectAllClicked() {
		deselectedRecordsWithSchema.clear();
		allItemsSelected = true;
		allItemsDeselected = false;

		updateSelectionButton();
		header.refreshButtons();
	}

	void deselectAllClicked() {
		deselectedRecordsWithSchema.clear();

		SessionContext sessionContext = header.getSessionContext();
		String collection = sessionContext.getCurrentCollection();

		List<String> selectedRecordIds = sessionContext.getSelectedRecordIds();
		RecordServices recordServices = modelLayerFactory.newRecordServices();
		List<Record> selectedRecords = recordServices.getRecordsById(collection, selectedRecordIds);
		for (Record selectedRecord : selectedRecords) {
			String selectedRecordId = selectedRecord.getId();
			String selectedRecordSchemaType = SchemaUtils.getSchemaTypeCode(selectedRecord.getSchemaCode());
			deselectedRecordsWithSchema.put(selectedRecordId, selectedRecordSchemaType);
		}

		allItemsSelected = false;
		allItemsDeselected = true;

		updateSelectionButton();
		header.refreshButtons();
	}

}
