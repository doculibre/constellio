package com.constellio.model.services.pdf.pdfjs.signature;

import com.constellio.model.services.pdf.signature.PdfSignatureAnnotation;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.json.JSONObject;

import java.awt.*;

public class PdfJSSignatureAnnotation extends PdfSignatureAnnotation {

	public PdfJSSignatureAnnotation(JSONObject annotationJson, PDDocument pdDocument, int page) {
		super(
				page,
				createPositionRectangle(annotationJson, pdDocument, page),
				getUserId(annotationJson),
				getUsername(annotationJson),
				fetchImageData(annotationJson)
		);
	}

	private static Rectangle createPositionRectangle(JSONObject annotationJson, PDDocument pdDocument, int page) {
		float xPercent = (float) annotationJson.getDouble("x");
		float yPercent = (float) annotationJson.getDouble("y");
		float widthPercent = (float) annotationJson.getDouble("width");
		float heightPercent = (float) annotationJson.getDouble("height");

		// 1 based in PDF, 0 based in PDFBox
		PDRectangle pageRectangle = pdDocument.getPage(page).getMediaBox();
		float pageWidth = pageRectangle.getWidth();
		float pageHeight = pageRectangle.getHeight();

		float xRectangle = (xPercent / 100) * pageWidth;
		float yRectangle = (yPercent / 100) * pageHeight;
		// PDF rectangle y starts from the bottom 
		// float yRectangle = (100 - (yPercent / 100)) * pageHeight; 
		float widthRectangle = (widthPercent / 100) * pageWidth;
		float heightRectangle = (heightPercent / 100) * pageHeight;

		return new Rectangle(Math.round(xRectangle), Math.round(yRectangle), Math.round(widthRectangle), Math.round(heightRectangle));
	}

	private static String getUserId(JSONObject annotationJson) {
		return annotationJson.getString("userId");
	}

	private static String getUsername(JSONObject annotationJson) {
		return annotationJson.getString("lastModificationUser");
	}

	private static String fetchImageData(JSONObject annotationJson) {
		String imageUrl;
		String type = annotationJson.getString("type");
		if ("signature-image-annotation".equals(type)) {
			imageUrl = annotationJson.getString("url");
		} else if ("signature-pad-annotation".equals(type)) {
			imageUrl = annotationJson.getString("imageUrl");
		} else {
			imageUrl = null;
		}
		return imageUrl;
	}

}
