package org.jenkinsci.plugins.database.postgresql;

import hudson.Extension;
import hudson.Util;
import hudson.util.FormValidation;
import hudson.util.Secret;
import java.io.Serializable;
import jenkins.model.Jenkins;
import org.jenkinsci.plugins.database.AbstractRemoteDatabase;
import org.jenkinsci.plugins.database.AbstractRemoteDatabaseDescriptor;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.verb.POST;
import org.postgresql.Driver;

import java.io.IOException;
import java.sql.DriverPropertyInfo;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

/**
 * @author Kohsuke Kawaguchi
 */
public class PostgreSQLDatabase extends AbstractRemoteDatabase implements Serializable {

    private static final long serialVersionUID = 1L;
    
    @DataBoundConstructor
    public PostgreSQLDatabase(String hostname, String database, String username, Secret password, String properties) {
        super(hostname, database, username, password, properties);
    }

    @Override
    protected Class<Driver> getDriverClass() {
        return Driver.class;
    }

    @Override
    protected String getJdbcUrl() {
        return "jdbc:postgresql://" + hostname + '/' + database;
    }

    @Extension
    public static class DescriptorImpl extends AbstractRemoteDatabaseDescriptor {
        @Override
        public String getDisplayName() {
            return "PostgreSQL";
        }

        @POST
        public FormValidation doCheckProperties(@QueryParameter String properties) {
            Jenkins.get().checkPermission(Jenkins.ADMINISTER);
            
            try {
                Set<String> validPropertyNames = new HashSet<String>();
                Properties props = Util.loadProperties(properties);
                for (DriverPropertyInfo p : new Driver().getPropertyInfo("jdbc:postgresql://localhost/dummy", props)) {
                    validPropertyNames.add(p.name);
                }

                for (Map.Entry<Object, Object> e : props.entrySet()) {
                    String key = e.getKey().toString();
                    if (!validPropertyNames.contains(key))
                        return FormValidation.error("Unrecognized property: "+key);
                }
                return FormValidation.ok();
            } catch (Throwable e) {
                return FormValidation.warning(e,"Failed to validate the connection properties");
            }
        }
    }
}
