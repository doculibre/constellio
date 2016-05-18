package com.constellio.app.modules.rm.ui.pages.cart;

import static com.constellio.app.ui.i18n.i18n.$;

import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

import org.vaadin.dialogs.ConfirmDialog;

import com.constellio.app.ui.framework.buttons.DeleteButton;
import com.constellio.app.ui.framework.buttons.LinkButton;
import com.constellio.app.ui.framework.components.ReportViewer.DownloadStreamResource;
import com.constellio.app.ui.framework.components.table.RecordVOTable;
import com.constellio.app.ui.framework.containers.ButtonsContainer;
import com.constellio.app.ui.framework.containers.ButtonsContainer.ContainerButton;
import com.constellio.app.ui.framework.containers.RecordVOWithDistinctSchemaTypesLazyContainer;
import com.constellio.app.ui.framework.data.RecordVOWithDistinctSchemasDataProvider;
import com.constellio.app.ui.pages.base.BaseViewImpl;
import com.constellio.model.services.schemas.builders.CommonMetadataBuilder;
import com.vaadin.data.Container;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.server.Page;
import com.vaadin.server.Resource;
import com.vaadin.server.StreamResource.StreamSource;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.Table;
import com.vaadin.ui.VerticalLayout;

public class CartViewImpl extends BaseViewImpl implements CartView {
	private final CartPresenter presenter;

	public CartViewImpl() {
		presenter = new CartPresenter(this);
	}

	@Override
	protected void initBeforeCreateComponents(ViewChangeEvent event) {
		super.initBeforeCreateComponents(event);
		presenter.forParams(event.getParameters());
	}

	@Override
	protected String getTitle() {
		return $("CartView.viewTitle");
	}

	@Override
	protected List<Button> buildActionMenuButtons(ViewChangeEvent event) {
		List<Button> buttons = super.buildActionMenuButtons(event);
		buttons.add(buildPrepareEmailButton());
		buttons.add(buildBatchDuplicateButton());
		buttons.add(buildBatchDeleteButton());
		buttons.add(buildEmptyButton());
		return buttons;
	}

	@Override
	protected Component buildMainComponent(ViewChangeEvent event) {
		Container container = buildContainer(presenter.getRecords());
		Table table = new RecordVOTable($("CartView.records", container.size()), container);
		table.setColumnHeader(CommonMetadataBuilder.TITLE, $("init.allTypes.allSchemas.title"));
		table.setColumnHeader(ButtonsContainer.DEFAULT_BUTTONS_PROPERTY_ID, "");
		table.setColumnWidth(ButtonsContainer.DEFAULT_BUTTONS_PROPERTY_ID, 50);
		table.setPageLength(Math.min(15, container.size()));
		table.setSizeFull();

		VerticalLayout layout = new VerticalLayout(table);
		layout.setSizeFull();
		return layout;
	}

	@Override
	public void startDownload(final InputStream stream) {
		Resource resource = new DownloadStreamResource(new StreamSource() {
			@Override
			public InputStream getStream() {
				return stream;
			}
		}, "cart.eml");
		Page.getCurrent().open(resource, null, false);
	}

	private Button buildPrepareEmailButton() {
		Button button = new LinkButton($("CartView.prepareEmail")) {
			@Override
			protected void buttonClick(ClickEvent event) {
				presenter.emailPreparationRequested();
			}
		};
		button.setEnabled(presenter.canPrepareEmail());
		return button;
	}

	private Button buildBatchDuplicateButton() {
		Button button = new LinkButton($("CartView.batchDuplicate")) {
			@Override
			protected void buttonClick(ClickEvent event) {
				presenter.duplicationRequested();
			}
		};
		button.setEnabled(presenter.canDelete());
		return button;
	}

	private Button buildBatchDeleteButton() {
		Button button = new DeleteButton(false) {
			@Override
			protected void confirmButtonClick(ConfirmDialog dialog) {
				presenter.deletionRequested();
			}
		};
		button.setEnabled(presenter.canDelete());
		return button;
	}

	private Button buildEmptyButton() {
		Button button = new LinkButton($("CartView.empty")) {
			@Override
			protected void buttonClick(ClickEvent event) {
				presenter.cartEmptyingRequested();
			}
		};
		button.setEnabled(presenter.canEmptyCart());
		return button;
	}

	private Container buildContainer(final RecordVOWithDistinctSchemasDataProvider dataProvider) {
		RecordVOWithDistinctSchemaTypesLazyContainer records = new RecordVOWithDistinctSchemaTypesLazyContainer(
				dataProvider, Arrays.asList(CommonMetadataBuilder.TITLE));
		ButtonsContainer<RecordVOWithDistinctSchemaTypesLazyContainer> container = new ButtonsContainer<>(records);
		container.addButton(new ContainerButton() {
			@Override
			protected Button newButtonInstance(final Object itemId) {
				return new DeleteButton() {
					@Override
					protected void confirmButtonClick(ConfirmDialog dialog) {
						int index = (int) itemId;
						presenter.itemRemovalRequested(dataProvider.getRecordVO(index));
					}
				};
			}
		});
		return container;
	}
}
