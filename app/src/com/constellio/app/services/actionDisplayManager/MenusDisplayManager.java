package com.constellio.app.services.actionDisplayManager;

import com.constellio.app.services.actionDisplayManager.MenusDisplayTransaction.TransactionElement;
import com.constellio.data.dao.managers.StatefulService;
import com.constellio.data.dao.managers.config.ConfigManager;
import com.constellio.data.dao.managers.config.DocumentAlteration;
import com.constellio.data.dao.services.cache.ConstellioCache;
import com.constellio.data.dao.services.cache.ConstellioCacheManager;
import com.constellio.model.frameworks.validation.ValidationErrors;
import com.constellio.model.frameworks.validation.ValidationException;
import com.constellio.model.services.collections.CollectionsListManager;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.utils.OneXMLConfigPerCollectionManager;
import com.constellio.model.utils.OneXMLConfigPerCollectionManagerListener;
import com.constellio.model.utils.XMLConfigReader;
import org.jdom2.Document;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

public class MenusDisplayManager implements StatefulService, OneXMLConfigPerCollectionManagerListener<MenuDisplayListBySchemaType> {
	public static String ACTIONS_DISPLAY_CONFIG = "/actionsDisplayConfig.xml";
	public static final String REQUIRED_ELEMENT = "valueRequired";
	public static final String INVALID_PARENT = "parentDoesExist";
	public static final String UNICITY_CODE_RULE = "unicityCodeRule";
	public static final String HAS_CHILDREN = "hasChildren";
	public static final String CANNOT_UPDATE_MENU_DISPLAY_CONTAINER_WITH_MENU_DISPLAY_ITEM = "cannotUpdateMenuDisplayContainerWithMenuDisplayItem";
	public static final String CANNOT_UPDATE_MENU_DISPLAY_ITEM_WITH_MENU_DISPLAY_CONTAINER = "cannotUpdateMenuDisplayItemWithMenuDisplayContainer";

	private ConfigManager configManager;
	private CollectionsListManager collectionsListManager;
	private ConstellioCacheManager cacheManager;
	private OneXMLConfigPerCollectionManager<MenuDisplayListBySchemaType> oneXMLConfigPerCollectionManager;


	public MenusDisplayManager(ModelLayerFactory modelLayerFactory) {
		this.configManager = modelLayerFactory.getDataLayerFactory().getConfigManager();
		this.collectionsListManager = modelLayerFactory.getCollectionsListManager();
		this.cacheManager = modelLayerFactory.getDataLayerFactory().getLocalCacheManager();
	}

	@Override
	public void initialize() {
		ConstellioCache cache = cacheManager.getCache(MenusDisplayManager.class.getName());
		this.oneXMLConfigPerCollectionManager = new OneXMLConfigPerCollectionManager<>(configManager, collectionsListManager,
				ACTIONS_DISPLAY_CONFIG, xmlConfigReader(), this, cache);
	}

	public void execute(String collection, MenusDisplayTransaction transaction) throws ValidationException {
		validateTransaction(collection, transaction);

		DocumentAlteration alteration = new DocumentAlteration() {
			@Override
			public void alter(Document document) {
				MenusDisplayManagerWriter writer = newActionDisplayWriter(document);
				writer.execute(transaction);
			}
		};

		oneXMLConfigPerCollectionManager.updateXML(collection, alteration);
	}

	public void withActionsDisplay(String collection, String schemaType, List<MenuDisplayItem> menuDisplayItem)
			throws ValidationException {
		validateMenuDisplayItem(menuDisplayItem);

		DocumentAlteration documentAlteration = new DocumentAlteration() {
			@Override
			public void alter(Document document) {
				MenusDisplayManagerWriter menusDisplayManagerWriter = newActionDisplayWriter(document);
				menusDisplayManagerWriter.withActionsDisplay(schemaType, menuDisplayItem);
			}
		};

		oneXMLConfigPerCollectionManager.updateXML(collection, documentAlteration);
	}

	protected void validateMenuDisplayItem(List<MenuDisplayItem> menuDisplayItems) throws ValidationException {
		List<String> avalibleParentCode = new ArrayList<>();
		List<String> usedCodes = new ArrayList<>();
		ValidationErrors validationErrors = new ValidationErrors();

		for (MenuDisplayItem menuDisplayItem : menuDisplayItems) {
			validateMenuDisplayItem(menuDisplayItem);

			if (menuDisplayItem.getParentCode() != null && !avalibleParentCode.contains(menuDisplayItem.getParentCode())) {
				Map<String, Object> errorParamMap = new HashMap<>();
				errorParamMap.put("actionCode", menuDisplayItem.getCode());
				errorParamMap.put("parentCode", menuDisplayItem.getParentCode());
				validationErrors.add(getClass(), INVALID_PARENT, errorParamMap);
				validationErrors.throwIfNonEmpty();
			}

			if (usedCodes.contains(menuDisplayItem.getCode())) {
				Map<String, Object> errorParamMap = new HashMap<>();
				errorParamMap.put("code", menuDisplayItem.getCode());
				validationErrors.add(getClass(), UNICITY_CODE_RULE, errorParamMap);
				validationErrors.throwIfNonEmpty();
			}

			if (menuDisplayItem.isContainer()) {
				avalibleParentCode.add(menuDisplayItem.getCode());
			}

			usedCodes.add(menuDisplayItem.getCode());
		}
	}

	public void createEmptyCollectionMenusDisplay(String collection) {
		DocumentAlteration createConfigAlteration = new DocumentAlteration() {
			@Override
			public void alter(Document document) {
				MenusDisplayManagerWriter writer = newActionDisplayWriter(document);
				writer.createEmptyMenusDisplay();
			}
		};
		oneXMLConfigPerCollectionManager.createCollectionFile(collection, createConfigAlteration);
	}

	public MenuDisplayListBySchemaType getMenuDisplayList(String collection) {
		return oneXMLConfigPerCollectionManager.get(collection);
	}

	@Override
	public void close() {

	}

	private List getOrCreateListFromMap(Map<String, List<String>> map, String key) {
		List list = getCurrentListOrEmptyList(map, key);

		if (list == null) {
			list = new ArrayList();
			map.put(key, list);
		}

		return list;
	}

	protected void validateTransaction(String collection, MenusDisplayTransaction menusDisplayTransaction)
			throws ValidationException {
		Map<String, List<String>> beingDeleted = new HashMap();
		Map<String, List<String>> beingAdded = new HashMap();
		List<String> containersAddedInTransaction = new ArrayList<>();

		List<TransactionElement> transactionElements = menusDisplayTransaction.getTransactionElements();

		for (TransactionElement transactionElement : transactionElements) {
			String schemaType = transactionElement.getSchemaType();
			MenuDisplayList menuDisplayList = this.getMenuDisplayList(collection).getActionDisplayList(schemaType);


			MenuDisplayItem menuDisplayItem = transactionElement.getMenuDisplayItem();
			ValidationErrors validationErrors = new ValidationErrors();

			String code = menuDisplayItem.getCode();

			switch (transactionElement.getAction()) {
				case REMOVE:
					doesMenuDisplayItemHasCode(menuDisplayItem, validationErrors);
					validationErrors.throwIfNonEmpty();
					if (!menuDisplayList.getSubMenu(menuDisplayItem.getCode()).isEmpty()) {
						Map<String, Object> errorParamMap = new HashMap<>();
						errorParamMap.put("code", menuDisplayItem.getCode());
						validationErrors.add(getClass(), HAS_CHILDREN, errorParamMap);
						validationErrors.throwIfNonEmpty();
					}
					elementRemoved(beingDeleted, beingAdded, schemaType, code);
					containersAddedInTransaction.remove(menuDisplayItem.getCode());
					break;
				case ADD_UPDATE:
					List<String> codeList = menuDisplayList == null ? new ArrayList<>() : menuDisplayList.getRawMenus().stream().map(r -> r.getCode()).collect(Collectors.toList());
					List<String> containers = menuDisplayList == null ? new ArrayList<>() : menuDisplayList.getRawMenus().stream().filter(r -> r.isContainer()).map(r -> r.getCode()).collect(Collectors.toList());
					codeList.removeAll(getCurrentListOrEmptyList(beingDeleted, schemaType));
					codeList.addAll(getCurrentListOrEmptyList(beingAdded, schemaType));
					containers.addAll(containersAddedInTransaction);

					if (menuDisplayItem.getParentCode() != null && !codeList.contains(menuDisplayItem.getParentCode())) {
						Map<String, Object> errorParamMap = new HashMap<>();
						errorParamMap.put("actionCode", menuDisplayItem.getCode());
						errorParamMap.put("parentCode", menuDisplayItem.getParentCode());
						validationErrors.add(getClass(), INVALID_PARENT, errorParamMap);
					}

					if (menuDisplayItem.isContainer() && !containers.contains(menuDisplayItem.getCode())) {
						Map<String, Object> errorParamMap = new HashMap<>();
						errorParamMap.put("code", menuDisplayItem.getCode());
						validationErrors.add(getClass(), CANNOT_UPDATE_MENU_DISPLAY_ITEM_WITH_MENU_DISPLAY_CONTAINER, errorParamMap);
					} else if (!menuDisplayItem.isContainer() && containers.contains(menuDisplayItem.getCode())) {
						Map<String, Object> errorParamMap = new HashMap<>();
						errorParamMap.put("code", menuDisplayItem.getCode());

						validationErrors.add(getClass(), CANNOT_UPDATE_MENU_DISPLAY_CONTAINER_WITH_MENU_DISPLAY_ITEM, errorParamMap);
					}
					validationErrors.throwIfNonEmpty();

					if (menuDisplayItem.isContainer()) {
						containersAddedInTransaction.add(menuDisplayItem.getCode());
					}

					validateMenuDisplayItem(menuDisplayItem);

					this.elementAdded(beingDeleted, beingAdded, schemaType, code);
					break;
			}
		}
	}

	private List<String> getCurrentListOrEmptyList(Map<String, List<String>> beingDeleted, String schemaType) {
		List<String> list = beingDeleted.get(schemaType);

		return list == null ? new ArrayList<>() : list;
	}

	private void elementAdded(Map<String, List<String>> beingDeleted, Map<String, List<String>> beingAdded,
							  String schemaType, String code) {
		List elementBeingDeleted = this.getOrCreateListFromMap(beingDeleted, schemaType);
		elementBeingDeleted.remove(code);
		List elementBeingAdded = this.getOrCreateListFromMap(beingAdded, schemaType);
		elementBeingAdded.add(code);
	}

	private void elementRemoved(Map<String, List<String>> beingDeleted, Map<String, List<String>> beingAdded,
								String schemaType, String code) {
		List elementBeingDeleted = this.getOrCreateListFromMap(beingDeleted, schemaType);
		elementBeingDeleted.add(code);
		List elementBeingAdded = this.getOrCreateListFromMap(beingAdded, schemaType);
		elementBeingAdded.remove(code);
	}


	private void validateMenuDisplayItem(MenuDisplayItem menuDisplayItem) throws ValidationException {
		ValidationErrors validationErrors = new ValidationErrors();

		doesMenuDisplayItemHasCode(menuDisplayItem, validationErrors);

		if (menuDisplayItem instanceof MenuDisplayContainer) {
			Map<Locale, String> labels = ((MenuDisplayContainer) menuDisplayItem).getLabels();
			doesMenuDisplayItemHas(validationErrors, labels == null || labels.size() == 0, "labels");

		}
		if (menuDisplayItem instanceof MenuDisplayItem && !(menuDisplayItem instanceof MenuDisplayContainer)) {
			doesMenuDisplayItemHas(validationErrors, menuDisplayItem.getI18nKey() == null, "i18nKey");
		}

		doesMenuDisplayItemHas(validationErrors, menuDisplayItem.getIcon() == null, "icon");

		validationErrors.throwIfNonEmpty();
	}

	private void doesMenuDisplayItemHasCode(MenuDisplayItem menuDisplayItem, ValidationErrors validationErrors) {
		doesMenuDisplayItemHas(validationErrors, menuDisplayItem.getCode() == null, "code");
	}

	private void doesMenuDisplayItemHas(ValidationErrors validationErrors, boolean condition, String code) {
		if (condition) {
			Map<String, Object> errorParamMap = new HashMap();
			errorParamMap.put("code", code);
			validationErrors.add(getClass(), REQUIRED_ELEMENT, errorParamMap);
		}
	}

	private XMLConfigReader<MenuDisplayListBySchemaType> xmlConfigReader() {
		return new XMLConfigReader<MenuDisplayListBySchemaType>() {
			@Override
			public MenuDisplayListBySchemaType read(String collection, Document document) {
				return newActionDisplayReader(document).getActionDisplayListBySchemaType();
			}
		};
	}

	private MenusDisplayManagerReader newActionDisplayReader(Document document) {
		return new MenusDisplayManagerReader(document);
	}

	private MenusDisplayManagerWriter newActionDisplayWriter(Document document) {
		return new MenusDisplayManagerWriter(document);
	}

	@Override
	public void onValueModified(String collection, MenuDisplayListBySchemaType newValue) {

	}
}
