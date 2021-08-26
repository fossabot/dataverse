package edu.harvard.iq.dataverse.search.schema;

import org.apache.commons.lang3.stream.Streams;

import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * This class depicts actual <fieldType> entries in schema.xml.
 *
 * Note: It is not necessary to have a precise XML representation of all fields possible for now.
 *       Instead, we will only use aspects that are important to us, so we can use it for validation.
 *       Down the road, it might be interesting to define and configure some <fieldType> completely
 *       via Managed Schema API.
 */
public final class SolrFieldType extends SolrField {
    /**
    * @todo: Make this configurable from text_en to text_general or
    *        non-English languages? We changed it to text_en to improve English
    *        language searching in https://github.com/IQSS/dataverse/issues/444.
    *        We want to get away from always using "text_en" (especially to
    *        support range queries) in https://github.com/IQSS/dataverse/issues/370
    */
    public static final SolrFieldType STRING = new SolrFieldType("string", "solr.StrField", Map.of(SolrFieldProperty.DOCVALUES, "true", SolrFieldProperty.SORTMISSINGLAST, "true"));
    public static final SolrFieldType STRINGS = new SolrFieldType("strings", "solr.StrField", Map.of(SolrFieldProperty.DOCVALUES, "true", SolrFieldProperty.MULTIVALUED, "true", SolrFieldProperty.SORTMISSINGLAST, "true"));
    
    public static final SolrFieldType INTEGER = new SolrFieldType("pint", "solr.IntPointField", Map.of(SolrFieldProperty.DOCVALUES, "true"));
    public static final SolrFieldType INTEGERS = new SolrFieldType("pints", "solr.IntPointField", Map.of(SolrFieldProperty.DOCVALUES, "true", SolrFieldProperty.MULTIVALUED, "true"));
    public static final SolrFieldType LONG = new SolrFieldType("plong", "solr.LongPointField", Map.of(SolrFieldProperty.DOCVALUES, "true"));
    public static final SolrFieldType LONGS = new SolrFieldType("plongs", "solr.LongPointField", Map.of(SolrFieldProperty.DOCVALUES, "true", SolrFieldProperty.MULTIVALUED, "true"));
    public static final SolrFieldType FLOAT = new SolrFieldType("pfloat", "solr.FloatPointField", Map.of(SolrFieldProperty.DOCVALUES, "true"));
    public static final SolrFieldType FLOATS = new SolrFieldType("pfloats", "solr.FloatPointField", Map.of(SolrFieldProperty.DOCVALUES, "true", SolrFieldProperty.MULTIVALUED, "true"));
    public static final SolrFieldType DOUBLE = new SolrFieldType("pdouble", "solr.DoublePointField", Map.of(SolrFieldProperty.DOCVALUES, "true"));
    public static final SolrFieldType DOUBLES = new SolrFieldType("pdoubles", "solr.DoublePointField", Map.of(SolrFieldProperty.DOCVALUES, "true", SolrFieldProperty.MULTIVALUED, "true"));
    
    public static final SolrFieldType DATE = new SolrFieldType("pdate", "solr.DatePointField", Map.of(SolrFieldProperty.DOCVALUES, "true"));
    public static final SolrFieldType DATES = new SolrFieldType("pdates", "solr.DatePointField", Map.of(SolrFieldProperty.DOCVALUES, "true", SolrFieldProperty.MULTIVALUED, "true"));
    
    public static final SolrFieldType BOOLEAN = new SolrFieldType("boolean", "solr.BoolField",  Map.of(SolrFieldProperty.SORTMISSINGLAST, "true"));
    public static final SolrFieldType BOOLEANS = new SolrFieldType("booleans", "solr.BoolField",  Map.of(SolrFieldProperty.SORTMISSINGLAST, "true", SolrFieldProperty.MULTIVALUED, "true"));
    
    /**
     * This field type should not be used anymore in newer Solr versions.
     */
    @Deprecated
    public static final SolrFieldType TRIE_INTEGER = new SolrFieldType("int", "solr.TrieIntField", Map.of(SolrFieldProperty.PRECISIONSTEP, "0", SolrFieldProperty.POSITIONINCREMENTGAP, "0"));
    /**
     * This field type should not be used anymore in newer Solr versions.
     */
    @Deprecated
    public static final SolrFieldType TRIE_LONG = new SolrFieldType("long", "solr.TrieLongField", Map.of(SolrFieldProperty.PRECISIONSTEP, "0", SolrFieldProperty.POSITIONINCREMENTGAP, "0"));
    /**
     * This field type should not be used anymore in newer Solr versions.
     */
    @Deprecated
    public static final SolrFieldType TRIE_DATE = new SolrFieldType("date", "solr.TrieDateField", Map.of(SolrFieldProperty.PRECISIONSTEP, "0", SolrFieldProperty.POSITIONINCREMENTGAP, "0"));
    
        
    // TODO: Especially textfields do have more than this (analysers, tokenizers, ...).
    //       We do not yet depict these in this class and it might be better suited for a class on its own.
    //       https://solr.apache.org/guide/8_8/field-type-definitions-and-properties.html#field-type-definitions-in-schema-xml
    public static final SolrFieldType TEXT_EN = new SolrFieldType("text_en", "solr.TextField");
    public static final SolrFieldType TEXT_GENERAL = new SolrFieldType("text_general", "solr.TextField", Map.of(SolrFieldProperty.MULTIVALUED, "true"));
    public static final SolrFieldType TEXT_GENERAL_REV = new SolrFieldType("text_general_rev", "solr.TextField");
    public static final SolrFieldType ALPHA_ONLY_SORT = new SolrFieldType("alphaOnlySort", "solr.TextField");
    
    private SolrFieldType(String typeName, String typeClass) {
        super(typeName);
        this.properties.put(SolrFieldProperty.CLASS, typeClass);
    }
    
    private SolrFieldType(String typeName, String typeClass, Map<SolrFieldProperty, String> properties) {
        this(typeName, typeClass);
        this.properties.putAll(properties);
    }
    
    // TODO: Later on, we should be able to save more configuration aspects as Analysers (including Tokenizers, Filters, ...)
    //       and Similarity.
    
    /**
     * A list of all types definied here to be iterable via reflection. Uses Apache Commons
     * {@link org.apache.commons.lang3.stream.Streams.FailableStream} due to the checked exceptions for object access.
     */
    public static final List<SolrFieldType> ALL = Streams.stream(Arrays.stream(SolrFieldType.class.getDeclaredFields()))
                                                    .filter(f -> SolrFieldType.class.equals(f.getType()))
                                                    .filter(f -> Modifier.isPublic(f.getModifiers()))
                                                    .map(f -> (SolrFieldType)f.get(null))
                                                    .collect(Collectors.toList());
}
