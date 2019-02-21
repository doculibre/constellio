package com.constellio.model.services.event;

import com.constellio.data.io.services.facades.IOServices;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.Event;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.schemas.MetadataSchemasManager;
import org.apache.commons.collections.CollectionUtils;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;

public class FileEventXMLWriter implements EventXMLWriter {
	File file;
	boolean isFirstWrite;
	ModelLayerFactory modelLayerFactory;
	IOServices ioServices;
	OutputStream fileOutputStream;
	XMLStreamWriter xmlStreamWriter;
	MetadataSchemasManager metadataSchemasManager;

	public FileEventXMLWriter(File file, ModelLayerFactory modelayerFactory) {
			this.file = file;
			this.modelLayerFactory = modelayerFactory;
			this.ioServices = modelayerFactory.getIOServicesFactory().newIOServices();
			this.metadataSchemasManager = modelayerFactory.getMetadataSchemasManager();
		isFirstWrite = true;
	}

	private boolean isRecordAnEvent(Record event) {
		return event.getTypeCode().equals(Event.SCHEMA_TYPE);
	}

	@Override
	public void write(Record event) {
		if(!isRecordAnEvent(event)) {
			throw new RuntimeException("This method only white events. FileEventXMLWriter#write");
		}

		initializeXmlFile();

		try {
			xmlStreamWriter.writeStartElement(EventService.EVENT_XML_TAG);

			MetadataSchemaTypes metadataSchemaTypes = metadataSchemasManager.getSchemaTypes(event.getCollection());
			MetadataSchema metadataSchema = metadataSchemaTypes.getSchema(event.getSchemaCode());
			for (Metadata metadata : metadataSchema.getMetadatas()) {
				Object value = event.get(metadata);

				boolean write;
				if (value != null) {
					write = true;
					if (value instanceof java.util.Collection) {
						if (CollectionUtils.isNotEmpty((java.util.Collection) value)) {
							write = true;
						} else {
							write = false;
						}
					}

					if (write) {
						xmlStreamWriter.writeAttribute(metadata.getLocalCode(), event.get(metadata).toString());
					}
				}
			}

			xmlStreamWriter.writeEndElement();
		} catch (XMLStreamException xmlStreamException) {
			throw new RuntimeException("Stream exception", xmlStreamException);
		}
	}

	@Override
	public File getXMLFile() {
		return file;
	}

	private void initializeXmlFile() {

		if(!isFirstWrite) {
			return;
		}

		XMLOutputFactory factory = XMLOutputFactory.newInstance();
		try {
			isFirstWrite = false;
			file.getParentFile().mkdirs();
			if(!file.exists()) {
				file.createNewFile();
			}
			fileOutputStream = ioServices.newFileOutputStream(file, EventService.IO_STREAM_NAME_BACKUP_EVENTS_IN_VAULT, true);
			xmlStreamWriter = factory.createXMLStreamWriter(fileOutputStream, EventService.ENCODING);
			xmlStreamWriter.writeStartDocument(EventService.ENCODING, "1.0");
			xmlStreamWriter.writeStartElement(EventService.EVENTS_XML_TAG);
		} catch (XMLStreamException xmlStreamException) {
			throw new RuntimeException("ioServices", xmlStreamException);
		} catch (FileNotFoundException file) {
			throw new RuntimeException("File not found", file);
		} catch (IOException e) {
			throw new RuntimeException("IOException", e);
		}
	}

	public void closeXMLFile() {
		try {
			if (xmlStreamWriter != null && !isFirstWrite) {

				xmlStreamWriter.writeEndElement();
				xmlStreamWriter.writeEndDocument();
				xmlStreamWriter.flush();

				xmlStreamWriter.close();
			}
		} catch (XMLStreamException e) {
			throw new RuntimeException("Error while closing the Event writer outputStream. File : " + file.getName(), e);
		} finally {
			if(!isFirstWrite) {
				ioServices.deleteQuietly(file);
				ioServices.closeQuietly(fileOutputStream);
			}
		}
	}

	@Override
	public void close() {
		if(!isFirstWrite) {
			closeXMLFile();
		}
	}
}
