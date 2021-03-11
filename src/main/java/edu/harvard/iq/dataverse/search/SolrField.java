package edu.harvard.iq.dataverse.search;

public class SolrField {

    private String nameSearchable;
    private String nameFacetable;
    private SolrType solrType;
    private boolean allowedToBeMultivalued;
    private boolean facetable;
    
    public SolrField(String name, SolrType solrType) {
        this(name, solrType, false, false);
    }
    
    public SolrField(String name, SolrType solrType, boolean allowToBeMultivalued) {
        this(name, solrType, allowToBeMultivalued, false);
    }

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
    
    /**
     * If this field can be used as a search facet, we will return the facetable name.
     * This means a field name that can be used with dynamic Solr fields (_s/_ss).
     * @return Searchable or facetable name, depending on {@code facetable}
     */
    public String getName() {
        return this.facetable ? this.nameFacetable : this.nameSearchable;
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

    public boolean isFacetable() {
        return facetable;
    }
    
}
