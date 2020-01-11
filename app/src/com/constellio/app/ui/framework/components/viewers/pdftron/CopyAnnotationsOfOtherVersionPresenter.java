package com.constellio.app.ui.framework.components.viewers.pdftron;

import com.constellio.app.ui.entities.ContentVersionVO;
import com.constellio.model.services.pdftron.PdfTronXMLException.PdfTronXMLException_IOExeption;
import com.constellio.model.services.pdftron.PdfTronXMLException.PdfTronXMLException_XMLParsingException;

import java.util.List;

public interface CopyAnnotationsOfOtherVersionPresenter {
	List<ContentVersionVO> getAvailableVersion();

	void addAnnotation(ContentVersionVO contentVErsionVO)
			throws PdfTronXMLException_IOExeption, PdfTronXMLException_XMLParsingException;

	String getHash();

	String getVersion();

	String getRecordId();
}
