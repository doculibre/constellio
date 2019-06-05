package com.constellio.app.ui.framework.components;

import com.constellio.app.api.extensions.params.MetadataDisplayCustomValueExtentionParams;
import com.constellio.app.entities.schemasDisplay.enums.MetadataInputType;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.wrappers.structures.CommentFactory;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.services.factories.ConstellioFactories;
import com.constellio.app.ui.application.ConstellioUI;
import com.constellio.app.ui.entities.ContentVersionVO;
import com.constellio.app.ui.entities.MetadataVO;
import com.constellio.app.ui.entities.MetadataValueVO;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.entities.UserVO;
import com.constellio.app.ui.framework.buttons.BaseLink;
import com.constellio.app.ui.framework.components.converters.BaseStringToDateConverter;
import com.constellio.app.ui.framework.components.converters.BaseStringToDateTimeConverter;
import com.constellio.app.ui.framework.components.converters.JodaDateTimeToStringConverter;
import com.constellio.app.ui.framework.components.converters.JodaDateToStringConverter;
import com.constellio.app.ui.framework.components.display.EnumWithSmallCodeDisplay;
import com.constellio.app.ui.framework.components.display.ReferenceDisplay;
import com.constellio.app.ui.framework.components.fields.comment.RecordCommentsDisplayImpl;
import com.constellio.app.ui.framework.components.fields.comment.RecordCommentsEditorImpl;
import com.constellio.app.ui.pages.base.SessionContext;
import com.constellio.model.entities.EnumWithSmallCode;
import com.constellio.model.entities.Taxonomy;
import com.constellio.model.entities.schemas.AllowedReferences;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.entities.schemas.StructureFactory;
import com.vaadin.data.util.converter.StringToDoubleConverter;
import com.vaadin.data.util.converter.StringToIntegerConverter;
import com.vaadin.server.ExternalResource;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.Component;
import com.vaadin.ui.Label;
import com.vaadin.ui.Link;
import com.vaadin.ui.VerticalLayout;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;

import java.io.Serializable;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import static com.constellio.app.ui.i18n.i18n.$;

@SuppressWarnings("serial")
public class MetadataDisplayFactory implements Serializable {

	private BaseStringToDateConverter utilDateConverter = new BaseStringToDateConverter();

	private BaseStringToDateTimeConverter utilDateTimeConverter = new BaseStringToDateTimeConverter();

	private JodaDateToStringConverter jodaDateConverter = new JodaDateToStringConverter();

	private JodaDateTimeToStringConverter jodaDateTimeConverter = new JodaDateTimeToStringConverter();

	public Component build(RecordVO recordVO, MetadataValueVO metadataValue) {
		Component displayComponent;
		MetadataVO metadataVO = metadataValue.getMetadata();
		Object displayValue = metadataValue.getValue();
		String metadataCode = metadataVO.getCode();
		StructureFactory structureFactory = metadataVO.getStructureFactory();

		MetadataValueType metadataValueType = metadataVO.getType();

		AppLayerFactory appLayerFactory = ConstellioFactories.getInstance().getAppLayerFactory();

		Object customValue = appLayerFactory.getExtensions().forCollection(metadataVO.getCollection())
				.getMetadataDisplayCustomValueExtention(
						new MetadataDisplayCustomValueExtentionParams(displayValue, recordVO, metadataVO));

		if (customValue != null) {
			displayValue = customValue;
		}

		if (!metadataVO.isEnabled()) {
			displayComponent = null;
		} else if (metadataVO.isMultivalue() && structureFactory != null && structureFactory instanceof CommentFactory) {
			if (Boolean.TRUE.equals(recordVO.getMetadataValue(recordVO.getMetadata(Schemas.LOGICALLY_DELETED_STATUS.getLocalCode())).getValue())) {
				displayComponent = new RecordCommentsDisplayImpl(recordVO, metadataCode);
			} else {
				displayComponent = new RecordCommentsEditorImpl(recordVO, metadataCode);
			}

			displayComponent.setWidthUndefined();
		} else if (displayValue == null) {
			displayComponent = null;
		} else if (displayValue instanceof Collection<?>) {
			Collection<?> collectionDisplayValue = (Collection<?>) displayValue;
			if (collectionDisplayValue.isEmpty()) {
				displayComponent = null;
			} else if (MetadataValueType.STRING.equals(metadataValueType) && metadataVO.getMetadataInputType() != MetadataInputType.URL) {
				displayComponent = newStringCollectionValueDisplayComponent((Collection<String>) collectionDisplayValue);
			} else {
				List<Component> elementDisplayComponents = new ArrayList<Component>();
				boolean hasAVisibleComponent = false;
				for (Object elementDisplayValue : collectionDisplayValue) {
					Component elementDisplayComponent = buildSingleValue(recordVO,
							metadataValue.getMetadata(), elementDisplayValue);
					if (elementDisplayComponent != null) {
						elementDisplayComponent.setSizeFull();
						elementDisplayComponents.add(elementDisplayComponent);
						hasAVisibleComponent = hasAVisibleComponent || elementDisplayComponent.isVisible();
					}
				}
				if (!elementDisplayComponents.isEmpty()) {
					displayComponent = newCollectionValueDisplayComponent(elementDisplayComponents);
					displayComponent.setVisible(hasAVisibleComponent);
				} else {
					displayComponent = null;
				}
			}
		} else {
			displayComponent = buildSingleValue(recordVO, metadataVO, displayValue);
		}
		return displayComponent;
	}


	/**
	 * @param recordVO     May be null, be careful!
	 * @param metadata     The metadata for which we want a display component
	 * @param displayValue The value to display
	 * @return
	 */
	public Component buildSingleValue(RecordVO recordVO, MetadataVO metadata, Object displayValue) {
		Component displayComponent;
		Locale locale = ConstellioUI.getCurrentSessionContext().getCurrentLocale();

		String[] taxonomyCodes = metadata.getTaxonomyCodes();
		AllowedReferences allowedReferences = metadata.getAllowedReferences();

		MetadataInputType metadataInputType = metadata.getMetadataInputType();
		MetadataValueType metadataValueType = metadata.getType();

		if (displayValue == null) {
			displayComponent = null;
		} else if ((displayValue instanceof String) && StringUtils.isBlank(displayValue.toString())) {
			displayComponent = null;
		} else {
			switch (metadataValueType) {
				case BOOLEAN:
					String key = Boolean.TRUE.equals(displayValue) ? "yes" : "no";
					displayComponent = new Label($(key));
					break;
				case DATE:
					if (displayValue instanceof LocalDate) {
						String convertedJodaDate = jodaDateConverter
								.convertToPresentation((LocalDate) displayValue, String.class, locale);
						displayComponent = new Label(convertedJodaDate);
					} else if (displayValue instanceof Date) {
						String convertedDate = utilDateConverter.convertToPresentation((Date) displayValue, String.class, locale);
						displayComponent = new Label(convertedDate);
					} else {
						displayComponent = null;
					}
					break;
				case DATE_TIME:
					if (displayValue instanceof LocalDateTime) {
						String convertedJodaDate = jodaDateTimeConverter
								.convertToPresentation((LocalDateTime) displayValue, String.class, locale);
						displayComponent = new Label(convertedJodaDate);
					} else if (displayValue instanceof Date) {
						String convertedDate = utilDateTimeConverter.convertToPresentation((Date) displayValue, String.class, locale);
						displayComponent = new Label(convertedDate);
					} else {
						displayComponent = null;
					}
					break;
				case NUMBER:
					NumberFormat numberFormat = NumberFormat.getInstance();
					numberFormat.setGroupingUsed(false);

					String strDisplayValue = numberFormat.format(displayValue);
					if (strDisplayValue.endsWith(".0")) {
						strDisplayValue = StringUtils.substringBefore(strDisplayValue, ".");
					}
					displayComponent = new Label(strDisplayValue);
					((Label) displayComponent).setConverter(new StringToDoubleConverter());
					break;
				case INTEGER:
					NumberFormat intFormat = NumberFormat.getInstance();
					intFormat.setGroupingUsed(false);
					displayComponent = new Label(intFormat.format(displayValue));
					((Label) displayComponent).setConverter(new StringToIntegerConverter());
					break;
				case STRING:
					if (MetadataInputType.PASSWORD.equals(metadataInputType)) {
						displayComponent = null;
					} else if (MetadataInputType.URL.equals(metadataInputType)) {
						String url = displayValue.toString();
						if (!url.startsWith("http://") && !url.startsWith("https://")) {
							url = "http://" + url;
						}
						Link link = new BaseLink(url, new ExternalResource(url));
						link.setTargetName("_blank");
						displayComponent = link;
					} else {
						String stringValue = StringUtils.replace(displayValue.toString(), "\n", "<br/>");
						displayComponent = new Label(stringValue, ContentMode.HTML);
					}
					break;
				case TEXT:
					switch (metadataInputType) {
						case RICHTEXT:
							displayComponent = new Label(displayValue.toString(), ContentMode.HTML);
							break;
						default:
							String stringValue = StringUtils.replace(displayValue.toString(), "\n", "<br/>");
							displayComponent = new Label(stringValue, ContentMode.HTML);
							break;
					}
					break;
				case STRUCTURE:
					displayComponent = new Label(displayValue.toString());
					break;
				case CONTENT:
					ContentVersionVO contentVersionVO = (ContentVersionVO) displayValue;
					displayComponent = new ContentVersionDisplay(recordVO, contentVersionVO, new BaseUpdatableContentVersionPresenter());
					break;
				case REFERENCE:
					switch (metadataInputType) {
						case LOOKUP:
							AppLayerFactory appLayerFactory = ConstellioFactories.getInstance().getAppLayerFactory();
							String currentCollection = metadata.getCollection();
							displayComponent = appLayerFactory.getExtensions().forCollection(currentCollection).getDisplayForReference(allowedReferences, displayValue.toString());
							break;
						default:
							if (allowedReferences != null) {
								displayComponent = new ReferenceDisplay(displayValue.toString());
							} else if (taxonomyCodes.length > 0) {
								displayComponent = new Label(taxonomyCodes.toString());
							} else {
								displayComponent = new Label(displayValue.toString());
							}
							break;
					}
					if (displayComponent != null && taxonomyCodes != null && taxonomyCodes.length > 0) {
						displayComponent.setVisible(hasCurrentUserRightsOnTaxonomy(taxonomyCodes[0]));
					}
					break;
				case ENUM:
					if (displayValue instanceof EnumWithSmallCode) {
						displayComponent = new EnumWithSmallCodeDisplay<>((EnumWithSmallCode) displayValue);
					} else if (displayValue instanceof String) {
						displayComponent = new Label($(metadata.getEnumClass().getSimpleName() + "." + displayValue));
					} else {
						displayComponent = null;
					}
					break;
				default:
					displayComponent = null;
					break;
			}
		}
		return displayComponent;
	}

	protected Component newStringCollectionValueDisplayComponent(Collection<String> stringCollectionValue) {
		StringBuilder sb = new StringBuilder();
		for (String stringValue : stringCollectionValue) {
			if (sb.length() > 0) {
				sb.append(", ");
			}
			sb.append(stringValue);
		}
		return new Label(sb.toString());
	}

	//	protected Component newContentVersionDisplayComponent(RecordVO recordVO, ContentVersionVO contentVersionVO) {
	//		return new DownloadContentVersionLink(contentVersionVO);
	//	}

	public Component newCollectionValueDisplayComponent(List<Component> elementDisplayComponents) {
		VerticalLayout verticalLayout = new VerticalLayout();
		for (Component elementDisplayComponent : elementDisplayComponents) {
			verticalLayout.addComponent(elementDisplayComponent);
		}
		return verticalLayout;
	}

	private boolean hasCurrentUserRightsOnTaxonomy(String taxonomyCode) {
		SessionContext currentSessionContext = ConstellioUI.getCurrentSessionContext();
		AppLayerFactory appLayerFactory = ConstellioFactories.getInstance().getAppLayerFactory();
		Taxonomy taxonomy = appLayerFactory.getModelLayerFactory().getTaxonomiesManager().getTaxonomyFor(currentSessionContext.getCurrentCollection(), taxonomyCode);
		UserVO currentUser = currentSessionContext.getCurrentUser();
		String userid = currentUser.getId();

		if (taxonomy != null) {
			RMSchemasRecordsServices rmSchemasRecordsServices = new RMSchemasRecordsServices(currentSessionContext.getCurrentCollection(), appLayerFactory);
			List<String> taxonomyGroupIds = taxonomy.getGroupIds();
			List<String> taxonomyUserIds = taxonomy.getUserIds();
			List<String> userGroups = rmSchemasRecordsServices.getUser(currentUser.getId()).getUserGroups();
			for (String group : taxonomyGroupIds) {
				for (String userGroup : userGroups) {
					if (userGroup.equals(group)) {
						return true;
					}
				}
			}
			return (taxonomyGroupIds.isEmpty() && taxonomyUserIds.isEmpty()) || taxonomyUserIds.contains(userid);
		} else {
			return true;
		}
	}
}
