package edu.harvard.iq.dataverse.search.schema;

import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.impl.Http2SolrClient;
import org.apache.solr.client.solrj.request.schema.SchemaRequest;
import org.apache.solr.client.solrj.response.schema.SchemaRepresentation;
import org.apache.solr.client.solrj.response.schema.SchemaResponse;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.SolrContainer;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;


import static org.junit.jupiter.api.Assertions.*;

@Tag("testcontainers")
@Testcontainers
class IndexCheckerBeanIT {
    
    private final static DockerImageName SOLR_IMAGE = DockerImageName.parse("ghcr.io/gdcc/solr-k8s:nightly").asCompatibleSubstituteFor("solr");
    private static SolrContainer container;
    private static SolrClient solrClient;
    
    @BeforeAll
    public static void setUp() throws Exception {
        // Create the solr container.
        container = new SolrContainer(SOLR_IMAGE).withZookeeper(false);
        // Start the container. This step might take some time...
        container.start();
        // Create the dataverse flavored collection
        container.execInContainer("solr", "create_core", "-c", "collection1", "-d", "dataverse");
        
        // Create the actual client
        solrClient = new Http2SolrClient.Builder("http://" + container.getContainerIpAddress() + ":" + container.getSolrPort() + "/solr/collection1").build();
    }
    
    @AfterAll
    public static void tearDown() {
        // Stop the container.
        container.stop();
    }
    
    @Test
    public void testGetResult() throws Exception {
        SchemaRequest request = new SchemaRequest();
        SchemaResponse response = request.process(solrClient);
        assertNotNull(response);
        
        SchemaRepresentation representation = response.getSchemaRepresentation();
        assertEquals(1.7f, representation.getVersion());
        
        IndexCheckerBean idx = new IndexCheckerBean();
        idx.validateSchema(response.getSchemaRepresentation());
        
    }

}