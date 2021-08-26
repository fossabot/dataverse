package edu.harvard.iq.dataverse.search.schema;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SolrDynamicField extends SolrBaseField {
    
    protected static final Matcher validNameMatcher = Pattern.compile("^\\*_[\\w]+$").matcher("");
    
    /**
     * Create a dynamic Solr field, requiring a name and a type.
     * Will use a standard field config of "stored + indexed" ({@link StdConf#STORED_INDEXED}).
     *
     * @param nameProperty Value for XML property "name" in schema
     * @param type A {@link SolrFieldType}, whose name will be use for the XML schema
     * @throws IllegalArgumentException When nameProperty does not follow Solr naming convention.
     * @see #isValidName(String)
     */
    public SolrDynamicField(String nameProperty, SolrFieldType type) {
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
    public SolrDynamicField(String nameProperty, SolrFieldType type, Map<SolrFieldProperty, String> properties) {
        super(nameProperty);
        this.properties.put(SolrFieldProperty.TYPE, type.getName());
        if (properties.containsKey(SolrFieldProperty.NAME) || properties.containsKey(SolrFieldProperty.TYPE))
            throw new IllegalArgumentException("Given properties map may not override fields name or type.");
        this.properties.putAll(properties);
    }
    
    /**
     * Check if a name is matching Solr naming convention for dynamic field definitions:
     * "A dynamic field is just like a regular field except it has a name with a wildcard in it."
     * Visibility "package private" to provide relaxed testability.
     * @param name The name to check. Null-safe.
     * @return true if matching Solr dynamic field name convention, false otherwise.
     * @see <a href="https://solr.apache.org/guide/8_8/dynamic-fields.html">Solr Docs: Dynamic Fields</a>
     */
    @Override
    boolean isValidName(String name) {
        if (name == null) return false;
        return validNameMatcher.reset(name).matches();
    }
    
    /**
     * Create a modelled Solr Field from a raw schema response, containing the attributes.
     *
     * @param rawMap The attributes map as given by {@ SchemaResponse}
     * @return An instance of the Solr Field ready for our internal usage.
     * @throws IllegalArgumentException if any attributes are not known/invalid or the type has not been implemented by us
     */
    public static SolrDynamicField build(Map<String,Object> rawMap) throws IllegalArgumentException {
        Map<SolrFieldProperty, String> properties = SolrBaseField.convertToProperties(rawMap);
        
        String name = properties.remove(SolrFieldProperty.NAME);
        String typeName = properties.remove(SolrFieldProperty.TYPE);
        SolrFieldType type = SolrFieldType.ALL.stream()
                                .filter(t -> t.getName().equals(typeName))
                                .findFirst()
                                .orElseThrow(() -> new IllegalArgumentException("Given type \""+typeName+"\" from Solr not implemented"));
        
        return new SolrDynamicField(name, type, properties);
    }
}
