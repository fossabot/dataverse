package edu.harvard.iq.dataverse.search.schema;

import edu.harvard.iq.dataverse.DatasetFieldServiceBean;
import edu.harvard.iq.dataverse.DatasetFieldType;
import edu.harvard.iq.dataverse.search.SearchFields;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.Initialized;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.inject.Named;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

@Named
@ApplicationScoped
public class SolrSchemaCache {

    /** The idea:
        hold the schema as in-memory construct, retrieved from the datasetfieldtype service
        and the "static" fields from SearchFields
    */
    private ConcurrentHashMap<String, SolrStaticField> staticFields;
    private ConcurrentHashMap<String, SolrDynamicField> dynamicFields;
    // Maybe replace with a map used as a set to ensure uniqueness.
    private CopyOnWriteArrayList<SolrCopyField> copyFields;

    @Inject
    DatasetFieldServiceBean datasetFieldService;
    
    public static final List<String> IGNORED_DYNAMIC_FIELDS =
        List.of("*_p", "*_srpt", "*_dpf", "*_dpi", "*_dps", "attr_*", "*_ws", "*_txt_en", "*_txt_en_split",
            "*_txt_en_split_tight", "*_txt_rev", "*_phon_en", "*_s_lower", "*_descendent_path", "*_ancestor_path",
            "*_point", "*_txt_ar", "*_txt_bg", "*_txt_ca", "*_txt_cjk", "*_txt_cz", "*_txt_da", "*_txt_de", "*_txt_el",
            "*_txt_es", "*_txt_eu", "*_txt_fa", "*_txt_fi", "*_txt_fr", "*_txt_ga", "*_txt_gl", "*_txt_hi", "*_txt_hu",
            "*_txt_hy", "*_txt_id", "*_txt_it", "*_txt_ja", "*_txt_lv", "*_txt_nl", "*_txt_no", "*_txt_pt", "*_txt_ro",
            "*_txt_ru", "*_txt_sv", "*_txt_th", "*_txt_tr");

    public static final SolrStaticField FULLTEXT = new SolrStaticField(SearchFields.FULL_TEXT,
                                                                       SolrFieldType.TEXT_GENERAL,
                                                                       Map.of(SolrFieldProperty.INDEXED, "true",
                                                                              SolrFieldProperty.STORED, "false",
                                                                              SolrFieldProperty.MULTIVALUED, "true"));
    
    /**
     * Executing during startup (after the application context is ready for us)
     *
     * @param init Can be ignored, is not used but necessary for the event catching
     */
    public void onStart(@Observes @Initialized(ApplicationScoped.class) Object init) {
        loadSchema();
    }
    
    public void loadSchema() {
        // TODO: get static fields {@link SearchFields}
        
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
                    ? SolrBaseField.StdConf.STORED_INDEXED_MULTIVALUED.getConfig()
                    : SolrBaseField.StdConf.STORED_INDEXED.getConfig()
            );
        else
            throw new RuntimeException(String.format("Solr Type '%s' not supported yet.", type.getName()));
    }

    SolrCopyField buildFullTextCopyField(SolrStaticField source) {
        return new SolrCopyField(source, FULLTEXT);
    }
}
