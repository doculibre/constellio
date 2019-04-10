package com.constellio.app.ui.framework.components.diff;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.IOUtils;

import com.constellio.app.ui.application.ConstellioUI;
import com.constellio.app.ui.entities.ContentVersionVO;
import com.constellio.app.ui.framework.components.layouts.I18NHorizontalLayout;
import com.constellio.model.entities.records.ParsedContent;
import com.constellio.model.services.contents.ContentManager;
import com.constellio.model.services.contents.ParsedContentProvider;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.Label;

import difflib.ChangeDelta;
import difflib.Chunk;
import difflib.DeleteDelta;
import difflib.Delta;
import difflib.DiffUtils;
import difflib.InsertDelta;
import difflib.Patch;

public class DiffPanel extends I18NHorizontalLayout {

	public DiffPanel(ContentVersionVO contentVersionVO1, ContentVersionVO contentVersionVO2) {
		setSizeFull();
		setSpacing(true);
		
		try {
			List<Delta<String>> deltas = getDifferences(contentVersionVO1, contentVersionVO2);
			Label label1 = newDeltaLabel(contentVersionVO2, deltas, false);
			Label label2 = newDeltaLabel(contentVersionVO1, deltas, true);
			addComponents(label1, label2);
		} catch (IOException e) {
			Label errorMessage = new Label("Impossible to display differences");
			errorMessage.setWidth("100%");
			addComponent(errorMessage);
		}
	}
	
	private Label newDeltaLabel(ContentVersionVO contentVersionVO, List<Delta<String>> deltas, boolean original) {
		StringBuilder html = new StringBuilder();
		html.append("<div><b>" + contentVersionVO.getFileName() + " (" + contentVersionVO.getVersion() + ")</b><hr/></div>");
		String deltasHtml = getDeltasHtml(deltas, original);
		html.append(deltasHtml);
		Label label = new Label(html.toString(), ContentMode.HTML);
		label.setHeight("100%");
		return label;
	}
	
	private static String getDeltasHtml(List<Delta<String>> deltas, boolean original) {
		StringBuilder deltasHtml = new StringBuilder();
		for (Delta<String> delta : deltas) {
			String deltaHtml = getDeltaHtml(delta, original);
			deltasHtml.append(deltaHtml);
		}
		return deltasHtml.toString();
	}
	
	private static String getDeltaHtml(Delta<String> delta, boolean original) {
		List<String> lines = new ArrayList<String>();
		Chunk<String> chunk;
		if (original) {
			chunk = delta.getOriginal();
		} else {
			chunk = delta.getRevised();
		}
		lines.addAll((List<String>) chunk.getLines());
		
		StringBuilder html = new StringBuilder("<pre style='white-space: pre-wrap;");
		if (delta instanceof DeleteDelta) {
			html.append("text-decoration:line-through;");
			html.append("background-color:#ff9191;");
		} else if (delta instanceof InsertDelta) {
			html.append("background-color:#8ef458;");
		} else if (delta instanceof ChangeDelta) {
			html.append("background-color:#fcfc28;");
		}
		html.append("'>");
		if (delta instanceof DeleteDelta) {
			html.append("- ");
		} else if (delta instanceof InsertDelta) {
			html.append("+ ");
		} else if (delta instanceof ChangeDelta) {
			html.append("≠ ");
		}
		
		for (String line : lines) {
			StringBuffer sbLine = new StringBuffer();
			for (int i = 0; i < line.length(); i++) {
				char c = line.charAt(i);
				if (!"�".equals(c + "")) {
					sbLine.append(c);
				}
			}
			html.append(sbLine);
			html.append("\n");
		}
		html.append("</pre>");
		
		return html.toString();
	} 
	
	public static List<Delta<String>> getDifferences(ContentVersionVO contentVersionVO1, ContentVersionVO contentVersionVO2) throws IOException {
		String hash1 = contentVersionVO1.getHash();
		String hash2 = contentVersionVO2.getHash();
		
		ContentManager contentManager = ConstellioUI.getCurrent().getConstellioFactories().getModelLayerFactory().getContentManager();
		ParsedContentProvider parsedContentProvider = new ParsedContentProvider(contentManager);
		
		ParsedContent parsedContent1 = parsedContentProvider.getParsedContentParsingIfNotYetDone(hash1);
		ParsedContent parsedContent2 = parsedContentProvider.getParsedContentParsingIfNotYetDone(hash2);
		
		String parsedContentText1 = parsedContent1.getParsedContent();
		String parsedContentText2 = parsedContent2.getParsedContent();
		

		InputStream in1 = IOUtils.toInputStream(parsedContentText1, "UTF-8");
		InputStream in2 = IOUtils.toInputStream(parsedContentText2, "UTF-8");
		
		return getDifferences(in1, in2);
	}
	
	public static List<Delta<String>> getDifferences(InputStream in1, InputStream in2) throws IOException {
		List<Delta<String>> differences = new ArrayList<>();
		
		List<String> lines1 = IOUtils.readLines(in1);
		List<String> lines2 = IOUtils.readLines(in2);
		IOUtils.closeQuietly(in1);
		IOUtils.closeQuietly(in2);
		
		Patch<String> patch = DiffUtils.diff(lines1, lines2);
		List<Delta<String>> deltas = patch.getDeltas();
		differences.addAll(deltas);	
		return differences;
	}

}
