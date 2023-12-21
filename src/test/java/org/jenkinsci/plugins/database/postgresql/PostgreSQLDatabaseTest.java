package org.jenkinsci.plugins.database.postgresql;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;

import hudson.util.FormValidation;
import hudson.util.Secret;
import java.io.IOException;
import org.jenkinsci.plugins.database.GlobalDatabaseConfiguration;
import org.junit.jupiter.api.Test;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.junit.jupiter.WithJenkins;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@WithJenkins
@Testcontainers(disabledWithoutDocker = true)
public class PostgreSQLDatabaseTest {

    public static final String TEST_IMAGE = "postgres:16.1";

    @Container
    private static final PostgreSQLContainer<?> postgresql = new PostgreSQLContainer<>(TEST_IMAGE);

    public void setConfiguration() throws IOException {
        PostgreSQLDatabase database = new PostgreSQLDatabase(
                postgresql.getHost() + ":" + postgresql.getMappedPort(5432),
                postgresql.getDatabaseName(),
                postgresql.getUsername(),
                Secret.fromString(postgresql.getPassword()),
                null);
        database.setValidationQuery("SELECT 1");
        GlobalDatabaseConfiguration.get().setDatabase(database);
    }

    @Test
    public void shouldSetConfiguration(JenkinsRule j) throws IOException {
        setConfiguration();
        assertThat(GlobalDatabaseConfiguration.get().getDatabase(), instanceOf(PostgreSQLDatabase.class));
    }

    @Test
    public void shouldConstructDatabase(JenkinsRule j) throws IOException {
        PostgreSQLDatabase database = new PostgreSQLDatabase(
                postgresql.getHost() + ":" + postgresql.getMappedPort(5432),
                postgresql.getDatabaseName(),
                postgresql.getUsername(),
                Secret.fromString(postgresql.getPassword()),
                null);
        assertThat(database.getDescriptor().getDisplayName(), is("PostgreSQL"));
        assertThat(
                database.getJdbcUrl(),
                is("jdbc:postgresql://" + postgresql.getHost() + ":" + postgresql.getMappedPort(5432) + "/"
                        + postgresql.getDatabaseName() + ""));
        assertThat(database.getDriverClass(), is(org.postgresql.Driver.class));
    }

    @Test
    public void shouldCheckProperties(JenkinsRule j) throws IOException {
        setConfiguration();
        PostgreSQLDatabase database = new PostgreSQLDatabase(
                postgresql.getHost() + ":" + postgresql.getMappedPort(5432),
                postgresql.getDatabaseName(),
                postgresql.getUsername(),
                Secret.fromString(postgresql.getPassword()),
                null);
        assertThat(((PostgreSQLDatabase.DescriptorImpl)database.getDescriptor()).doCheckProperties("key=value").getMessage(), is("Unrecognized property: key"));
        assertThat(((PostgreSQLDatabase.DescriptorImpl)database.getDescriptor()).doCheckProperties("ssl=true"), is(FormValidation.ok()));
        
    }

}
