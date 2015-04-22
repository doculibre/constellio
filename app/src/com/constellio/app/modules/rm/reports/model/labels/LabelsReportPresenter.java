/*Constellio Enterprise Information Management

Copyright (c) 2015 "Constellio inc."

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as
published by the Free Software Foundation, either version 3 of the
License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program. If not, see <http://www.gnu.org/licenses/>.
*/
package com.constellio.app.modules.rm.reports.model.labels;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.wrappers.ContainerRecord;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.model.services.factories.ModelLayerFactory;

public class LabelsReportPresenter {
	public static final String FOLDER_LEFT = "left";
	public static final String FOLDER_RIGHT = "right";
	public static final String CONTAINER = "container";

	// Empirical values
	private static final int FIRST_ROW_AT_TOP = 0;
	private static final int FOURTH_ROW_FROM_TOP = 3;
	private static final int BOTTOM_ROW = 9;
	private static final int FIRST_COLUMN = 0;
	private static final int LABEL_FULL_WIDTH = 29;
	private static final int LABEL_HALF_WIDTH = LABEL_FULL_WIDTH / 2;
	private static final int LABEL_THIRD_WIDTH = LABEL_FULL_WIDTH / 3;
	private static final int MAX_SYMBOLS_PER_FULL_ROW = 120;
	private static final int MAX_SYMBOLS_PER_HALF_ROW_FOLDER = 55;
	private static final int MAX_SYMBOLS_PER_HALF_ROW_CONTAINER = 50;
	private static final int MAX_SYMBOLS_PER_THIRD_OF_ROW = 28;
	private static final int ROW_HEIGHT = 2;
	private static final int MIDDLE_COLUMN = LABEL_FULL_WIDTH / 2 + 2;
	private static final int ONE_THIRD_COLUMN = LABEL_FULL_WIDTH / 3;
	private static final int TWO_THIRD_COLUMN = LABEL_FULL_WIDTH * 2 / 3;
	// private static final int LAST_COLUMN = LABEL_FULL_WIDTH;

	public static final LabelsReportFont FONT = new LabelsReportFont().setSize(8.0f).setBold(true).setItalic(true);

	private String collection;
	private ModelLayerFactory modelLayerFactory;
	private RMSchemasRecordsServices rmSchemasRecordsServices;

	private int startPosition;

	public LabelsReportPresenter(String collection, ModelLayerFactory modelLayerFactory) {
		this.collection = collection;
		this.modelLayerFactory = modelLayerFactory;
	}

	public LabelsReportModel build(List<String> folderOrContainerIds, int startPosition, int copies,
			final String modelCode) {
		rmSchemasRecordsServices = new RMSchemasRecordsServices(collection, modelLayerFactory);
		this.startPosition = startPosition;

		LabelsReportModel labelsReportModel = new LabelsReportModel();
		labelsReportModel.setLayout(LabelsReportLayout.AVERY_5159);
		labelsReportModel.setPrintBorders(true);

		List<LabelsReportLabel> labels = new ArrayList<>();

		addBlankLabelsBelowStartPosition(labels);

		switch (modelCode) {
		case FOLDER_LEFT:
			for (String folderId : folderOrContainerIds) {
				for (int i = 0; i < copies; i++) {
					try {
						labels.add(getLeftAlignedLabelFor(folderId));
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
			break;
		case FOLDER_RIGHT:
			for (String folderId : folderOrContainerIds) {
				for (int i = 0; i < copies; i++) {
					try {
						labels.add(getRightAlignedLabelFor(folderId));
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
			break;
		case CONTAINER:
			for (String folderId : folderOrContainerIds) {
				for (int i = 0; i < copies; i++) {
					try {
						labels.add(getContainerLabelFor(folderId));
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}

			break;
		}

		if (labels.isEmpty()) {
			labels.add(new LabelsReportLabel(new ArrayList<LabelsReportField>()));
		}

		labelsReportModel.setLabelsReportLabels(labels);

		return labelsReportModel;
	}

	private void addBlankLabelsBelowStartPosition(List<LabelsReportLabel> labels) {
		for (int i = 1; i < startPosition; i++) {
			List<LabelsReportField> fields = new ArrayList<>();
			LabelsReportLabel emptyLabel = new LabelsReportLabel(fields);
			labels.add(emptyLabel);
		}
	}

	private LabelsReportLabel getLeftAlignedLabelFor(String folderId) {
		Folder folder = getFolder(folderId);

		List<LabelsReportField> fields = new ArrayList<>();

		if (folder != null) {
			addCategoryCode(folder, FIRST_COLUMN, FIRST_ROW_AT_TOP, ROW_HEIGHT, LABEL_HALF_WIDTH, fields, false);

			addFolderOrContainerId(folderId, MIDDLE_COLUMN, FIRST_ROW_AT_TOP, ROW_HEIGHT, LABEL_HALF_WIDTH, fields,
					true);

			addFolderTitle(folder, FIRST_COLUMN, FOURTH_ROW_FROM_TOP, ROW_HEIGHT, LABEL_FULL_WIDTH, fields);

			addFilingSpaceCode(folder, FIRST_COLUMN, BOTTOM_ROW, ROW_HEIGHT, LABEL_THIRD_WIDTH, fields);

			addCopyStatusCode(folder, MIDDLE_COLUMN - 2, BOTTOM_ROW, ROW_HEIGHT, fields);

			addOpenDate(folder, TWO_THIRD_COLUMN + 2, BOTTOM_ROW, ROW_HEIGHT, LABEL_THIRD_WIDTH, fields);
		}

		LabelsReportLabel label = new LabelsReportLabel(fields);

		return label;

	}

	private Folder getFolder(String folderId) {

		Folder folder = rmSchemasRecordsServices.getFolder(folderId);

		return folder;
	}

	private LabelsReportLabel getRightAlignedLabelFor(String folderId) {

		Folder folder = getFolder(folderId);

		List<LabelsReportField> fields = new ArrayList<>();

		if (folder != null) {
			addFolderOrContainerId(folderId, FIRST_COLUMN, FIRST_ROW_AT_TOP, ROW_HEIGHT, LABEL_HALF_WIDTH, fields,
					false);

			addCategoryCode(folder, MIDDLE_COLUMN, FIRST_ROW_AT_TOP, ROW_HEIGHT, LABEL_HALF_WIDTH, fields, true);

			addFolderTitle(folder, FIRST_COLUMN, FOURTH_ROW_FROM_TOP, ROW_HEIGHT, LABEL_FULL_WIDTH, fields);

			addFilingSpaceCode(folder, FIRST_COLUMN, BOTTOM_ROW, ROW_HEIGHT, LABEL_THIRD_WIDTH, fields);

			addCopyStatusCode(folder, MIDDLE_COLUMN - 2, BOTTOM_ROW, ROW_HEIGHT, fields);

			addOpenDate(folder, TWO_THIRD_COLUMN + 2, BOTTOM_ROW, ROW_HEIGHT, LABEL_THIRD_WIDTH, fields);
		}

		LabelsReportLabel label = new LabelsReportLabel(fields);

		return label;
	}

	private LabelsReportLabel getContainerLabelFor(String containerId) {

		ContainerRecord container = rmSchemasRecordsServices.getContainerRecord(containerId);

		List<LabelsReportField> fields = new ArrayList<>();

		if (container != null) {
			addFolderOrContainerId(containerId, FIRST_COLUMN, FIRST_ROW_AT_TOP, ROW_HEIGHT, LABEL_HALF_WIDTH + 1,
					fields, false);

			addContainerTitle(container, MIDDLE_COLUMN, FIRST_ROW_AT_TOP, ROW_HEIGHT, LABEL_HALF_WIDTH, fields);

		}

		LabelsReportLabel label = new LabelsReportLabel(fields);

		return label;
	}

	private void addFolderOrContainerId(String folderId, int x, int y, int height, int width,
			List<LabelsReportField> fields, boolean padded) {
		String truncatedId = truncate(folderId, MAX_SYMBOLS_PER_HALF_ROW_FOLDER);
		if (padded) {
			truncatedId = pad(truncatedId, MAX_SYMBOLS_PER_HALF_ROW_FOLDER);
		}
		LabelsReportField idField = newField(truncatedId, x, y, height, width, FONT);
		fields.add(idField);
	}

	private LabelsReportField newField(String value, int x, int y, int height, LabelsReportFont font) {
		LabelsReportField field = newField(value, x, y, height, value.length(), font);
		return field;
	}

	private LabelsReportField newField(String value, int x, int y, int height, int width, LabelsReportFont font) {
		LabelsReportField field = new LabelsReportField();

		field.setValue(value);
		field.positionX = x;
		field.positionY = y;
		field.height = height;
		field.width = width;
		field.setFont(font);
		return field;
	}

	private void addFolderTitle(Folder folder, int x, int y, int height, int width, List<LabelsReportField> fields) {
		String folderTitle = "";
		try {
			folderTitle = folder.getTitle();
			folderTitle = truncate(folderTitle, MAX_SYMBOLS_PER_FULL_ROW);
		} catch (Exception e) {
		}
		LabelsReportField titleField = newField(folderTitle, x, y, height, width, FONT);
		fields.add(titleField);
	}

	private void addContainerTitle(ContainerRecord container, int x, int y, int height, int width,
			List<LabelsReportField> fields) {
		String containerTitle = "";
		try {
			containerTitle = container.getTitle();
			containerTitle = truncate(containerTitle, MAX_SYMBOLS_PER_HALF_ROW_CONTAINER);
			containerTitle = pad(containerTitle, MAX_SYMBOLS_PER_HALF_ROW_CONTAINER);
		} catch (Exception e) {
		}
		LabelsReportField titleField = newField(containerTitle, x, y, height, width, FONT);
		fields.add(titleField);
	}

	private void addCategoryCode(Folder folder, int x, int y, int height, int width, List<LabelsReportField> fields,
			boolean padded) {
		String categoryCode = "";
		try {
			categoryCode = folder.getCategoryCode();
			categoryCode = truncate(categoryCode, MAX_SYMBOLS_PER_HALF_ROW_FOLDER);
			if (padded) {
				categoryCode = pad(categoryCode, MAX_SYMBOLS_PER_HALF_ROW_FOLDER);
			}
		} catch (Exception e) {
		}
		LabelsReportField categoryCodeField = newField(categoryCode, x, y, height, width, FONT);
		fields.add(categoryCodeField);
	}

	private void addFilingSpaceCode(Folder folder, int x, int y, int height, int width, List<LabelsReportField> fields) {
		String filingSpaceCode = "";
		try {
			filingSpaceCode = folder.getFilingSpaceCode();
		} catch (Exception e) {
		}
		LabelsReportField filingSpaceCodeField = newField(filingSpaceCode, x, y, height, width, FONT);
		fields.add(filingSpaceCodeField);
	}

	private void addCopyStatusCode(Folder folder, int x, int y, int height, List<LabelsReportField> fields) {
		String copyStatusCode = "";

		try {
			copyStatusCode = folder.getCopyStatus().getCode();
		} catch (Exception e) {
		}
		LabelsReportField copyStatusCodeField = newField(copyStatusCode, x, y, height, FONT);
		fields.add(copyStatusCodeField);
	}

	private void addOpenDate(Folder folder, int x, int y, int height, int width, List<LabelsReportField> fields) {
		String openDateValue = "";

		try {
			openDateValue = folder.getOpenDate().toString();
			openDateValue = pad(openDateValue, MAX_SYMBOLS_PER_THIRD_OF_ROW);

		} catch (Exception e) {
		}

		LabelsReportField openDateField = newField(openDateValue, x, y, height, width, FONT);
		fields.add(openDateField);
	}

	private String truncate(String value, int maximumCharacters) {
		return StringUtils.substring(value, 0, maximumCharacters);
	}

	private String pad(String value, int size) {
		return StringUtils.leftPad(value, size, " ");
	}

}
