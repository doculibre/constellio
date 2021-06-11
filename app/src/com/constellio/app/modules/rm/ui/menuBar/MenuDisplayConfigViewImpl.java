package com.constellio.app.modules.rm.ui.menuBar;

import com.constellio.app.modules.rm.ui.menuBar.editableTree.MenuDisplayConfigEditableTree;
import com.constellio.app.modules.rm.ui.menuBar.editableTree.treeNodes.MenuDisplayConfigComponent;
import com.constellio.app.modules.rm.ui.menuBar.editableTree.treeNodes.subMenu.MenuDisplayConfigSubMenu;
import com.constellio.app.modules.rm.ui.menuBar.forms.MenuDisplayConfigSubMenuForm;
import com.constellio.app.modules.tasks.extensions.action.Action;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.services.factories.ConstellioFactories;
import com.constellio.app.ui.application.ConstellioUI;
import com.constellio.app.ui.framework.components.BaseForm;
import com.constellio.app.ui.framework.components.BaseWindow;
import com.constellio.app.ui.framework.components.layouts.I18NHorizontalLayout;
import com.constellio.app.ui.pages.base.BaseViewImpl;
import com.constellio.app.ui.pages.base.SessionContext;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Component;
import com.vaadin.ui.Panel;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ValoTheme;

import java.util.function.Consumer;
import java.util.function.Supplier;

import static com.constellio.app.ui.i18n.i18n.$;

public class MenuDisplayConfigViewImpl extends BaseViewImpl implements MenuDisplayConfigView {
	private MenuDisplayConfigPresenter presenter;

	private final AppLayerFactory appLayerFactory;
	private final SessionContext sessionContext;

	public MenuDisplayConfigViewImpl() {
		appLayerFactory = ConstellioFactories.getInstance().getAppLayerFactory();
		sessionContext = ConstellioUI.getCurrentSessionContext();

		addStyleName("menu-display-config");
	}

	@Override
	protected void initBeforeCreateComponents(ViewChangeEvent event) {
		presenter = new MenuDisplayConfigPresenter(this, event.getParameters(), appLayerFactory, sessionContext);
	}

	@Override
	protected Component buildMainComponent(ViewChangeEvent event) {
		VerticalLayout verticalLayout = new VerticalLayout();
		verticalLayout.setSpacing(true);

		MenuDisplayConfigEditableTree tree = buildTree();
		verticalLayout.addComponent(tree);

		I18NHorizontalLayout buttonLayout = new I18NHorizontalLayout();
		buttonLayout.setSpacing(true);
		buttonLayout.addComponents(
				buildSaveButton(tree::getRootComponent),
				buildCancelButton());

		verticalLayout.addComponent(buttonLayout);
		verticalLayout.setComponentAlignment(buttonLayout, Alignment.MIDDLE_CENTER);


		return verticalLayout;
	}

	private MenuDisplayConfigEditableTree buildTree() {

		return new MenuDisplayConfigEditableTree(rootNodeFactory -> rootNodeFactory.build(presenter.rebuildRoot())) {
			@Override
			public void newNode(Class<? extends MenuDisplayConfigComponent> nodeTypeRequired,
								MenuDisplayConfigComponent parent,
								Consumer<MenuDisplayConfigComponent> newNodeCallback) {
				presenter.newNode(nodeTypeRequired, parent, newNodeCallback, (menuBarComponent, menuBarComponentConsumer) -> {
					if (menuBarComponent.getMainClass().equals(MenuDisplayConfigSubMenu.class)) {
						showSubMenuEditionForm(menuBarComponent, menuBarComponentConsumer);
					}
				});
			}

			@Override
			public void deleteNode(MenuDisplayConfigComponent nodeToDelete,
								   Consumer<MenuDisplayConfigComponent> highestParentToRefreshCallback) {
				presenter.deleteNode(nodeToDelete, highestParentToRefreshCallback);
			}

			@Override
			public void moveNode(MenuDisplayConfigComponent nodeToMove, MenuDisplayConfigComponent newParent,
								 MenuDisplayConfigComponent sibling,
								 boolean insertBefore, Consumer<MenuDisplayConfigComponent> nodeToRefreshCallback) {
				presenter.moveNode(nodeToMove, newParent, sibling, insertBefore, nodeToRefreshCallback);
			}

			@Override
			public void editNode(MenuDisplayConfigComponent nodeToEdit,
								 Consumer<MenuDisplayConfigComponent> highestParentToRefreshCallback) {
				presenter.editNode(nodeToEdit, highestParentToRefreshCallback, (subMenuToEdit, subMenuEditedCallback) -> {
					if (subMenuToEdit.getMainClass().equals(MenuDisplayConfigSubMenu.class)) {
						showSubMenuEditionForm(subMenuToEdit, subMenuEditedCallback);
					}
				});
			}
		};
	}

	private Component buildSaveButton(Supplier<MenuDisplayConfigComponent> rootProvider) {
		Button saveButton = new Button(getSaveButtonCaption());
		saveButton.addClickListener((ClickListener) event -> presenter.saveMenu(rootProvider.get(), this::showSaveConfirmation, this::navigateBackToDisplaySchemaType, this::showErrorMessage));

		saveButton.addStyleName(ValoTheme.BUTTON_PRIMARY);

		return saveButton;
	}

	public String getSaveButtonCaption() {
		return $("save");
	}

	private void showSaveConfirmation(Action saveConfirmedCallback) {
		saveConfirmedCallback.doAction();
	}

	public void navigateBackToDisplaySchemaType() {
		navigateTo().listSchemaTypes();
	}


	private Component buildCancelButton() {
		Button cancelButton = new Button(getCancelButtonCaption());
		cancelButton.addStyleName(BaseForm.CANCEL_BUTTON);
		cancelButton.addClickListener((ClickListener) event -> navigateBackToDisplaySchemaType());

		return cancelButton;
	}

	public String getCancelButtonCaption() {
		return $("cancel");
	}

	@Override
	protected String getTitle() {
		return presenter.getTitle();
	}

	private void showSubMenuEditionForm(MenuDisplayConfigComponent subMenu,
										Consumer<MenuDisplayConfigComponent> subMenuEditedCallback) {
		if (subMenu instanceof MenuDisplayConfigSubMenu) {
			final BaseWindow window = new BaseWindow();

			MenuDisplayConfigSubMenuForm form = new MenuDisplayConfigSubMenuForm((MenuDisplayConfigSubMenu) subMenu, appLayerFactory) {
				@Override
				protected void saveButtonClick(EditableMenuDisplayConfigSubMenu viewObject) {
					if (validateFields()) {
						subMenuEditedCallback.accept(viewObject.getSubMenu());
						window.close();
					}
				}

				@Override
				protected void cancelButtonClick(EditableMenuDisplayConfigSubMenu viewObject) {
					window.close();
				}
			};
			form.setSizeFull();


			window.setWidth("325px");
			window.setHeight("350px");
			window.setResizable(true);
			window.setModal(false);
			window.center();

			window.setContent(new Panel(form));
			ConstellioUI.getCurrent().addWindow(window);
		}
	}
}
