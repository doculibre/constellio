package com.constellio.app.modules.tasks.ui.pages.workflow;

import static com.constellio.app.ui.i18n.i18n.$;

import java.util.ArrayList;
import java.util.List;

import org.vaadin.dialogs.ConfirmDialog;

import com.constellio.app.modules.tasks.ui.entities.WorkflowVO;
import com.constellio.app.ui.framework.buttons.AddButton;
import com.constellio.app.ui.framework.buttons.DeleteButton;
import com.constellio.app.ui.framework.buttons.DisplayButton;
import com.constellio.app.ui.framework.buttons.EditButton;
import com.constellio.app.ui.framework.components.table.RecordVOTable;
import com.constellio.app.ui.framework.containers.ButtonsContainer;
import com.constellio.app.ui.framework.containers.ButtonsContainer.ContainerButton;
import com.constellio.app.ui.pages.base.BaseViewImpl;
import com.vaadin.data.Item;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Component;
import com.vaadin.ui.Table;
import com.vaadin.ui.VerticalLayout;

public class ListWorkflowsViewImpl extends BaseViewImpl implements ListWorkflowsView {
	
	private List<WorkflowVO> workflowVOs = new ArrayList<WorkflowVO>();
	
	private VerticalLayout mainLayout;
	
	private AddButton addButton;
	
	private Table table;
	
	private ListWorkflowsPresenter presenter;

	public ListWorkflowsViewImpl() {
		this.presenter = new ListWorkflowsPresenter(this);
	}

	@Override
	public void setWorkflowVOs(List<WorkflowVO> workflowVOs) {
		this.workflowVOs = workflowVOs;
	}

	@Override
	public void remove(WorkflowVO workflowVO) {
		table.removeItem(workflowVO);
	}

	@Override
	protected Component buildMainComponent(ViewChangeEvent event) {
		mainLayout = new VerticalLayout();
		mainLayout.setSizeFull();
		mainLayout.setSpacing(true);
		
		createAddButton();
		createTable();
		
		mainLayout.addComponents(addButton, table);
		mainLayout.setComponentAlignment(addButton, Alignment.TOP_RIGHT);
		mainLayout.setExpandRatio(table, 1);
		
		return mainLayout;
	}
	
	private void createAddButton() {
		addButton = new AddButton() {
			@Override
			protected void buttonClick(ClickEvent event) {
				presenter.addButtonClicked();
			}
		};
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private void createTable() {
		table = new RecordVOTable($("ListWorkflowsView.tableTitle"));
		table.setWidth("100%");
		
		table.addContainerProperty("code", String.class, "");
		table.addContainerProperty("title", String.class, "");
		
		ButtonsContainer<?> buttonsContainer = new ButtonsContainer(table.getContainerDataSource());
		buttonsContainer.addButton(new ContainerButton() {
			@Override
			protected Button newButtonInstance(final Object itemId) {
				return new DisplayButton() {
					@Override
					protected void buttonClick(ClickEvent event) {
						presenter.displayButtonClicked((WorkflowVO) itemId); 
					}
				};
			}
		});
		buttonsContainer.addButton(new ContainerButton() {
			@Override
			protected Button newButtonInstance(final Object itemId) {
				return new EditButton() {
					@Override
					protected void buttonClick(ClickEvent event) {
						presenter.editButtonClicked((WorkflowVO) itemId); 
					}
				};
			}
		});
		buttonsContainer.addButton(new ContainerButton() {
			@Override
			protected Button newButtonInstance(final Object itemId) {
				return new DeleteButton() {
					@Override
					protected void confirmButtonClick(ConfirmDialog dialog) {
						presenter.deleteButtonClicked((WorkflowVO) itemId); 
					}
				};
			}
		});
		
		table.setContainerDataSource(buttonsContainer);
		table.setColumnHeader("code", $("ListWorkflowsView.table.code"));
		table.setColumnHeader("title", $("ListWorkflowsView.table.title"));
		table.setColumnHeader(ButtonsContainer.DEFAULT_BUTTONS_PROPERTY_ID, "");
		table.setColumnExpandRatio("title", 1);
		
		for (WorkflowVO workflowVO : workflowVOs) {
			Item item = table.addItem(workflowVO);
			item.getItemProperty("code").setValue(workflowVO.getCode());
			item.getItemProperty("title").setValue(workflowVO.getTitle());
		}
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
	protected String getTitle() {
		return $("ListWorkflowsView.viewTitle");
	}

}
