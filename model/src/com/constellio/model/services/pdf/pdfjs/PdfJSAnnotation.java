package com.constellio.model.services.pdf.pdfjs;

import java.awt.Rectangle;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.json.JSONObject;

import com.constellio.model.services.pdf.PdfAnnotation;

public class PdfJSAnnotation extends PdfAnnotation {

	public PdfJSAnnotation(JSONObject annotationJson, PDDocument pdDocument, int page) {
		super(
				page,
				createPositionRectangle(annotationJson, pdDocument, page),
				getUserId(annotationJson),
				getUsername(annotationJson),
				fetchImageData(annotationJson),
				fetchSignature(annotationJson),
				fetchBaked(annotationJson)
		);
	}

	private static Rectangle createPositionRectangle(JSONObject annotationJson, PDDocument pdDocument, int page) {
		float xPercent = (float) annotationJson.getDouble("x");
		float yPercent = (float) annotationJson.getDouble("y");
		float widthPercent = (float) annotationJson.getDouble("width");
		float heightPercent = (float) annotationJson.getDouble("height");

		// 1 based in PDF, 0 based in PDFBox
		PDPage pdPage = pdDocument.getPage(page);
		int rotation = pdPage.getRotation();
		PDRectangle mediaBox = pdPage.getMediaBox();
		float pageWidth;
		float pageHeight;
		if (rotation == 90 || rotation == -90 || rotation == 270) {
			pageWidth = mediaBox.getHeight();
			pageHeight = mediaBox.getWidth();
		} else {
			pageWidth = mediaBox.getWidth();
			pageHeight = mediaBox.getHeight();
		}

		float widthRectangle = (widthPercent / 100) * pageWidth;
		float heightRectangle = (heightPercent / 100) * pageHeight;
		float xRectangle = (xPercent / 100) * pageWidth;
		// PDF rectangle y starts from the bottom 
		float yRectangle = (1 - yPercent / 100) * pageHeight; 

		return new Rectangle(Math.round(xRectangle), Math.round(yRectangle), Math.round(widthRectangle), Math.round(heightRectangle));
	}

	private static String getUserId(JSONObject annotationJson) {
		return annotationJson.has("userId") ? annotationJson.getString("userId") : null;
	}

	private static String getUsername(JSONObject annotationJson) {
		return annotationJson.has("lastModificationUser") ? annotationJson.getString("lastModificationUser") : null;
	}

	private static String fetchImageData(JSONObject annotationJson) {
		String imageUrl;
		String type = annotationJson.getString("type");
		if ("signature-image-annotation".equals(type)) {
			imageUrl = annotationJson.getString("imageUrl");
		} else if ("signature-pad-annotation".equals(type)) {
			imageUrl = annotationJson.getString("imageUrl");
		} else if ("signature-text-annotation".equals(type)) {
			imageUrl = annotationJson.getString("imageUrl");
		} else if ("text-annotation".equals(type)) {
			imageUrl = annotationJson.getString("imageUrl");
		} else {
			imageUrl = null;
		}
		return imageUrl;
	}

	private static boolean fetchSignature(JSONObject annotationJson) {
		return annotationJson.has("signature") ? annotationJson.getBoolean("signature") : false;
	}

	private static boolean fetchBaked(JSONObject annotationJson) {
		return annotationJson.has("baked") ? annotationJson.getBoolean("baked") : false;
	}

}
