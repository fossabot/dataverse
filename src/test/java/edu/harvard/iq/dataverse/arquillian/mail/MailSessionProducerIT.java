package edu.harvard.iq.dataverse.arquillian.mail;

import edu.harvard.iq.dataverse.arquillian.GdccBaseContainer;
import edu.harvard.iq.dataverse.settings.JvmSettings;
import edu.harvard.iq.dataverse.util.MailSessionFactory;
import edu.harvard.iq.dataverse.util.testing.IntegrationTesting;
import edu.harvard.iq.dataverse.util.testing.JvmSetting;
import edu.harvard.iq.dataverse.util.testing.JvmSettingBroker;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.testcontainers.junit.jupiter.Container;

import javax.inject.Inject;
import javax.mail.Session;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/*

TODO: Basically, this is a good first effort. Some remaining things need to be cleaned out:
      1. The IT class should not contain the base container to avoid the necessity of adding the TC to the deployment
      2. Instead, the container management for the base container should live inside the @IntegrationTesting extension.
         Lifecycle of the server container is controlled in there, which already relieves us of the @Testcontainers annotation.
      3. It might be necessary to check in the extension if we are running inside Arquillian deployment, as the
         test class is copied, which means it could still try to start containers inside the server. The Arquillian
         Junit5 extension seems to have some internal mechanism for that?
      4. To determine whether what happens with extension in the deployment, log outputs might be helpful
      5. The server log needs some better and nicer handling than the hacky "@AfterAll" which will not execute
         when a deployment error occurs.
      6. The broker needs to be adapted to the new lifecycle control of the container in the extension.
         Maybe the extension needs to share the broker via the extension store to make it reachable for
         the RemoteJvmSettings extension.
      7. Probably more stuff to think about...

*/



@IntegrationTesting
@JvmSetting(key = JvmSettings.MAIL_PROTOCOL, value = "test")
public class MailSessionProducerIT {
    
    private final static Logger logger = Logger.getLogger(MailSessionProducerIT.class.getName());
    
    @Container
    static GdccBaseContainer appserver = new GdccBaseContainer();//.withLogConsumer(consumer);
    
    @Deployment
    public static WebArchive createDeployment() throws Exception {
        logger.info("Creating deployment");
        
        WebArchive war = ShrinkWrap.create(WebArchive.class);
       
        /*
        Files.list(Path.of("src/main/webapp/WEB-INF"))
            .filter(f -> ! f.endsWith("faces-config.xml"))
            .filter(f -> ! f.endsWith("web.xml"))
            .forEach(
            path -> war.addAsWebInfResource(path.toFile())
        );
        */
        
        war.addAsWebInfResource(new StringAsset("<beans/>"), "beans.xml");
        
        war.addClasses(
            MailSessionFactory.class,
            JvmSettings.class,
            GdccBaseContainer.class,
            JvmSettingBroker.class
        );
        
        war.addAsLibraries(Maven.resolver().resolve("org.testcontainers:testcontainers:1.17.6").withTransitivity().asFile());
        war.addAsLibraries(Maven.resolver().resolve("org.slf4j:slf4j-jdk14:1.7.36").withTransitivity().asFile());
        
        return war;
    }
    
    /*
    @Order(2)
    @Test
    @RunAsClient
    void verifyDeployment() {
        System.out.println(appserver.getLogs());
    }
    */
    
    @AfterAll
    static void showlog() {
        System.out.println(appserver.getLogs());
    }
    
    @Inject
    private Session session;
    
    @Test
    void run() {
        
        logger.warning("TEST");
        
        assertNotNull(session);
        assertTrue(session.getProperties().isEmpty());
        assertTrue(session.getDebug(), "debug not enabled");
    }
    
    
    /*
    @Test
    @JvmSetting(key = JvmSettings.MAIL_HOST, value = "localhost")
    @JvmSetting(key = JvmSettings.MAIL_FROM, value = "Your Mama <mama@example.org>")
    void createNewSession() {
        var msp = new MailSessionProducer();
        Session session = msp.getSystemMailSession();
        
        assertNotNull(session);
    }
    
    @Test
    @JvmSetting(key = JvmSettings.MAIL_HOST, value = "localhost")
    @JvmSetting(key = JvmSettings.MAIL_FROM, value = "Your Mama <mama@example.org>")
    void createNewSessionFails() throws Exception {
        var msp = new MailSessionProducer();
        Session session = msp.getSystemMailSession();
        
        MimeMessage msg = new MimeMessage(session);
        msg.setText("");
        session.getTransport().sendMessage(msg, InternetAddress.parse("root@localhost"));
    }
    */
    

}
