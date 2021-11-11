package edu.harvard.iq.dataverse.api;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.testcontainers.containers.BindMode;
import org.testcontainers.containers.Container;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.SolrContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.utility.DockerImageName;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Logger;

/**
 * Abstract, non-instiatable base class for Testcontainer usage.
 * TC usage is optional and needs to be enabled programmatically or via system property!
 *
 * All test classes reusing containers need to extend this base class, activating the hooks
 * to enable container lifecycle control.
 */
public abstract class TestContainersBase {
    
    private static final Logger logger = Logger.getLogger(TestContainersBase.class.getCanonicalName());
    private static final boolean testContainersEnabled = Boolean.getBoolean("tc.enabled");
    
    private static final String pgsqlVersion = System.getProperty("postgresql.server.version");
    private static final String pgsqlNetworkAlias = "postgresql";
    
    private static final String solrImage = System.getProperty("solr.image");
    private static final String solrNetworkAlias = "solr";
    
    private static final String appImage = System.getProperty("app.image");
    private static final Integer appWaitTimeout = Integer.valueOf(System.getProperty("app.wait", "90000"));
    private static final String appNetworkAlias = "dataverse";
    private static final String appEnvFile = System.getProperty("app.envfile");
    private static final String appDirSecrets = System.getProperty("app.dir.secrets");
    private static final String appDirData = System.getProperty("app.dir.data");
    private static final String appMountSecrets = System.getProperty("app.mount.secrets", "/secrets");
    private static final String appMountData = System.getProperty("app.mount.data", "/data");
    
    private static Network network;
    private static PostgreSQLContainer<?> pgsql;
    private static SolrContainer solr;
    private static GenericContainer<?> appserver;
    private static boolean isBootstrapped = false;
    
    public static boolean isEnabled() {
        return testContainersEnabled;
    }
    
    /**
     * Starting phase of custom container lifecycle to enable reusing containers for multiple tests.
     *
     * For Maven based tests, TC testing may be enabled via a system property 'tc.enabled'.
     * For programmatic enabling, see {@link #setup(boolean)}.
     */
    @BeforeAll
    public static void setup() {
        setup(false);
    }
    
    /**
     * Starting phase of custom container lifecycle to enable reusing containers for multiple tests.
     * @param enableTestContainers enforce Testcontainers usage
     */
    public static void setup(boolean enableTestContainers) {
        logger.info("TestContainersBase usage " + (testContainersEnabled ? "enabled" : "disabled"));
        if (testContainersEnabled || enableTestContainers) {
            try {
                network = Network.newNetwork();
                setupPostgres();
                setupSolr();
                setupAppserver();
                bootstrapAppserver();
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
                teardown();
            }
        }
    }
    
    /**
     * Stopping phase of custom container lifecycle to enable reusing containers for multiple tests.
     * Will stop any containers that have been created before.
     */
    @AfterAll
    public static void teardown() {
        if (pgsql != null) {
            pgsql.stop();
        }
        if (solr != null) {
            solr.stop();
        }
        if (appserver != null) {
            appserver.stop();
        }
    }
    
    /**
     * Retrieve HTTP URL to application for REST API tests
     */
    public static Optional<String> getAppUrl() {
        if (pgsql != null && solr != null && appserver != null && pgsql.isRunning() && solr.isRunning() && appserver.isRunning()) {
            String url = "http://"+appserver.getContainerIpAddress()+":"+appserver.getFirstMappedPort();
            logger.info("TC based app serving at "+url);
            return Optional.of(url);
        }
        return Optional.empty();
    }
    
    private static void setupPostgres() {
        if (pgsql == null) {
            logger.fine("TC using postgres:" + pgsqlVersion + " image");
            pgsql = new PostgreSQLContainer(DockerImageName.parse("postgres:" + pgsqlVersion));
            pgsql.withNetwork(network).withNetworkAliases(pgsqlNetworkAlias);
    
            // Set options
            pgsql.withReuse(true); // Do not delete after each test
    
            // Start the container
            pgsql.start();
        }
    }
    
    private static void setupSolr() throws IOException, InterruptedException {
        if (solr == null) {
            logger.fine("TC using Solr image " + solrImage);
    
            // Create container from pre-built image (either remote or Maven Docker build in package phase)
            solr = new SolrContainer(DockerImageName.parse(solrImage).asCompatibleSubstituteFor("solr"))
                .withNetwork(network)
                .withNetworkAliases(solrNetworkAlias)
                .withZookeeper(false) // Use Standalone, not SolrCloud mode!
                .withReuse(true); // Do not delete after each test
            
            // Start the container
            solr.start();
    
            // Create our core
            // TODO: make the collection name and configset more dynamic / create source of truth
            Container.ExecResult res = solr.execInContainer("solr", "create_core", "-c", "collection1", "-d", "dataverse");
            logger.fine(res.toString());
        }
    }
    
    private static void setupAppserver() throws IOException, InterruptedException {
        if (appserver == null) {
            logger.fine("TC using Dataverse image " + appImage);
            
            // Setup environment variables
            Map<String, String> env = new LinkedHashMap<>();
            // Of course we always activate this when running from Testcontainers...
            env.put("ENABLE_INTEGRATION_TESTS", "1");
    
            // Let's ensure Postgres is up and running
            Assertions.assertTrue(pgsql.isRunning());
            // Add the database container access details via MPCONFIG
            env.put("DATAVERSE_DB_HOST", pgsqlNetworkAlias);
            env.put("DATAVERSE_DB_USER", pgsql.getUsername());
            env.put("DATAVERSE_DB_PASSWORD", pgsql.getPassword());
            env.put("DATAVERSE_DB_NAME", pgsql.getDatabaseName());
            env.put("DATAVERSE_DB_PORT", "5432");
            // Add Solr connection details
            env.put("SOLR_K8S_HOST", solrNetworkAlias);
            // Add entries from env file
            Files.readAllLines(Path.of(appEnvFile)).stream()
                // filter out comment lines
                .filter(line -> ! line.startsWith("#"))
                // filter out lines with no split item
                .filter(line -> ! line.contains("="))
                // split and put into env map
                .map(line -> line.split("=", 2))
                .forEach(array -> env.put(array[0], array[1]));
            
            // Create Dataverse container
            appserver = new GenericContainer<>(DockerImageName.parse(appImage))
                // expose to host on random port targeting port 8080 in container
                .withExposedPorts(8080)
                
                // add to network to make inter-container communication possible
                .withNetwork(network)
                .withNetworkAliases(appNetworkAlias)
                
                // set waiting strategy to analyse logs
                .waitingFor(
                    Wait.forLogMessage(".*dataverse was successfully deployed.*", 1)
                        .withStartupTimeout(Duration.ofMillis(appWaitTimeout))
                )
                
                // mount temporary file systems to relevant places
                .withTmpFs(Map.of(
                    "/tmp", "rw",
                    "/opt/dataverse/appserver/glassfish/domains/domain1/generated/jsp/dataverse", "rw,mode=777,uid=1000,gid=1000,size=200M",
                    appMountData, "rw,uid=1000,gid=1000,mode=700,size=200M"
                ))
                
                // mount bind mounts from host for secrets & data
                .withFileSystemBind(appDirSecrets, appMountSecrets, BindMode.READ_ONLY)
                //.withFileSystemBind(appDirData, appMountData, BindMode.READ_WRITE)
                
                // send in the collected environment variables
                .withEnv(env)
                // enable reuse after each test
                .withReuse(true);
            
            // Start the container
            appserver.start();
        }
    }
    
    private static void bootstrapAppserver() throws IOException, InterruptedException {
        if ( !isBootstrapped ) {
            // Assert we're up and running
            Assertions.assertNotNull(pgsql, "Postgres not initialized?");
            Assertions.assertNotNull(solr, "Solr not initialized?");
            Assertions.assertNotNull(appserver, "Appserver not initialized?");
            Assertions.assertTrue(pgsql.isRunning(), "Postgres not running?");
            Assertions.assertTrue(solr.isRunning(), "Solr not running?");
            Assertions.assertTrue(appserver.isRunning(), "Appserver not running?");
    
            // Execute bootstrapping
            appserver.execInContainer("/opt/payara/scripts/bootstrap-job.sh");
            
            isBootstrapped = true;
        }
    }
}
