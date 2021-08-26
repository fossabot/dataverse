package edu.harvard.iq.dataverse.search.schema;

import edu.harvard.iq.dataverse.search.SolrClientService;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.request.schema.SchemaRequest;
import org.apache.solr.client.solrj.response.schema.SchemaRepresentation;
import org.apache.solr.client.solrj.response.schema.SchemaResponse;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.Initialized;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.inject.Named;
import java.io.IOException;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * A bean for different check and validation tasks between Solr and the Database.
 *
 * - Can validate schema in Solr against static fields and fields from metadata blocks
 * - Some methods from IndexServiceBean handling orphans can be moved here for more consistency.
 *
 */

@Named
@ApplicationScoped
public class IndexCheckerBean {
    
    @Inject
    private SolrClientService clientService;
    @Inject
    private SolrSchemaCache schemaCache;
    
    private SolrClient solrClient;
    
    /**
     * Obligatory empty constructor because this is a managed bean.
     */
    IndexCheckerBean() {}
    
    /**
     * Used for testing purposes to inject dependencies without CDI present
     * @param solrClient
     */
    IndexCheckerBean(SolrClient solrClient) {
        this.solrClient = solrClient;
    }
    
    /**
     * Executing during startup (after the application context is ready for us)
     *
     * @param init Can be ignored, is not used but necessary for the event catching
     */
    public void onStart(@Observes @Initialized(ApplicationScoped.class) Object init) {
        solrClient = clientService.getSolrClient();
    }
    
    /**
     * Retrieve the Solr Schema in a computable fashion
     * @return The schema, wrapped in Optional to avoid things go bad
     */
    public Optional<SchemaRepresentation> retrieveSolrSchema() {
        try {
            SchemaRequest request = new SchemaRequest();
            SchemaResponse response = request.process(solrClient);
            return Optional.of(response.getSchemaRepresentation());
        } catch (SolrServerException e) {
            // TODO
        } catch (IOException e) {
            // TODO
        }
        return Optional.empty();
    }
    
    public void validateSchema() {
        Optional<SchemaRepresentation> oSchema = retrieveSolrSchema();
        if(oSchema.isPresent())
            // TODO: response...
            validateSchema(oSchema.get());
        else
            // TODO
            return;
    }
    
    public void validateSchema(SchemaRepresentation schema) {
        List<Map<String,Object>> rawCopyFields = schema.getCopyFields();
        
        // TODO: Missing handling the cases where a target type is not found (logging warning? failing validation?)
        //       Or when some property could not be found / is unknown...
        List<SolrStaticField> staticFields = schema.getFields().stream()
                                                .map(f -> SolrStaticField.build(f))
                                                .collect(Collectors.toList());
        List<SolrDynamicField> dynamicFields = schema.getDynamicFields().stream()
                                                .filter(field -> !SolrSchemaCache.IGNORED_DYNAMIC_FIELDS.contains(field.get(SolrFieldProperty.NAME.getKey())))
                                                .map(field -> SolrDynamicField.build(field))
                                                .collect(Collectors.toList());
        
        System.out.println(staticFields);
        System.out.println(dynamicFields);
        
        // TODO: Missing the case what happens when no target or source field can be found.
        //       Would be good to have a warning logged plus continued parsing to find all errors...
        List<SolrCopyField> copyFields = rawCopyFields.stream()
            .map(field -> new SolrCopyField(
                staticFields.stream().filter(f -> f.getName().equals(field.get(SolrFieldProperty.SOURCE.getKey()))).findFirst().get(),
                staticFields.stream().filter(f -> f.getName().equals(field.get(SolrFieldProperty.DEST.getKey()))).findFirst().get()
            ))
            .collect(Collectors.toUnmodifiableList());
        
        System.out.println(copyFields);
    }
    
    
    
}
