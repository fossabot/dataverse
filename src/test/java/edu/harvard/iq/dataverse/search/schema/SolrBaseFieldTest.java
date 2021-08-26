package edu.harvard.iq.dataverse.search.schema;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class SolrBaseFieldTest {
    
    @Test
    public void convertToPropertiesMap_valid() {
        // given
        Map<String,Object> subject = Map.of("name", "test",
            "type", "test",
            "stored", "true");
        Map<SolrFieldProperty,String> expected = Map.of(SolrFieldProperty.NAME, "test",
            SolrFieldProperty.TYPE, "test",
            SolrFieldProperty.STORED, "true");
        
        // when && then
        assertEquals(expected, SolrBaseField.convertToProperties(subject));
    }
    
    @Test
    public void convertToPropertiesMap_invalid() {
        // given
        Map<String,Object> subject = Map.of("name", "test",
            "type", "test",
            "GARBAGE", "true");
        
        // when && then
        assertThrows(IllegalArgumentException.class, () -> SolrBaseField.convertToProperties(subject));
    }
    
}