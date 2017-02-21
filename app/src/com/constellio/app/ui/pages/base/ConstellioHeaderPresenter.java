package com.constellio.app.ui.pages.base;

import com.constellio.app.api.extensions.params.AvailableActionsParam;
import com.constellio.app.entities.navigation.NavigationConfig;
import com.constellio.app.entities.navigation.NavigationItem;
import com.constellio.app.entities.schemasDisplay.MetadataDisplayConfig;
import com.constellio.app.entities.schemasDisplay.SchemaTypeDisplayConfig;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.ui.builders.UserToVOBuilder;
import com.constellio.app.modules.rm.wrappers.Cart;
import com.constellio.app.modules.rm.wrappers.ContainerRecord;
import com.constellio.app.modules.rm.wrappers.Document;
import com.constellio.app.modules.rm.wrappers.Folder;
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
import com.constellio.data.utils.ImpossibleRuntimeException;
import com.constellio.data.utils.TimeProvider;
import com.constellio.model.entities.Language;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchemaType;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.records.RecordServicesException;
import com.constellio.model.services.schemas.MetadataSchemasManager;
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
	
	private final ConstellioHeader header;
	private String schemaTypeCode;
	private transient AppLayerFactory appLayerFactory;
	private transient ModelLayerFactory modelLayerFactory;
	private transient SchemasDisplayManager schemasDisplayManager;
	private boolean advancedSearchFormVisible;
	private boolean selectionPanelVisible;

	private BasePresenterUtils presenterUtils;

	private UserToVOBuilder voBuilder = new UserToVOBuilder();

	private boolean refreshSelectionPanel;
	
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
				if (config.isAdvancedSearch()) {
					result.add(builder.build(type));
				}
			}
		}
		return result;
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
		MetadataToVOBuilder builder = new MetadataToVOBuilder();

		List<MetadataVO> result = new ArrayList<>();
		result.add(builder.build(schemaType.getMetadataWithAtomicCode(CommonMetadataBuilder.PATH)));
		for (Metadata metadata : schemaType.getAllMetadatas()) {
			MetadataDisplayConfig config = schemasDisplayManager().getMetadata(header.getCollection(), metadata.getCode());
			if (config.isVisibleInAdvancedSearch()) {
				result.add(builder.build(metadata));
			}
		}
		sort(result);
		return result;
	}

	private void sort(List<MetadataVO> metadataVOs) {
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
		if(StringUtils.isBlank(userPreferredLanguage)){
			return getLocale(systemLanguage);
		} else {
			List<String> collectionLanguages = modelLayerFactory.getCollectionsListManager().getCollectionLanguages(userInLastCollection.getCollection());
			if(collectionLanguages == null || collectionLanguages.isEmpty() || !collectionLanguages.contains(userPreferredLanguage)){
				return getLocale(systemLanguage);
			} else {
				return getLocale(userPreferredLanguage);
			}
		}
	}

	private Locale getLocale(String languageCode) {
		i18n.getSupportedLanguages();
		for(Language language : Language.values()){
			if(language.getCode().equals(languageCode)){
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
		selectionPanelVisible = false;
	}
	
	private boolean isPopupClosedIgnored() {
		return System.currentTimeMillis() - popupClosedTS < 500;
	}

	void advancedSearchFormButtonClicked() {
		selectionPanelVisible = false;
		if (advancedSearchFormVisible) {
			advancedSearchFormVisible = false;
		} else {
			advancedSearchFormVisible = !isPopupClosedIgnored();
		}
		header.setAdvancedSearchFormVisible(advancedSearchFormVisible);
	}

	void selectionButtonClicked() {
		advancedSearchFormVisible = false;
		if (selectionPanelVisible) {
			selectionPanelVisible = false;
		} else {
			selectionPanelVisible = !isPopupClosedIgnored();
		}
		header.setSelectionPanelVisible(selectionPanelVisible, refreshSelectionPanel);
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
		sessionContext.clearSelectedRecordIds();
		refreshSelectionPanel = true;
		header.setSelectionPanelVisible(true, refreshSelectionPanel);
	}

	public boolean isSelected(String recordId) {
		SessionContext sessionContext = header.getSessionContext();
		return sessionContext.getSelectedRecordIds().contains(recordId);
	}

	public void selectionChanged(String recordId, Boolean selected) {
		SessionContext sessionContext = header.getSessionContext();
		SearchServices searchServices = modelLayerFactory.newSearchServices();
		Record record = searchServices.searchSingleResult(LogicalSearchQueryOperators.fromAllSchemasIn(sessionContext.getCurrentCollection())
				.where(Schemas.IDENTIFIER).isEqualTo(recordId));
		String schemaTypeCode = record == null? null:record.getTypeCode();
		if (selected) {
			sessionContext.addSelectedRecordId(recordId, schemaTypeCode);
		} else {
			sessionContext.removeSelectedRecordId(recordId, schemaTypeCode);
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
		AvailableActionsParam param = new AvailableActionsParam(header.getSessionContext().getSelectedRecordIds(),
				new ArrayList<>(header.getSessionContext().getSelectedRecordSchemaTypeCodes().keySet()), getCurrentUser(), actionMenuLayout);
		appLayerFactory.getExtensions().forCollection(header.getCollection()).addAvailableActions(param);
	}

	public void createNewCartAndAddToItRequested(String title) {
		RMSchemasRecordsServices rm = new RMSchemasRecordsServices(header.getCollection(), appLayerFactory);
		Cart cart = rm.newCart();
		cart.setTitle(title);
		cart.setOwner(getCurrentUser());
		List<String> selectedRecords = header.getSessionContext().getSelectedRecordIds();
		RecordServices recordServices = modelLayerFactory.newRecordServices();
		for(String record: selectedRecords) {
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
//			view.showMessage($("SearchView.addedToCart"));
		} catch (RecordServicesException e) {
			e.printStackTrace();
		}
	}

	public RecordVODataProvider getOwnedCartsDataProvider() {
		MetadataSchemaToVOBuilder schemaToVOBuilder = new MetadataSchemaToVOBuilder();
		final RMSchemasRecordsServices rm = new RMSchemasRecordsServices(header.getCollection(),appLayerFactory);
		final MetadataSchemaVO cartSchemaVO = schemaToVOBuilder.build(rm.cartSchema(), RecordVO.VIEW_MODE.TABLE, header.getSessionContext());
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
		final RMSchemasRecordsServices rm = new RMSchemasRecordsServices(header.getCollection(),appLayerFactory);
		final MetadataSchemaVO cartSchemaVO = schemaToVOBuilder.build(rm.cartSchema(), RecordVO.VIEW_MODE.TABLE, header.getSessionContext());
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
		Cart cart = rm.getOrCreateUserCart(getCurrentUser());
		RecordServices recordServices = modelLayerFactory.newRecordServices();
		for(String record: recordIds) {
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
			showMessage($("ConstellioHeader.selection.actions.actionCompleted"));
		} catch (RecordServicesException e) {
			showMessage($(e));
		}
	}

	public void showMessage(String errorMessage) {
		Notification notification = new Notification(errorMessage + "<br/><br/>" + $("clickToClose"), Notification.Type.WARNING_MESSAGE);
		notification.setHtmlContentAllowed(true);
		notification.show(Page.getCurrent());
	}
}
