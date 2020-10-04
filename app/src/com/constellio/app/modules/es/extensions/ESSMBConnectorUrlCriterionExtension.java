package com.constellio.app.modules.es.extensions;

import com.constellio.app.api.extensions.SearchCriterionExtension;
import com.constellio.app.modules.es.constants.ESTaxonomies;
import com.constellio.app.modules.es.model.connectors.smb.ConnectorSmbFolder;
import com.constellio.app.modules.es.services.ESSchemasRecordsServices;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.services.factories.ConstellioFactories;
import com.constellio.app.ui.framework.components.BaseWindow;
import com.constellio.app.ui.framework.components.converters.TaxonomyRecordIdToContextCaptionConverter;
import com.constellio.app.ui.framework.components.fields.BaseComboBox;
import com.constellio.app.ui.framework.components.fields.lookup.LookupRecordField;
import com.constellio.app.ui.framework.data.RecordLookupTreeDataProvider;
import com.constellio.app.ui.framework.data.RecordTextInputDataProvider;
import com.constellio.app.ui.framework.data.trees.SmbRecordTreeNodesDataProvider;
import com.constellio.app.ui.pages.base.SessionContext;
import com.constellio.app.ui.pages.search.criteria.Criterion;
import com.constellio.model.entities.records.Record;
import com.constellio.model.services.records.RecordServices;
import com.vaadin.data.Property;
import com.vaadin.ui.AbstractSelect;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;

import java.util.Arrays;
import java.util.Locale;

import static com.constellio.app.services.factories.ConstellioFactories.getInstance;
import static com.constellio.app.ui.application.ConstellioUI.getCurrentSessionContext;
import static com.constellio.app.ui.i18n.i18n.$;

public class ESSMBConnectorUrlCriterionExtension extends SearchCriterionExtension {

	AppLayerFactory appLayerFactory;
	String collection;
	ESSchemasRecordsServices esSchemasRecordsServices;
	RecordServices recordServices;

	public ESSMBConnectorUrlCriterionExtension(AppLayerFactory appLayerFactory, String collection) {
		this.appLayerFactory = appLayerFactory;
		this.collection = collection;
		this.esSchemasRecordsServices = new ESSchemasRecordsServices(collection, appLayerFactory);
		this.recordServices = appLayerFactory.getModelLayerFactory().newRecordServices();
	}

	@Override
	public Component getComponentForCriterion(Criterion criterion) {
		String metadataCode = criterion.getMetadataCode();
		if (metadataCode.endsWith("_" + ConnectorSmbFolder.PARENT_CONNECTOR_URL) ||
			(metadataCode.startsWith(ConnectorSmbFolder.SCHEMA_TYPE + "_") &&
			 metadataCode.endsWith("_" + ConnectorSmbFolder.CONNECTOR_URL))) {

			return buildComponentForParentConnectorUrl(criterion);
		}
		return null;
	}

	private Component buildComponentForParentConnectorUrl(final Criterion criterion) {
		final SMBParentConnectorUrlLookupField value = new SMBParentConnectorUrlLookupField();
		value.setWidth("100%");
		value.setValue((String) criterion.getValue());
		value.addValueChangeListener(new Property.ValueChangeListener() {
			@Override
			public void valueChange(Property.ValueChangeEvent event) {
				String connectorUrl = null;
				if (value.getValue() != null) {
					connectorUrl = esSchemasRecordsServices.wrapConnectorSmbFolder(recordServices.getDocumentById((String) value.getValue())).getConnectorUrl();
				}
				criterion.setValue(connectorUrl);
			}
		});

		final ComboBox operator = buildIsEmptyIsNotEmptyComponent(criterion);
		operator.setNullSelectionAllowed(false);
		operator.addValueChangeListener(new Property.ValueChangeListener() {
			@Override
			public void valueChange(Property.ValueChangeEvent event) {
				Criterion.SearchOperator newOperator = (Criterion.SearchOperator) operator.getValue();
				if (newOperator != null) {
					criterion.setSearchOperator(newOperator);
					value.setVisible(
							!newOperator.equals(Criterion.SearchOperator.IS_NULL) && !newOperator.equals(Criterion.SearchOperator.IS_NOT_NULL));
				} else {
					value.setVisible(true);
				}
			}
		});

		HorizontalLayout component = new HorizontalLayout(operator, value);
		component.setComponentAlignment(value, Alignment.MIDDLE_RIGHT);
		component.setExpandRatio(value, 1);
		component.setWidth("100%");
		component.setSpacing(true);
		return component;
	}

	private class SMBParentConnectorUrlLookupField extends LookupRecordField {
		public SMBParentConnectorUrlLookupField() {
			super(new SMBParentConnectorUrlTextInputDataProvider(getInstance(),
							getCurrentSessionContext(), ConnectorSmbFolder.SCHEMA_TYPE, false),
					new LookupTreeDataProvider[]{getRecordLookupTreeDataProvider(appLayerFactory, recordServices)},
					getTaxonomyRecordIdToContextCaptionConverter(esSchemasRecordsServices, recordServices)
			);
		}
	}

	private class SMBParentConnectorUrlTextInputDataProvider extends RecordTextInputDataProvider {

		public SMBParentConnectorUrlTextInputDataProvider(ConstellioFactories constellioFactories,
														  SessionContext sessionContext, String schemaTypeCode,
														  boolean writeAccess) {
			super(constellioFactories, sessionContext, ConnectorSmbFolder.SCHEMA_TYPE, writeAccess);
		}
	}

	static private RecordLookupTreeDataProvider getRecordLookupTreeDataProvider(AppLayerFactory appLayerFactory,
																				final RecordServices recordServices) {
		return new RecordLookupTreeDataProvider(ConnectorSmbFolder.SCHEMA_TYPE, false, new SmbRecordTreeNodesDataProvider(ESTaxonomies.SMB_FOLDERS, appLayerFactory)) {
			@Override
			public boolean isSelectable(String selection) {
				try {
					Record documentById = recordServices.getDocumentById(selection);
					return super.isSelectable(selection) && documentById.isOfSchemaType(ConnectorSmbFolder.SCHEMA_TYPE);
				} catch (Exception e) {
					return super.isSelectable(selection);
				}
			}
		};
	}

	static private TaxonomyRecordIdToContextCaptionConverter getTaxonomyRecordIdToContextCaptionConverter(
			final ESSchemasRecordsServices esSchemasRecordsServices, final RecordServices recordServices) {
		return new TaxonomyRecordIdToContextCaptionConverter() {
			@Override
			public String convertToPresentation(String value, Class<? extends String> targetType, Locale locale)
					throws ConversionException {
				try {
					return super.convertToPresentation(value, targetType, locale);
				} catch (Exception e) {
					String connectorUrl = value;
					if (connectorUrl != null) {
						if (connectorUrl.startsWith("\"")) {
							connectorUrl = connectorUrl.replaceFirst("\"", "");
						}
						if (connectorUrl.endsWith("\"")) {
							connectorUrl = connectorUrl.substring(0, connectorUrl.lastIndexOf("\""));
						}
						Record connector = recordServices.getRecordByMetadata(esSchemasRecordsServices.connectorSmbFolder.connectorUrl(), connectorUrl);
						String id = null;
						if (connector != null) {
							id = connector.getId();
						}
						return super.convertToPresentation(id, targetType, locale);
					} else {
						throw e;
					}
				}
			}
		};
	}

	private ComboBox buildIsEmptyIsNotEmptyComponent(final Criterion criterion) {
		final ComboBox operator = new BaseComboBox();
		addIsEmptyIsNotEmpty(criterion, operator);
		operator.setWidth("100px");
		operator.setItemCaptionMode(AbstractSelect.ItemCaptionMode.EXPLICIT);
		operator.setNullSelectionAllowed(true);
		operator.setValue(criterion.getSearchOperator());
		operator.addValueChangeListener(new Property.ValueChangeListener() {
			@Override
			public void valueChange(Property.ValueChangeEvent event) {
				criterion.setSearchOperator((Criterion.SearchOperator) operator.getValue());
			}
		});

		return operator;
	}

	private void addIsEmptyIsNotEmpty(final Criterion criterion, final ComboBox operator) {

		Object defaultValue = Criterion.SearchOperator.CONTAINS_TEXT;
		if (Arrays.asList(Criterion.SearchOperator.CONTAINS_TEXT, Criterion.SearchOperator.IS_NULL, Criterion.SearchOperator.IS_NOT_NULL).contains(criterion.getSearchOperator())) {
			defaultValue = criterion.getSearchOperator();
		}

		operator.addItem(Criterion.SearchOperator.CONTAINS_TEXT);
		operator.setItemCaption(Criterion.SearchOperator.CONTAINS_TEXT, "=");
		operator.addItem(Criterion.SearchOperator.IS_NULL);
		operator.setItemCaption(Criterion.SearchOperator.IS_NULL, $("AdvancedSearchView.isEmpty"));
		operator.addItem(Criterion.SearchOperator.IS_NOT_NULL);
		operator.setItemCaption(Criterion.SearchOperator.IS_NOT_NULL, $("AdvancedSearchView.isNotEmpty"));
		operator.setValue(defaultValue);
	}
}
