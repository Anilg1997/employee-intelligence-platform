package com.employeeintelligence.infrastructure;

import com.employeeintelligence.api.graphql.DashboardGraphQlController;
import com.employeeintelligence.api.dto.DashboardResponse;
import com.employeeintelligence.api.service.DashboardService;
import graphql.GraphQL;
import graphql.schema.idl.SchemaParser;
import graphql.schema.idl.SchemaGenerator;
import graphql.schema.idl.RuntimeWiring;
import org.junit.jupiter.api.Test;
import java.io.InputStreamReader;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class DashboardGraphQlProofTest {
    @Test
    void selectedFieldsAndEmptySnapshotResolveAgainstTheRealSchema() {
        var service = mock(DashboardService.class);
        when(service.getDashboardSummary()).thenReturn(new DashboardResponse(0, Map.of(), 0, 0, null, null, null));
        var controller = new DashboardGraphQlController(service);
        var schema = new SchemaParser().parse(new InputStreamReader(getClass().getResourceAsStream("/graphql/dashboard.graphqls")));
        var wiring = RuntimeWiring.newRuntimeWiring().type("Query", type -> type.dataFetcher("dashboard", env -> controller.dashboard())).build();
        var graphQl = GraphQL.newGraphQL(new SchemaGenerator().makeExecutableSchema(schema, wiring)).build();
        var result = graphQl.execute("{ dashboard { totalEmployees attritionRate departments { name count share } } }");
        assertTrue(result.getErrors().isEmpty(), result.getErrors().toString());
        Map<String, Object> data = result.getData();
        var dashboard = (Map<?, ?>) data.get("dashboard");
        assertEquals(0, dashboard.get("totalEmployees"));
        assertNull(dashboard.get("attritionRate"));
        assertEquals(java.util.List.of(), dashboard.get("departments"));
        assertFalse(graphQl.execute("{ dashboard { unknownField } }").getErrors().isEmpty());
    }
}
