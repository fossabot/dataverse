package edu.harvard.iq.dataverse.search.schema;

import java.util.Map;

public class SolrStaticField extends SolrFieldBase {
    
    public enum StdConf {
        S(Map.of(SolrFieldProperty.STORED, "true", SolrFieldProperty.INDEXED, "false", SolrFieldProperty.MULTIVALUED, "false")),
        SI(Map.of(SolrFieldProperty.STORED, "true", SolrFieldProperty.INDEXED, "true", SolrFieldProperty.MULTIVALUED, "false")),
        SIM(Map.of(SolrFieldProperty.STORED, "true", SolrFieldProperty.INDEXED, "true", SolrFieldProperty.MULTIVALUED, "true"));
    
        private Map<SolrFieldProperty, String> config;
    
        StdConf(Map<SolrFieldProperty, String> config) {
            this.config = config;
        }
    
        public Map<SolrFieldProperty, String> getConfig() {
            return config;
        }
    }
    
    /**
     * Create a static Solr field, requiring a name and a type.
     * Will use a standard field config of "stored + indexed" ({@link StdConf#SI}).
     *
     * @param nameProperty Value for XML property "name" in schema
     * @param type A {@link SolrFieldType}, whose name will be use for the XML schema
     * @throws IllegalArgumentException When nameProperty does not follow Solr naming convention.
     * @see #isValidName(String)
     */
    public SolrStaticField(String nameProperty, SolrFieldType type) {
        this(nameProperty, type, StdConf.SI.getConfig());
    }
    
    /**
     * Create a static Solr field, requiring a name, a type and a map of more properties, e. g. one from
     * {@link StdConf}
     *
     * @param nameProperty Value for XML property "name" in schema
     * @param type A {@link SolrFieldType}, whose name will be used for the XML schema
     * @throws IllegalArgumentException When nameProperty does not follow Solr naming convention.
     * @see #isValidName(String)
     */
    public SolrStaticField(String nameProperty, SolrFieldType type, Map<SolrFieldProperty, String> properties) {
        super(nameProperty);
        this.properties.put(SolrFieldProperty.TYPE, type.getName());
        if (properties.containsKey(SolrFieldProperty.NAME) || properties.containsKey(SolrFieldProperty.TYPE))
            throw new IllegalArgumentException("Given properties map may not override fields name or type.");
        this.properties.putAll(properties);
    }
}
