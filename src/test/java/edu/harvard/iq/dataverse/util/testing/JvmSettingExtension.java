package edu.harvard.iq.dataverse.util.testing;

import edu.harvard.iq.dataverse.settings.JvmSettings;
import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.AfterTestExecutionCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.BeforeTestExecutionCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.platform.commons.support.AnnotationSupport;
import org.junit.platform.commons.support.HierarchyTraversalMode;
import org.junit.platform.commons.support.ReflectionSupport;

import java.lang.reflect.Field;
import java.util.List;

/*

TODO: before all / after all to allow type level settings
TODO: enable broker to load different backends to set settings
      -> annotation at type for local sys props
      -> annotation at field for target broker (must implement broker interface, must only be used once)

 */
public class JvmSettingExtension implements BeforeTestExecutionCallback, AfterTestExecutionCallback, BeforeAllCallback, AfterAllCallback {
    
    @Override
    public void beforeAll(ExtensionContext extensionContext) throws Exception {
        List<JvmSetting> settings = AnnotationSupport.findRepeatableAnnotations(extensionContext.getTestClass(), JvmSetting.class);
        ExtensionContext.Store store = extensionContext.getStore(ExtensionContext.Namespace.create(getClass(), extensionContext.getRequiredTestClass()));
        
        setSetting(settings, getBroker(extensionContext), store);
    }
    
    @Override
    public void afterAll(ExtensionContext extensionContext) throws Exception {
        List<JvmSetting> settings = AnnotationSupport.findRepeatableAnnotations(extensionContext.getTestClass(), JvmSetting.class);
        ExtensionContext.Store store = extensionContext.getStore(ExtensionContext.Namespace.create(getClass(), extensionContext.getRequiredTestClass()));
        
        resetSetting(settings, getBroker(extensionContext), store);
    }
    
    @Override
    public void beforeTestExecution(ExtensionContext extensionContext) throws Exception {
        List<JvmSetting> settings = AnnotationSupport.findRepeatableAnnotations(extensionContext.getTestMethod(), JvmSetting.class);
        ExtensionContext.Store store = extensionContext.getStore(ExtensionContext.Namespace.create(getClass(), extensionContext.getRequiredTestClass(), extensionContext.getRequiredTestMethod()));
        
        setSetting(settings, getBroker(extensionContext), store);
    }
    
    @Override
    public void afterTestExecution(ExtensionContext extensionContext) throws Exception {
        List<JvmSetting> settings = AnnotationSupport.findRepeatableAnnotations(extensionContext.getTestMethod(), JvmSetting.class);
        ExtensionContext.Store store = extensionContext.getStore(ExtensionContext.Namespace.create(getClass(), extensionContext.getRequiredTestClass(), extensionContext.getRequiredTestMethod()));
        
        resetSetting(settings, getBroker(extensionContext), store);
    }
    
    private void setSetting(List<JvmSetting> settings, JvmSettingBroker broker, ExtensionContext.Store store) throws Exception {
        for (JvmSetting setting : settings) {
            // get the setting name (might need var args substitution)
            String settingName = getSettingName(setting);
        
            // get the setting ...
            String oldSetting = broker.getJvmSetting(settingName);
        
            // if present - store in context to restore later
            if (oldSetting != null) {
                store.put(settingName, oldSetting);
            }
        
            // set to new value
            broker.setJvmSetting(settingName, setting.value());
        }
    }
    
    private void resetSetting(List<JvmSetting> settings, JvmSettingBroker broker, ExtensionContext.Store store) throws Exception {
        for (JvmSetting setting : settings) {
            // get the setting name (might need var args substitution)
            String settingName = getSettingName(setting);
    
            // get a stored setting from context
            String oldSetting = store.remove(settingName, String.class);
    
            // if present before, restore
            if (oldSetting != null) {
                broker.setJvmSetting(settingName, oldSetting);
                // if NOT present before, delete
            } else {
                broker.deleteJvmSetting(settingName);
            }
        }
    }
    
    private String getSettingName(JvmSetting setting) {
        JvmSettings target = setting.key();
        
        if (target.needsVarArgs()) {
            String[] variableArguments = setting.varArgs();
            
            if (variableArguments == null || variableArguments.length != target.numberOfVarArgs()) {
                throw new IllegalArgumentException("You must provide " + target.numberOfVarArgs() +
                    " variable arguments via varArgs = {...} for setting " + target +
                    " (\"" + target.getScopedKey() + "\")");
            }
            
            return target.insert(variableArguments);
        }
        
        return target.getScopedKey();
    }
    
    private JvmSettingBroker getBroker(ExtensionContext extensionContext) throws Exception {
        // Is this test class using local system properties, then get a broker for these
        if (AnnotationSupport.isAnnotated(extensionContext.getTestClass(), LocalJvmSettings.class)) {
            return LocalJvmSettings.localBroker;
        } else if (AnnotationSupport.isAnnotated(extensionContext.getTestClass(), RemoteJvmSettings.class)) {
            // Check for remote targets
            List<Field> matchingFields = ReflectionSupport.findFields(
                extensionContext.getRequiredTestClass(),
                field -> JvmSettingBroker.class.isAssignableFrom(field.getType()),
                HierarchyTraversalMode.TOP_DOWN);
            
            // TODO: For now, we only support 1 injection target.
            if (matchingFields.size() != 1) {
                throw new IllegalStateException("You must provide exactly one JvmSettingBroker implementation in a test class' field for RemoteJvmSettings to work");
            }
            
            // Get field value and return broker
            Field field = matchingFields.get(0);
            field.setAccessible(true);
            return (JvmSettingBroker) field.get(extensionContext.getTestInstance());
        } else {
            throw new IllegalStateException("You must provide either @LocalJvmSettings or @RemoteJvmSettings annotations");
        }
    }
}
