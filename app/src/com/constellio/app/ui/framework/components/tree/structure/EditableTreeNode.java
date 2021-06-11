package com.constellio.app.ui.framework.components.tree.structure;

import com.constellio.app.ui.framework.components.fields.BaseTextField;
import com.constellio.app.ui.framework.components.layouts.I18NHorizontalLayout;
import com.constellio.app.ui.framework.components.menuBar.BaseMenuBar;
import com.constellio.app.ui.framework.components.tree.structure.actions.EnterPressActionHandler;
import com.constellio.app.ui.framework.components.tree.structure.actions.EscapePressActionHandler;
import com.constellio.app.ui.framework.components.tree.structure.listener.nodeselected.EditableTreeNodeSelectedEvent;
import com.constellio.app.ui.framework.components.tree.structure.listener.nodeselected.EditableTreeNodeSelectedEventListener;
import com.constellio.app.ui.framework.components.tree.structure.listener.nodeselected.EditableTreeNodeSelectedEventObservable;
import com.constellio.app.ui.framework.components.tree.structure.listener.removethistreenode.RemoveThisEditableTreeNodeEvent;
import com.constellio.app.ui.framework.components.tree.structure.listener.removethistreenode.RemoveThisEditableTreeNodeEventListener;
import com.constellio.app.ui.framework.components.tree.structure.listener.removethistreenode.RemoveThisEditableTreeNodeEventObservable;
import com.constellio.app.ui.framework.components.tree.structure.listener.replacethistreenode.ReplaceThisEditableTreeNodeEventListener;
import com.constellio.app.ui.framework.components.tree.structure.listener.replacethistreenode.ReplaceThisEditableTreeNodeEventObservable;
import com.constellio.app.ui.framework.components.tree.structure.listener.treenodeisbuilding.EditableTreeNodeIsBuildingEvent;
import com.constellio.app.ui.framework.components.tree.structure.listener.treenodeisbuilding.EditableTreeNodeIsBuildingEventListener;
import com.constellio.app.ui.framework.components.tree.structure.listener.treenodeisbuilding.EditableTreeNodeIsBuildingEventObservable;
import com.constellio.app.ui.framework.components.tree.structure.listener.treenodeiscommiting.EditableTreeNodeHasBuildedEvent;
import com.constellio.app.ui.framework.components.tree.structure.listener.treenodeiscommiting.EditableTreeNodeHasBuildedEventListener;
import com.constellio.app.ui.framework.components.tree.structure.listener.treenodeiscommiting.EditableTreeNodeHasBuildedEventObservable;
import com.google.common.base.Strings;
import com.vaadin.data.Property;
import com.vaadin.data.util.ObjectProperty;
import com.vaadin.event.Transferable;
import com.vaadin.server.FontAwesome;
import com.vaadin.server.Resource;
import com.vaadin.ui.Component;
import com.vaadin.ui.DragAndDropWrapper;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.MenuBar;
import com.vaadin.ui.MenuBar.MenuItem;
import com.vaadin.ui.Panel;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ValoTheme;
import org.jetbrains.annotations.NotNull;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public abstract class EditableTreeNode<T> extends DragAndDropWrapper {
	private final T model;
	private final List<EditableTreeNodeFactoryType> possibleChildrenTypes;

	private boolean editing = false;
	private boolean isCommitedOnce;
	private boolean isDeletable;
	private boolean isEditable;
	private boolean isForcedNotEditable;

	private final Component displayComponent;
	private final Component editingComponent;
	private Component controls;
	private MenuBar.MenuItem deleteComponent;


	private final EditableTreeNodeIsBuildingEventObservable treeNodeBuildingListeners;
	private final EditableTreeNodeHasBuildedEventObservable treeNodeHasBuildedlisteners;
	private final ReplaceThisEditableTreeNodeEventObservable replaceThisTreeNodeListeners;
	private final RemoveThisEditableTreeNodeEventObservable removeThisTreeNodeListeners;
	private final EditableTreeNodeSelectedEventObservable treeNodeSelectedListeners;

	private final Property<String> titleProperty;

	public EditableTreeNode() {
		this(null);
	}

	public boolean hasTreeNodeSelectedListeners() {
		return !CollectionUtils.isEmpty(treeNodeSelectedListeners.getListeners());
	}

	public EditableTreeNode(T model) {
		this.model = model;
		treeNodeBuildingListeners = new EditableTreeNodeIsBuildingEventObservable();
		treeNodeHasBuildedlisteners = new EditableTreeNodeHasBuildedEventObservable();
		replaceThisTreeNodeListeners = new ReplaceThisEditableTreeNodeEventObservable();
		removeThisTreeNodeListeners = new RemoveThisEditableTreeNodeEventObservable();
		treeNodeSelectedListeners = new EditableTreeNodeSelectedEventObservable();

		possibleChildrenTypes = getPossibleChildrenTypes();

		isCommitedOnce = model != null;
		isEditable = true;
		isForcedNotEditable = false;

		titleProperty = new ObjectProperty("", String.class);
		if (isCommitedOnce) {
			titleProperty.setValue(getCaption());
		}


		displayComponent = buildDisplayComponent();
		editingComponent = buildEditingComponent();
		editingComponent.setVisible(false);

		VerticalLayout layout = new VerticalLayout();
		layout.addComponents(displayComponent, editingComponent);

		layout.addStyleName(ValoTheme.FORMLAYOUT_LIGHT);

		addStyleName("structure-tree-editor-tree-node");

		setCompositionRoot(layout);

		if (isDraggable()) {
			setDragStartMode(DragStartMode.COMPONENT);
		}

		setDeletable(true);
	}

	private Component buildDisplayComponent() {

		final EditableTreeNode currentEditableTreeNode = this;

		HorizontalLayout layout = new I18NHorizontalLayout();
		layout.addStyleName("display-value-layout");
		layout.setWidthUndefined();
		layout.setHeightUndefined();

		HorizontalLayout titleLayout = new I18NHorizontalLayout();

		Label icon = new Label("");
		icon.setIcon(getIcon());

		Label title = new Label();
		title.setPropertyDataSource(this.titleProperty);
		title.addStyleName("v-caption");

		titleLayout.addComponents(icon, title);


		Panel titlePanel = new Panel();
		titlePanel.setWidthUndefined();
		titlePanel.setHeightUndefined();
		titlePanel.setContent(titleLayout);
		titlePanel.addStyleName(ValoTheme.PANEL_BORDERLESS);

		if (isDraggable()) {
			titlePanel.addStyleName("title-panel-draggable");
		}

		if (isNodeSelectedFiredOnCaptionClick()) {
			titlePanel.addClickListener(event -> fireTreeNodeSelectedEvent(currentEditableTreeNode));
		}

		controls = buildControlsLayout(currentEditableTreeNode);
		updateControlsVisibility();

		layout.addComponents(titlePanel, controls);

		return layout;
	}

	protected Component buildControlsLayout(EditableTreeNode currentEditableTreeNode) {
		HorizontalLayout controlsLayout = new HorizontalLayout();
		controlsLayout.addComponents(buildAddControl(), buildDeleteControl(currentEditableTreeNode));

		return controlsLayout;
	}

	@NotNull
	public MenuBar buildDeleteControl(EditableTreeNode currentEditableTreeNode) {
		MenuBar deleteMenuBar = new BaseMenuBar(true, false);
		deleteComponent = deleteMenuBar.addItem("", FontAwesome.TRASH, selectedItem -> {
			removeThisTreeNodeListeners.fire(new RemoveThisEditableTreeNodeEvent(currentEditableTreeNode));
		});
		return deleteMenuBar;
	}

	@NotNull
	public Component buildAddControl() {
		MenuBar addMenuBar = new BaseMenuBar(true, false);

		if (!possibleChildrenTypes.isEmpty()) {
			MenuItem addOption = addMenuBar.addItem("", FontAwesome.PLUS, null);
			possibleChildrenTypes.stream().forEach(possibleChildrenType -> {
				addOption.addItem(possibleChildrenType.getCaption(), possibleChildrenType.getIcon(), menuBarItem -> {

					EditableTreeNode newChildEditableTreeNode = possibleChildrenType.build();
					newChildEditableTreeNode.setEditing(true);

					fireChildNodeIsBuildingEvent(newChildEditableTreeNode);
				});
			});
		}
		return addMenuBar;
	}

	protected Component buildEditingComponent() {
		final EditableTreeNode currentEditableTreeNode = this;

		Label icon = new Label();
		icon.setIcon(getIcon());

		TextField edit = new BaseTextField();
		edit.setPropertyDataSource(titleProperty);
		edit.addBlurListener(event -> {

			if (!isCommitedOnce && Strings.isNullOrEmpty(getEditedTitle())) {
				removeThisTreeNodeListeners.fire(new RemoveThisEditableTreeNodeEvent(currentEditableTreeNode));
			} else {
				treeNodeHasBuildedlisteners.fire(new EditableTreeNodeHasBuildedEvent(currentEditableTreeNode));
			}

		});
		edit.setImmediate(true);
		edit.addStyleName(ValoTheme.TEXTFIELD_BORDERLESS);

		Panel panel = new Panel();
		panel.setWidthUndefined();
		panel.setHeightUndefined();
		panel.setContent(edit);
		panel.addActionHandler(new EnterPressActionHandler((sender, target) -> treeNodeHasBuildedlisteners.fire(new EditableTreeNodeHasBuildedEvent(currentEditableTreeNode))));
		panel.addActionHandler(new EscapePressActionHandler((sender, target) -> {
			removeThisTreeNodeListeners.fire(new RemoveThisEditableTreeNodeEvent(currentEditableTreeNode));
		}));

		panel.addStyleName(ValoTheme.PANEL_BORDERLESS);

		HorizontalLayout layout = new I18NHorizontalLayout() {
			@Override
			public void setVisible(boolean visible) {
				super.setVisible(visible);
				edit.focus();
			}
		};

		layout.addStyleName("display-value-layout");
		layout.addStyleName(ValoTheme.FORMLAYOUT_LIGHT);

		layout.addComponents(icon, panel);

		return layout;
	}

	private void updateControlsVisibility() {
		if (controls != null) {
			controls.setVisible(isEditableComputed());
		}
	}

	public abstract String getCaption();

	protected abstract boolean hasChildren();

	@NotNull
	public abstract List<EditableTreeNode> getChildren();

	public Resource getIcon() {
		return getCurrentTreeNodeFactoryType().getIcon();
	}


	@NotNull
	protected List<EditableTreeNodeFactoryType> getPossibleChildrenTypes() {
		return new ArrayList<>();
	}

	@NotNull
	protected List<EditableTreeNodeFactoryType> getDroppableChildrenTypes() {
		return Collections.emptyList();
	}

	@NotNull
	protected abstract EditableTreeNodeFactoryType<T> getCurrentTreeNodeFactoryType();

	public T getModel() {
		return model;
	}

	public Class<?> getModelClass() {
		return getCurrentTreeNodeFactoryType().getSourceClass();
	}

	protected <TValue> TValue getValueFromModel(Function<T, TValue> supplierProvider) {
		return getValueFromModel(supplierProvider, value -> value);
	}

	protected <TValue> TValue getValueFromModel(Function<T, TValue> supplierProvider,
												Function<TValue, TValue> applyDefaultValueInstead) {
		TValue value = null;
		T model = getModel();

		if (model == null) {
			return applyDefaultValueInstead.apply(value);
		}

		return applyDefaultValueInstead.apply(supplierProvider.apply(model));
	}

	protected boolean isNodeSelectedFiredOnCaptionClick() {
		return true;
	}

	public String getEditedTitle() {
		return titleProperty.getValue();
	}

	public void setEditing(boolean editing) {
		this.editing = editing;

		displayComponent.setVisible(!editing);
		editingComponent.setVisible(editing);
	}

	public void setEditable(boolean editable) {
		isEditable = editable;
		updateControlsVisibility();
	}

	public boolean isEditable() {
		return isEditable;
	}

	public void setForcedNotEditable(boolean forcedNotEditable) {
		isForcedNotEditable = forcedNotEditable;

		List<EditableTreeNode> children = getChildren();

		if (children != null) {
			children.forEach(child -> child.setForcedNotEditable(forcedNotEditable));
		}

		updateControlsVisibility();
	}

	public boolean isForcedNotEditable() {
		return isForcedNotEditable;
	}

	public boolean isEditableComputed() {
		return !isForcedNotEditable() && isEditable();
	}

	public boolean isEditing() {
		return editing;
	}

	public boolean isCommitedOnce() {
		return isCommitedOnce;
	}

	public void setDeletable(boolean deletable) {
		isDeletable = deletable;

		if (deleteComponent != null) {
			deleteComponent.setVisible(deletable);
		}
	}

	public boolean isDeletable() {
		return isDeletable;
	}

	public void addChildNodeIsBuildingListener(EditableTreeNodeIsBuildingEventListener listener) {
		treeNodeBuildingListeners.addListener(listener);
	}

	public void removeChildNodeIsBuildingListener(EditableTreeNodeIsBuildingEventListener listener) {
		treeNodeBuildingListeners.removeListener(listener);
	}

	protected void fireChildNodeIsBuildingEvent(EditableTreeNode editableTreeNode) {
		treeNodeBuildingListeners.fire(new EditableTreeNodeIsBuildingEvent(editableTreeNode));
	}

	public void addNodeHasBuildedListener(EditableTreeNodeHasBuildedEventListener listener) {
		treeNodeHasBuildedlisteners.addListener(listener);
	}

	public void removeNodeHasBuildedListener(EditableTreeNodeHasBuildedEventListener listener) {
		treeNodeHasBuildedlisteners.removeListener(listener);
	}

	public void addReplaceThisTreeNodeListener(ReplaceThisEditableTreeNodeEventListener listener) {
		replaceThisTreeNodeListeners.addListener(listener);
	}

	public void removeReplaceThisTreeNodeListener(ReplaceThisEditableTreeNodeEventListener listener) {
		replaceThisTreeNodeListeners.removeListener(listener);
	}

	public void addRemoveThisTreeNodeListener(RemoveThisEditableTreeNodeEventListener listener) {
		removeThisTreeNodeListeners.addListener(listener);
	}

	public void removeRemoveThisTreeNodeListener(RemoveThisEditableTreeNodeEventListener listener) {
		removeThisTreeNodeListeners.removeListener(listener);
	}

	protected void fireRemoveThisTreeNodeEvent(EditableTreeNode editableTreeNode) {
		removeThisTreeNodeListeners.fire(new RemoveThisEditableTreeNodeEvent(editableTreeNode));
	}

	public void addTreeNodeSelectedListener(EditableTreeNodeSelectedEventListener listener) {
		treeNodeSelectedListeners.addListener(listener);
	}

	public void removeTreeNodeSelectedListener(EditableTreeNodeSelectedEventListener listener) {
		treeNodeSelectedListeners.removeListener(listener);
	}

	protected void fireTreeNodeSelectedEvent(EditableTreeNode editableTreeNode) {
		treeNodeSelectedListeners.fire(new EditableTreeNodeSelectedEvent(editableTreeNode));
	}

	@Override
	public String toString() {
		return getCaption();
	}

	@Override
	public Transferable getTransferable(Map<String, Object> rawVariables) {
		rawVariables.put("editableTreeNode", this);
		return super.getTransferable(rawVariables);
	}

	public boolean isDraggable() {
		return false;
	}
}
