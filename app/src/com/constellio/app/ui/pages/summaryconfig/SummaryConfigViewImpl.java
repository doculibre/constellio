package com.constellio.app.ui.pages.summaryconfig;

import com.constellio.app.ui.application.CoreViews;
import com.constellio.app.ui.application.Navigation;
import com.constellio.app.ui.application.NavigatorConfigurationService;
import com.constellio.app.ui.entities.MetadataVO;
import com.constellio.app.ui.entities.SummaryConfigElementVO;
import com.constellio.app.ui.framework.components.BaseForm;
import com.constellio.app.ui.framework.components.breadcrumb.BaseBreadcrumbTrail;
import com.constellio.app.ui.framework.components.breadcrumb.IntermediateBreadCrumbTailItem;
import com.constellio.app.ui.framework.components.breadcrumb.TitleBreadcrumbTrail;
import com.constellio.app.ui.framework.components.fields.BaseTextField;
import com.constellio.app.ui.framework.components.fields.ListOptionGroup;
import com.constellio.app.ui.framework.components.fields.lookup.MetadataVOLookupField;
import com.constellio.app.ui.framework.components.table.BaseTable;
import com.constellio.app.ui.framework.containers.SummaryConfigContainer;
import com.constellio.app.ui.framework.data.SummaryConfigDataProvider;
import com.constellio.app.ui.pages.base.BaseViewImpl;
import com.constellio.app.ui.pages.viewGroups.AdminViewGroup;
import com.constellio.app.ui.params.ParamUtils;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.vaadin.data.Property;
import com.vaadin.data.fieldgroup.PropertyId;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.Table;
import com.vaadin.ui.TextField;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import org.vaadin.dialogs.ConfirmDialog;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static com.constellio.app.ui.i18n.i18n.$;
import static java.util.Arrays.asList;

public class SummaryConfigViewImpl extends BaseViewImpl implements SummaryConfigView, AdminViewGroup {

	private SummaryConfigPresenter presenter;

	@PropertyId("metadataVO")
	private MetadataVOLookupField metadataLookupField;

	@PropertyId("prefix")
	private TextField prefix;

	@PropertyId("displayCondition")
	private ListOptionGroup displayCondition;

	@PropertyId("referenceMetadataDisplay")
	private ComboBox referenceMetadataDisplayComboBox;

	private SummaryConfigDataProvider summaryConfigDataProvider;

	private Table table;
	private SummaryConfigContainer summaryConfigContainer;
	private SummaryConfigElementVO modifingSummaryConfigElementVO;

	private VerticalLayout mainVerticalLayout;

	List<MetadataVO> metadataVOList;

	public SummaryConfigViewImpl() {
		presenter = new SummaryConfigPresenter(this);
	}


	@Override
	protected void initBeforeCreateComponents(ViewChangeListener.ViewChangeEvent event) {
		Map<String, String> params = ParamUtils.getParamsMap(event.getParameters());
		presenter.setSchemaCode(params.get("schemaCode"));
		presenter.setParameters(params);
	}

	@Override
	public String getTitle() {
		return $("SummaryConfigViewImpl.title");
	}

	@Override
	public SummaryConfigPresenter getSummaryConfigPresenter() {
		return presenter;
	}

	@Override
	protected Component buildMainComponent(ViewChangeListener.ViewChangeEvent event) {

		metadataLookupField = new MetadataVOLookupField(new ArrayList<MetadataVO>());

		metadataLookupField.setCaption($("SummaryConfigViewImpl.metadataHeader"));
		//$("SummaryConfigViewImpl.metadata")

		List<SummaryConfigElementVO> summaryConfigElementVOList = presenter.summaryConfigVOList();

		metadataLookupField.setRequired(true);
		metadataLookupField.setImmediate(true);

		metadataLookupField.addValueChangeListener(new Property.ValueChangeListener() {
			@Override
			public void valueChange(Property.ValueChangeEvent event) {
				if (modifingSummaryConfigElementVO != null) {
					clearFields(false);
					removeMetadataFromPossibleSelection();
				}

				MetadataVO metadataVO = metadataLookupField.getValue();

				if (metadataVO != null && metadataVO.getType() == MetadataValueType.REFERENCE) {
					referenceMetadataDisplayComboBox.setRequired(true);
					referenceMetadataDisplayComboBox.setVisible(true);
				} else {
					referenceMetadataDisplayComboBox.setVisible(false);
					referenceMetadataDisplayComboBox.setRequired(false);
				}
			}
		});


		refreshMetadataLookup();

		table = new BaseTable(getClass().getName());

		summaryConfigDataProvider = new SummaryConfigDataProvider(summaryConfigElementVOList);
		summaryConfigContainer = new SummaryConfigContainer(summaryConfigDataProvider, this);

		table.setContainerDataSource(summaryConfigContainer);
		table.setColumnHeader(SummaryConfigContainer.UP, "");
		table.setColumnHeader(SummaryConfigContainer.DOWN, "");
		table.setColumnHeader(SummaryConfigContainer.METADATA_VO, $("SummaryConfigViewImpl.metadataHeader"));
		table.setColumnHeader(SummaryConfigContainer.PREFIX, $("SummaryConfigViewImpl.prefixHeader"));
		table.setColumnHeader(SummaryConfigContainer.DISPLAY_CONDITION, $("SummaryConfigViewImpl.displayConditionHeader"));
		table.setColumnHeader(SummaryConfigContainer.REFERENCE_METADATA_DISPLAY, $("SummaryConfigViewImpl.referenceMetadataDisplay"));
		table.setColumnHeader(SummaryConfigContainer.MODIFY_DELETE, "");


		prefix = new BaseTextField($("SummaryConfigViewImpl.prefix"));
		displayCondition = new ListOptionGroup($("SummaryConfigViewImpl.displayCondition"));
		displayCondition.setRequired(true);
		displayCondition.addItem(SummaryConfigParams.DisplayCondition.COMPLETED);
		displayCondition.addItem(SummaryConfigParams.DisplayCondition.ALWAYS);

		referenceMetadataDisplayComboBox = new ComboBox($("SummaryConfigViewImpl.referenceMetadataDisplay"));
		referenceMetadataDisplayComboBox.setImmediate(true);
		referenceMetadataDisplayComboBox.setTextInputAllowed(false);
		referenceMetadataDisplayComboBox.setVisible(false);
		referenceMetadataDisplayComboBox.addItem(SummaryConfigParams.ReferenceMetadataDisplay.CODE);
		referenceMetadataDisplayComboBox.addItem(SummaryConfigParams.ReferenceMetadataDisplay.TITLE);

		BaseForm<SummaryConfigParams> baseForm = new BaseForm<SummaryConfigParams>(new SummaryConfigParams(), this, metadataLookupField, prefix, displayCondition, referenceMetadataDisplayComboBox) {
			@Override
			protected void saveButtonClick(final SummaryConfigParams viewObject) {
				if (!presenter.isReindextionFlag() && presenter.isThereAModification(viewObject)) {
					ConfirmDialog.show(
							UI.getCurrent(),
							$("SummaryConfigViewImpl.save.title"),
							$("SummaryConfigViewImpl.save.message"),
							$("Ok"),
							$("cancel"),
							new ConfirmDialog.Listener() {
								@Override
								public void onClose(ConfirmDialog dialog) {
									if (dialog.isConfirmed()) {
										addConfiguration(viewObject);
									}
								}
							});

				} else {
					addConfiguration(viewObject);
				}
			}

			@Override
			protected void cancelButtonClick(SummaryConfigParams viewObject) {
				clearFields();
			}
		};


		this.table.setWidth("100%");

		mainVerticalLayout = new VerticalLayout();
		mainVerticalLayout.setSpacing(true);
		mainVerticalLayout.addComponent(baseForm);
		mainVerticalLayout.addComponent(table);

		return mainVerticalLayout;
	}

	private void addConfiguration(SummaryConfigParams viewObject) {
		SummaryConfigElementVO summaryConfigElementVO = summaryColumnParamsToSummaryVO(viewObject);
		if (modifingSummaryConfigElementVO != null) {
			presenter.modifyMetadataForSummaryConfig(viewObject);
			List<SummaryConfigElementVO> summaryConfigElementVOList = presenter.summaryConfigVOList();
			int index = presenter.findMetadataIndex(summaryConfigElementVOList, viewObject.getMetadataVO().getCode());
			summaryConfigDataProvider.removeSummaryConfigItemVO(index);
			summaryConfigDataProvider.addSummaryConfigItemVO(index, summaryConfigElementVO);
		} else {
			presenter.addMetadaForSummary(viewObject);
			summaryConfigDataProvider.addSummaryConfigItemVO(summaryConfigElementVO);
		}

		summaryConfigDataProvider.fireDataRefreshEvent();
		clearFields();
		removeMetadataFromPossibleSelection();
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


	private SummaryConfigElementVO summaryColumnParamsToSummaryVO(SummaryConfigParams summaryConfigParams) {
		SummaryConfigElementVO summaryConfigElementVO = new SummaryConfigElementVO();
		summaryConfigElementVO.setMetadataVO(summaryConfigParams.getMetadataVO());
		summaryConfigElementVO.setAlwaysShown(summaryConfigParams.getDisplayCondition() == SummaryConfigParams.DisplayCondition.ALWAYS);
		summaryConfigElementVO.setPrefix(summaryConfigParams.getPrefix());
		SummaryConfigParams.ReferenceMetadataDisplay referenceMetadataDisplay = summaryConfigParams.getReferenceMetadataDisplay();
		if (referenceMetadataDisplay != null) {
			summaryConfigElementVO.setReferenceMetadataDisplay((referenceMetadataDisplay.ordinal()));
		}

		return summaryConfigElementVO;
	}


	private void removeMetadataFromPossibleSelection() {
		List<SummaryConfigElementVO> summaryConfigElementVOList = presenter.summaryConfigVOList();

		for (SummaryConfigElementVO summaryConfigElementVO : summaryConfigElementVOList) {
			removeMetadataVOFromList(summaryConfigElementVO.getMetadataVO().getLocalCode());
		}
	}

	private void removeMetadataVOFromList(String code) {
		MetadataVO foundMetadataVO = null;
		for (MetadataVO metadataVO : metadataVOList) {
			if (metadataVO.getLocalCode().equals(code)) {
				foundMetadataVO = metadataVO;
				break;
			}
		}

		metadataVOList.remove(foundMetadataVO);
	}

	private void clearFields() {
		this.clearFields(true);
	}

	private void clearFields(boolean clearMetadataTo) {
		this.displayCondition.setValue(null);
		this.prefix.setValue("");
		if (clearMetadataTo) {
			this.metadataLookupField.setValue(null);
		}
		this.referenceMetadataDisplayComboBox.setValue(null);
		this.modifingSummaryConfigElementVO = null;
	}

	public void addMetadataToStringLookupField(SummaryConfigElementVO summaryConfigElementVO) {
		metadataLookupField.setValue(null);
		metadataVOList.add(summaryConfigElementVO.getMetadataVO());
		metadataLookupField.setValue(summaryConfigElementVO.getMetadataVO());
	}

	@Override
	public void alterSummaryMetadata(SummaryConfigElementVO summaryConfigView) {
		addMetadataToStringLookupField(summaryConfigView);
		this.prefix.setValue(summaryConfigView.getPrefix());
		if (summaryConfigView.isAlwaysShown()) {
			this.displayCondition.setValue(SummaryConfigParams.DisplayCondition.ALWAYS);
		} else {
			this.displayCondition.setValue(SummaryConfigParams.DisplayCondition.COMPLETED);
		}
		if (summaryConfigView.getReferenceMetadataDisplay() != null) {
			this.referenceMetadataDisplayComboBox.setValue(SummaryConfigParams.ReferenceMetadataDisplay
					.fromInteger(summaryConfigView.getReferenceMetadataDisplay()));
		}
		this.modifingSummaryConfigElementVO = summaryConfigView;
	}

	public void deleteSummaryMetadata(SummaryConfigElementVO summaryConfigElementVO) {
		this.presenter.deleteMetadataForSummaryConfig(summaryConfigElementVO);
		this.summaryConfigDataProvider.removeSummaryConfigItemVO(summaryConfigElementVO);
		refreshMetadataLookup();
		this.summaryConfigDataProvider.fireDataRefreshEvent();
	}

	public void deleteRow(final SummaryConfigElementVO columnVO) {

		String message = $("SummaryConfigViewImpl.deleteConfirmationMesssage");
		if (!presenter.isReindextionFlag()) {
			message = $("SummaryConfigViewImpl.save.message") + " " + message;
		}


		ConfirmDialog.show(
				UI.getCurrent(),
				$("SummaryConfigViewImpl.deleteConfirmation"),
				message,
				$("Ok"),
				$("cancel"),
				new ConfirmDialog.Listener() {
					@Override
					public void onClose(ConfirmDialog dialog) {
						if (dialog.isConfirmed()) {
							deleteSummaryMetadata(columnVO);
						}
					}
				});
	}


	@Override
	public boolean showReindexationWarningIfRequired(ConfirmDialog.Listener confirmDialogListener) {
		if (presenter.isReindextionFlag()) {
			ConfirmDialog.show(
					UI.getCurrent(),
					$("SummaryConfigViewImpl.save.title"),
					$("SummaryConfigViewImpl.save.message"),
					$("Ok"),
					$("cancel"),
					confirmDialogListener);
			return true;
		} else {
			return false;
		}

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
