package com.constellio.app.ui.pages.unicitymetadataconf;

import com.constellio.app.ui.application.CoreViews;
import com.constellio.app.ui.application.Navigation;
import com.constellio.app.ui.application.NavigatorConfigurationService;
import com.constellio.app.ui.entities.FolderUnicityVO;
import com.constellio.app.ui.entities.MetadataVO;
import com.constellio.app.ui.framework.components.BaseForm;
import com.constellio.app.ui.framework.components.breadcrumb.BaseBreadcrumbTrail;
import com.constellio.app.ui.framework.components.breadcrumb.IntermediateBreadCrumbTailItem;
import com.constellio.app.ui.framework.components.breadcrumb.TitleBreadcrumbTrail;
import com.constellio.app.ui.framework.components.fields.lookup.MetadataVOLookupField;
import com.constellio.app.ui.framework.components.table.BaseTable;
import com.constellio.app.ui.framework.containers.FolderUniqueKeyContainer;
import com.constellio.app.ui.framework.data.FolderUniqueKeyDataProvider;
import com.constellio.app.ui.pages.base.BaseViewImpl;
import com.constellio.app.ui.pages.viewGroups.AdminViewGroup;
import com.constellio.app.ui.params.ParamUtils;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.vaadin.data.fieldgroup.PropertyId;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.ui.Component;
import com.vaadin.ui.Table;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import org.vaadin.dialogs.ConfirmDialog;

import java.util.*;

import static com.constellio.app.ui.i18n.i18n.$;
import static java.util.Arrays.asList;

public class FolderUniqueKeyConfiguratorViewImpl extends BaseViewImpl implements FolderUniqueKeyConfiguratorView, AdminViewGroup {

	private FolderUniqueKeyConfiguratorPresenter presenter;

	@PropertyId("metadataVO")
	private MetadataVOLookupField metadataLookupField;
	private String schemaCode;

	private FolderUniqueKeyDataProvider folderUniqueKeyDataProvider;

	private Table table;
	private FolderUniqueKeyContainer folderUniqueKeyContainer;

	List<MetadataVO> metadataVOList;

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
		metadataLookupField = new MetadataVOLookupField(new ArrayList<MetadataVO>());
		metadataLookupField.setCaption($("FolderUniqueKeyMetadataConfiguratorViewImpl.metadata"));

		List<FolderUnicityVO> unicityVOList = presenter.unicityVOList();

		metadataLookupField.setRequired(true);
		metadataLookupField.setImmediate(true);

		refreshMetadataLookup();

		table = new BaseTable(getClass().getName());

		folderUniqueKeyDataProvider = new FolderUniqueKeyDataProvider(unicityVOList);
		folderUniqueKeyContainer = new FolderUniqueKeyContainer(folderUniqueKeyDataProvider, this);

		table.setContainerDataSource(folderUniqueKeyContainer);
		table.setColumnHeader(FolderUniqueKeyContainer.METADATA_VO, $("FolderUniqueKeyMetadataConfiguratorViewImpl.metadataHeader"));
		table.setColumnHeader(FolderUniqueKeyContainer.DELETE, "");
		table.setColumnExpandRatio(FolderUniqueKeyContainer.METADATA_VO, 1);

		BaseForm<FolderUniqueKeyParams> baseForm = new BaseForm<FolderUniqueKeyParams>(new FolderUniqueKeyParams(), this, metadataLookupField) {
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
		FolderUnicityVO folderUnicityVO = uniqueKeyparamsToUniqueKeyVO(viewObject);

		presenter.addMetadaForUnicity(viewObject);
		folderUniqueKeyDataProvider.addFolderUnicityVO(folderUnicityVO);

		folderUniqueKeyDataProvider.fireDataRefreshEvent();
		clearFields();
		removeMetadataFromPossibleSelection();
	}

	private void clearFields() {
		this.metadataLookupField.setValue(null);
	}

	private void refreshMetadataLookup() {
		List<MetadataVO> metadataVOS = presenter.getMetadatas();

		List<MetadataVO> newMetadataVOList = new ArrayList<>();

		Collections.sort(metadataVOS, new Comparator<MetadataVO>() {
			@Override
			public int compare(MetadataVO o1, MetadataVO o2) {
				Locale currentLocale = getSessionContext().getCurrentLocale();
				return o1.getLabel(currentLocale).compareTo(o2.getLabel(currentLocale));
			}
		});

		for (MetadataVO metadataVO : metadataVOS) {
			if (metadataVO.getType() != MetadataValueType.STRUCTURE && !metadataVO.getLocalCode().equals("summary")) {
				newMetadataVOList.add(metadataVO);
			}
		}
		metadataVOList = newMetadataVOList;
		metadataLookupField.setOptions(newMetadataVOList);

		removeMetadataFromPossibleSelection();
	}



	private FolderUnicityVO uniqueKeyparamsToUniqueKeyVO(FolderUniqueKeyParams folderUniqueKeyParams) {
		FolderUnicityVO unicityVO = new FolderUnicityVO();
		unicityVO.setMetadataVO(folderUniqueKeyParams.getMetadataVO());

		return unicityVO;
	}


	private void removeMetadataFromPossibleSelection() {
		List<FolderUnicityVO> unictyVOList = presenter.unicityVOList();

		for (FolderUnicityVO uncityVO : unictyVOList) {
			removeMetadataVOFromList(uncityVO.getMetadataVO().getLocalCode());
		}
	}

	private void removeMetadataVOFromList(String code) {
		MetadataVO foundMetadataVO = null;
		for(MetadataVO metadataVO : metadataVOList){
			if(metadataVO.getLocalCode().equals(code)) {
				foundMetadataVO = metadataVO;
				break;
			}
		}

		metadataVOList.remove(foundMetadataVO);
	}

	public void deleteSummaryMetadata(FolderUnicityVO unicityVO) {
		this.presenter.deleteMetadataInUnicityConfig(unicityVO);
		this.folderUniqueKeyDataProvider.removeFolderUnicityVO(unicityVO);
		this.folderUniqueKeyDataProvider.fireDataRefreshEvent();
		refreshMetadataLookup();
	}

	public void deleteRow(final FolderUnicityVO unicityVO) {

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
							deleteSummaryMetadata(unicityVO);
							if (!isReindextionFlag) {
								updateUI();
							}
						}
					}
				});
	}

	@Override
	protected BaseBreadcrumbTrail buildBreadcrumbTrail() {
		return new TitleBreadcrumbTrail(this, getTitle()) {
			@Override
			public List<? extends IntermediateBreadCrumbTailItem> getIntermediateItems() {
				IntermediateBreadCrumbTailItem intermediateBreadCrumbTailItem1 = new IntermediateBreadCrumbTailItem() {
					@Override
					public String getTitle() {
						return $("ViewGroup.AdminViewGroup");
					}

					@Override
					public void activate(Navigation navigate) {
						navigate.to(CoreViews.class).adminModule();
					}

					@Override
					public boolean isEnabled() {
						return true;
					}
				};

				IntermediateBreadCrumbTailItem intermediateBreadCrumbTailItem2 = new IntermediateBreadCrumbTailItem() {
					@Override
					public boolean isEnabled() {
						return true;
					}

					@Override
					public String getTitle() {
						return $("ListSchemaTypeView.viewTitle");
					}

					@Override
					public void activate(Navigation navigate) {
						navigate.to(CoreViews.class).listSchemaTypes();
					}
				};

				IntermediateBreadCrumbTailItem intermediateBreadCrumbTailItem3 = new IntermediateBreadCrumbTailItem() {
					@Override
					public boolean isEnabled() {
						return true;
					}

					@Override
					public String getTitle() {
						return $("ListSchemaView.viewTitle");
					}

					@Override
					public void activate(Navigation navigate) {
						String schemaTypeCode = presenter.getSchemaCode().substring(0, presenter.getSchemaCode().indexOf("_"));

						Map<String, String> paramsMap = new HashMap<>();
						paramsMap.put("schemaTypeCode", schemaTypeCode);
						String params = ParamUtils.addParams(NavigatorConfigurationService.DISPLAY_SCHEMA, paramsMap);

						navigate.to(CoreViews.class).listSchema(params);
					}
				};

				List<IntermediateBreadCrumbTailItem> intermediateBreadCrumbTailItemsList = new ArrayList<>();
				intermediateBreadCrumbTailItemsList.addAll(super.getIntermediateItems());
				intermediateBreadCrumbTailItemsList.addAll(asList(intermediateBreadCrumbTailItem1, intermediateBreadCrumbTailItem2, intermediateBreadCrumbTailItem3));
				return intermediateBreadCrumbTailItemsList;
			}
		};
	}
}
