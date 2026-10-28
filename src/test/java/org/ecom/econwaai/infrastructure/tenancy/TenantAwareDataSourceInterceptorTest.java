package org.ecom.econwaai.infrastructure.tenancy;

import org.ecom.econwaai.common.context.OrgContext;
import org.ecom.econwaai.common.context.OrgContextHolder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class TenantAwareDataSourceInterceptorTest {

    @AfterEach
    void tearDown() {
        OrgContextHolder.clear();
    }

    @Test
    @DisplayName("Should wrap DataSource and execute SET LOCAL session variables from OrgContextHolder")
    void testTenantContextApplicationOnConnection() throws SQLException {
        DataSource mockDataSource = mock(DataSource.class);
        Connection mockConnection = mock(Connection.class);
        Statement mockStatement = mock(Statement.class);

        when(mockDataSource.getConnection()).thenReturn(mockConnection);
        when(mockConnection.createStatement()).thenReturn(mockStatement);

        UUID orgId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        OrgContext context = new OrgContext(orgId, "SUPPLIER", userId, "MANAGER", "test-corr-id");
        OrgContextHolder.set(context);

        TenantAwareDataSourceInterceptor interceptor = new TenantAwareDataSourceInterceptor();
        Object wrapped = interceptor.postProcessAfterInitialization(mockDataSource, "dataSource");

        assertThat(wrapped).isInstanceOf(TenantAwareDataSourceInterceptor.TenantAwareDataSourceWrapper.class);

        DataSource tenantDataSource = (DataSource) wrapped;
        Connection conn = tenantDataSource.getConnection();

        assertThat(conn).isEqualTo(mockConnection);

        // Verify SET LOCAL statements were executed for PostgreSQL RLS scoping
        verify(mockStatement).execute("SET LOCAL app.current_org_id = '" + orgId + "'");
        verify(mockStatement).execute("SET LOCAL app.current_user_id = '" + userId + "'");
        verify(mockStatement).execute("SET LOCAL app.current_org_type = 'SUPPLIER'");
        verify(mockStatement).execute("SET LOCAL app.current_role = 'MANAGER'");
    }
}
