package com.constellio.model.services.pdf.pdfjs.signature;

import com.constellio.model.services.pdf.signature.PdfSignatureAnnotation;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class PdfJSAnnotations {

	private JSONObject jsonObject;

	public PdfJSAnnotations(JSONObject jsonObject) {
		this.jsonObject = jsonObject;
	}

	public JSONObject getJSONObject() {
		return jsonObject;
	}

	public String getApiVersion() {
		return jsonObject.getString("apiVersion");
	}

	public String getVersion() {
		return jsonObject.getString("version");
	}

	public void setVersion(String version) {
		jsonObject.put("version", version);
	}

	public List<PdfSignatureAnnotation> getSignatureAnnotations(PDDocument pdDocument) {
		List<PdfSignatureAnnotation> signatureAnnotations = new ArrayList<>();
		JSONObject pagesAndAnnotations = jsonObject.getJSONObject("pagesAndAnnotations");
		for (String pageStr : pagesAndAnnotations.keySet()) {
			// 1 based in PDFJS, 0 based in PDFBox
			JSONArray pageAnnotations = pagesAndAnnotations.getJSONArray(pageStr);
			for (int i = 0; i < pageAnnotations.length(); i++) {
				JSONObject annotationJson = pageAnnotations.getJSONObject(i);
				if (isSignatureAnnotation(annotationJson)) {
					int page0Base = Integer.parseInt(pageStr) - 1;
					PdfJSSignatureAnnotation signatureAnnotation = new PdfJSSignatureAnnotation(annotationJson, pdDocument, page0Base);
					signatureAnnotations.add(signatureAnnotation);
				}
			}
		}
		return signatureAnnotations;
	}

	public void clearSignatureAnnotations() {
		JSONObject pagesAndAnnotations = jsonObject.getJSONObject("pagesAndAnnotations");
		for (String pageStr : pagesAndAnnotations.keySet()) {
			// 1 based in PDFJS, 0 based in PDFBox
			JSONArray pageAnnotations = pagesAndAnnotations.getJSONArray(pageStr);
			for (Iterator<Object> it = pageAnnotations.iterator(); it.hasNext(); ) {
				JSONObject annotationJson = (JSONObject) it.next();
				if (isSignatureAnnotation(annotationJson)) {
					it.remove();
				}
			}
		}
	}

	private boolean isSignatureAnnotation(JSONObject annotationJson) {
		boolean signatureAnnotation;
		String type = annotationJson.getString("type");
		if ("signature-image-annotation".equals(type) || "signature-pad-annotation".equals(type)) {
			signatureAnnotation = true;
		} else {
			signatureAnnotation = false;
		}
		return signatureAnnotation;
	}

}
