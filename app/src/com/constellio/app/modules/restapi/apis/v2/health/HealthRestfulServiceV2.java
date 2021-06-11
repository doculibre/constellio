package com.constellio.app.modules.restapi.apis.v2.health;

import com.constellio.app.modules.restapi.apis.v1.health.HealthRestfulService;
import io.swagger.v3.oas.annotations.tags.Tag;

import javax.ws.rs.Path;

@Path("health")
@Tag(name = "health")
public class HealthRestfulServiceV2 extends HealthRestfulService {
}
