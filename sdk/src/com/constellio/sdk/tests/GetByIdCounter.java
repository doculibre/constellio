package com.constellio.sdk.tests;

import com.constellio.data.dao.services.factories.DataLayerFactory;
import com.constellio.data.extensions.AfterGetByIdParams;
import com.constellio.data.extensions.BigVaultServerExtension;
import com.constellio.model.services.factories.ModelLayerFactory;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.assertj.core.api.ListAssert;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

public class GetByIdCounter extends BigVaultServerExtension {

	private Function<AfterGetByIdParams, Boolean> filter;
	private List<GetByIdCounterCall> calls = new ArrayList<>();

	public GetByIdCounter(Class<?> occuringFrom) {
		this.filter = (AfterGetByIdParams p) -> {
			for (StackTraceElement stackLine : Thread.currentThread().getStackTrace()) {
				if (stackLine.getClassName().contains(occuringFrom.getClass().getSimpleName())) {
					return true;
				}
			}
			return false;
		};
	}

	public GetByIdCounter(DataLayerFactory dataLayerFactory, Class<?> occuringFrom) {
		this(occuringFrom);
		listening(dataLayerFactory);
	}


	public GetByIdCounter listening(DataLayerFactory dataLayerFactory) {
		dataLayerFactory.getExtensions().getSystemWideExtensions().bigVaultServerExtension.add(this);
		return this;
	}

	public GetByIdCounter listening(ModelLayerFactory modelLayerFactory) {
		return listening(modelLayerFactory.getDataLayerFactory());
	}

	@Override
	public void afterRealtimeGetById(AfterGetByIdParams params) {
		if (filter.apply(params)) {
			calls.add(new GetByIdCounterCall(params.getId(), new Throwable(), params.found()));
		}
	}

	@Override
	public void afterGetById(AfterGetByIdParams params) {
		if (filter.apply(params)) {
			calls.add(new GetByIdCounterCall(params.getId(), new Throwable(), params.found()));
		}
	}

	public int countNewCalls() {
		int callsCount = calls.size();
		calls.clear();
		return callsCount;
	}

	public List<GetByIdCounterCall> newCalls() {
		List<GetByIdCounterCall> newCalls = new ArrayList<>(this.calls);
		calls.clear();
		return newCalls;
	}

	public List<String> newIdCalled() {
		List<String> newCalls = this.calls.stream().map(GetByIdCounterCall::getId).collect(Collectors.toList());
		calls.clear();
		return newCalls;
	}

	public void reset() {
		calls.clear();
	}

	public ListAssert<Object> assertCalledIds() {
		StringBuilder sb = new StringBuilder();

		for (GetByIdCounterCall call : calls) {
			sb.append("Get '" + call.id + "' : " + (call.found ? "found" : "not found") + "\n");
			sb.append(ExceptionUtils.getStackTrace(call.getStackTrace()) + "\n");
		}


		return assertThat(calls).extracting("id").describedAs(sb.toString());
	}

	@AllArgsConstructor
	public static class GetByIdCounterCall {
		@Getter
		String id;
		@Getter
		Throwable stackTrace;
		@Getter
		boolean found;

		@Override
		public String toString() {
			String stack = ExceptionUtils.getStackTrace(stackTrace);

			while (stack.startsWith("java.lang.Throwable")
				   || stack.startsWith("com.constellio.data.extensions.DataLayerSystemExtensions")
				   || stack.startsWith("com.constellio.data.dao.services.bigVault")) {
				stack = StringUtils.substringAfter(stack, "\n");
			}
			return "get '" + id + "' : " + stack + "\n\n";
		}
	}
}
