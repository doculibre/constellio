package com.constellio.app.modules.rm.reports.model.labels;

import static com.constellio.app.ui.i18n.i18n.$;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
import org.krysalis.barcode4j.impl.AbstractBarcodeBean;
import org.krysalis.barcode4j.impl.code39.Code39Bean;
import org.krysalis.barcode4j.output.bitmap.BitmapCanvasProvider;
import org.krysalis.barcode4j.tools.UnitConv;

import com.constellio.app.modules.rm.model.labelTemplate.BarCodeLabelTemplateField;
import com.constellio.app.modules.rm.model.labelTemplate.LabelTemplate;
import com.constellio.app.modules.rm.model.labelTemplate.LabelTemplateField;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.model.entities.EnumWithSmallCode;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.records.RecordServices;
import com.itextpdf.text.Font.FontFamily;

public class LabelsReportPresenter {

	public static final LabelsReportFont FONT = new LabelsReportFont().setSize(8.0f).setBold(true).setItalic(true);

	private String collection;
	private ModelLayerFactory modelLayerFactory;
	private RMSchemasRecordsServices rmSchemasRecordsServices;

	private int startPosition;

	public LabelsReportPresenter(String collection, ModelLayerFactory modelLayerFactory) {
		this.collection = collection;
		this.modelLayerFactory = modelLayerFactory;
	}

	public LabelsReportModel build(List<String> ids, int startPosition, int copies,
			final LabelTemplate labelTemplate) {
		rmSchemasRecordsServices = new RMSchemasRecordsServices(collection, modelLayerFactory);
		this.startPosition = startPosition;

		LabelsReportModel labelsReportModel = new LabelsReportModel();
		labelsReportModel.setPrintBorders(labelTemplate.isPrintBorders());
		labelsReportModel.setLayout(labelTemplate.getLabelsReportLayout());
		labelsReportModel.setColumnsNumber(labelTemplate.getColumns());
		labelsReportModel.setRowsNumber(labelTemplate.getLines());
		//		labelsReportModel.setPrintBorders(true);

		List<LabelsReportLabel> labels = new ArrayList<>();

		addBlankLabelsBelowStartPosition(labels);

		List<Record> records = modelLayerFactory.newRecordServices().getRecordsById(collection, ids);
		for (Record record : records) {
			for (int i = 0; i < copies; i++) {
				List<LabelsReportField> fields = new ArrayList<>();
				for (LabelTemplateField fieldInfo : labelTemplate.getFields()) {
					String value = getPrintedValue(fieldInfo, record);
					LabelsReportField field = buildField(fieldInfo, value);
					fields.add(field);
				}
				LabelsReportLabel label = new LabelsReportLabel(fields);
				labels.add(label);
			}
		}

		if (labels.isEmpty()) {
			labels.add(new LabelsReportLabel(new ArrayList<LabelsReportField>()));
		}

		labelsReportModel.setLabelsReportLabels(labels);

		return labelsReportModel;
	}

	private File createBarCode(String value, LabelTemplateField fieldInfo) {
		File tempFile;
		try {
			tempFile = File.createTempFile(LabelsReportPresenter.class.getSimpleName(), ".png");
			tempFile.deleteOnExit();

			//Create the barcode bean
			AbstractBarcodeBean bean = new Code39Bean();

			final int dpi = 300;

			//Configure the barcode generator
			bean.setModuleWidth(UnitConv.in2mm(1.0f / dpi)); //makes the narrow bar 
			//width exactly one pixel

			double height = fieldInfo.getHeight();
			//			double width = fieldInfo.getWidth();

			bean.setHeight(height);
			bean.setFontSize(1.0d);
			//			bean.setWideFactor(3);
			bean.doQuietZone(false);

			//Open output file
			OutputStream out = new FileOutputStream(tempFile);
			try {
				//Set up the canvas provider for monochrome PNG output
				BitmapCanvasProvider canvas = new BitmapCanvasProvider(
						out, "image/x-png", dpi, BufferedImage.TYPE_BYTE_BINARY, false, 0);

				//Generate the barcode
				bean.generateBarcode(canvas, value);

				//Signal end of generation
				canvas.finish();
			} finally {
				out.close();
			}
		} catch (Throwable t) {
			tempFile = null;
		}
		return tempFile;
	}

	private LabelsReportField buildField(LabelTemplateField fieldInfo, String value) {
		LabelsReportField labelsReportField;
		if (fieldInfo instanceof BarCodeLabelTemplateField) {
			labelsReportField = new ImageLabelsReportField();
			File barCode = createBarCode(value, fieldInfo);
			if (barCode != null) {
				labelsReportField.setValue(barCode.getAbsolutePath());
				int width = fieldInfo.getWidth() != 0 ? fieldInfo.getWidth() : value.length();
				labelsReportField.width = width;

				int height = fieldInfo.getHeight();
				if (height != 0) {
					labelsReportField.height = height;
				}
			} else {
				labelsReportField = buildLabelField(fieldInfo, value);
			}
		} else {
			labelsReportField = buildLabelField(fieldInfo, value);
		}
		return labelsReportField;
	}

	private LabelsReportField buildLabelField(LabelTemplateField fieldInfo, String value) {
		FontFamily fontFamily = FontFamily.valueOf(fieldInfo.getFontName());
		LabelsReportFont font = new LabelsReportFont().setFontFamily(fontFamily)
				.setSize(fieldInfo.getFontSize()).setBold(fieldInfo.isBold()).setItalic(fieldInfo.isItalic());
		int width = fieldInfo.getWidth() != 0 ? fieldInfo.getWidth() : value.length();
		int horizontalAlignment;
		int verticalAlignment;
		horizontalAlignment = getHorizontalAligment(fieldInfo);

		verticalAlignment = getVerticalAligment(fieldInfo);
		LabelsReportField labelsReportField = newField(value, fieldInfo.getX(), fieldInfo.getY(), fieldInfo.getHeight(),
				width, horizontalAlignment, verticalAlignment, font);
		return labelsReportField;
	}

	private int getVerticalAligment(LabelTemplateField fieldInfo) {
		int verticalAlignment;
		if (fieldInfo.getVerticalAlignment() == null) {
			return 0;
		}
		switch (fieldInfo.getVerticalAlignment()) {
		case TOP:
			verticalAlignment = com.itextpdf.text.Element.ALIGN_TOP;
			break;
		case CENTER:
			verticalAlignment = com.itextpdf.text.Element.ALIGN_CENTER;
			break;
		case BOTTOM:
			verticalAlignment = com.itextpdf.text.Element.ALIGN_BOTTOM;
			break;
		default:
			verticalAlignment = com.itextpdf.text.Element.ALIGN_CENTER;
			break;
		}
		return verticalAlignment;
	}

	private int getHorizontalAligment(LabelTemplateField fieldInfo) {
		int horizontalAlignment;
		if (fieldInfo.getHorizontalAlignment() == null) {
			return 0;
		}
		switch (fieldInfo.getHorizontalAlignment()) {
		case LEFT:
			horizontalAlignment = com.itextpdf.text.Element.ALIGN_LEFT;
			break;
		case CENTER:
			horizontalAlignment = com.itextpdf.text.Element.ALIGN_CENTER;
			break;
		case RIGHT:
			horizontalAlignment = com.itextpdf.text.Element.ALIGN_RIGHT;
			break;
		default:
			horizontalAlignment = com.itextpdf.text.Element.ALIGN_LEFT;
			break;
		}
		return horizontalAlignment;
	}

	private String getPrintedValue(LabelTemplateField fieldInfo, Record record) {
		MetadataSchemaTypes types = rmSchemasRecordsServices.getTypes();
		RecordServices recordServices = modelLayerFactory.newRecordServices();
		String value;
		if (StringUtils.isNotBlank(fieldInfo.getReferenceMetadataCode3())) {
			Record referenceRecord3 = null;
			Metadata referenceMetadata3 = null;
			try {
				Metadata metadata = types.getMetadata(fieldInfo.getMetadataCode());
				String referenceId = record.get(metadata);
				Record referenceRecord = recordServices.getDocumentById(referenceId);
				Metadata referenceMetadata = types.getMetadata(fieldInfo.getReferenceMetadataCode());

				Metadata metadata2 = referenceMetadata;
				String referenceId2 = referenceRecord.get(metadata2);
				Record referenceRecord2 = recordServices.getDocumentById(referenceId2);
				Metadata referenceMetadata2 = types.getMetadata(fieldInfo.getReferenceMetadataCode2());

				Metadata metadata3 = referenceMetadata2;
				String referenceId3 = referenceRecord2.get(metadata3);
				referenceRecord3 = recordServices.getDocumentById(referenceId3);
				referenceMetadata3 = types.getMetadata(fieldInfo.getReferenceMetadataCode3());
				Object valueObject = referenceRecord3.get(referenceMetadata3);
				value = getStringValue(valueObject, fieldInfo);
			} catch (Exception e) {
				Object valueObject = "";
				value = getStringValue(valueObject, fieldInfo);
			}

		} else if (StringUtils.isNotBlank(fieldInfo.getReferenceMetadataCode2())) {
			Record referenceRecord2 = null;
			Metadata referenceMetadata2 = null;
			try {
				Metadata metadata = types.getMetadata(fieldInfo.getMetadataCode());
				String referenceId = record.get(metadata);
				Record referenceRecord = recordServices.getDocumentById(referenceId);
				Metadata referenceMetadata = types.getMetadata(fieldInfo.getReferenceMetadataCode());

				Metadata metadata2 = referenceMetadata;
				String referenceId2 = referenceRecord.get(metadata2);
				referenceRecord2 = recordServices.getDocumentById(referenceId2);
				referenceMetadata2 = types.getMetadata(fieldInfo.getReferenceMetadataCode2());
				Object valueObject = referenceRecord2.get(referenceMetadata2);
				value = getStringValue(valueObject, fieldInfo);
			} catch (Exception e) {
				Object valueObject = "";
				value = getStringValue(valueObject, fieldInfo);
			}

		} else if (StringUtils.isNotBlank(fieldInfo.getReferenceMetadataCode())) {
			try {
				Object valueObject = getReferenceMetadataValue(record, types, recordServices, fieldInfo.getMetadataCode(),
						fieldInfo.getReferenceMetadataCode());
				value = getStringValue(valueObject, fieldInfo);
			} catch (Exception e) {
				Object valueObject = "";
				value = getStringValue(valueObject, fieldInfo);
			}

		} else if (StringUtils.isNotBlank(fieldInfo.getMetadataCode())) {
			try {
				Metadata metadata = types.getMetadata(fieldInfo.getMetadataCode());
				Object valueObject = record.get(metadata);
				value = getStringValue(valueObject, fieldInfo);
			} catch (Exception e) {
				Object valueObject = "";
				value = getStringValue(valueObject, fieldInfo);
			}
		} else {
			value = getStringValue("", fieldInfo);
		}
		String value2 = "";
		if (StringUtils.isNotBlank(fieldInfo.getConcatenateReferenceMetadataCode())) {
			try {
				Object valueObject = getReferenceMetadataValue(record, types, recordServices,
						fieldInfo.getConcatenateMetadataCode(),
						fieldInfo.getConcatenateReferenceMetadataCode());
				value2 += " " + getStringValue(valueObject, fieldInfo);
			} catch (Exception e) {
				Object valueObject = "";
				value2 = getStringValue(valueObject, fieldInfo);
			}
		} else if (StringUtils.isNotBlank(fieldInfo.getConcatenateMetadataCode())) {
			try {
				Metadata metadata = types.getMetadata(fieldInfo.getConcatenateMetadataCode());
				Object valueObject = record.get(metadata);
				value2 += " " + getStringValue(valueObject, fieldInfo);
			} catch (Exception e) {
				Object valueObject = "";
				value2 = getStringValue(valueObject, fieldInfo);
			}
		}
		if (fieldInfo.isEmpty() && StringUtils.isBlank(value)) {
			Object valueObject = "";
			value = getStringValue(valueObject, fieldInfo);
		} else {
			if (StringUtils.isNotBlank(fieldInfo.getSeparator()) && StringUtils.isNotBlank(value2)) {
				value = value + fieldInfo.getSeparator() + value2;
			} else {
				value = value + value2;
			}
			if (StringUtils.isNotBlank(fieldInfo.getPrefix())) {
				value = fieldInfo.getPrefix() + value;
			}
			if (StringUtils.isNotBlank(fieldInfo.getSuffix())) {
				value = value + fieldInfo.getSuffix();
			}
			if (fieldInfo.isUppercase()) {
				value = value.toUpperCase();
			}
		}
		value = truncate(value, fieldInfo.getMaxLength());
		return value;
	}

	private Object getReferenceMetadataValue(Record record, MetadataSchemaTypes types, RecordServices recordServices,
			String metadataCode, String referenceMetadataCode) {
		Metadata metadata = types.getMetadata(metadataCode);
		String referenceId = record.get(metadata);
		Record referenceRecord = recordServices.getDocumentById(referenceId);
		Metadata referenceMetadata = types.getMetadata(referenceMetadataCode);
		return referenceRecord.get(referenceMetadata);
	}

	private String getStringValue(Object valueObject, LabelTemplateField fieldInfo) {
		String value;
		if (valueObject instanceof EnumWithSmallCode) {
			if (fieldInfo.isDisplayEnumTitle()) {
				value = $(valueObject.getClass().getSimpleName() + "." + ((EnumWithSmallCode) valueObject).getCode());
			} else {
				value = ((EnumWithSmallCode) valueObject).getCode();
			}
		} else if (valueObject instanceof LocalDate || valueObject instanceof LocalDateTime) {
			if (StringUtils.isNotBlank(fieldInfo.getPattern())) {
				if (valueObject instanceof LocalDate) {
					value = ((LocalDate) valueObject).toString(fieldInfo.getPattern());
				} else {
					value = ((LocalDateTime) valueObject).toString(fieldInfo.getPattern());
				}
			} else {
				value = valueObject.toString();
			}
		} else {
			value = valueObject.toString();
		}
		return value;
	}

	private void addBlankLabelsBelowStartPosition(List<LabelsReportLabel> labels) {
		for (int i = 1; i < startPosition; i++) {
			List<LabelsReportField> fields = new ArrayList<>();
			LabelsReportLabel emptyLabel = new LabelsReportLabel(fields);
			labels.add(emptyLabel);
		}
	}

	private LabelsReportField newField(String value, int x, int y, int height, int width, int horizontalAlignment,
			int verticalAlignment, LabelsReportFont font) {
		LabelsReportField field = new LabelsReportField();

		field.setValue(value);
		field.positionX = x;
		field.positionY = y;
		field.height = height;
		field.width = width;
		field.horizontalAlignment = horizontalAlignment;
		field.verticalAlignment = verticalAlignment;
		field.setFont(font);
		return field;
	}

	private String truncate(String value, int maximumCharacters) {
		return StringUtils.substring(value, 0, maximumCharacters);
	}

	private String pad(String value, int size) {
		return StringUtils.leftPad(value, size, " ");
	}

}
