package demo;

import java.util.List;

import com.constellio.app.entities.modules.InstallableModule;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.model.services.factories.ModelLayerFactory;

public interface DemoInitScript {

	public void setup(AppLayerFactory appLayerFactory, ModelLayerFactory modelLayerFactory)
			throws Exception;

	List<InstallableModule> getModules();
}
