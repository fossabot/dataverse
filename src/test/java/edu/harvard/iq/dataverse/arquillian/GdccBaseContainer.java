package edu.harvard.iq.dataverse.arquillian;

import com.github.dockerjava.api.command.InspectContainerResponse;
import edu.harvard.iq.dataverse.util.testing.JvmSettingBroker;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.utility.DockerImageName;
import org.testcontainers.utility.MountableFile;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;

public class GdccBaseContainer extends GenericContainer<GdccBaseContainer> implements JvmSettingBroker {
    
    private static final Logger log = Logger.getLogger(GdccBaseContainer.class.getCanonicalName());
    
    private static final DockerImageName DEFAULT_IMAGE_NAME = DockerImageName.parse("gdcc/base:unstable");
    public static final int HTTP_PORT = 8080;
    public static final int ADMIN_PORT = 4848;
    public static final String ADMIN_USER = "admin";
    public static final String ADMIN_PASS = "admin";
    
    /* These vars are the same as defined within the base image as ENV vars and documented in the container guide. */
    public static final String CONTAINER_SECRETS_DIR = "/secrets/";
    public static final String CONTAINER_STORAGE_DIR = "/dv/";
    
    public GdccBaseContainer() {
        this(DEFAULT_IMAGE_NAME);
    }
    
    public GdccBaseContainer(final DockerImageName dockerImageName) {
        super(dockerImageName);
        dockerImageName.assertCompatibleWith(DEFAULT_IMAGE_NAME);
        withExposedPorts(HTTP_PORT, ADMIN_PORT);
    }
    
    @Override
    protected void containerIsStarting(InspectContainerResponse containerInfo) {
        super.containerIsStarting(containerInfo);
        
        // Set the container address and mapped host ports here, to be picked up by Arquillian container definition
        System.setProperty("tc.payara.host", this.getHost());
        System.setProperty("tc.payara.admin.port", this.getMappedPort(ADMIN_PORT)+"");
        System.setProperty("tc.payara.http.port", this.getMappedPort(HTTP_PORT)+"");
        
        //Transfer admin user and password, too
        System.setProperty("tc.payara.admin.user", ADMIN_USER);
        System.setProperty("tc.payara.admin.pass", ADMIN_PASS);
    }
    
    private static void deleteRecursively(Path path) throws IOException {
        Files.walk(path)
            .sorted(Comparator.reverseOrder())
            .map(Path::toFile)
            .forEach(File::delete);
    }
    
    @Override
    public String getJvmSetting(String key) throws IOException {
        // TODO
        return null;
    }
    
    @Override
    public void setJvmSetting(String key, String value) throws IOException {
        Path tmpFile = Files.createTempFile("setting", UUID.randomUUID().toString());
        Files.write(tmpFile, List.of(value), StandardCharsets.UTF_8);
        this.copyFileToContainer(MountableFile.forHostPath(tmpFile), CONTAINER_SECRETS_DIR + key);
    }
    
    @Override
    public String deleteJvmSetting(String key) throws IOException {
        return null;
    }
}
