package com.constellio.app.modules.rm.ui.menuBar;

import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.ui.menuBar.editableTree.treeNodes.MenuDisplayConfigComponent;
import com.constellio.app.modules.rm.ui.menuBar.editableTree.treeNodes.MenuDisplayConfigComponentBase;
import com.constellio.app.modules.rm.ui.menuBar.editableTree.treeNodes.action.MenuDisplayConfigAction;
import com.constellio.app.modules.rm.ui.menuBar.editableTree.treeNodes.root.MenuDisplayConfigRoot;
import com.constellio.app.modules.rm.ui.menuBar.editableTree.treeNodes.subMenu.MenuDisplayConfigSubMenu;
import com.constellio.app.modules.tasks.extensions.action.Action;
import com.constellio.app.services.actionDisplayManager.MenuDisplayContainer;
import com.constellio.app.services.actionDisplayManager.MenuDisplayItem;
import com.constellio.app.services.actionDisplayManager.MenuDisplayList;
import com.constellio.app.services.actionDisplayManager.MenuDisplayListBySchemaType;
import com.constellio.app.services.actionDisplayManager.MenusDisplayManager;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.services.icons.DefaultIconService;
import com.constellio.app.services.icons.IconService;
import com.constellio.app.ui.entities.MetadataSchemaTypeVO;
import com.constellio.app.ui.framework.builders.MetadataSchemaTypeToVOBuilder;
import com.constellio.app.ui.pages.base.SessionContext;
import com.constellio.app.ui.pages.base.SingleSchemaBasePresenter;
import com.constellio.app.ui.util.MessageUtils;
import com.constellio.model.entities.CorePermissions;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.frameworks.validation.ValidationException;
import com.google.common.collect.Streams;
import org.h2.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.constellio.app.services.menu.MenuItemServices.BATCH_ACTIONS_FAKE_SCHEMA_TYPE;
import static com.constellio.app.ui.i18n.i18n.$;

public class MenuDisplayConfigPresenter extends SingleSchemaBasePresenter<MenuDisplayConfigView> {
	private static Logger LOGGER = LoggerFactory.getLogger(MenuDisplayConfigPresenter.class);

	private final String schemaTypeCode;

	private final AppLayerFactory appLayerFactory;
	private final SessionContext sessionContext;
	private final IconService iconService;
	private final MenusDisplayManager menusDisplayManager;
	private final MenuDisplayListBySchemaType menuDisplayList;

	public MenuDisplayConfigPresenter(MenuDisplayConfigView view,
									  String schemaTypeCode, AppLayerFactory appLayerFactory,
									  SessionContext sessionContext) {
		super(view);

		this.appLayerFactory = appLayerFactory;
		this.sessionContext = sessionContext;
		this.iconService = new DefaultIconService(appLayerFactory, sessionContext);
		this.schemaTypeCode = schemaTypeCode;

		menusDisplayManager = appLayerFactory.getMenusDisplayManager();
		menuDisplayList = menusDisplayManager.getMenuDisplayList(sessionContext.getCurrentCollection());
	}

	MenuDisplayConfigRoot rebuildRoot() {
		List<MenuDisplayConfigComponent> children = new ArrayList<>();
		MenuDisplayConfigRoot root = new MenuDisplayConfigRoot(sessionContext, iconService) {
			@Override
			public List<MenuDisplayConfigComponent> getChildren() {
				return children;
			}
		};

		MenuDisplayList actionDisplayList = menuDisplayList.getActionDisplayList(schemaTypeCode);
		if (actionDisplayList != null) {
			children.addAll(actionDisplayList.getRootMenuList().stream().map(menuDisplayItem -> rebuildNodeFromMenuDisplayItem(root, menuDisplayItem, actionDisplayList)).collect(Collectors.toList()));
		}

		return root;
	}

	private MenuDisplayConfigComponent rebuildNodeFromMenuDisplayItem(MenuDisplayConfigComponent parent,
																	  MenuDisplayItem menuDisplayItem,
																	  MenuDisplayList menuDisplayList) {
		MenuDisplayConfigComponent component;

		if (menuDisplayItem.isContainer() && menuDisplayItem instanceof MenuDisplayContainer) {
			MenuDisplayContainer menuDisplayContainer = (MenuDisplayContainer) menuDisplayItem;
			List<MenuDisplayConfigComponent> children = new ArrayList<>();
			final HashMap<Locale, String> labels = new HashMap<>(menuDisplayContainer.getLabels());

			component = new MenuDisplayConfigSubMenu(menuDisplayItem.getCode(), sessionContext, iconService) {
				@Override
				public MenuDisplayConfigComponent getParent() {
					return parent;
				}

				@Override
				public Map<Locale, String> getCaptions() {
					return labels;
				}

				@Override
				public String getIconName() {
					return menuDisplayContainer.getIcon();
				}

				@Override
				public List<MenuDisplayConfigComponent> getChildren() {
					return children;
				}
			};

			children.addAll(menuDisplayList.getSubMenu(menuDisplayItem.getCode()).stream().map(subMenuDisplayItem -> rebuildNodeFromMenuDisplayItem(component, subMenuDisplayItem, menuDisplayList)).collect(Collectors.toList()));
		} else {
			component = new MenuDisplayConfigAction(menuDisplayItem.getCode(), $(menuDisplayItem.getI18nKey()), sessionContext, iconService) {
				@Override
				public MenuDisplayConfigComponent getParent() {
					return parent;
				}

				@Override
				public boolean isEnabled() {
					return menuDisplayItem.isActive();
				}

				@Override
				public boolean isAlwaysEnabled() {
					return menuDisplayItem.isAlwaysActive();
				}

				@Override
				public String getIconName() {
					return menuDisplayItem.getIcon();
				}
			};
		}

		return component;
	}

	public void newNode(Class<? extends MenuDisplayConfigComponent> nodeTypeRequired, MenuDisplayConfigComponent parent,
						Consumer<MenuDisplayConfigComponent> newNodeCallback,
						BiConsumer<MenuDisplayConfigComponent, Consumer<MenuDisplayConfigComponent>> showFormToBuildNode) {


		if (nodeTypeRequired.equals(MenuDisplayConfigSubMenu.class)) {
			String code = schemaTypeCode + "_" + UUID.randomUUID().toString().replace("-", "_");

			showFormToBuildNode.accept(new MenuDisplayConfigSubMenu(code, sessionContext, iconService) {
				@Override
				public MenuDisplayConfigComponent getParent() {
					return parent;
				}
			}, menuBarComponent -> {
				parent.getChildren().add(menuBarComponent);

				newNodeCallback.accept(menuBarComponent);
			});
		}
	}

	public void moveNode(MenuDisplayConfigComponent nodeToMove, MenuDisplayConfigComponent newParent,
						 MenuDisplayConfigComponent sibling, boolean insertBefore,
						 Consumer<MenuDisplayConfigComponent> nodeToRefreshCallback) {
		if (nodeToMove != null) {
			MenuDisplayConfigComponent currentParent = nodeToMove.getParent();
			currentParent.getChildren().remove(nodeToMove);

			boolean insertAtTheEnd = newParent == sibling;


			nodeToMove = nodeToMove.applyModification(new MenuDisplayConfigComponentBase(nodeToMove) {
				@Override
				public MenuDisplayConfigComponent getParent() {
					return newParent;
				}
			});

			List<MenuDisplayConfigComponent> siblings = newParent.getChildren();
			if (insertAtTheEnd) {
				siblings.add(nodeToMove);
			} else {
				int siblingIndex = siblings.indexOf(sibling);
				int insertLocation;

				if (insertBefore) {
					insertLocation = siblingIndex;
				} else {
					insertLocation = siblingIndex + 1;
				}

				if (insertLocation >= siblings.size()) {
					siblings.add(nodeToMove);
				} else {
					siblings.add(insertLocation, nodeToMove);
				}
			}

			nodeToRefreshCallback.accept(findRootNode(newParent));
		}


	}

	public void deleteNode(MenuDisplayConfigComponent nodeToDelete,
						   Consumer<MenuDisplayConfigComponent> highestParentToRefresh) {
		if (nodeToDelete != null) {

			final MenuDisplayConfigComponent parent = nodeToDelete.getParent();
			final List<MenuDisplayConfigComponent> siblings = parent.getChildren();

			if (nodeToDelete.getMainClass().equals(MenuDisplayConfigSubMenu.class)) {
				final List<MenuDisplayConfigComponent> children = nodeToDelete.getChildren();
				int indexOfNode = siblings.indexOf(nodeToDelete);
				siblings.addAll(indexOfNode, children.stream().map(child -> child.applyModification(new MenuDisplayConfigComponentBase(child) {
					@Override
					public MenuDisplayConfigComponent getParent() {
						return parent;
					}
				})).collect(Collectors.toList()));

				siblings.remove(nodeToDelete);

			} else {
				int nodeIndex = siblings.indexOf(nodeToDelete);
				siblings.set(nodeIndex, new MenuDisplayConfigAction(nodeToDelete) {
					@Override
					public boolean isEnabled() {
						return !super.isEnabled();
					}
				});
			}

			highestParentToRefresh.accept(findRootNode(parent));
		}
	}

	public void editNode(MenuDisplayConfigComponent nodeToEdit,
						 Consumer<MenuDisplayConfigComponent> highestParentToRefreshCallback,
						 BiConsumer<MenuDisplayConfigComponent, Consumer<MenuDisplayConfigComponent>> showFormToBuildNode) {
		if (nodeToEdit.getMainClass().equals(MenuDisplayConfigSubMenu.class)) {
			MenuDisplayConfigComponent parent = nodeToEdit.getParent();
			List<MenuDisplayConfigComponent> siblings = parent.getChildren();
			int indexInSiblings = siblings.indexOf(nodeToEdit);

			showFormToBuildNode.accept(nodeToEdit, subMenuEdited -> {
				siblings.set(indexInSiblings, nodeToEdit.applyModification(subMenuEdited));

				highestParentToRefreshCallback.accept(findRootNode(parent));
			});
		}
	}

	public void saveMenu(MenuDisplayConfigComponent root, Consumer<Action> confirmSave, Action menuSavedCallback,
						 Consumer<String> showError) {
		MenuDisplayList actionDisplayList = menuDisplayList.getActionDisplayList(schemaTypeCode);
		final List<MenuDisplayItem> currentActionMenuList = root.getChildren().stream().flatMap(child -> convertChildrenToFlatMenuDisplayItemList(root, child)).collect(Collectors.toList());

		if (actionDisplayList.hasChanges(currentActionMenuList)) {
			confirmSave.accept(() -> {
				try {
					menusDisplayManager.withActionsDisplay(
							sessionContext.getCurrentCollection(),
							schemaTypeCode, currentActionMenuList);

					menuSavedCallback.doAction();
				} catch (ValidationException e) {
					LOGGER.error(e.getMessage());
					showError.accept(MessageUtils.toMessage(e));
				}
			});
		} else {
			menuSavedCallback.doAction();
		}
	}

	private Stream<MenuDisplayItem> convertChildrenToFlatMenuDisplayItemList(MenuDisplayConfigComponent root,
																			 MenuDisplayConfigComponent component) {
		Stream<MenuDisplayItem> flattenStream;

		MenuDisplayItem currentItem;
		if (component instanceof MenuDisplayConfigSubMenu) {
			currentItem = new MenuDisplayContainer(component.getCode(), component.getCaptions(), component.getIconName());
		} else {
			MenuDisplayConfigComponent parent = component.getParent();
			currentItem = new MenuDisplayItem(component.getCode(), component.getIconName(), component.getI18NKey(), component.isEnabled(), parent != root ? parent.getCode() : null, component.isAlwaysEnabled());
		}

		flattenStream = Stream.of(currentItem);

		List<MenuDisplayConfigComponent> children = component.getChildren();
		if (!children.isEmpty()) {
			flattenStream = Streams.concat(
					flattenStream,
					children.stream().flatMap(child -> convertChildrenToFlatMenuDisplayItemList(root, child))
			);
		}

		return flattenStream;
	}

	public SessionContext getSessionContext() {
		return sessionContext;
	}

	@Override
	protected boolean hasPageAccess(String params, User user) {
		return user.has(CorePermissions.MANAGE_METADATASCHEMAS).globally();
	}

	public AppLayerFactory getAppLayerFactory() {
		return appLayerFactory;
	}

	public String getTitle() {
		String label;

		if (!StringUtils.equals(schemaTypeCode, BATCH_ACTIONS_FAKE_SCHEMA_TYPE)) {
			RMSchemasRecordsServices rm = new RMSchemasRecordsServices(sessionContext.getCurrentCollection(), appLayerFactory);
			MetadataSchemaTypeToVOBuilder schemaTypeToVOBuilder = new MetadataSchemaTypeToVOBuilder();
			MetadataSchemaTypeVO schemaTypeVO = schemaTypeToVOBuilder.build(rm.schemaType(schemaTypeCode), sessionContext);
			label = $("MenuDisplayConfigViewImpl.title", schemaTypeVO.getLabel());
		} else {
			label = "Gérer le menu d'action d'une sélection multiple";//$("");
		}

		return label;
	}

	public MenuDisplayConfigComponent findRootNode(MenuDisplayConfigComponent node) {
		return node.getParent() == null ? node : findRootNode(node.getParent());
	}
}
