package com.constellio.app.ui.framework.components;

import com.constellio.app.entities.schemasDisplay.enums.MetadataDisplayType;
import com.constellio.app.entities.schemasDisplay.enums.MetadataInputType;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.wrappers.structures.CommentFactory;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.services.factories.ConstellioFactories;
import com.constellio.app.ui.application.ConstellioUI;
import com.constellio.app.ui.entities.MetadataVO;
import com.constellio.app.ui.entities.UserVO;
import com.constellio.app.ui.framework.components.fields.BasePasswordField;
import com.constellio.app.ui.framework.components.fields.BaseRichTextArea;
import com.constellio.app.ui.framework.components.fields.BaseTextArea;
import com.constellio.app.ui.framework.components.fields.BaseTextField;
import com.constellio.app.ui.framework.components.fields.BooleanOptionGroup;
import com.constellio.app.ui.framework.components.fields.date.JodaDateField;
import com.constellio.app.ui.framework.components.fields.date.JodaDateTimeField;
import com.constellio.app.ui.framework.components.fields.enumWithSmallCode.EnumWithSmallCodeComboBox;
import com.constellio.app.ui.framework.components.fields.enumWithSmallCode.EnumWithSmallCodeOptionGroup;
import com.constellio.app.ui.framework.components.fields.exception.ValidationException.ToManyCharacterToLongException;
import com.constellio.app.ui.framework.components.fields.list.ListAddRemoveCommentField;
import com.constellio.app.ui.framework.components.fields.list.ListAddRemoveDoubleField;
import com.constellio.app.ui.framework.components.fields.list.ListAddRemoveEnumWithSmallCodeComboBox;
import com.constellio.app.ui.framework.components.fields.list.ListAddRemoveIntegerField;
import com.constellio.app.ui.framework.components.fields.list.ListAddRemoveJodaDateField;
import com.constellio.app.ui.framework.components.fields.list.ListAddRemoveJodaDateTimeField;
import com.constellio.app.ui.framework.components.fields.list.ListAddRemoveRecordComboBox;
import com.constellio.app.ui.framework.components.fields.list.ListAddRemoveRecordLookupField;
import com.constellio.app.ui.framework.components.fields.list.ListAddRemoveRichTextArea;
import com.constellio.app.ui.framework.components.fields.list.ListAddRemoveTaxonomyComboBox;
import com.constellio.app.ui.framework.components.fields.list.ListAddRemoveTextArea;
import com.constellio.app.ui.framework.components.fields.list.ListAddRemoveTextField;
import com.constellio.app.ui.framework.components.fields.lookup.LookupRecordField;
import com.constellio.app.ui.framework.components.fields.number.BaseDoubleField;
import com.constellio.app.ui.framework.components.fields.number.BaseIntegerField;
import com.constellio.app.ui.framework.components.fields.record.RecordComboBox;
import com.constellio.app.ui.framework.components.fields.record.RecordOptionGroup;
import com.constellio.app.ui.framework.components.fields.taxonomy.TaxonomyComboBox;
import com.constellio.app.ui.framework.components.fields.taxonomy.TaxonomyOptionGroup;
import com.constellio.app.ui.framework.components.fields.upload.ContentVersionUploadField;
import com.constellio.app.ui.pages.base.SessionContext;
import com.constellio.model.entities.Taxonomy;
import com.constellio.model.entities.schemas.AllowedReferences;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.model.entities.schemas.StructureFactory;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.schemas.MetadataSchemasManager;
import com.vaadin.data.Validator;
import com.vaadin.ui.AbstractTextField;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.Field;
import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import static com.constellio.app.ui.i18n.i18n.$;

@SuppressWarnings("serial")
public class MetadataFieldFactory implements Serializable {

	private boolean isViewOnly;

	public MetadataFieldFactory() {
		this(false);
	}

	public MetadataFieldFactory(boolean isViewOnly) {
		this.isViewOnly = isViewOnly;
	}

	public final Field<?> build(MetadataVO metadata, String recordId) {
		return build(metadata, recordId, null);
	}

	public Field<?> build(MetadataVO metadata, String recordId, Locale locale) {
		Field<?> field;

		boolean multivalue = metadata.isMultivalue();
		if (multivalue) {
			field = newMultipleValueField(metadata, recordId);
		} else {
			field = newSingleValueField(metadata, recordId);
		}
		// FIXME Temporary workaround for inconsistencies
		if (metadata.getJavaType() == null) {
			field = null;
		}
		if (field != null) {
			postBuild(field, metadata);
		}
		return field;
	}

	protected void postBuild(final Field<?> field, MetadataVO metadata) {
		boolean readOnly = metadata.isReadOnly();
		boolean required = metadata.isRequired();

		String caption = metadata.getLabel(ConstellioUI.getCurrentSessionContext().getCurrentLocale(), true, true);

		addMaxLenghtValidator(field, metadata);

		field.setId(metadata.getCode());
		field.setCaption(caption);
		field.setRequired(required);
		field.setReadOnly(readOnly);
		if (field instanceof AbstractTextField) {
			((AbstractTextField) field).setNullRepresentation("");
		}
	}

	private void addMaxLenghtValidator(Field<?> field, MetadataVO metadata) {
		if (metadata.getMaxLength() != null && metadata.isMaxLenghtSupported()) {
			field.addValidator((Validator) value -> {
				if (value != null && value instanceof String) {
					if (((String) value).length() > metadata.getMaxLength()) {
						throw new ToManyCharacterToLongException($("invalidFieldValueToManyCharacter",
								metadata.getLabel(ConstellioUI.getCurrentSessionContext().getCurrentLocale(), false, false)));
					}
				}
			});
		}
	}

	@SuppressWarnings({"rawtypes", "unchecked"})
	protected Field<?> newSingleValueField(MetadataVO metadata, String recordId) {
		Field<?> field;

		String collection = metadata.getCollection();
		String schemaTypeCode = metadata.getSchemaTypeCode();
		Class<? extends Enum<?>> enumClass = metadata.getEnumClass();
		String[] taxonomyCodes = metadata.getTaxonomyCodes();
		String firstTaxonomyCode = taxonomyCodes != null && taxonomyCodes.length > 0 ? taxonomyCodes[0] : null;
		AllowedReferences allowedReferences = metadata.getAllowedReferences();
		boolean required = metadata.isRequired();

		MetadataInputType metadataInputType = metadata.getMetadataInputType();
		MetadataDisplayType metadataDisplayType = metadata.getMetadataDisplayType();
		MetadataValueType metadataValueType = metadata.getType();

		if (metadataInputType == MetadataInputType.HIDDEN) {
			field = null;
		} else {
			switch (metadataValueType) {
				case ENUM:
					switch (metadataInputType) {
						case DROPDOWN:
							if (enumClass != null) {
								field = new EnumWithSmallCodeComboBox(enumClass);
							} else if (firstTaxonomyCode != null) {
								field = new TaxonomyComboBox(firstTaxonomyCode, schemaTypeCode);
							} else {
								field = null;
							}
							break;
						case RADIO_BUTTONS:
							if (enumClass != null) {
								field = new EnumWithSmallCodeOptionGroup(enumClass, metadataDisplayType);
							} else if (firstTaxonomyCode != null) {
								field = new TaxonomyOptionGroup(firstTaxonomyCode, schemaTypeCode, metadataDisplayType);
							} else {
								field = null;
							}
							break;
						default:
							field = null;
							break;
					}
					break;
				case TEXT:
					if (metadataInputType != null) {
						switch (metadataInputType) {
							case RICHTEXT:
								String richInputMask = metadata.getInputMask();
								BaseRichTextArea richTextArea = new BaseRichTextArea();
								richTextArea.setInputMask(richInputMask);
								field = richTextArea;
								break;
							default:
								String inputMask = metadata.getInputMask();
								BaseTextArea textArea = new BaseTextArea();
								textArea.setInputMask(inputMask);
								field = textArea;
								break;
						}
					} else {
						field = new BaseTextArea();
						break;
					}
					break;
				case REFERENCE:
					switch (metadataInputType) {
						case LOOKUP:
							field = new LookupRecordField(schemaTypeCode);
							break;
						case DROPDOWN:
							if (enumClass != null) {
								field = new EnumWithSmallCodeComboBox(enumClass);
							} else if (firstTaxonomyCode != null) {
								field = new TaxonomyComboBox(firstTaxonomyCode, schemaTypeCode);
							} else if (allowedReferences != null) {
								String firstSchemaCode = getFirstSchemaCode(allowedReferences, collection);
								if (firstSchemaCode != null) {
									field = new RecordComboBox(firstSchemaCode);
								} else {
									field = null;
								}
							} else {
								field = null;
							}
							break;
						case RADIO_BUTTONS:
							if (enumClass != null) {
								field = new EnumWithSmallCodeOptionGroup(enumClass, metadataDisplayType);
							} else if (firstTaxonomyCode != null) {
								field = new TaxonomyOptionGroup(firstTaxonomyCode, schemaTypeCode, metadataDisplayType);
							} else if (allowedReferences != null) {
								String firstSchemaCode = getFirstSchemaCode(allowedReferences, collection);
								if (firstSchemaCode != null) {
									field = new RecordOptionGroup(firstSchemaCode, metadataDisplayType);
								} else {
									field = null;
								}
							} else {
								field = null;
							}
							break;
						default:
							field = null;
							break;
					}
					if (field != null && firstTaxonomyCode != null) {
						field.setVisible(hasCurrentUserRightsOnTaxonomy(schemaTypeCode));
					}
					break;
				case BOOLEAN:
					if (required) {
						field = new BooleanOptionGroup();
					} else {
						field = new CheckBox();
					}
					break;
				case DATE:
					field = new JodaDateField();
					break;
				case DATE_TIME:
					field = new JodaDateTimeField();
					break;
				case INTEGER:
					field = new BaseIntegerField();
					break;
				case NUMBER:
					field = new BaseDoubleField();
					break;
				case STRING:
					if (MetadataInputType.PASSWORD.equals(metadataInputType)) {
						field = new BasePasswordField();
					} else {
						String inputMask = metadata.getInputMask();
						BaseTextField textField = new BaseTextField();
						textField.setInputMask(inputMask);
						field = textField;
					}
					break;
				case CONTENT:
					// Two input types : CONTENTS OR CONTENT_CHECK_IN_CHECK_OUT
					switch (metadataInputType) {
						case CONTENT_CHECK_IN_CHECK_OUT:
							field = new ContentVersionUploadField(recordId, metadata.getLocalCode());
							break;
						default:
							field = new ContentVersionUploadField(recordId, metadata.getLocalCode());
							((ContentVersionUploadField) field).setMajorVersionFieldVisible(false);
							break;
					}
					break;
				case STRUCTURE:
					field = null;
					break;
				default:
					field = null;
					break;
			}
		}

		return field;
	}

	@SuppressWarnings({"rawtypes", "unchecked"})
	protected Field<?> newMultipleValueField(MetadataVO metadata, String recordId) {
		Field<?> field;

		String collection = metadata.getCollection();
		String schemaTypeCode = metadata.getSchemaTypeCode();
		Class<? extends Enum<?>> enumClass = metadata.getEnumClass();
		String[] taxonomyCodes = metadata.getTaxonomyCodes();
		String firstTaxonomyCode = (taxonomyCodes != null && taxonomyCodes.length > 0) ? taxonomyCodes[0] : null;
		AllowedReferences allowedReferences = metadata.getAllowedReferences();
		StructureFactory structureFactory = metadata.getStructureFactory();

		MetadataInputType metadataInputType = metadata.getMetadataInputType();
		MetadataDisplayType metadataDisplayType = metadata.getMetadataDisplayType();
		MetadataValueType metadataValueType = metadata.getType();

		if (metadataInputType == MetadataInputType.HIDDEN) {
			field = null;
		} else {
			switch (metadataValueType) {
				case ENUM:
					switch (metadataInputType) {
						case DROPDOWN:
							if (enumClass != null) {
								field = new ListAddRemoveEnumWithSmallCodeComboBox(enumClass);
							} else if (firstTaxonomyCode != null) {
								field = new ListAddRemoveTaxonomyComboBox(firstTaxonomyCode, schemaTypeCode);
							} else {
								field = null;
							}
							break;
						case CHECKBOXES:
							if (enumClass != null) {
								field = new EnumWithSmallCodeOptionGroup(enumClass, metadataDisplayType);
								((EnumWithSmallCodeOptionGroup) field).setMultiSelect(true);
							} else if (firstTaxonomyCode != null) {
								field = new TaxonomyOptionGroup(firstTaxonomyCode, schemaTypeCode, metadataDisplayType);
								((TaxonomyOptionGroup) field).setMultiSelect(true);
							} else {
								field = null;
							}
							break;
						default:
							field = null;
							break;
					}
					break;
				case TEXT:
					if (metadataInputType != null) {
						switch (metadataInputType) {
							case RICHTEXT:
								field = new ListAddRemoveRichTextArea();
								break;
							default:
								field = new ListAddRemoveTextArea();
								break;
						}
					} else {
						field = new ListAddRemoveTextArea();
					}
					break;
				case REFERENCE:
					switch (metadataInputType) {
						case LOOKUP:
							field = new ListAddRemoveRecordLookupField(schemaTypeCode);
							break;
						case DROPDOWN:
							if (enumClass != null) {
								field = new ListAddRemoveEnumWithSmallCodeComboBox(enumClass);
							} else if (firstTaxonomyCode != null) {
								field = new ListAddRemoveTaxonomyComboBox(firstTaxonomyCode, schemaTypeCode);
							} else if (allowedReferences != null) {
								String firstSchemaCode = getFirstSchemaCode(allowedReferences, collection);
								if (firstSchemaCode != null) {
									field = new ListAddRemoveRecordComboBox(firstSchemaCode);
								} else {
									field = null;
								}
							} else {
								field = null;
							}
							break;
						case CHECKBOXES:
							if (enumClass != null) {
								field = new EnumWithSmallCodeOptionGroup(enumClass, metadataDisplayType);
							} else if (firstTaxonomyCode != null) {
								field = new TaxonomyOptionGroup(firstTaxonomyCode, schemaTypeCode, metadataDisplayType);
								((TaxonomyOptionGroup) field).setMultiSelect(true);
							} else if (allowedReferences != null) {
								String firstSchemaCode = getFirstSchemaCode(allowedReferences, collection);
								if (firstSchemaCode != null) {
									field = new RecordOptionGroup(firstSchemaCode, metadataDisplayType);
									((RecordOptionGroup) field).setMultiSelect(true);
								} else {
									field = null;
								}
							} else {
								field = null;
							}
							break;
						default:
							field = null;
							break;
					}
					if (field != null && firstTaxonomyCode != null) {
						field.setVisible(hasCurrentUserRightsOnTaxonomy(schemaTypeCode));
					}
					break;
				case DATE:
					field = new ListAddRemoveJodaDateField();
					break;
				case DATE_TIME:
					field = new ListAddRemoveJodaDateTimeField();
					break;
				case INTEGER:
					field = new ListAddRemoveIntegerField();
					break;
				case NUMBER:
					field = new ListAddRemoveDoubleField();
					break;
				case STRING:
					ListAddRemoveTextField addRemoveField = new ListAddRemoveTextField();
					String inputMask = metadata.getInputMask();
					addRemoveField.setInputMask(inputMask);
					field = addRemoveField;
					break;
				case CONTENT:
					switch (metadataInputType) {
						case CONTENT_CHECK_IN_CHECK_OUT:
							field = new ContentVersionUploadField(true, false, isViewOnly, recordId, metadata.getLocalCode());
							break;
						default:
							field = new ContentVersionUploadField(true, true, isViewOnly, recordId, metadata.getLocalCode());
							((ContentVersionUploadField) field).setMajorVersionFieldVisible(false);
							break;
					}
					break;
				case STRUCTURE:
					if (structureFactory != null && structureFactory instanceof CommentFactory) {
						field = new ListAddRemoveCommentField();
					} else {
						field = null;
					}
					break;
				default:
					field = null;
					break;
			}
		}

		return field;
	}

	private String getFirstSchemaCode(AllowedReferences allowedReferences, String collection) {
		String firstSchemaCode;
		Set<String> allowedSchemas = allowedReferences.getAllowedSchemas();
		String allowedSchemaType = allowedReferences.getAllowedSchemaType();
		if (!allowedSchemas.isEmpty()) {
			firstSchemaCode = allowedSchemas.iterator().next();
		} else if (StringUtils.isNotBlank(allowedSchemaType)) {
			ConstellioFactories constellioFactories = ConstellioFactories.getInstance();
			ModelLayerFactory modelLayerFactory = constellioFactories.getModelLayerFactory();
			MetadataSchemasManager metadataSchemasManager = modelLayerFactory.getMetadataSchemasManager();
			MetadataSchemaTypes schemaTypes = metadataSchemasManager.getSchemaTypes(collection);
			firstSchemaCode = schemaTypes.getDefaultSchema(allowedSchemaType).getCode();
		} else {
			firstSchemaCode = null;
		}
		return firstSchemaCode;
	}

	private boolean hasCurrentUserRightsOnTaxonomy(String taxonomyCode) {
		SessionContext currentSessionContext = ConstellioUI.getCurrentSessionContext();
		AppLayerFactory appLayerFactory = ConstellioFactories.getInstance().getAppLayerFactory();
		Taxonomy taxonomy = appLayerFactory.getModelLayerFactory().getTaxonomiesManager()
				.getTaxonomyFor(currentSessionContext.getCurrentCollection(), taxonomyCode);
		UserVO currentUser = currentSessionContext.getCurrentUser();
		String userid = currentUser.getId();

		if (taxonomy != null) {
			RMSchemasRecordsServices rmSchemasRecordsServices = new RMSchemasRecordsServices(
					currentSessionContext.getCurrentCollection(), appLayerFactory);
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
