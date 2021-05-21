package edu.harvard.iq.dataverse.search.schema;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Solr <field>, <dynamicField>, <fieldType> base class.
 *
 * See {@link SolrFieldType}, {@link SolrDynamicField}, {@link SolrStaticField} for implementing subclasses.
 */
public class SolrField {
    
    /**
     * Enumeration of often / commonly used field property configurations, ready to reuse.
     */
    public enum StdConf {
        STORED(Map.of(SolrFieldProperty.STORED, "true", SolrFieldProperty.INDEXED, "false", SolrFieldProperty.MULTIVALUED, "false")),
        STORED_INDEXED(Map.of(SolrFieldProperty.STORED, "true", SolrFieldProperty.INDEXED, "true", SolrFieldProperty.MULTIVALUED, "false")),
        STORED_INDEXED_MULTIVALUED(Map.of(SolrFieldProperty.STORED, "true", SolrFieldProperty.INDEXED, "true", SolrFieldProperty.MULTIVALUED, "true"));
        
        private Map<SolrFieldProperty, String> config;
        
        StdConf(Map<SolrFieldProperty, String> config) {
            this.config = config;
        }
        
        public Map<SolrFieldProperty, String> getConfig() {
            return config;
        }
    }
    
    protected static final Matcher validNameMatcher = Pattern.compile("^[A-Za-z_][\\w]+$").matcher("");
    protected final Map<SolrFieldProperty, String> properties = new EnumMap<>(SolrFieldProperty.class);
    
    /**
     * Create a very basic Solr field (can be used as a fieldtype, dynamicfield or field, but not copyfield),
     * requiring a name.
     * Visibility "package private" to allow testing and usage from subclasses. Not meant to be used directly.
     * @param nameProperty Value for XML property "name" in schema
     * @throws IllegalArgumentException When nameProperty does not follow Solr naming convention.
     * @see #isValidName(String)
     */
    SolrField(String nameProperty) {
        if(! isValidName(nameProperty))
            throw new IllegalArgumentException(nameProperty+" does not meet Solr naming convention.");
        this.properties.put(SolrFieldProperty.NAME, nameProperty);
    }
    
    public final String getName() {
        if (! hasProperty(SolrFieldProperty.NAME)) {
            // This should never happen, as the name property is required.
            // If it does, sth. has gone wrong very seriously.
            throw new NoSuchElementException();
        }
        return properties.get(SolrFieldProperty.NAME);
    }
    
    /**
     * Check if a name is matching Solr naming convention:
     * "Field names should consist of alphanumeric or underscore characters only and not start with a digit.
     * This is not currently strictly enforced, but other field names will not have first class support from all
     * components and back compatibility is not guaranteed. Names with both leading and trailing underscores
     * (e.g., _version_) are reserved."
     *
     * This may be overriden by {@link SolrDynamicField}, as names for these always start with "*_" matchers.
     * Visibility "package private" to provide relaxed testability.
     * @param name The name to check. Null-safe.
     * @return true if matching Solr convention, false otherwise.
     * @see <a href="https://solr.apache.org/guide/8_8/defining-fields.html">Solr Docs: Defining Fields</a>
     */
    boolean isValidName(String name) {
        if (name == null) return false;
        return validNameMatcher.reset(name).matches();
    }
    
    public final boolean hasProperty(SolrFieldProperty p) {
        return properties.containsKey(p);
    }
    public final Optional<String> getProperty(SolrFieldProperty p) { return Optional.ofNullable(properties.get(p)); }
    public final Map<SolrFieldProperty, String> getProperties() { return properties; }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SolrField)) return false;
        SolrField that = (SolrField) o;
        // Uniqueness is on names in Solr Schema.
        return this.getName().equals(that.getName());
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(properties);
    }
}
