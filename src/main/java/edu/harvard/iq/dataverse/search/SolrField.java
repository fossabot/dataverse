package edu.harvard.iq.dataverse.search;

public class SolrField {
    
    /**
     * The human (and machine) read- and usable name of this field, coming from the metadata block (schema) definition.
     * This name is usually used in queries, UI, etc, yet some queries requiring facets might rely on
     * {@link #nameFacetable}, too.
     */
    private String nameSearchable;
    /**
     * The name of a dynamic, string-based Solr field, usable as facet in a search query.
     * Only used for text fields, as not necessary for others (primitives).
     */
    private String nameFacetable;
    /**
     * The Solr data type to be used for this metadata field. {@link edu.harvard.iq.dataverse.DatasetFieldType.FieldType}
     * links the datatype from metadata blocks and the Solr data types together.
     */
    private SolrType solrType;
    private boolean allowedToBeMultivalued;
    /**
     * This field is *not* meant to depict a facetability aspect of the Solr field inside the Solr schema.
     * This saves the state whether this field is either defined as a search facet within a metadata block (schema)
     * or declared as such within specific Dataverse Collection(s).
     *
     * For text-based fields, it will be use to craft faceted search queries against Solr via {@link #isFacetable()}.
     * Text fields need a string representation in Solr, as they cannot be used as facets in Solr queries.
     * The docs suggest using a <copyField> to create a facetable string copy of the field.
     *
     * @see <a href="https://solr.apache.org/guide/8_8/faceting.html">Solr: Faceting in Searches</a>
     */
    private boolean facetable;

    public SolrField(String name, SolrType solrType, boolean allowedToBeMultivalued, boolean facetable) {
        this.nameSearchable = name;
        this.solrType = solrType;
        this.allowedToBeMultivalued = allowedToBeMultivalued;
        this.facetable = facetable;
        if (allowedToBeMultivalued) {
            /**
             * @todo Should we expose this Solr-specific "_ss" fieldname here?
             * Instead should the field end with "FacetMultiple"?
             */
            this.nameFacetable = name + "_ss";
        } else {
            /**
             * @todo Should we expose this Solr-specific "_s" fieldname here?
             * Instead should the field end with "FacetSingle"?
             */
            this.nameFacetable = name + "_s";
        }
    }

    public String getNameSearchable() {
        return nameSearchable;
    }

    public String getNameFacetable() {
        return nameFacetable;
    }

    public SolrType getSolrType() {
        return solrType;
    }

    public Boolean isAllowedToBeMultivalued() {
        return allowedToBeMultivalued;
    }

    public void setAllowedToBeMultivalued(boolean allowedToBeMultivalued) {
        this.allowedToBeMultivalued = allowedToBeMultivalued;
    }
    
    /**
     * Is this field to be used as a search facet in Solr queries?
     * @return true or false
     * @see #facetable
     */
    public boolean isFacetable() {
        return facetable;
    }

    public enum SolrType {

        /**
         * @todo: make this configurable from text_en to text_general or
         * non-English languages? We changed it to text_en to improve English
         * language searching in https://github.com/IQSS/dataverse/issues/444
         *
         * We want to get away from always using "text_en" (especially to
         * support range queries) in
         * https://github.com/IQSS/dataverse/issues/370
         */
        STRING("string"), TEXT_EN("text_en"), INTEGER("int"), LONG("long"), DATE("text_en"), EMAIL("text_en");

        private String type;

        private SolrType(String string) {
            type = string;
        }

        public String getType() {
            return type;
        }

    }

}
