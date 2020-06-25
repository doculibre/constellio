package com.constellio.model.services.pdf.pdfjs.signature;

import com.constellio.model.services.pdf.signature.PdfSignatureAnnotation;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.json.JSONArray;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
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

	public List<PdfSignatureAnnotation> getSignatureAnnotations(PDDocument pdDocument, boolean unbakedOnly) {
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
					if (!unbakedOnly || !signatureAnnotation.isBaked()) {
						signatureAnnotations.add(signatureAnnotation);
					}
				}
			}
		}
		return signatureAnnotations;
	}

	private boolean isSignatureAnnotation(JSONObject annotationJson) {
		boolean signatureAnnotation;
		String type = annotationJson.getString("type");
		if ("signature-image-annotation".equals(type) || "signature-pad-annotation".equals(type) || "signature-text-annotation".equals(type)) {
			signatureAnnotation = true;
		} else {
			signatureAnnotation = false;
		}
		return signatureAnnotation;
	}

	public void markSignatureAnnotationsAsBaked(String bakeUser, Date bakeDate) {
		JSONObject pagesAndAnnotations = jsonObject.getJSONObject("pagesAndAnnotations");
		for (String pageStr : pagesAndAnnotations.keySet()) {
			// 1 based in PDFJS, 0 based in PDFBox
			JSONArray pageAnnotations = pagesAndAnnotations.getJSONArray(pageStr);
			for (Iterator<Object> it = pageAnnotations.iterator(); it.hasNext(); ) {
				JSONObject annotationJson = (JSONObject) it.next();
				if (isSignatureAnnotation(annotationJson) && !annotationJson.getBoolean("baked")) {
					String type = annotationJson.getString("type");
					if ("signature-pad-annotation".equals(type)
						|| "signature-text-annotation".equals(type)
						|| "text-annotation".equals(type)) {
						annotationJson.put("type", "signature-image-annotation");
						String imageUrl = annotationJson.getString("imageUrl");
						annotationJson.remove("imageUrl");
						annotationJson.put("url", imageUrl);
					}
					annotationJson.put("readOnly", true);
					annotationJson.put("baked", true);
					annotationJson.put("bakeUser", bakeUser);
					annotationJson.put("bakeDate", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(bakeDate));
				}
			}
		}
	}

}
