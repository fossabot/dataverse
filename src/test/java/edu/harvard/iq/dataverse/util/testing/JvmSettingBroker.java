package edu.harvard.iq.dataverse.util.testing;

import java.io.IOException;

public interface JvmSettingBroker {
    
    String getJvmSetting(String key) throws IOException;
    void setJvmSetting(String key, String value) throws IOException;
    String deleteJvmSetting(String key) throws IOException;
    
}
