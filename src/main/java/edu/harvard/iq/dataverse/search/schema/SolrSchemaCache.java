package edu.harvard.iq.dataverse.search.schema;

import edu.harvard.iq.dataverse.DatasetFieldServiceBean;
import edu.harvard.iq.dataverse.DatasetFieldType;
import edu.harvard.iq.dataverse.search.SearchFields;

import javax.annotation.PostConstruct;
import javax.ejb.ConcurrencyManagement;
import javax.ejb.ConcurrencyManagementType;
import javax.ejb.EJB;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

@Singleton
@Startup
@ConcurrencyManagement(ConcurrencyManagementType.BEAN)
public class SolrSchemaCache {

    /** The idea:
        hold the schema as in-memory construct, retrieved from the datasetfieldtype service
        and the "static" fields from SearchFields
    */
    private ConcurrentHashMap<String, SolrStaticField> staticFields;
    private ConcurrentHashMap<String, SolrDynamicField> dynamicFields;
    // Maybe replace with a map used as a set to ensure uniqueness.
    private CopyOnWriteArrayList<SolrCopyField> copyFields;

    @EJB
    DatasetFieldServiceBean datasetFieldService;

    public final SolrStaticField fulltext = new SolrStaticField(SearchFields.FULL_TEXT,
                                                                SolrFieldType.TEXT_GENERAL,
                                                                Map.of(SolrFieldProperty.INDEXED, "true",
                                                                    SolrFieldProperty.STORED, "false",
                                                                    SolrFieldProperty.MULTIVALUED, "true"));

    @PostConstruct
    public void loadSchema() {
        // get static fields {@link SearchFields}
        // get schema fields (from database)
        List<DatasetFieldType> mdbFields = datasetFieldService.findAllOrderedByName();
        // convert from database model to solr model
        List<SolrStaticField> mdbSolrFields = mdbFields.stream()
                                              .map(datasetFieldType -> buildStaticFieldFromMDB(datasetFieldType))
                                              .collect(Collectors.toList());
        // store in schema cache
        mdbSolrFields.stream().map(solrStaticField -> staticFields.putIfAbsent(solrStaticField.getName(), solrStaticField));
        // create <copyFields> from metadata block fields
        List<SolrCopyField> mdbSolrCopyFields = mdbSolrFields.stream()
                                                .map(solrStaticField -> buildFullTextCopyField(solrStaticField))
                                                .collect(Collectors.toList());
        // store in schema cache
        copyFields.addAllAbsent(mdbSolrCopyFields);
    }

    SolrStaticField buildStaticFieldFromMDB(DatasetFieldType dft) {
        SolrFieldType type = dft.getFieldType().getSolrType();

        if (SolrFieldType.TEXT_EN.equals(type))
            return new SolrStaticField(
                dft.getName(),
                dft.getFieldType().getSolrType(),
                dft.isAllowMultiples()
                    ? SolrField.StdConf.STORED_INDEXED_MULTIVALUED.getConfig()
                    : SolrField.StdConf.STORED_INDEXED.getConfig()
            );
        else
            throw new RuntimeException(String.format("Solr Type '%s' not supported yet.", type.getName()));
    }

    SolrCopyField buildFullTextCopyField(SolrStaticField source) {
        return new SolrCopyField(source, fulltext);
    }
}
