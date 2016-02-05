package com.constellio.model.services.search;

import java.io.Reader;
import java.io.Writer;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.namespace.QName;
import javax.xml.transform.stream.StreamSource;

import com.constellio.data.io.concurrent.data.DataWrapper;
import com.constellio.data.io.concurrent.data.ReaderWriterDataWrapper;

public class ElevationsView extends ReaderWriterDataWrapper<Elevations> {
	private Elevations elevations;

	@Override
	protected void init(Reader reader) {
		this.elevations = makeElevations(reader);
	}

	private Elevations makeElevations(Reader reader) {
		try {
			JAXBContext jc = JAXBContext.newInstance( Elevations.class );
			Unmarshaller unmarshaller = jc.createUnmarshaller();
			Elevations elevations = unmarshaller.unmarshal( new StreamSource(reader), Elevations.class).getValue();
			return elevations;
		} catch (JAXBException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	protected void toBytes(Writer writer) {
		JAXBContext jc;
		try {
			jc = JAXBContext.newInstance(Elevations.class);
			Marshaller marshaller = jc.createMarshaller();
			marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
			JAXBElement<Elevations> jaxbElement = new JAXBElement<Elevations>(new QName("elevate"), Elevations.class, elevations);
			marshaller.marshal(jaxbElement, writer);
		} catch (JAXBException e) {
			throw new RuntimeException(e);
		}
	
	}

	@Override
	public Elevations getData() {
		
		return makeElevations(getReader(toBytes()));
	}

	@Override
	public DataWrapper<Elevations> setData(Elevations data) {
		elevations = data;
		return this;
	}
	
	

}
