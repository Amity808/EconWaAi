package org.ecom.econwaai.infrastructure.tenancy;

import org.ecom.econwaai.common.context.OrgContext;
import org.ecom.econwaai.common.context.OrgContextHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.jdbc.datasource.DelegatingDataSource;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Interceptor that wraps the Spring HikariCP DataSource.
 * Upon connection checkout, it parameterizes PostgreSQL session variables:
 * - app.current_org_id
 * - app.current_user_id
 * - app.current_org_type
 * - app.current_role
 * 
 * Enforces database kernel-level Row-Level Security (RLS) policies.
 */
@Component
public class TenantAwareDataSourceInterceptor implements BeanPostProcessor {

    private static final Logger log = LoggerFactory.getLogger(TenantAwareDataSourceInterceptor.class);

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        if (bean instanceof DataSource dataSource && !(bean instanceof TenantAwareDataSourceWrapper)) {
            log.info("Wrapping DataSource [{}] with TenantAwareDataSourceWrapper for RLS enforcement", beanName);
            return new TenantAwareDataSourceWrapper(dataSource);
        }
        return bean;
    }

    public static class TenantAwareDataSourceWrapper extends DelegatingDataSource {

        public TenantAwareDataSourceWrapper(DataSource targetDataSource) {
            super(targetDataSource);
        }

        @Override
        public Connection getConnection() throws SQLException {
            Connection connection = super.getConnection();
            applyTenantContext(connection);
            return connection;
        }

        @Override
        public Connection getConnection(String username, String password) throws SQLException {
            Connection connection = super.getConnection(username, password);
            applyTenantContext(connection);
            return connection;
        }

        private void applyTenantContext(Connection connection) {
            OrgContextHolder.get().ifPresent(ctx -> {
                try (Statement stmt = connection.createStatement()) {
                    if (ctx.orgId() != null) {
                        stmt.execute("SET LOCAL app.current_org_id = '" + ctx.orgId() + "'");
                    }
                    if (ctx.userId() != null) {
                        stmt.execute("SET LOCAL app.current_user_id = '" + ctx.userId() + "'");
                    }
                    if (ctx.orgType() != null) {
                        stmt.execute("SET LOCAL app.current_org_type = '" + sanitize(ctx.orgType()) + "'");
                    }
                    if (ctx.role() != null) {
                        stmt.execute("SET LOCAL app.current_role = '" + sanitize(ctx.role()) + "'");
                    }
                } catch (SQLException e) {
                    // Non-fatal: in H2 test mode or non-transactional contexts SET LOCAL may be unsupported
                    log.trace("Tenant session variable application skipped or unsupported: {}", e.getMessage());
                }
            });
        }

        private String sanitize(String input) {
            return input != null ? input.replaceAll("[^a-zA-Z0-9_-]", "") : "";
        }
    }
}
