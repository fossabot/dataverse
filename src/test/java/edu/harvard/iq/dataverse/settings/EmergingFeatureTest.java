package edu.harvard.iq.dataverse.settings;

import edu.harvard.iq.dataverse.util.testing.JvmSetting;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.assertEquals;

class EmergingFeatureTest {

    private static final String FLAG_NAME = "test-name";
    
    @ParameterizedTest
    @JvmSetting(key = JvmSettings.FEATURE_PROTECTED_BRANCH_NAME, value = "example")
    @JvmSetting(key = JvmSettings.FEATURE_BUILD_BRANCH_NAME, value = "example")
    @JvmSetting(key = JvmSettings.FEATURE_EMERGING_FLAG, value = "1", varArgs = FLAG_NAME)
    @CsvSource(value = {
        "false,true",
        "true,false"
    })
    void testFlagOnReleaseBranch(boolean securityRelevant, boolean expectedStatus) {
        // given
        EmergingFeature sut = new EmergingFeature(FLAG_NAME, securityRelevant);
        
        // when & then
        assertEquals(expectedStatus, sut.enabled());
    }
    
    @ParameterizedTest
    @JvmSetting(key = JvmSettings.FEATURE_PROTECTED_BRANCH_NAME, value = "BOOM")
    @JvmSetting(key = JvmSettings.FEATURE_BUILD_BRANCH_NAME, value = "master")
    @JvmSetting(key = JvmSettings.FEATURE_EMERGING_FLAG, value = "on", varArgs = FLAG_NAME)
    @CsvSource(value = {
        "false,true",
        "true,true"
    })
    void testBlowFuseByDeletingName(boolean securityRelevant, boolean expectedStatus) {
        // given
        EmergingFeature sut = new EmergingFeature(FLAG_NAME, securityRelevant);
        
        // when & then
        assertEquals(expectedStatus, sut.enabled());
    }
    
    @ParameterizedTest
    @JvmSetting(key = JvmSettings.FEATURE_PROTECTED_BRANCH_NAME, value = "foobar")
    @JvmSetting(key = JvmSettings.FEATURE_BUILD_BRANCH_NAME, value = "barbecue")
    @JvmSetting(key = JvmSettings.FEATURE_EMERGING_FLAG, value = "true", varArgs = FLAG_NAME)
    @CsvSource(value = {
        "false,true",
        "true,true"
    })
    void testFlagOnDevelopmentBranch(boolean securityRelevant, boolean expected) {
        // given
        EmergingFeature sut = new EmergingFeature(FLAG_NAME, securityRelevant);
        
        // when & then
        assertEquals(expected, sut.enabled());
    }
    
}
