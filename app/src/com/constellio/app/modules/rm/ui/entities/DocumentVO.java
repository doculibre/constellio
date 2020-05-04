package com.constellio.app.modules.rm.ui.entities;

import com.constellio.app.ui.entities.ContentVersionVO;
import com.constellio.app.ui.entities.MetadataValueVO;
import com.constellio.app.ui.entities.RecordVO;
import org.joda.time.LocalDate;

import java.util.List;

import static com.constellio.app.modules.rm.wrappers.Document.CONTENT;
import static com.constellio.app.modules.rm.wrappers.Document.DESCRIPTION;
import static com.constellio.app.modules.rm.wrappers.Document.FOLDER;
import static com.constellio.app.modules.rm.wrappers.Document.KEYWORDS;
import static com.constellio.app.modules.rm.wrappers.Document.PUBLISHED_EXPIRATION_DATE;
import static com.constellio.app.modules.rm.wrappers.Document.PUBLISHED_START_DATE;
import static com.constellio.app.modules.rm.wrappers.Document.TYPE;

public class DocumentVO extends RecordVO {

	public DocumentVO(String id, List<MetadataValueVO> metadataValues, VIEW_MODE viewMode, List<String> excludedMetadata) {
		super(id, metadataValues, viewMode, excludedMetadata);
	}

	public DocumentVO(RecordVO recordVO) {
		super(recordVO.getId(), recordVO.getMetadataValues(), recordVO.getViewMode());
	}

	public String getFolder() {
		return get(FOLDER);
	}

	public void setFolder(String folder) {
		set(FOLDER, folder);
	}

	public void setFolder(FolderVO folder) {
		set(FOLDER, folder);
	}

	public String getDescription() {
		return get(DESCRIPTION);
	}

	public void setDescription(String description) {
		set(DESCRIPTION, description);
	}

	public String getKeywords() {
		return get(KEYWORDS);
	}

	public void setKeywords(String keywords) {
		set(KEYWORDS, keywords);
	}

	public ContentVersionVO getContent() {
		return get(CONTENT);
	}

	public void setContent(ContentVersionVO content) {
		set(CONTENT, content);
	}

	public String getType() {
		return get(TYPE);
	}

	public void setType(RecordVO type) {
		set(TYPE, type);
	}

	public void setType(String type) {
		set(TYPE, type);
	}

	public LocalDate getPublishingStartDate() {
		return get(PUBLISHED_START_DATE);
	}

	public void setPublishingStartDate(LocalDate publishingStartDate) {
		set(PUBLISHED_START_DATE, publishingStartDate);
	}

	public LocalDate getPublishingExpirationDate() {
		return get(PUBLISHED_EXPIRATION_DATE);
	}

	public void setPublishingExpirationDate(LocalDate publishingEndDate) {
		set(PUBLISHED_EXPIRATION_DATE, publishingEndDate);
	}

}
