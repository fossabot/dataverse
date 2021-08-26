package edu.harvard.iq.dataverse.search.schema;

import java.util.Objects;

/**
 * A class to depict a Solr <copyField> in the schema of a collection/core.
 *
 * TODO: This class is opinionated with how the Solr index has been used for Dataverse for the past years.
 *       We do not use wildcard matches for <copyField> at all, so there is no option backed in to use any
 *       arbitraty wildcard matching, except for formerly defined dynamic fields. This might need to change
 *       in a future timeline.
 *
 * @see <a href="https://solr.apache.org/guide/8_8/copying-fields.html">Solr Guide: Copying Fields</a>
 */
public class SolrCopyField {
    
    private SolrBaseField sourceField;
    private SolrBaseField destinationField;
    private Integer maxChars;
    
    /**
     * Create a Solr field copying data from a source to a target field.
     *
     * This is an opinionated constructor, as it narrows the degrees of freedom allowed inside Solr.
     * You cannot use arbitrary strings here, but at least you could use a {@link SolrDynamicField} on
     * both ends. The other opinion is about limiting the copied data to max 3000 chars to avoid
     * blowing up your index (see {@link SolrCopyField(String, String, Integer)} to set other limit).
     *
     * Rules:
     * 1. Wildcard in "dest" may only appear if wildcard in "source". (Matching glob will be reused)
     * 2. The destination field should be using {@link SolrFieldProperty#MULTIVALUED} if source is
     *    multivalued or targeted by multiple copy fields.
     *
     * @param source The "source" field of the data to be copied, might be a dynamic field (using wildcard matching).
     * @param dest The "destination" field, where the data will be stored. Make sure to follow the rules regarding
     *             wildcard matches and multivalued (see comment above).
     * @throws IllegalArgumentException If source or dest are null or instances of {@link SolrFieldType}.
     */
    public SolrCopyField(SolrBaseField source, SolrBaseField dest) {
        if ( source == null || dest == null )
            throw new IllegalArgumentException("Given fields source and dest may not be null.");
        if ( source instanceof SolrFieldType || dest instanceof SolrFieldType)
            throw new IllegalArgumentException("Given fields source and dest may only be static or dynamic fields, not types.");
        this.sourceField = source;
        this.destinationField = dest;
        this.maxChars = 3000;
    }
    
    /**
     * Create a Solr field copying data from a source to a target field and set the maximum number of
     * characters copied. This is an effective possibility to not blow up your index size by accident.
     *
     * @param source The "source" field of the data to be copied, might be a dynamic field (using wildcard matching).
     * @param dest The "destination" field, where the data will be stored. Make sure to follow the rules regarding
     *             wildcard matches and multivalued (see comment above).
     * @param maxChars The copy limit in chars.
     *                 The opinionated constructor {@link #SolrCopyField(SolrBaseField, SolrBaseField)} uses a limit of 3000
     *                 chars.Be extra careful here. You have been warned.
     * @throws IllegalArgumentException If any parameter is null, maxChars < 1 or the src/dst fields are of wrong type.
     */
    public SolrCopyField(SolrBaseField source, SolrBaseField dest, Integer maxChars) {
        this(source, dest);
        if (maxChars != null && maxChars.intValue() > 0)
            this.maxChars = maxChars;
        else
            throw new IllegalArgumentException("Given property maxChars may not be < 1 or null.");
    }
    
    // TODO: When we go for validation / schema management, we need some mechanism to create the entry.
    //       We should make sure that if "destination" is a dynamic field and "source" a static one, we
    //       create the <copyField> with a nonwildcard using destination string, reusing the static field name
    //       as a replacement for the wildcard.
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SolrCopyField)) return false;
        SolrCopyField that = (SolrCopyField) o;
        return sourceField.equals(that.sourceField) && destinationField.equals(that.destinationField);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(sourceField, destinationField);
    }
}
