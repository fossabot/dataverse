package edu.harvard.iq.dataverse.search;

import edu.harvard.iq.dataverse.search.schema.SolrFieldType;

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
    private SolrFieldType solrFieldType;
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

    public SolrField(String name, SolrFieldType solrFieldType, boolean allowedToBeMultivalued, boolean facetable) {
        this.nameSearchable = name;
        this.solrFieldType = solrFieldType;
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

    public SolrFieldType getSolrType() {
        return solrFieldType;
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

}
