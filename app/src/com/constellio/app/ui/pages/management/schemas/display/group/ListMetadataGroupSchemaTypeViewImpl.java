package com.constellio.app.ui.pages.management.schemas.display.group;

import com.constellio.app.ui.framework.buttons.AddButton;
import com.constellio.app.ui.framework.buttons.DeleteButton;
import com.constellio.app.ui.framework.buttons.EditButton;
import com.constellio.app.ui.framework.components.BaseForm;
import com.constellio.app.ui.framework.components.BaseWindow;
import com.constellio.app.ui.framework.components.fields.MultilingualTextField;
import com.constellio.app.ui.framework.components.layouts.I18NHorizontalLayout;
import com.constellio.app.ui.framework.components.table.BaseTable;
import com.constellio.app.ui.pages.base.BaseViewImpl;
import com.constellio.model.frameworks.validation.ValidationException;
import com.vaadin.data.Item;
import com.vaadin.data.fieldgroup.PropertyId;
import com.vaadin.event.Transferable;
import com.vaadin.event.dd.DragAndDropEvent;
import com.vaadin.event.dd.DropHandler;
import com.vaadin.event.dd.acceptcriteria.AcceptAll;
import com.vaadin.event.dd.acceptcriteria.AcceptCriterion;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.shared.ui.dd.VerticalDropLocation;
import com.vaadin.ui.AbstractSelect.AbstractSelectTargetDetails;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Component;
import com.vaadin.ui.Table;
import com.vaadin.ui.Table.TableDragMode;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import org.vaadin.dialogs.ConfirmDialog;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static com.constellio.app.ui.i18n.i18n.$;

public class ListMetadataGroupSchemaTypeViewImpl extends BaseViewImpl implements ListMetadataGroupSchemaTypeView {

	public static final String GROUP_TABLE = "metadata-groups";
	public static final String GROUP_NAME = "label";
	public static final String GROUP_BUTTON = "button";
	public static final String GROUP_EDIT_BUTTON = "edit_button";
	public static final String GROUP_DELETE_BUTTON = "delete_button";

	private VerticalLayout viewLayout;
	private Button addButton;
	private Table table;
	
	private List<String> metadataGroups;
	
	@PropertyId("code")
	private TextField codeField;
	
	@PropertyId("labels")
	private MultilingualTextField labelsField;

	private ListMetadataGroupSchemaTypePresenter presenter;
	
	public ListMetadataGroupSchemaTypeViewImpl() {
		this.presenter = new ListMetadataGroupSchemaTypePresenter(this);
	}

	@Override
	protected String getTitle() {
		return $("ListMetadataGroupSchemaTypeView.viewTitle");
	}

	@Override
	protected ClickListener getBackButtonClickListener() {
		return new ClickListener() {
			@Override
			public void buttonClick(ClickEvent event) {
				presenter.backButtonClicked();
			}
		};
	}

	@Override
	protected void initBeforeCreateComponents(ViewChangeEvent event) {
		presenter.forParams(event.getParameters());
	}

	@Override
	protected Component buildMainComponent(ViewChangeEvent event) {
		viewLayout = new VerticalLayout();
		viewLayout.setSizeFull();
		viewLayout.setSpacing(true);
		buildAddButton();
		buildTable();
		viewLayout.addComponents(addButton, table);
		viewLayout.setComponentAlignment(addButton, Alignment.TOP_RIGHT);
		viewLayout.setExpandRatio(table, 1);
		return viewLayout;
	}
	
	private void buildAddButton() {
		addButton = new AddButton() {
			@Override
			protected void buttonClick(ClickEvent event) {
				presenter.addButtonClicked();
			}
		};
	}

	private void buildTable() {
		table = new BaseTable(getClass().getName());
		table.addStyleName(GROUP_TABLE);
		table.setWidth("100%");
		
		table.setColumnHeader("buttons", "");
		table.setColumnWidth("buttons", 80);
		table.setColumnHeader("code", $("ListMetadataGroupSchemaTypeView.code"));
		table.addContainerProperty("code", String.class, "");
		
		for (String language : presenter.getCollectionLanguages()) {
			table.setColumnHeader(language, language);
			table.addContainerProperty(language, String.class, "");
		}
		table.addContainerProperty("buttons", I18NHorizontalLayout.class, null);

		for (String group : metadataGroups) {
			setItem(group, presenter.getGroupLabels(group));
		}
		
		table.setDragMode(TableDragMode.ROW);
		table.setDropHandler(new DropHandler() {
			@Override
			public AcceptCriterion getAcceptCriterion() {
				return AcceptAll.get();
			}
			
			@Override
			public void drop(DragAndDropEvent event) {
                Transferable t = event.getTransferable();
                if (t.getSourceComponent() != table || table.size() <= 1) {
                    return;
                }

                AbstractSelectTargetDetails target = (AbstractSelectTargetDetails) event.getTargetDetails();
                Object sourceItemId = t.getData("itemId");
                Object targetItemId = target.getItemIdOver();

                Boolean above;
                if (target.getDropLocation().equals(VerticalDropLocation.TOP)) {
                	above = true;
                } else if (target.getDropLocation().equals(VerticalDropLocation.MIDDLE) && targetItemId.equals(table.firstItemId())) {	
                	above = true;
                } else {
                	above = false;
                }
                
            	presenter.groupDroppedOn((String) sourceItemId, (String) targetItemId, above);
        		moveAfter(targetItemId, sourceItemId);
            	if (Boolean.TRUE.equals(above)) {
            		moveAfter(sourceItemId, targetItemId);
            	}
			}
		});
	}

	@SuppressWarnings("unchecked")
	private void setItem(final String group, Map<String, String> labels) {
		if (table.getItem(group) == null) {
			table.addItem(group);
		}
		table.getContainerProperty(group, "code").setValue(group);
		for (String language : presenter.getCollectionLanguages()) {
			String groupLabel = labels.get(language);
			table.getContainerProperty(group, language).setValue(groupLabel);
		}
		
		Button editButton = new EditButton() {
			@Override
			protected void buttonClick(ClickEvent event) {
				presenter.editButtonClicked(group);
			}
		};
		editButton.addStyleName(GROUP_EDIT_BUTTON);

		Button deleteButton = new DeleteButton() {
			@Override
			protected void confirmButtonClick(ConfirmDialog dialog) {
				presenter.deleteButtonClicked(group);
			}
		};
		deleteButton.addStyleName(GROUP_DELETE_BUTTON);
		deleteButton.setVisible(isNotDefaultGroup(group));
		deleteButton.setEnabled(isNotDefaultGroup(group));
		
		I18NHorizontalLayout buttonsLayout = new I18NHorizontalLayout(editButton, deleteButton);

		table.getContainerProperty(group, "buttons").setValue(buttonsLayout);
	}

	private boolean isNotDefaultGroup(String group) {
		return !group.equals(presenter.getDefaultMetadataGroupCode());
	}

    @SuppressWarnings("unchecked")
    /**
     * 
     * @param targetItemId
     * @param sourceItemId
     * @return ItemId of the object the item moved to
     */
    public Object moveAfter(Object targetItemId, Object sourceItemId) {
        if(sourceItemId == null)
            return null;
        Item sourceItem = table.getItem(sourceItemId);

        Object[] propertyIds = table.getContainerPropertyIds().toArray();
        int size = propertyIds.length;
        Object[][] properties = new Object[size][2];

        // backup source item properties and values
        for(int i = 0; i < size; i++) {
            Object propertyId = propertyIds[i];
            Object value = sourceItem.getItemProperty(propertyId).getValue();
            properties[i][0] = propertyId;
            properties[i][1] = value;
        }
        table.removeItem(sourceItemId);
        Item item = table.addItemAfter(targetItemId, sourceItemId);

        // restore source item properties and values
        for(int i = 0; i < size; i++) {
            Object propertyId = properties[i][0];
            Object value = properties[i][1];
            item.getItemProperty(propertyId).setValue(value);
        }

        return sourceItemId;
    }

	@Override
	public void setMetadataGroups(List<String> metadataGroups) {
		this.metadataGroups = metadataGroups;
	}

	@Override
	public void addMetadataGroup(String code, Map<String, String> labels) {
		setItem(code, labels);
	}

	@Override
	public void updateMetadataGroup(String code, Map<String, String> labels) {
		setItem(code, labels);
	}

	@Override
	public void removeMetadataGroup(String code) {
		table.removeItem(code);
	}

	@Override
	public void displayAddError() {
		this.showErrorMessage($("ListMetadataGroupSchemaTypeView.addError"));
	}

	@Override
	public void displayDeleteError() {
		this.showErrorMessage($("ListMetadataGroupSchemaTypeView.deleteError"));
	}

	@Override
	public void invalidCodeOrLabels() {
		this.showErrorMessage($("ListMetadataGroupSchemaTypeView.invalidCodeOrLabels"));
	}
	
	@Override
	public void showAddWindow(List<String> languageCodes) {
		Map<String, String> labels = new LinkedHashMap<>();
		for (String languageCode : languageCodes) {
			labels.put(languageCode, "");
		}
		Window window = newFormWindow(null, labels, true);
		getUI().addWindow(window);
	}

	@Override
	public void showEditWindow(String code, Map<String, String> labels) {
		Window window = newFormWindow(code, labels, false);
		getUI().addWindow(window);
	}

	private Window newFormWindow(String code, Map<String, String> labels, final boolean addForm) {
		final MetadataGroupVO viewObject = new MetadataGroupVO();
		viewObject.setCode(code);
		viewObject.setLabels(labels);
		
		codeField = new TextField();
		codeField.setRequired(addForm);
		codeField.setEnabled(addForm);
		codeField.setCaption($("ListMetadataGroupSchemaTypeView.code"));
		codeField.setNullRepresentation("");

		labelsField = new MultilingualTextField(true);
		labelsField.setRequired(true);
		
		BaseForm<MetadataGroupVO> form = new BaseForm<MetadataGroupVO>(viewObject, this, codeField, labelsField) {
			@Override
			protected void saveButtonClick(MetadataGroupVO viewObject) throws ValidationException {
				String code = viewObject.getCode();
				Map<String, String> labels = viewObject.getLabels();
				presenter.saveButtonClicked(code, labels, addForm);
			}

			@Override
			protected void cancelButtonClick(MetadataGroupVO viewObject) {
				String code = viewObject.getCode();
				Map<String, String> labels = viewObject.getLabels();
				presenter.cancelButtonClicked(code, labels, addForm);
			}
		};
		
		String windowCaption = addForm ? $("ListMetadataGroupSchemaTypeView.addGroup") : $("ListMetadataGroupSchemaTypeView.editGroup");
		Window baseWindow = new BaseWindow(windowCaption, form);
		baseWindow.center();
		baseWindow.setWidth("500px");
		baseWindow.setHeight("400px");
		return baseWindow;
	}
	
	@Override
	protected boolean isFullWidthIfActionMenuAbsent() {
		return true;
	}

	public static class MetadataGroupVO {
		
		private String code;
		
		private Map<String, String> labels = new LinkedHashMap<>();

		public String getCode() {
			return code;
		}

		public void setCode(String code) {
			this.code = code;
		}

		public Map<String, String> getLabels() {
			return labels;
		}

		public void setLabels(Map<String, String> labels) {
			this.labels = labels;
		}
		
	}
	
}
