package com.constellio.app.modules.rm.ui.pages.retentionRule.retentionRuleDocumentType;

import com.constellio.app.services.factories.ConstellioFactories;
import com.constellio.app.ui.entities.MetadataVO;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.framework.components.RecordFieldFactory;
import com.constellio.app.ui.framework.components.RecordForm;
import com.constellio.model.frameworks.validation.ValidationException;
import com.vaadin.ui.Field;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

public abstract class RecordFormWithHiddableMetadatas extends RecordForm {
	public RecordFormWithHiddableMetadatas(RecordVO recordVO,
										   ConstellioFactories constellioFactories) {
		this(
				recordVO,
				constellioFactories,
				Collections.emptyList(),
				Collections.emptyList()
		);
	}

	public RecordFormWithHiddableMetadatas(RecordVO recordVO,
										   ConstellioFactories constellioFactories,
										   List<MetadataVO> metadatasToHide) {
		this(
				recordVO,
				constellioFactories,
				metadatasToHide,
				Collections.emptyList()
		);
	}

	public RecordFormWithHiddableMetadatas(RecordVO recordVO,
										   ConstellioFactories constellioFactories,
										   List<MetadataVO> metadatasToHide,
										   CurrentInstanceScopeFieldFactoryExtension fieldFactoryExtension) {
		this(
				recordVO,
				constellioFactories,
				metadatasToHide,
				fieldFactoryExtension != null ? Collections.singletonList(fieldFactoryExtension) : Collections.emptyList()
		);
	}

	public RecordFormWithHiddableMetadatas(RecordVO recordVO,
										   ConstellioFactories constellioFactories,
										   List<MetadataVO> metadatasToHide,
										   List<CurrentInstanceScopeFieldFactoryExtension> fieldFactoryExtensions) {
		super(
				recordVO,
				new RetentionrRuleDocumentTypeFormFieldFactory(metadatasToHide, fieldFactoryExtensions),
				constellioFactories
		);
	}

	@Override
	protected void saveButtonClick(RecordVO viewObject) throws ValidationException {
		fillRecordUsingRecordVO(viewObject.getRecord(), viewObject, true);

		saveButtonClicked(viewObject);
	}

	protected abstract void saveButtonClicked(RecordVO viewObject) throws ValidationException;

	public interface CurrentInstanceScopeFieldFactoryExtension {
		Field<?> build(RecordVO recordVO, MetadataVO metadataVO, Locale locale);
	}

	private static class RetentionrRuleDocumentTypeFormFieldFactory extends RecordFieldFactory {
		private final List<String> metadataCodesToHide;
		private final List<CurrentInstanceScopeFieldFactoryExtension> fieldFactoryExtensions;

		public RetentionrRuleDocumentTypeFormFieldFactory(List<MetadataVO> metadatasToHide) {
			this(metadatasToHide, Collections.emptyList());
		}

		public RetentionrRuleDocumentTypeFormFieldFactory(List<MetadataVO> metadatasToHide,
														  CurrentInstanceScopeFieldFactoryExtension fieldFactoryExtension) {
			this(metadatasToHide, fieldFactoryExtension != null ? Collections.singletonList(fieldFactoryExtension) : Collections.emptyList());
		}

		public RetentionrRuleDocumentTypeFormFieldFactory(List<MetadataVO> metadatasToHide,
														  List<CurrentInstanceScopeFieldFactoryExtension> fieldFactoryExtensions) {
			this.fieldFactoryExtensions = copyListOrEmptyListIfNull(fieldFactoryExtensions).stream()
					.filter(Objects::nonNull)
					.collect(Collectors.toList());

			if (metadatasToHide == null) {
				metadatasToHide = Collections.emptyList();
			}

			metadataCodesToHide = metadatasToHide.stream().map(MetadataVO::getCode).collect(Collectors.toList());
		}

		@Override
		public Field<?> build(RecordVO recordVO, MetadataVO metadataVO, Locale locale) {

			Field<?> field;

			Optional<? extends Field<?>> fieldCreatedWithExtension = fieldFactoryExtensions.stream()
					.map(fieldFactoryExtension -> fieldFactoryExtension.build(recordVO, metadataVO, locale))
					.filter(Objects::nonNull)
					.findFirst();

			if (fieldCreatedWithExtension.isPresent()) {
				field = fieldCreatedWithExtension.get();
			} else {
				field = super.build(recordVO, metadataVO, locale);
			}

			if (field != null) {
				postBuild(field, recordVO, metadataVO);

				if (metadataCodesToHide.contains(metadataVO.getCode())) {
					field.setVisible(false);
				}
			}

			return field;
		}

		private <T> List<T> copyListOrEmptyListIfNull(List<T> listToCopy) {
			List<T> copy;

			if (listToCopy != null) {
				copy = new ArrayList<>(listToCopy);
			} else {
				copy = Collections.emptyList();
			}

			return copy;
		}
	}
}
