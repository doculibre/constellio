package com.constellio.app.servlet;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;

import org.apache.chemistry.opencmis.server.impl.atompub.CmisAtomPubServlet;

public class ConstellioCmisAtomPubServlet extends CmisAtomPubServlet {

	@Override
	public void init(ServletConfig config)
			throws ServletException {
		super.init(config);
	}
}
