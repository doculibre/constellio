package com.constellio.app.modules.rm.model.SIPArchivedGenerator.constellio.sip.ead;

import com.constellio.app.modules.rm.model.SIPArchivedGenerator.constellio.sip.model.SIPObject;
import com.constellio.app.modules.rm.model.SIPArchivedGenerator.constellio.sip.xsd.XMLDocumentValidator;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.Namespace;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class EAD {
	
	private SIPObject sipObject;
	
	private Namespace eadNamespace = Namespace.getNamespace("ead", "urn:isbn:1-931666-22-9");
	private Namespace xsiNamespace = Namespace.getNamespace("xsi", "http://www.w3.org/2001/XMLSchema-instance");
	
	private Document doc;
	
	private Element eadElement;
	
	private EADArchdesc archdesc;
	
	private static SAXBuilder builder;
	
	private static XMLDocumentValidator validator = new XMLDocumentValidator();
	
	static {
//		URL eadXSD = ConstellioSIP.class.getResource("xsd/ead.xsd");
//		URL xlinkXSD = ConstellioSIP.class.getResource("xsd/xlink.xsd");
//		XMLReaderJDOMFactory schemaFactory;
//		try {
//			schemaFactory = new XMLReaderXSDFactory(eadXSD, xlinkXSD);
//		} catch (JDOMException e) {
//			throw new RuntimeException(e);
//		}
//		builder = new SAXBuilder(schemaFactory);
		builder = new SAXBuilder();
	}
	
	public EAD(SIPObject sipObject, EADArchdesc archdesc) {
		this.sipObject = sipObject;
		this.archdesc = archdesc;
		
		this.eadElement = new Element("ead", eadNamespace);
		this.eadElement.addNamespaceDeclaration(xsiNamespace);
		eadElement.setAttribute("schemaLocation", "urn:isbn:1-931666-22-9 http://www.loc.gov/ead/ead.xsd", xsiNamespace);
		
		this.doc = new Document(eadElement);
		addHeader();
		addArchdesc();
	}
	
	@SuppressWarnings("unchecked")
	public <T extends SIPObject> T getSIPObject() {
		return (T) sipObject;
	}

	private void addHeader() {
		Element eadheaderElement = new Element("eadheader", eadNamespace);
		
		Element eadidElement = new Element("eadid", eadNamespace);
		eadidElement.setAttribute("identifier", "Identifiant externe");
		eadidElement.setText(sipObject.getId());
		
		Element filedescElement = new Element("filedesc", eadNamespace);
		Element titlestmtElement = new Element("titlestmt", eadNamespace);
		Element titleproperElement = new Element("titleproper", eadNamespace);
		titleproperElement.setText(sipObject.getTitle());
		
		eadElement.addContent(eadheaderElement);
		eadheaderElement.addContent(eadidElement);
		eadheaderElement.addContent(filedescElement);
		filedescElement.addContent(titlestmtElement);
		titlestmtElement.addContent(titleproperElement);
	}
	
	private void addArchdesc() {
		Element archdescElement = new Element("archdesc", eadNamespace);
		archdescElement.setAttribute("level", "class");
		
		Element didElement = new Element("did", eadNamespace);
		
		Element unitidElement = new Element("unitid", eadNamespace);
		didElement.addContent(unitidElement);
		unitidElement.setAttribute("type", "Identifiant externe");
		unitidElement.setText(sipObject.getId());
		
		Element unittitleElement = new Element("unittitle", eadNamespace);
		didElement.addContent(unittitleElement);
		unittitleElement.setText(sipObject.getTitle());
		
		Map<String, String> didUnitDates = archdesc.getDidUnitDates();
		for (Entry<String, String> entry : didUnitDates.entrySet()) {
			String datechar = entry.getKey();
			String unitdateValue = entry.getValue();

			Element unitdateElement = new Element("unitdate", eadNamespace);
			didElement.addContent(unitdateElement);
			unitdateElement.setAttribute("datechar", datechar);
			unitdateElement.setText(unitdateValue);
		}
		
		List<String> didLangmaterials = archdesc.getDidLangmaterials();
		for (String didLangmaterial : didLangmaterials) {
			Element langmaterialElement = new Element("langmaterial", eadNamespace);
			didElement.addContent(langmaterialElement);
			langmaterialElement.setText(didLangmaterial);
		}
		
		List<String> didAbstracts = archdesc.getDidAbstracts();
		for (String didAbstract : didAbstracts) {
			Element didAbstractElement = new Element("abstract", eadNamespace);
			didElement.addContent(didAbstractElement);
			didAbstractElement.setText(didAbstract);
		}
		
		String didOriginationCorpname = archdesc.getDidOriginationCorpname();
		if (StringUtils.isNotBlank(didOriginationCorpname)) {
			Element originationElement = new Element("origination", eadNamespace);
			didElement.addContent(originationElement);
			Element corpnameElement = new Element("corpname", eadNamespace);
			originationElement.addContent(corpnameElement);
			corpnameElement.setText(didOriginationCorpname);
		}
		
		List<String> didNotePs = archdesc.getDidNotePs();
		for (String didNoteP : didNotePs) {
			Element didNoteElement = new Element("note", eadNamespace);
			didElement.addContent(didNoteElement);
			
			Element didNotePElement = new Element("p", eadNamespace);
			didNoteElement.addContent(didNotePElement);
			didNotePElement.setText(didNoteP);
		}
		
		archdescElement.addContent(didElement);
		
		String accessrestrictLegalstatus = archdesc.getAccessRestrictLegalStatus();
		if (StringUtils.isNotBlank(accessrestrictLegalstatus)) {
			Element accessrestrictElement = new Element("accessrestrict", eadNamespace);
			archdescElement.addContent(accessrestrictElement);
			Element legalstatusElement = new Element("legalstatus", eadNamespace);
			accessrestrictElement.addContent(legalstatusElement);
			legalstatusElement.setText(accessrestrictLegalstatus);
		}
		
		List<String> controlaccessSubjects = archdesc.getControlAccessSubjects();
		if (!controlaccessSubjects.isEmpty()) {
			Element controlaccessElement = new Element("controlaccess", eadNamespace);
			archdescElement.addContent(controlaccessElement);
			for (String controlaccessSubject : controlaccessSubjects) {
				Element subjectElement = new Element("subject", eadNamespace);
				controlaccessElement.addContent(subjectElement);
				subjectElement.setText(controlaccessSubject);
			}
		}
		
		List<List<String>> relatedmaterialLists = archdesc.getRelatedmaterialLists();
		for (List<String> relatedmaterialList : relatedmaterialLists) {
			Element relatedmaterialElement = new Element("relatedmaterial", eadNamespace);
			archdescElement.addContent(relatedmaterialElement);
			
			Element listElement = new Element("list", eadNamespace);
			relatedmaterialElement.addContent(listElement);
			for (String relatedmaterial : relatedmaterialList) {
				Element itemElement = new Element("item", eadNamespace);
				listElement.addContent(itemElement);
				itemElement.setText(relatedmaterial);
			}
		}
		
		eadElement.addContent(archdescElement);
		
		List<String> fileplanPs = archdesc.getFileplanPs();
		if (!fileplanPs.isEmpty()) {
			Element fileplanElement = new Element("fileplan", eadNamespace);
			archdescElement.addContent(fileplanElement);
			for (String fileplanP : fileplanPs) {
				Element fileplanPElement = new Element("p", eadNamespace);
				fileplanElement.addContent(fileplanPElement);
				fileplanPElement.setText(fileplanP);
			}
		}
		
		List<String> altformavailPs = archdesc.getAltformavailPs();
		if (!altformavailPs.isEmpty()) {
			Element altformavailElement = new Element("altformavail", eadNamespace);
			archdescElement.addContent(altformavailElement);
			for (String altformavailP : altformavailPs) {
				Element altformavailPElement = new Element("p", eadNamespace);
				altformavailElement.addContent(altformavailPElement);
				altformavailPElement.setText(altformavailP);
			}
		}
	}
	
	public void build(File file) throws IOException {
		validator.validate(doc, "xlink.xsd", "ead.xsd");
		OutputStream out = new FileOutputStream(file);
        try {
            // Output as XML
            // create XMLOutputter
            XMLOutputter xml = new XMLOutputter();
            // we want to format the xml. This is used only for demonstration.
            // pretty formatting adds extra spaces and is generally not required.
            xml.setFormat(Format.getPrettyFormat());
            xml.output(doc, out);
//            String xmlStr = xml.outputString(doc);
//            System.out.println(xmlStr);
            
    		// Validating XML
    		builder.build(file);
		} catch (JDOMException e) {
			String fileContent = FileUtils.readFileToString(file);
			System.out.println(fileContent);
			throw new RuntimeException("Exception for object of type " + sipObject.getType() + " (" + sipObject.getId() + ")", e);
		} finally {
			out.close();
		}
	}

}
