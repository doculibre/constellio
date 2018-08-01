package com.constellio.app.ui.pages.unicitymetadataconf;

import com.constellio.app.ui.entities.FolderUnicityVO;
import com.constellio.app.ui.entities.MetadataVO;
import com.constellio.app.ui.framework.components.BaseForm;
import com.constellio.app.ui.framework.components.fields.BaseComboBox;
import com.constellio.app.ui.framework.components.table.BaseTable;
import com.constellio.app.ui.framework.containers.FolderUniqueKeyContainer;
import com.constellio.app.ui.framework.data.FolderUniqueKeyDataProvider;
import com.constellio.app.ui.pages.base.BaseViewImpl;
import com.constellio.app.ui.params.ParamUtils;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.vaadin.data.fieldgroup.PropertyId;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.ui.*;
import org.vaadin.dialogs.ConfirmDialog;

import java.util.*;

import static com.constellio.app.ui.i18n.i18n.$;

public class FolderUniqueKeyConfiguratorViewImpl extends BaseViewImpl implements FolderUniqueKeyConfiguratorView {

	private FolderUniqueKeyConfiguratorPresenter presenter;

	@PropertyId("metadataVO")
	private ComboBox metadataComboBox;
	private String schemaCode;

	private FolderUniqueKeyDataProvider folderUniqueKeyDataProvider;

	private Table table;
	private FolderUniqueKeyContainer folderUniqueKeyContainer;

	public FolderUniqueKeyConfiguratorViewImpl() {
		presenter = new FolderUniqueKeyConfiguratorPresenter(this);
	}

	@Override
	protected void initBeforeCreateComponents(ViewChangeListener.ViewChangeEvent event) {
		Map<String, String> params = ParamUtils.getParamsMap(event.getParameters());
		schemaCode = params.get("schemaCode");
		presenter.setSchemaCode(schemaCode);
	}

	@Override
	public String getTitle() {
		return $("FolderUniqueKeyMetadataConfiguratorViewImpl.title");
	}

	@Override
	protected Component buildMainComponent(ViewChangeListener.ViewChangeEvent event) {
		metadataComboBox = new BaseComboBox($("FolderUniqueKeyMetadataConfiguratorViewImpl.metadata"));

		List<FolderUnicityVO> summaryColumnVOList = presenter.folderUnicityVOList();

		metadataComboBox.setTextInputAllowed(false);
		metadataComboBox.setRequired(true);
		metadataComboBox.setImmediate(true);

		refreshMetadataCombox();

		table = new BaseTable(getClass().getName());

		folderUniqueKeyDataProvider = new FolderUniqueKeyDataProvider(summaryColumnVOList);
		folderUniqueKeyContainer = new FolderUniqueKeyContainer(folderUniqueKeyDataProvider, this);

		table.setContainerDataSource(folderUniqueKeyContainer);
		table.setColumnHeader(FolderUniqueKeyContainer.METADATA_VO, $("FolderUniqueKeyMetadataConfiguratorViewImpl.metadataHeader"));
		table.setColumnHeader(FolderUniqueKeyContainer.DELETE, "");
		table.setColumnExpandRatio(FolderUniqueKeyContainer.METADATA_VO, 1);

		BaseForm<FolderUniqueKeyParams> baseForm = new BaseForm<FolderUniqueKeyParams>(new FolderUniqueKeyParams(), this, metadataComboBox) {
			@Override
			protected void saveButtonClick(final FolderUniqueKeyParams viewObject) {
				if (!presenter.isReindextionFlag()) {
					ConfirmDialog.show(
							UI.getCurrent(),
							$("FolderUniqueKeyMetadataConfiguratorViewImpl.save.title"),
							$("FolderUniqueKeyMetadataConfiguratorViewImpl.save.message"),
							$("Ok"),
							$("cancel"),
							new ConfirmDialog.Listener() {
								@Override
								public void onClose(ConfirmDialog dialog) {
									if (dialog.isConfirmed()) {
										addConfiguration(viewObject);
										updateUI();
									}
								}
							});

				} else {
					addConfiguration(viewObject);
				}
			}

			@Override
			protected void cancelButtonClick(FolderUniqueKeyParams viewObject) {
				// button not visible so no action here.
			}

			@Override
			protected boolean isCancelButtonVisible() {
				return false;
			}
		};

		baseForm.getSaveButton().setCaption($("add"));

		this.table.setWidth("100%");

		VerticalLayout verticalLayout = new VerticalLayout();
		verticalLayout.setSpacing(true);
		verticalLayout.addComponent(baseForm);
		verticalLayout.addComponent(table);

		return verticalLayout;
	}

	private void addConfiguration(FolderUniqueKeyParams viewObject) {
		FolderUnicityVO folderUnicityVO = summaryColumnParamsToSummaryVO(viewObject);

		presenter.addMetadaForUnicity(viewObject);
		folderUniqueKeyDataProvider.addFolderUnicityVO(folderUnicityVO);

		folderUniqueKeyDataProvider.fireDataRefreshEvent();
		clearFields();
		removeMetadataFromPossibleSelection();
	}

	private void clearFields() {
		this.metadataComboBox.setValue(null);
	}

	private void refreshMetadataCombox() {
		metadataComboBox.removeAllItems();

		List<MetadataVO> metadataVOS = presenter.getMetadatas();

		Collections.sort(metadataVOS, new Comparator<MetadataVO>() {
			@Override
			public int compare(MetadataVO o1, MetadataVO o2) {
				Locale currentLocale = getSessionContext().getCurrentLocale();
				return o1.getLabel(currentLocale).compareTo(o2.getLabel(currentLocale));
			}
		});

		for (MetadataVO metadataVO : metadataVOS) {
			if (metadataVO.getType() != MetadataValueType.STRUCTURE && !metadataVO.getLocalCode().equals("summary")) {
				metadataComboBox.addItem(metadataVO);
			}
		}

		removeMetadataFromPossibleSelection();
	}


	private FolderUnicityVO summaryColumnParamsToSummaryVO(FolderUniqueKeyParams folderUniqueKeyParams) {
		FolderUnicityVO summaryColumnVO = new FolderUnicityVO();
		summaryColumnVO.setMetadataVO(folderUniqueKeyParams.getMetadataVO());

		return summaryColumnVO;
	}


	private void removeMetadataFromPossibleSelection() {
		List<FolderUnicityVO> summaryColumnVOList = presenter.folderUnicityVOList();

		for (FolderUnicityVO summaryColumnVO : summaryColumnVOList) {
			this.metadataComboBox.removeItem(summaryColumnVO.getMetadataVO());
		}
	}


	public void deleteSummaryMetadata(FolderUnicityVO summaryColumnVO) {
		this.presenter.deleteMetadataForSummaryColumn(summaryColumnVO);
		this.folderUniqueKeyDataProvider.removeFolderUnicityVO(summaryColumnVO);
		refreshMetadataCombox();
		this.folderUniqueKeyDataProvider.fireDataRefreshEvent();
	}

	public void deleteRow(final FolderUnicityVO columnVO) {

		String message = $("FolderUniqueKeyMetadataConfiguratorViewImpl.deleteConfirmationMesssage");
		if (!presenter.isReindextionFlag()) {
			message = $("FolderUniqueKeyMetadataConfiguratorViewImpl.save.message") + " " + message;
		}


		ConfirmDialog.show(
				UI.getCurrent(),
				$("FolderUniqueKeyMetadataConfiguratorViewImpl.deleteConfirmation"),
				message,
				$("Ok"),
				$("cancel"),
				new ConfirmDialog.Listener() {
					@Override
					public void onClose(ConfirmDialog dialog) {
						if (dialog.isConfirmed()) {
							boolean isReindextionFlag = presenter.isReindextionFlag();
							deleteSummaryMetadata(columnVO);
							if (!isReindextionFlag) {
								updateUI();
							}
						}
					}
				});
	}
}
