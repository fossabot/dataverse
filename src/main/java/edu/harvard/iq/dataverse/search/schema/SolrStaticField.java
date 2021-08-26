package edu.harvard.iq.dataverse.search.schema;

import java.util.Map;

public class SolrStaticField extends SolrField {

    /**
     * Create a static Solr field, requiring a name and a type.
     * Will use a standard field config of "stored + indexed" ({@link StdConf#STORED_INDEXED}).
     *
     * @param nameProperty Value for XML property "name" in schema
     * @param type A {@link SolrFieldType}, whose name will be use for the XML schema
     * @throws IllegalArgumentException When nameProperty does not follow Solr naming convention.
     * @see #isValidName(String)
     */
    public SolrStaticField(String nameProperty, SolrFieldType type) {
        this(nameProperty, type, StdConf.STORED_INDEXED.getConfig());
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
    
    /**
     * Create a modelled Solr Field from a raw schema response, containing the attributes.
     *
     * @param rawMap The attributes map as given by {@ SchemaResponse}
     * @return An instance of the Solr Field ready for our internal usage.
     * @throws IllegalArgumentException if any attributes are not known/invalid or the type has not been implemented by us
     */
    public static SolrStaticField build(Map<String,Object> rawMap) throws IllegalArgumentException {
        Map<SolrFieldProperty, String> properties = SolrField.convertToProperties(rawMap);
        
        String name = properties.remove(SolrFieldProperty.NAME);
        String typeName = properties.remove(SolrFieldProperty.TYPE);
        SolrFieldType type = SolrFieldType.ALL.stream()
                                .filter(t -> t.getName().equals(typeName))
                                .findFirst()
                                .orElseThrow(() -> new IllegalArgumentException("Given type \""+typeName+"\" from Solr not implemented"));
        
        return new SolrStaticField(name, type, properties);
    }
}
