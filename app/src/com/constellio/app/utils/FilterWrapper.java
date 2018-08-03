package com.constellio.app.utils;

import javax.servlet.*;
import java.io.IOException;
import java.io.Serializable;

public class FilterWrapper implements Filter, Serializable {

	private Filter wrapped;

	public FilterWrapper(Filter wrapped) {
		super();

		this.wrapped = wrapped;
	}

	@Override
	public void destroy() {
		wrapped.destroy();
	}

	@Override
	public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain)
			throws IOException, ServletException {
		wrapped.doFilter(servletRequest, servletResponse, filterChain);
	}

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
		wrapped.init(filterConfig);
	}

}
