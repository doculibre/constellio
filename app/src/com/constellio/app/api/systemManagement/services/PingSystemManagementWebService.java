package com.constellio.app.api.systemManagement.services;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;

public class PingSystemManagementWebService extends HttpServlet {

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {

		Element rootElement = new Element("response");

		PrintWriter writer = resp.getWriter();

		rootElement.setText("success");
		writer.append(xmlDocumentToString(rootElement));
		writer.close();

	}

	String xmlDocumentToString(Element rootElement) {
		Document document = new Document();
		document.addContent(rootElement);
		XMLOutputter xmlOutput = new XMLOutputter(Format.getPrettyFormat());
		return xmlOutput.outputString(document);
	}

}

