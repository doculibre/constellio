package com.constellio.app.modules.robots.ui.components.actionParameters;

import com.constellio.app.api.extensions.params.RecordFieldFactoryExtensionParams;
import com.constellio.app.extensions.AppLayerCollectionExtensions;
import com.constellio.app.modules.complementary.esRmRobots.model.enums.ActionAfterClassification;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.services.factories.ConstellioFactories;
import com.constellio.app.ui.application.ConstellioUI;
import com.constellio.app.ui.entities.MetadataVO;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.framework.buttons.WindowButton;
import com.constellio.app.ui.framework.buttons.WindowButton.WindowConfiguration;
import com.constellio.app.ui.framework.components.RecordDisplay;
import com.constellio.app.ui.framework.components.RecordFieldFactory;
import com.constellio.app.ui.framework.components.RecordForm;
import com.constellio.model.frameworks.validation.ValidationException;
import com.vaadin.ui.*;
import com.vaadin.ui.TabSheet.Tab;
import com.vaadin.ui.themes.ValoTheme;
import org.apache.commons.lang3.StringUtils;
import org.vaadin.dialogs.ConfirmDialog;

import java.util.Iterator;

import static com.constellio.app.modules.complementary.esRmRobots.model.ClassifyConnectorDocumentInFolderActionParameters.ACTION_AFTER_CLASSIFICATION;
import static com.constellio.app.ui.i18n.i18n.$;

public class DynamicParametersField extends CustomField<String> {

	public static final String RECORD_FIELD_FACTORY_KEY = DynamicParametersField.class.getName();

	private final DynamicParametersPresenter presenter;
	private VerticalLayout layout;
	private Button button;

	private RecordVO record;

	public DynamicParametersField(DynamicParametersPresenter presenter) {
		this.presenter = presenter;
	}

	@Override
	public void setEnabled(boolean enabled) {
		super.setEnabled(enabled);
		if (button != null) {
			button.setEnabled(enabled);
		}
	}

	public void resetWithRecord(RecordVO record) {
		this.record = record;
		layout.removeAllComponents();
		if (record == null) {
			initEmptyContent();
		} else {
			initRecordContent();
		}
		button.setEnabled(isEnabled());
	}

	@Override
	protected Component initContent() {
		layout = new VerticalLayout();
		layout.setSpacing(true);
		resetWithRecord(presenter.getDynamicParametersRecord());
		return layout;
	}

	@Override
	protected String getInternalValue() {
		return record != null ? record.getId() : null;
	}

	private void initEmptyContent() {
		button = buildEditButton();
		layout.addComponent(button);
	}

	private void initRecordContent() {
		button = buildEditButton();
		layout.addComponents(new RecordDisplay(record), button);
	}

	private WindowButton buildEditButton() {
		WindowButton button = new WindowButton($("DynamicParametersField.editParametersButton"),
				$("DynamicRecordParametersField.editParametersWindow"), WindowConfiguration.modalDialog("75%", "75%")) {
			@SuppressWarnings("serial")
			@Override
			protected Component buildWindowContent() {
				RecordVO effectiveRecord;
				if (record != null) {
					effectiveRecord = record;
				} else {
					effectiveRecord = presenter.newDynamicParametersRecord();
				}

				String collection = ConstellioUI.getCurrentSessionContext().getCurrentCollection();
				AppLayerFactory appLayerFactory = ConstellioUI.getCurrent().getConstellioFactories().getAppLayerFactory();
				AppLayerCollectionExtensions extensions = appLayerFactory.getExtensions().forCollection(collection);
				RecordFieldFactory recordFieldFactory = extensions
						.newRecordFieldFactory(new RecordFieldFactoryExtensionParams(RECORD_FIELD_FACTORY_KEY, null, effectiveRecord));
				if (recordFieldFactory == null) {
					recordFieldFactory = new RecordFieldFactory();
				}

				return new LocalRecordForm(effectiveRecord, recordFieldFactory, new SaveActionListener() {

					@Override
					public void confirmBeforeSave(final RecordVO viewObject) {
						ConfirmDialog confirmDialog = createConfirmDialog();

						confirmDialog.getOkButton().addClickListener(new ClickListener() {
							@Override
							public void buttonClick(ClickEvent event) {
								completeSaveAction(viewObject);
							}
						});

						confirmDialog.show(UI.getCurrent(), new ConfirmDialog.Listener() {
							@Override
							public void onClose(ConfirmDialog dialog) {
							}
						}, true);
					}

					private ConfirmDialog createConfirmDialog() {
						return ConfirmDialog.getFactory().create($("ActionAfterClassification.d.confirmation.dialog.title"),
								$("ActionAfterClassification.d.confirmation"), $("OK"), $("cancel"), null);
					}

					@Override
					public void completeSaveAction(final RecordVO viewObject) {
						if (presenter.saveParametersRecord(viewObject)) {
							getWindow().close();
						}
					}

					@Override
					public void completeCancelAction(RecordVO viewObject) {
						presenter.cancelParametersEdit(viewObject);
						getWindow().close();
					}
				});
			}
		};
		button.addStyleName(ValoTheme.BUTTON_LINK);
		return button;
	}

	private final class LocalRecordForm extends RecordForm {
		private static final String TAB_TO_HIDE = "tab.to.hide";
		private final SaveActionListener actionListener;

		private LocalRecordForm(RecordVO pRecordVO, RecordFieldFactory pFormFieldFactory,
								SaveActionListener actionListener) {
			super(pRecordVO, pFormFieldFactory, ConstellioFactories.getInstance());

			this.actionListener = actionListener;

			for (Iterator<Component> iterator = tabSheet.iterator(); iterator.hasNext(); ) {
				Tab tab = tabSheet.getTab(iterator.next());
				tab.setVisible(!StringUtils.endsWith(tab.getCaption(), TAB_TO_HIDE));
			}
		}

		@Override
		protected void saveButtonClick(final RecordVO viewObject)
				throws ValidationException {
			MetadataVO metadata = viewObject.getSchema().getMetadata(ACTION_AFTER_CLASSIFICATION);
			ActionAfterClassification aac = viewObject.get(metadata);
			if (aac == ActionAfterClassification.DELETE_DOCUMENTS_ON_ORIGINAL_SYSTEM) {
				actionListener.confirmBeforeSave(viewObject);
			} else {
				actionListener.completeSaveAction(viewObject);
			}
		}

		@Override
		protected void cancelButtonClick(RecordVO viewObject) {
			actionListener.completeCancelAction(viewObject);
		}
	}

	private interface SaveActionListener {
		public void confirmBeforeSave(RecordVO viewObject);

		public void completeSaveAction(RecordVO viewObject);

		public void completeCancelAction(RecordVO viewObject);
	}

	@Override
	public Class<? extends String> getType() {
		return String.class;
	}

	public interface DynamicParametersPresenter {
		RecordVO getDynamicParametersRecord();

		RecordVO newDynamicParametersRecord();

		boolean saveParametersRecord(RecordVO viewObject);

		void cancelParametersEdit(RecordVO viewObject);
	}
}
