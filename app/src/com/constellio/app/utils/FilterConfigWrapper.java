package com.constellio.app.utils;

import java.util.Enumeration;

import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;

public class FilterConfigWrapper implements FilterConfig {
	
	private FilterConfig wrapped;

	public FilterConfigWrapper(FilterConfig wrapped) {
		this.wrapped = wrapped;
	}

	@Override
	public String getFilterName() {
		return wrapped.getFilterName();
	}

	@Override
	public ServletContext getServletContext() {
		return wrapped.getServletContext();
	}

	@Override
	public String getInitParameter(String name) {
		return wrapped.getInitParameter(name);
	}

	@Override
	public Enumeration<String> getInitParameterNames() {
		return wrapped.getInitParameterNames();
	}
	
}
