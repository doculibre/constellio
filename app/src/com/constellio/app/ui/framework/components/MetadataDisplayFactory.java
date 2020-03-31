package com.constellio.app.ui.framework.components;

import com.constellio.app.api.extensions.params.MetadataDisplayCustomValueExtentionParams;
import com.constellio.app.entities.schemasDisplay.enums.MetadataInputType;
import com.constellio.app.entities.schemasDisplay.enums.MetadataSortingType;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.wrappers.structures.Comment;
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
import com.constellio.app.ui.framework.components.converters.CommentToStringConverter;
import com.constellio.app.ui.framework.components.converters.JodaDateTimeToStringConverter;
import com.constellio.app.ui.framework.components.converters.JodaDateToStringConverter;
import com.constellio.app.ui.framework.components.converters.RecordIdToCaptionConverter;
import com.constellio.app.ui.framework.components.display.EnumWithSmallCodeDisplay;
import com.constellio.app.ui.framework.components.display.ReferenceDisplay;
import com.constellio.app.ui.framework.components.fields.comment.RecordCommentsDisplayImpl;
import com.constellio.app.ui.framework.components.resource.ConstellioResourceHandler;
import com.constellio.app.ui.pages.base.SessionContext;
import com.constellio.data.utils.LangUtils;
import com.constellio.model.entities.EnumWithSmallCode;
import com.constellio.model.entities.Taxonomy;
import com.constellio.model.entities.schemas.AllowedReferences;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.entities.schemas.StructureFactory;
import com.vaadin.data.util.converter.StringToDoubleConverter;
import com.vaadin.data.util.converter.StringToIntegerConverter;
import com.vaadin.server.ExternalResource;
import com.vaadin.server.Resource;
import com.vaadin.server.ResourceReference;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.Component;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;

import java.io.Serializable;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
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

	@SuppressWarnings("unchecked")
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
			displayComponent = new RecordCommentsDisplayImpl(recordVO, metadataCode);
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
					displayComponent = newCollectionValueDisplayComponent(metadataVO, elementDisplayComponents);
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
						BaseLink link = new BaseLink(url, new ExternalResource(url));
						link.setTargetName("_blank");
						displayComponent = link;
					} else {
						String stringValue = StringUtils.replace(displayValue.toString(), "\n", "<br/>");
						if (metadata.codeMatches(Schemas.CAPTION.getCode())) {
							stringValue = StringUtils.replace(stringValue, "|", "/");
						}
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
					displayComponent = new ContentVersionDisplay(recordVO, contentVersionVO, metadata.getLocalCode(), new BaseUpdatableContentVersionPresenter());
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

	protected String newStringCollectionValueDisplayString(Collection<String> stringCollectionValue) {
		StringBuilder sb = new StringBuilder();
		for (String stringValue : stringCollectionValue) {
			if (sb.length() > 0) {
				sb.append(", ");
			}
			sb.append(stringValue);
		}
		return sb.toString();
	}

	protected Component newStringCollectionValueDisplayComponent(Collection<String> stringCollectionValue) {
		return new Label(newStringCollectionValueDisplayString(stringCollectionValue));
	}

	//	protected Component newContentVersionDisplayComponent(RecordVO recordVO, ContentVersionVO contentVersionVO) {
	//		return new DownloadContentVersionLink(contentVersionVO);
	//	}

	public Component newCollectionValueDisplayComponent(MetadataVO metadataVO,
														List<Component> elementDisplayComponents) {
		VerticalLayout verticalLayout = new VerticalLayout();
		if (metadataVO.getMetadataSortingType() == MetadataSortingType.ALPHANUMERICAL_ORDER) {
			Collections.sort(elementDisplayComponents, new Comparator<Component>() {
				@Override
				public int compare(Component o1, Component o2) {
					return LangUtils.compareStrings(o1.getCaption(), o2.getCaption());
				}
			});
		}

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
		String userId = currentUser.getId();

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
			return (taxonomyGroupIds.isEmpty() && taxonomyUserIds.isEmpty()) || taxonomyUserIds.contains(userId);
		} else {
			return true;
		}
	}

	@SuppressWarnings("unchecked")
	public String buildString(RecordVO recordVO, MetadataValueVO metadataValue) {
		String displayValueString;
		MetadataVO metadataVO = metadataValue.getMetadata();
		Object displayValue = metadataValue.getValue();
		StructureFactory structureFactory = metadataVO.getStructureFactory();
		MetadataValueType metadataValueType = metadataVO.getType();
		Locale locale = ConstellioUI.getCurrentSessionContext().getCurrentLocale();

		if (displayValue == null) {
			displayValueString = null;
		} else if (!metadataVO.isEnabled()) {
			displayValueString = null;
		} else if (metadataVO.isMultivalue() && structureFactory != null && structureFactory instanceof CommentFactory) {
			StringBuilder sb = new StringBuilder();
			List<Comment> comments = (List<Comment>) displayValue;
			CommentToStringConverter converter = new CommentToStringConverter();
			for (Comment comment : comments) {
				if (sb.length() > 0) {
					sb.append("<br/>");
				}
				sb.append(converter.convertToPresentation(comment, String.class, locale));
			}
			displayValueString = sb.toString();
		} else if (displayValue instanceof Collection<?>) {
			Collection<?> collectionDisplayValue = (Collection<?>) displayValue;
			if (collectionDisplayValue.isEmpty()) {
				displayValueString = null;
			} else if (MetadataValueType.STRING.equals(metadataValueType) && metadataVO.getMetadataInputType() != MetadataInputType.URL) {
				displayValueString = newStringCollectionValueDisplayString((Collection<String>) collectionDisplayValue);
			} else {
				List<String> elementDisplayStrings = new ArrayList<String>();
				for (Object elementDisplayValue : collectionDisplayValue) {
					String elementDisplayString = buildSingleValueString(recordVO, metadataValue.getMetadata(), elementDisplayValue);
					if (elementDisplayString != null) {
						elementDisplayStrings.add(elementDisplayString);
					}
				}
				if (!elementDisplayStrings.isEmpty()) {
					displayValueString = newStringCollectionValueDisplayString(elementDisplayStrings);
				} else {
					displayValueString = null;
				}
			}
		} else {
			displayValueString = buildSingleValueString(recordVO, metadataVO, displayValue);
		}
		return displayValueString;
	}

	private String buildSingleValueString(RecordVO recordVO, MetadataVO metadata, Object displayValue) {
		String displayValueString;
		Locale locale = ConstellioUI.getCurrentSessionContext().getCurrentLocale();

		String[] taxonomyCodes = metadata.getTaxonomyCodes();
		AllowedReferences allowedReferences = metadata.getAllowedReferences();

		MetadataInputType metadataInputType = metadata.getMetadataInputType();
		MetadataValueType metadataValueType = metadata.getType();

		if (displayValue == null) {
			displayValueString = null;
		} else if ((displayValue instanceof String) && StringUtils.isBlank(displayValue.toString())) {
			displayValueString = null;
		} else {
			switch (metadataValueType) {
				case BOOLEAN:
					String key = Boolean.TRUE.equals(displayValue) ? "yes" : "no";
					displayValueString = $(key);
					break;
				case DATE:
					if (displayValue instanceof LocalDate) {
						String convertedJodaDate = jodaDateConverter
								.convertToPresentation((LocalDate) displayValue, String.class, locale);
						displayValueString = convertedJodaDate;
					} else if (displayValue instanceof Date) {
						String convertedDate = utilDateConverter.convertToPresentation((Date) displayValue, String.class, locale);
						displayValueString = convertedDate;
					} else {
						displayValueString = null;
					}
					break;
				case DATE_TIME:
					if (displayValue instanceof LocalDateTime) {
						String convertedJodaDate = jodaDateTimeConverter
								.convertToPresentation((LocalDateTime) displayValue, String.class, locale);
						displayValueString = convertedJodaDate;
					} else if (displayValue instanceof Date) {
						String convertedDate = utilDateTimeConverter.convertToPresentation((Date) displayValue, String.class, locale);
						displayValueString = convertedDate;
					} else {
						displayValueString = null;
					}
					break;
				case NUMBER:
					NumberFormat numberFormat = NumberFormat.getInstance();
					numberFormat.setGroupingUsed(false);

					String strDisplayValue = numberFormat.format(displayValue);
					if (strDisplayValue.endsWith(".0")) {
						strDisplayValue = StringUtils.substringBefore(strDisplayValue, ".");
					}
					displayValueString = strDisplayValue;
					break;
				case INTEGER:
					NumberFormat intFormat = NumberFormat.getInstance();
					intFormat.setGroupingUsed(false);
					displayValueString = intFormat.format(displayValue);
					break;
				case STRING:
					if (metadata.codeMatches(Schemas.CAPTION.getCode())) {
						displayValueString = StringUtils.replace(displayValue.toString(), "|", "/");
					} else if (MetadataInputType.PASSWORD.equals(metadataInputType)) {
						displayValueString = null;
					} else if (MetadataInputType.URL.equals(metadataInputType)) {
						String url = displayValue.toString();
						if (!url.startsWith("http://") && !url.startsWith("https://")) {
							url = "http://" + url;
						}
						displayValueString = "<a href=\"" + url + "\" target=\"_blank\">" + url + "</a>";
					} else {
						String stringValue = StringUtils.replace(displayValue.toString(), "\n", "<br/>");
						;
						if (metadata.codeMatches(Schemas.CAPTION.getCode())) {
							stringValue = StringUtils.replace(stringValue, "|", "/");
						}
						displayValueString = stringValue;
					}
					break;
				case TEXT:
					switch (metadataInputType) {
						case RICHTEXT:
							displayValueString = displayValue.toString();
							break;
						default:
							String stringValue = StringUtils.replace(displayValue.toString(), "\n", "<br/>");
							displayValueString = stringValue;
							break;
					}
					break;
				case STRUCTURE:
					displayValueString = displayValue.toString();
					break;
				case CONTENT:
					ContentVersionVO contentVersionVO = (ContentVersionVO) displayValue;
					String recordId = recordVO.getId();
					String metadataCode = metadata.getCode();
					String version = contentVersionVO.getVersion();
					String filename = contentVersionVO.getFileName();
					Resource contentResource = ConstellioResourceHandler.createResource(recordId, metadataCode, version, filename);
					ResourceReference contentResourceReference = ResourceReference.create(contentResource, ConstellioUI.getCurrent(), "ImageViewer.file");
					String contentURL = contentResourceReference.getURL();
					displayValueString = "<a href=\"" + contentURL + "\" target=\"_blank\">" + contentVersionVO.toString() + "</a>";
					break;
				case REFERENCE:
					RecordIdToCaptionConverter converter = new RecordIdToCaptionConverter();
					switch (metadataInputType) {
						case LOOKUP:
							String referenceRecordId = displayValue.toString();
							//							AppLayerFactory appLayerFactory = ConstellioFactories.getInstance().getAppLayerFactory();
							//							String collection = metadata.getCollection();
							//							Component displayComponent = appLayerFactory.getExtensions().forCollection(collection).getDisplayForReference(allowedReferences, referenceRecordId);
							//							if (displayComponent != null && displayComponent.isVisible()) {
							//								if (allowedReferences != null && allowedReferences.getAllowedSchemaType() != null) {
							//									String allowedSchemaType = allowedReferences.getAllowedSchemaType();
							//									NavigationParams navigationParams = new NavigationParams(ConstellioUI.getCurrent().navigate(), referenceRecordId, allowedSchemaType, null, null);
							//									String viewHrefTag = null;
							//									AppLayerCollectionExtensions extensions = appLayerFactory.getExtensions().forCollection(collection);
							//									List<RecordNavigationExtension> recordNavigationExtensions = extensions.recordNavigationExtensions.getExtensions();
							//									for (final RecordNavigationExtension recordNavigationExtension : recordNavigationExtensions) {
							//										viewHrefTag = recordNavigationExtension.getViewHrefTag(navigationParams);
							//										if (viewHrefTag != null) {
							//											break;
							//										}
							//									}
							//									if (viewHrefTag != null) {
							//										displayValueString = viewHrefTag;
							//									} else {
							//										displayValueString = conversion.convertToPresentation(referenceRecordId, String.class, locale);
							//									}
							//								} else {
							//									displayValueString = conversion.convertToPresentation(referenceRecordId, String.class, locale);
							//								}
							//							} else {
							//								displayValueString = null;
							//							}
							displayValueString = converter.convertToPresentation(referenceRecordId, String.class, locale);
							break;
						default:
							if (allowedReferences != null) {
								displayValueString = converter.convertToPresentation(displayValue.toString(), String.class, locale);
							} else if (taxonomyCodes.length > 0) {
								displayValueString = taxonomyCodes.toString();
							} else {
								displayValueString = displayValue.toString();
							}
							break;
					}
					if (displayValueString != null && taxonomyCodes != null && taxonomyCodes.length > 0 && !hasCurrentUserRightsOnTaxonomy(taxonomyCodes[0])) {
						displayValueString = null;
					}
					break;
				case ENUM:
					if (displayValue instanceof EnumWithSmallCode) {
						EnumWithSmallCode enumWithSmallCode = (EnumWithSmallCode) displayValue;
						Class<?> enumWithSmallCodeClass = (Class<?>) enumWithSmallCode.getClass();
						String enumCode = enumWithSmallCode.getCode();
						displayValueString = $(enumWithSmallCodeClass.getSimpleName() + "." + enumCode);
					} else if (displayValue instanceof String) {
						displayValueString = $(metadata.getEnumClass().getSimpleName() + "." + displayValue);
					} else {
						displayValueString = null;
					}
					break;
				default:
					displayValueString = null;
					break;
			}
		}

		return displayValueString;
	}

}
