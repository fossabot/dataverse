package edu.harvard.iq.dataverse.search.schema;

import java.util.Optional;

public enum SolrFieldProperty {
    NAME("name", String.class, null),
    CLASS("class", String.class, null),
    TYPE("type", String.class, null),
    DEFAULT("default", String.class, null),
    
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
    LARGE("large", Boolean.class, "false");
    
    
    public final String fieldName;
    public final Class type;
    public final Optional<String> defaultValue;
    
    SolrFieldProperty(String fieldName, Class type, String defaultValue) {
        this.fieldName = fieldName;
        this.type = type;
        this.defaultValue = Optional.ofNullable(defaultValue);
    }
    
    public String getName() {
        return fieldName;
    }
    public Class getType() {
        return type;
    }
    public Optional<String> getDefault() {
        return defaultValue;
    }
    
}
