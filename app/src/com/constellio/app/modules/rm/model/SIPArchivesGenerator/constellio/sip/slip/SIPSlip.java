package com.constellio.app.modules.rm.model.SIPArchivesGenerator.constellio.sip.slip;

import com.constellio.app.modules.rm.model.SIPArchivesGenerator.constellio.sip.model.SIPDocument;
import com.constellio.app.modules.rm.model.SIPArchivesGenerator.constellio.sip.model.SIPFolder;
import jxl.Cell;
import jxl.CellView;
import jxl.JXLException;
import jxl.Workbook;
import jxl.format.Border;
import jxl.format.BorderLineStyle;
import jxl.format.VerticalAlignment;
import jxl.write.*;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class SIPSlip {
	
	private Map<String, SIPDocument> sipDocuments = new LinkedHashMap<>();
	
	private Map<String, SIPFolder> sipFolders = new LinkedHashMap<>();
	
	public void add(SIPDocument sipDocument) {
		String id = sipDocument.getId();
		if (!sipDocuments.containsKey(id)) {
			sipDocuments.put(id, sipDocument);
			SIPFolder currentFolder = sipDocument.getFolder();
			while (currentFolder != null) {
				String folderId = currentFolder.getId();
				if (!sipFolders.containsKey(folderId)) {
					sipFolders.put(folderId, currentFolder);
				} else {
					break;
				}
				currentFolder = currentFolder.getParentFolder();
			}
		}
	}
	
	private void sheetAutoFitColumns(WritableSheet sheet) {
		List<Integer> removedColumns = new ArrayList<Integer>();

	    for (int columnIndex = 0; columnIndex < sheet.getColumns(); columnIndex++) {
	        Cell[] cells = sheet.getColumn(columnIndex);
	        boolean emptyColumn = true;

	        if (cells.length == 0) {
	            continue;
	        }    

	        for (int rowIndex = 0; rowIndex < cells.length; rowIndex++) {
	        	String rowContents = cells[rowIndex].getContents();
                if (emptyColumn && StringUtils.isNotBlank(rowContents) && rowIndex > 0) {
                	emptyColumn = false;
                	break;
                }
	        }
	        if (emptyColumn) {
	        	removedColumns.add(0, columnIndex);
        	} 
	    }    
	    for (Integer removedColumn : removedColumns) {
			sheet.removeColumn(removedColumn);
		}
	    
	    for (int columnIndex = 0; columnIndex < sheet.getColumns(); columnIndex++) {
	        Cell[] cells = sheet.getColumn(columnIndex);
	        int longestStrLen = -1;

	        if (cells.length == 0) {
	            continue;
	        }    

	        /* Find the widest cell in the column. */
	        for (int rowIndex = 0; rowIndex < cells.length; rowIndex++) {
	        	String rowContents = cells[rowIndex].getContents();
	            if (rowContents.length() > longestStrLen) {
	                String str = rowContents;
	                if (str == null || str.isEmpty()) {
	                    continue;
	                }    
	                longestStrLen = str.trim().length();
	            }
	        }

	        /* If not found, skip the column. */
	        if (longestStrLen == -1) {
	            continue;
	        } 

	        /* If wider than the max width, crop width */
	        if (longestStrLen > 255) {
	            longestStrLen = 255;
	        }    

	        CellView cv = sheet.getColumnView(columnIndex);
	        cv.setSize(longestStrLen * 256 + 100); /* Every character is 256 units wide, so scale it. */
	        sheet.setColumnView(columnIndex, cv);
	    }
	}
	
	public void write(OutputStream out, List<String> bagInfoLines) throws IOException {
		WritableWorkbook workbook = Workbook.createWorkbook(out);
		WritableSheet descriptionSheet = workbook.createSheet("Description", 0);
		WritableSheet documentsSheet = workbook.createSheet("Documents", 1);
		WritableSheet foldersSheet = workbook.createSheet("Dossiers", 2);

		try {
			WritableCellFormat baseCellFormat = new WritableCellFormat();
//			baseCellFormat.setWrap(true);
			
			CellView baseColumnView = new CellView();
			baseColumnView.setAutosize(true);
			baseColumnView.setFormat(baseCellFormat);
			
			WritableCellFormat headerFormat = new WritableCellFormat(baseCellFormat);
			headerFormat.setBorder(Border.BOTTOM, BorderLineStyle.THIN);
			headerFormat.setVerticalAlignment(VerticalAlignment.TOP);
			WritableFont headerFont = new WritableFont(WritableFont.ARIAL, WritableFont.DEFAULT_POINT_SIZE, WritableFont.BOLD);
			headerFormat.setFont(headerFont);

			WritableCellFormat cellFormat = new WritableCellFormat(baseCellFormat);
			
			{
				int row = 0;
				for (String bagInfoLine : bagInfoLines) {
					descriptionSheet.addCell(new Label(0, row, bagInfoLine, headerFormat));
					row++;
				}
			}
			
			{
				int row = 0;
				for (SIPDocument sipDocument : sipDocuments.values()) {
					if (row == 0) {
						documentsSheet.addCell(new Label(0, row, "Chemin", headerFormat));
					}
					// FIXME
					documentsSheet.addCell(new Label(0, row + 1, sipDocument.getZipPath(), cellFormat));
					
					List<String> metadataIds = sipDocument.getMetadataIds();
					for (int column = 0; column < metadataIds.size(); column++) {
						String metadataId = metadataIds.get(column);
						if (row == 0) {
							String metadataLabel = sipDocument.getMetadataLabel(metadataId);
							documentsSheet.addCell(new Label(column + 1, row, metadataLabel, headerFormat));
						}
						String metadataValue = sipDocument.getMetadataValue(metadataId);
						documentsSheet.addCell(new Label(column + 1, row + 1, metadataValue, cellFormat));
					}
					row++;
				}
			}
			
			{
				int row = 0;
				for (SIPFolder sipFolder : sipFolders.values()) {
					if (row == 0) {
						foldersSheet.addCell(new Label(0, row, "Chemin", headerFormat));
					}
					foldersSheet.addCell(new Label(0, row + 1, sipFolder.getZipPath(), cellFormat));
					
					List<String> metadataIds = sipFolder.getMetadataIds();
					for (int column = 0; column < metadataIds.size(); column++) {
						String metadataId = metadataIds.get(column);
						if (row == 0) {
							String metadataLabel = sipFolder.getMetadataLabel(metadataId);
							foldersSheet.addCell(new Label(column + 1, row, metadataLabel, headerFormat));
						}
						String metadataValue = sipFolder.getMetadataValue(metadataId);
						foldersSheet.addCell(new Label(column + 1, row + 1, metadataValue, cellFormat));
					}
					row++;
				}
			}
			
			for (WritableSheet sheet : workbook.getSheets()) {
				sheetAutoFitColumns(sheet);
			}

			workbook.write();
			workbook.close();
		} catch (JXLException e) {
			throw new IOException(e);
		}
	}

}
