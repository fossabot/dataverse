package edu.harvard.iq.dataverse.search.schema;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SolrDynamicField extends SolrField {
    
    protected static final Matcher validNameMatcher = Pattern.compile("^\\*_[\\w]+$").matcher("");
    
    /**
     * Create a dynamic Solr field, requiring a name and a type.
     * Will use a standard field config of "stored + indexed" ({@link StdConf#SI}).
     *
     * @param nameProperty Value for XML property "name" in schema
     * @param type A {@link SolrFieldType}, whose name will be use for the XML schema
     * @throws IllegalArgumentException When nameProperty does not follow Solr naming convention.
     * @see #isValidName(String)
     */
    public SolrDynamicField(String nameProperty, SolrFieldType type) {
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
}
