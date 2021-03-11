package edu.harvard.iq.dataverse.search;

/**
 * We define Solr search fields here in one central place so they can be used
 * throughout the code but renamed here if need be.
 *
 * Note that there are many fields in Solr that are *not* here because their
 * values come from the database. For example "authorName" comes from the
 * database. We update the Solr schema.xml file by merging the output of `curl
 * http://localhost:8080/api/admin/index/solr/schema` into the file in the
 * source tree when a metadata block update warrants it.
 *
 * This process of updating schema.xml for new metadata block fields documented
 * at doc/sphinx-guides/source/admin/metadatacustomization.rst
 *
 * Generally speaking, we want the search fields to be readable. This is a
 * challenge for long field names but a power user should be able to type
 * "authorAffiliation:Harvard" into the general search box. A regular user is
 * much more likely to used Advanced Search to populate that field
 * automatically.
 *
 * Originally, these fields were all snake_case but since the dynamic fields are
 * camelCase we might want to standardize on that.
 *
 * You'll notice that dynamic fields like this are used...
 *
 * - _s (string)
 *
 * - _ss (multivalued string)
 *
 * - _l (long)
 *
 * - _dt (datetime)
 *
 * ... and these endings should not be changed unless you plan to convert them
 * to non-dynamic (by removing the ending) and specify their "type" in the Solr
 * schema.xml.
 *
 * Most fields we want to be searchable but some are stored with indexed=false
 * because we *don't* want them to be searchable and we're just using Solr as a
 * convenient key/value store. Why go to the database if you don't have to? For
 * a string here or there that needs to be available to both the GUI and the
 * Search API, we can just store them in Solr.
 *
 * For faceting we use a "string" type. If you use something like "text_general"
 * the field is tokenized ("Foo Bar" becomes "foo" "bar" which is not what we
 * want). See also
 * http://stackoverflow.com/questions/16559911/facet-query-will-give-wrong-output-on-dynamicfield-in-solr
 */
public enum SearchFields {
    
    /**
     * @todo: consider making various dynamic fields (_s) static in schema.xml
     * instead. Should they be stored in the database?
     */
    
    // standard fields from example/solr/collection1/conf/schema.xml
    // (but we are getting away from these...)
    ID(new SolrField("id", SolrType.STRING)),
    /**
     * Determine which DvObjects you might want to target for reindexing after
     * an upgrade such as between Dataverse 4.2 and 4.3.
     */
    DATAVERSE_VERSION_INDEXED_BY(new SolrField("dataverseVersionIndexedBy", SolrType.STRING, false, true)),
    NAME(new SolrField("name", SolrType.STRING)),
    /**
     * @todo Do we want to support finding dataverses, datasets, and files with
     * a query for description:foo? Maybe not, since people will probably just
     * use basic search for this. They could also use "dvDescription:foo OR
     * dsDescription:foo OR fileDescription:foo" if they *really* only want to
     * target the description of all three types at once.
     *
     * See also https://redmine.hmdc.harvard.edu/issues/3745
     */
    DESCRIPTION(new SolrField("description", SolrType.TEXT_EN)),
    /**
     * Identifiers differ per DvObject: alias for dataverses, globalId for
     * datasets, and database id for files.
     */
    IDENTIFIER(new SolrField("identifier", SolrType.STRING)),
    /**
     * Visible in the GUI as a facet to click: "Harvested" vs. "Root Dataverse".
     */
    METADATA_SOURCE(new SolrField("metadataSource", SolrType.STRING)),
    /**
     * Internal boolean used when creating OAI sets, for example.
     */
    IS_HARVESTED(new SolrField("isHarvested", SolrType.BOOLEAN)),
    /**
     * Such as https://doi.org/10.5072/FK2/HXI35W
     *
     * For files, the URL will be the parent dataset.
     */
    PERSISTENT_URL(new SolrField("persistentUrl", SolrType.STRING)),
    UNF(new SolrField("unf", SolrType.STRING)),
    DATAVERSE_NAME(new SolrField("dvName", SolrType.TEXT_EN)),
    DATAVERSE_ALIAS(new SolrField("dvAlias", SolrType.TEXT_EN)),
    DATAVERSE_AFFILIATION(new SolrField("dvAffiliation", SolrType.TEXT_EN)),
    DATAVERSE_DESCRIPTION(new SolrField("dvDescription", SolrType.TEXT_EN)),
    DATAVERSE_CATEGORY(new SolrField("dvCategory", SolrType.STRING)),
    
    /**
     * What is dvSubject_en for? How does it get populated into Solr? The
     * behavior changed so that now the subjects of dataverses are based on
     * their datasets. Should this be a string so we can facet on it more
     * properly? Should all checkboxes on the advanced search page (controlled
     * vocabularies) be backed by a string? When we rename this to "foobar" (a
     * field Solr doesn't know about) why doesn't Solr complain when we "index
     * all"? See also https://github.com/IQSS/dataverse/issues/1681
     */
    DATAVERSE_SUBJECT(new SolrField("dvSubject", SolrType.STRING, true)),
    /**
     * A "collapsed" facet (e.g. applies to both dataverses and datasets and is
     * merged as a single facet in the GUI) like affiliation that needs to match
     * the corresponding dynamic "facet" Solr field at the dataset level to work
     * properly. Should we use/expose "_ss" when you click a facet? It needs to
     * be different from "subject" which is used for general search but maybe we
     * could have a convention like "subjectFacet" for the facets?
     */
    SUBJECT(new SolrField("subject", SolrType.STRING, true, true)),
    
    /*
     * The category of the Dataverse (aka Dataverse Type). Named differently
     * than DATAVERSE_CATEGORY so it can be searched but doesn't show up on the
     * homepage facet
     */
    CATEGORY_OF_DATAVERSE(new SolrField("categoryOfDataverse", SolrType.STRING)),
    
    /*
     * The alias of the dataverse. This named differently because IDENTIFIER
     * is used for dataset for its own identifier.
     */
    IDENTIFIER_OF_DATAVERSE(new SolrField("identifierOfDataverse", SolrType.STRING)),
    
    /**
     * @todo think about how to tie the fact that this needs to be multivalued
     * (_ss) because a multivalued facet (authorAffilition_ss) will be collapsed
     * into it at index time. The business logic to determine if a data-driven
     * metadata field should be indexed into Solr as a single or multiple value
     * lives in the getSolrField() method of DatasetField.java
     *
     * AFFILIATION is used for the "collapsed" "Affiliation" facet that means
     * either "Author Affiliation" or dataverse affiliation. It needs to be a
     * string so we can facet on it and it needs to be multivalued because
     * "Author Affiliation" can be multivalued.
     */
    AFFILIATION(new SolrField("affiliation", SolrType.STRING, true, true)),
    FILE_NAME(new SolrField("fileName", SolrType.TEXT_EN, true)),
    FILE_DESCRIPTION(new SolrField("fileDescription", SolrType.TEXT_EN)),
    FILE_PERSISTENT_ID(new SolrField("filePersistentId", SolrType.TEXT_EN)),
    /**
     * Can be multivalued and includes both "friendly" and "group" versions:
     * "PNG Image", "image"
     */
    FILE_TYPE_SEARCHABLE(new SolrField("fileType", SolrType.TEXT_EN, true)),
    /**
     * @todo Thie static variable not named properly. We want to expose an
     * acutal MIME Type in https://github.com/IQSS/dataverse/issues/1595 . See
     * also cleanup ticket at https://github.com/IQSS/dataverse/issues/1314
     *
     * i.e. "PNG Image"
     */
    FILE_TYPE_FRIENDLY(new SolrField("fileTypeDisplay", SolrType.STRING)),
    FILE_CONTENT_TYPE(new SolrField("fileContentType", SolrType.STRING)),
    /**
     * Used as a facet for file groups like "image" or "document"
     */
    FILE_TYPE(new SolrField("fileTypeGroupFacet", SolrType.STRING)),
    FILE_SIZE_IN_BYTES(new SolrField("fileSizeInBytes", SolrType.LONG)),
    FILE_MD5(new SolrField("fileMd5", SolrType.STRING)),
    FILE_CHECKSUM_TYPE(new SolrField("fileChecksumType", SolrType.STRING)),
    FILE_CHECKSUM_VALUE(new SolrField("fileChecksumValue", SolrType.STRING)),
    FILENAME_WITHOUT_EXTENSION(new SolrField("fileNameWithoutExtension", SolrType.TEXT_EN, true)),
    /**
     * Indexed as a string so we can facet on it.
     */
    FILE_TAG(new SolrField("fileTag", SolrType.STRING, true)),
    /**
     * Indexed as text_en so it's searchable by lower case etc.
     */
    FILE_TAG_SEARCHABLE(new SolrField("fileTags", SolrType.TEXT_EN, true)),
    
    /**
     * Internal boolean indicating that the file has been deleted in the draft version.
     */
    FILE_DELETED(new SolrField("fileDeleted", SolrType.BOOLEAN)),
    /*
     * (tabular) Data Tags are indexed as a string, since we are only planning to
     * use these in facet-like, exact searches:
     */
    TABDATA_TAG(new SolrField("tabularDataTag", SolrType.STRING, true)),
    ACCESS(new SolrField("fileAccess", SolrType.STRING, true)),

    SUBTREE(new SolrField("subtreePaths", SolrType.STRING, true)),

    // i.e. http://localhost:8080/search.xhtml?q=*&fq0=citationdate_dt:[2008-01-01T00%3A00%3A00Z+TO+2011-01-01T00%3A00%3A00Z%2B1YEAR}
//    public static final String PRODUCTION_DATE_ORIGINAL = DatasetFieldConstant.productionDate + "_dt"),
//    public static final String PRODUCTION_DATE_YEAR_ONLY = DatasetFieldConstant.productionDate + "_i"),
//    public static final String DISTRIBUTION_DATE_ORIGINAL = DatasetFieldConstant.distributionDate + "_dt"),
//    public static final String DISTRIBUTION_DATE_YEAR_ONLY = DatasetFieldConstant.distributionDate + "_i"),
    /**
     * Solr refers to "relevance" as "score"
     */
    RELEVANCE(new SolrField("score", SolrType.INTERNAL)),

    /**
     * A dataverse, a dataset, or a file.
     */
    TYPE(new SolrField("dvObjectType", SolrType.STRING)),
    NAME_SORT(new SolrField("nameSort", SolrType.ALPHAONLYSORT)),
    // PUBLICATION_YEAR used to be called PUBLICATION_DATE.
    // TODO: shouldn't we rename the field in Solr???
    PUBLICATION_YEAR(new SolrField("publicationDate", SolrType.STRING)),
    RELEASE_OR_CREATE_DATE(new SolrField("dateSort", SolrType.DATE)),


    DEFINITION_POINT(new SolrField("definitionPointDocId", SolrType.STRING)),
    DEFINITION_POINT_DVOBJECT_ID(new SolrField("definitionPointDvObjectId", SolrType.STRING)),
    DISCOVERABLE_BY(new SolrField("discoverableBy", SolrType.STRING, true)),

    /**
     * i.e. "Unpublished", "Draft" (multivalued)
     */
    PUBLICATION_STATUS(new SolrField("publicationStatus", SolrType.STRING, true)),
    /**
     * @todo reconcile different with Solr schema.xml where type is Long rather
     * than String.
     */
    ENTITY_ID(new SolrField("entityId", SolrType.LONG)),
    PARENT_NAME(new SolrField("parentName", SolrType.STRING)),
    // Long standing todo for this field: convert from string to long
    PARENT_ID(new SolrField("parentId", SolrType.LONG)),
    PARENT_IDENTIFIER(new SolrField("parentIdentifier", SolrType.STRING)),
    /**
     * @todo Should we add a "parentCitationHtml" field now or wait for demand
     * for it?
     */
    PARENT_CITATION(new SolrField("parentCitation", SolrType.STRING)),

    // THIS FIELD SEEMS UNUSED SINCE A LONG TIME (not present in schema.xml) -> commented out 2021-03-10
    // DATASET_DESCRIPTION(new SolrField("dsDescriptionValue", SolrType.TEXT_EN)),
    /**
     * In Datavese 4.3 and earlier "citation" was indexed as the "online" or
     * HTML version, with the DOI link wrapped in an href tag but now it's the
     * plaintext version and anyone who was depending on the old version can
     * switch to the new "citationHTML" field.
     */
    DATASET_CITATION(new SolrField("citation", SolrType.STRING)),
    DATASET_CITATION_HTML(new SolrField("citationHtml", SolrType.STRING)),
    DATASET_DEACCESSION_REASON(new SolrField("deaccessionReason", SolrType.STRING)),
    /**
     * In contrast to PUBLICATION_YEAR, this field applies only to datasets for
 more targeted results for just datasets. The format is YYYY (i.e.
     * "2015").
     */
    DATASET_PUBLICATION_DATE(new SolrField("dsPublicationDate", SolrType.STRING)),
    DATASET_PERSISTENT_ID(new SolrField("dsPersistentId", SolrType.TEXT_EN)),
    DATASET_VERSION_ID(new SolrField("datasetVersionId", SolrType.LONG)),

    VARIABLE_NAME(new SolrField("variableName", SolrType.TEXT_EN, true)),
    VARIABLE_LABEL(new SolrField("variableLabel", SolrType.TEXT_EN, true)),
    LITERAL_QUESTION(new SolrField("literalQuestion", SolrType.TEXT_EN, true)),
    INTERVIEW_INSTRUCTIONS(new SolrField("interviewInstructions", SolrType.TEXT_EN, true)),
    POST_QUESTION(new SolrField("postQuestion", SolrType.TEXT_EN, true)),
    VARIABLE_UNIVERSE(new SolrField("variableUniverse", SolrType.TEXT_EN, true)),
    VARIABLE_NOTES(new SolrField("variableNotes", SolrType.TEXT_EN, true)),


    FULL_TEXT(new SolrField("_text_", SolrType.TEXT_GENERAL, true));
    
    private final SolrField field;
    
    SearchFields(SolrField field) {
        this.field = field;
    }
    
    public String getName() { return field.getName(); }
    public String getNameSearchable() {
        return field.getNameSearchable();
    }
    public String getNameFacetable() {
        return field.getNameFacetable();
    }
    public SolrType getSolrType() {
        return field.getSolrType();
    }
}
