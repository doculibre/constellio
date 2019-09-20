package com.constellio.app.services.sip.mets;

import au.edu.apsr.mtk.base.Agent;
import au.edu.apsr.mtk.base.Div;
import au.edu.apsr.mtk.base.DmdSec;
import au.edu.apsr.mtk.base.FLocat;
import au.edu.apsr.mtk.base.FileGrp;
import au.edu.apsr.mtk.base.FileSec;
import au.edu.apsr.mtk.base.Fptr;
import au.edu.apsr.mtk.base.METS;
import au.edu.apsr.mtk.base.METSException;
import au.edu.apsr.mtk.base.METSWrapper;
import au.edu.apsr.mtk.base.MdRef;
import au.edu.apsr.mtk.base.MetsHdr;
import au.edu.apsr.mtk.base.StructMap;
import com.constellio.app.services.sip.mets.MetsFileWriterRuntimeException.MetsFileWriterRuntimeException_CreatedFileIsInvalid;
import com.constellio.app.services.sip.mets.MetsFileWriterRuntimeException.MetsFileWriterRuntimeException_ErrorCreatingFile;
import com.constellio.app.services.sip.xsd.XMLDocumentValidator;
import com.constellio.app.services.sip.xsd.XMLDocumentValidatorException;
import com.constellio.data.io.services.facades.IOServices;
import org.jdom2.Element;
import org.jdom2.Namespace;
import org.jdom2.input.DOMBuilder;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static java.util.Arrays.asList;

public class MetsFileWriter {

	private XMLDocumentValidator validator = new XMLDocumentValidator();

	private Namespace constellioNamespace = Namespace.getNamespace("constellio", "http://www.constellio.com");

	private Namespace xsiNamespace = Namespace.getNamespace("xsi", "http://www.w3.org/2001/XMLSchema-instance");

	private SimpleDateFormat sdfTimestamp = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");

	private OutputStream outputStream;

	private Date sipCreationDate;

	private String metsFileZipPath;

	private Map<String, MetsDivisionInfo> divisionsInfoMap;

	private IOServices ioServices;

	private static List<String> METS_XSDs = asList("xlink.xsd", "mets.xsd");

	private static final Logger LOGGER = LoggerFactory.getLogger(MetsFileWriter.class);

	public MetsFileWriter(IOServices ioServices, OutputStream outputStream, String metsFileZipPath,
						  Date sipCreationDate,
						  Map<String, MetsDivisionInfo> divisionsInfoMap) {
		this.ioServices = ioServices;
		this.outputStream = outputStream;
		this.sipCreationDate = sipCreationDate;
		this.metsFileZipPath = metsFileZipPath;
		this.divisionsInfoMap = divisionsInfoMap;
	}


	public void write(List<MetsEADMetadataReference> metsEADMetadataReferences,
					  List<MetsContentFileReference> contentFileReferences) {

		List<MetsStructureDivision> divisionsHierarchy = buildStructureDivisionHierarchy(metsEADMetadataReferences, contentFileReferences);
		try {

			METSWrapper metsWrapper = null;

			metsWrapper = new METSWrapper();

			METS mets = metsWrapper.getMETSObject();

			MetsHdr metsHeader = mets.newMetsHdr();
			metsHeader.setCreateDate(sdfTimestamp.format(sipCreationDate));
			metsHeader.setRecordStatus("COMPLETE");
			mets.setMetsHdr(metsHeader);

			Agent agent = metsHeader.newAgent();
			agent.setRole("CREATOR");
			agent.setType("ORGANIZATION");
			agent.setName("");
			metsHeader.addAgent(agent);


			for (MetsEADMetadataReference metsEADMetadataReference : metsEADMetadataReferences) {
				DmdSec dmdSec = mets.newDmdSec();
				dmdSec.setID(metsEADMetadataReference.getId());
				mets.addDmdSec(dmdSec);

				MdRef mdRef = dmdSec.newMdRef();
				mdRef.setLocType("URN");
				mdRef.setMDType("EAD");
				mdRef.setHref(metsEADMetadataReference.getPath());
				dmdSec.setMdRef(mdRef);
			}

			FileSec fileSec = mets.newFileSec();

			FileGrp documentFileGroup = fileSec.newFileGrp();
			fileSec.addFileGrp(documentFileGroup);

			for (MetsContentFileReference contentFileReference : contentFileReferences) {
				au.edu.apsr.mtk.base.File file = documentFileGroup.newFile();
				documentFileGroup.addFile(file);
				if (contentFileReference.getDmdid() != null) {
					file.setDmdID(contentFileReference.getDmdid());
				}
				file.setID(contentFileReference.getId());
				file.setSize(contentFileReference.getSize());
				file.setChecksum(contentFileReference.getCheckSum());
				file.setChecksumType(contentFileReference.getCheckSumType());
				if (contentFileReference.getUse() != null) {
					file.setUse(contentFileReference.getUse());
				}

				FLocat fileLocation = file.newFLocat();
				file.addFLocat(fileLocation);
				fileLocation.setLocType("URL");
				fileLocation.setHref(contentFileReference.getPath());
				fileLocation.setTitle(contentFileReference.getTitle());

			}

			StructMap structMap = mets.newStructMap();

			Div bagDiv = structMap.newDiv();
			structMap.addDiv(bagDiv);
			bagDiv.setLabel("bag");
			bagDiv.setType("folder");

			Set<String> includedDivisionKeys = new HashSet<>();
			for (MetsEADMetadataReference metsEADMetadataReference : metsEADMetadataReferences) {
				includedDivisionKeys.add(metsEADMetadataReference.getId());
			}
			addDivisions(bagDiv, divisionsHierarchy, includedDivisionKeys);

			mets.setFileSec(fileSec);
			mets.addStructMap(structMap);

			org.w3c.dom.Document domDoc = metsWrapper.getMETSDocument();
			DOMBuilder domBuilder = new DOMBuilder();
			org.jdom2.Document jdomDoc = domBuilder.build(domDoc);

			Element rootElement = jdomDoc.getRootElement();
			rootElement.addNamespaceDeclaration(Namespace.getNamespace("xlink", "http://www.w3.org/1999/xlink"));
			rootElement.addNamespaceDeclaration(Namespace.getNamespace("PREMIS", "info:lc/xmlns/premis-v2"));
			rootElement.addNamespaceDeclaration(xsiNamespace);
			rootElement.addNamespaceDeclaration(constellioNamespace);
			rootElement.setAttribute("schemaLocation",
					"http://www.loc.gov/METS/ http://www.loc.gov/standards/mets/mets.xsd http://www.w3.org/1999/xlink http://www.loc.gov/standards/mets/xlink.xsd",
					xsiNamespace);
			rootElement.setAttribute("TYPE", "sa_all-formats-01_dss-01");

			XMLOutputter xml = new XMLOutputter();
			xml.setFormat(Format.getPrettyFormat());
			xml.output(jdomDoc, outputStream);
			outputStream.flush();

			validator.validate(jdomDoc, METS_XSDs);
		} catch (IOException | METSException e) {
			throw new MetsFileWriterRuntimeException_ErrorCreatingFile(metsFileZipPath, e);

		} catch (XMLDocumentValidatorException e) {
			throw new MetsFileWriterRuntimeException_CreatedFileIsInvalid(metsFileZipPath, e);
		}
	}

	public void close() {
		ioServices.closeQuietly(outputStream);
	}

	private void addDivisions(Div parentDiv, List<MetsStructureDivision> divisions, Set<String> includedDivisionKeys) {

		Collections.sort(divisions, new Comparator<MetsStructureDivision>() {
			@Override
			public int compare(MetsStructureDivision o1, MetsStructureDivision o2) {
				return o1.divisionInfo.getLabel().compareTo(o2.divisionInfo.getLabel());
			}
		});

		for (MetsStructureDivision division : divisions) {
			try {
				Div div = parentDiv.newDiv();

				if (division.divisionInfo instanceof MetsEADMetadataReference) {
					if (includedDivisionKeys.contains(division.divisionInfo.getId())) {
						div.setDmdID(division.divisionInfo.getId());
					} else {
						div.setID(division.divisionInfo.getId());
					}
				} else {
					div.setID(division.divisionInfo.getId());
				}
				div.setLabel(division.divisionInfo.getLabel());
				div.setType(division.divisionInfo.getType());
				parentDiv.addDiv(div);

				addDivisions(div, division.childDivisions, includedDivisionKeys);

				if (!division.filePointers.isEmpty()) {

					Collections.sort(division.filePointers, new Comparator<MetsFilePointer>() {
						@Override
						public int compare(MetsFilePointer o1, MetsFilePointer o2) {
							return o1.fileId.compareTo(o2.fileId);
						}
					});
				}

				for (MetsFilePointer filePointer : division.filePointers) {
					Fptr fileFptr = div.newFptr();
					fileFptr.setFileID(filePointer.fileId);
					div.addFptr(fileFptr);
				}

			} catch (METSException e) {
				throw new MetsFileWriterRuntimeException_ErrorCreatingFile(metsFileZipPath, e);
			}
		}

	}

	private List<MetsStructureDivision> buildStructureDivisionHierarchy(
			List<MetsEADMetadataReference> metsEADMetadataReferences,
			List<MetsContentFileReference> contentFileReferences) {

		List<MetsStructureDivision> rootDivisions = new ArrayList<>();
		Map<String, MetsStructureDivision> allDivisions = new HashMap<>();

		for (MetsEADMetadataReference metsEADMetadataReference : metsEADMetadataReferences) {
			MetsStructureDivision division = new MetsStructureDivision(metsEADMetadataReference);
			allDivisions.put(metsEADMetadataReference.getId(), division);
		}

		for (MetsEADMetadataReference metsEADMetadataReference : metsEADMetadataReferences) {
			MetsStructureDivision division = allDivisions.get(metsEADMetadataReference.getId());

			String parentId = division.divisionInfo.getParentId();
			if (parentId != null) {
				MetsStructureDivision parentDivision = allDivisions.get(parentId);

				if (parentDivision == null) {

					MetsStructureDivision lastAddedDivision = division;
					while (lastAddedDivision.divisionInfo.getParentId() != null) {
						MetsStructureDivision aParentDivision = allDivisions.get(lastAddedDivision.divisionInfo.getParentId());

						if (aParentDivision == null) {
							MetsDivisionInfo metsDivisionInfo = divisionsInfoMap.get(lastAddedDivision.divisionInfo.getParentId());
							if (metsDivisionInfo == null) {

								throw new IllegalArgumentException("No such division with id '" + lastAddedDivision.divisionInfo.getParentId() + "'");
							}
							aParentDivision = new MetsStructureDivision(metsDivisionInfo);
							aParentDivision.childDivisions.add(lastAddedDivision);
							allDivisions.put(aParentDivision.divisionInfo.getId(), aParentDivision);
							lastAddedDivision = aParentDivision;
						} else {
							break;
						}

					}
					parentDivision = allDivisions.get(parentId);
				} else {
					parentDivision.childDivisions.add(division);
				}

			}

			allDivisions.put(metsEADMetadataReference.getId(), division);
		}

		for (Map.Entry<String, MetsStructureDivision> entry : allDivisions.entrySet()) {
			if (entry.getValue().divisionInfo.getParentId() == null) {
				rootDivisions.add(entry.getValue());
			}
			divisionsInfoMap.put(entry.getKey(), entry.getValue().divisionInfo);
		}

		for (MetsContentFileReference contentFileReference : contentFileReferences) {
			MetsStructureDivision structureDivision = allDivisions.get(contentFileReference.getDmdid());
			if (structureDivision == null) {
				throw new IllegalArgumentException("No such division with id '" + contentFileReference.getDmdid() + "'");
			}
			structureDivision.filePointers.add(new MetsFilePointer(contentFileReference.getId()));
		}

		return rootDivisions;
	}


	public static class MetsStructureDivision {

		MetsDivisionInfo divisionInfo;

		List<MetsStructureDivision> childDivisions = new ArrayList<>();

		List<MetsFilePointer> filePointers = new ArrayList<>();

		public MetsStructureDivision(MetsDivisionInfo divisionInfo) {
			if (divisionInfo == null) {
				throw new IllegalArgumentException("divisionInfo must be not-null");
			}
			this.divisionInfo = divisionInfo;
		}

	}

	public static class MetsFilePointer {

		String fileId;

		public MetsFilePointer(String fileId) {
			this.fileId = fileId;
		}
	}
}
