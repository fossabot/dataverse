package edu.harvard.iq.dataverse.search.schema;

import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public enum SolrFieldProperty {
    // Used for <fieldType>, <dynamicField> and <field> only
    NAME("name", String.class, null),
    
    // Used for <fieldType> only
    CLASS("class", String.class, null),
    
    // Used for <dynamicField> and <field> only
    TYPE("type", String.class, null),
    // Used for <dynamicField> and <field> only
    DEFAULT("default", String.class, null),
    
    /* The properties below are used for <fieldType>, <dynamicField> and <field> only */
    
    PRECISIONSTEP("precisionStep", Long.class, null),
    POSITIONINCREMENTGAP("positionIncrementGap", String.class, null),
    AUTOGENERATEPHRASEQUERIES("autoGeneratePhraseQueries", Boolean.class, null),
    SYNONYMQUERYSTYLE("synonymQueryStyle", String.class, null),
    ENABLEGRAPHQUERIES("enableGraphQueries", Boolean.class, "true"),
    DOCVALUESFORMAT("docValuesFormat", String.class, null),
    POSTINGSFORMAT("postingsFormat", String.class, null),
    
    INDEXED("indexed", Boolean.class, "true"),
    STORED("stored", Boolean.class, "true"),
    DOCVALUES("docValues", Boolean.class, "false"),
    SORTMISSINGFIRST("sortMissingFirst", Boolean.class, "false"),
    SORTMISSINGLAST("sortMissingLast", Boolean.class, "false"),
    MULTIVALUED("multiValued", Boolean.class, "false"),
    UNINVERTIBLE("uninvertible", Boolean.class, "true"),
    OMITNORMS("omitNorms", Boolean.class, null),
    OMITTERMFREQANDPOSITIONS("omitTermFreqAndPositions", Boolean.class, null),
    OMITPOSITIONS("omitPositions", Boolean.class, null),
    TERMVECTORS("termVectors", Boolean.class, "false"),
    TERMPOSITIONS("termPositions", Boolean.class, "false"),
    TERMOFFSETS("termOffsets", Boolean.class, "false"),
    TERMPAYLOADS("termPayloads", Boolean.class, "false"),
    REQUIRED("required", Boolean.class, "false"),
    USEDOCVALUESASSTORED("useDocValuesAsStored", Boolean.class, "true"),
    LARGE("large", Boolean.class, "false"),
    
    /* The properties below are used for <copyField> only */
    
    SOURCE("source", String.class, null),
    DEST("dest", String.class, null),
    MAXCHARS("maxChars", Integer.class, null);
    
    public final String key;
    public final Class type;
    public final Optional<String> defaultValue;
    private static final Map<String, SolrFieldProperty> members;
    static {
        members = Arrays.stream(SolrFieldProperty.values()).collect(Collectors.toUnmodifiableMap(sfp -> sfp.getKey(), sfp -> sfp));
    }
    
    SolrFieldProperty(String key, Class type, String defaultValue) {
        this.key = key;
        this.type = type;
        this.defaultValue = Optional.ofNullable(defaultValue);
    }
    
    public static SolrFieldProperty of(String name) {
        if (name == null) {
            return null;
        }
        return members.get(name);
    }
    
    public String getKey() {
        return key;
    }
    public Class getType() {
        return type;
    }
    public Optional<String> getDefault() {
        return defaultValue;
    }
    
}
