package edu.harvard.iq.dataverse.util.testing;

import edu.harvard.iq.dataverse.arquillian.GdccBaseContainer;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.platform.commons.support.AnnotationSupport;
import org.junit.platform.commons.support.HierarchyTraversalMode;
import org.junit.platform.commons.support.ModifierSupport;
import org.junit.platform.commons.support.ReflectionSupport;
import org.testcontainers.junit.jupiter.Container;

import java.lang.reflect.Field;
import java.util.List;

public class ITChecksExtension implements BeforeAllCallback {
    @Override
    public void beforeAll(ExtensionContext extensionContext) throws Exception {
        // Check any field using a GdccBaseContainer is static, so Arquillian can get to it
        List<Field> baseContainerFields = ReflectionSupport.findFields(
            extensionContext.getRequiredTestClass(),
            field -> field.getType().isAssignableFrom(GdccBaseContainer.class),
            HierarchyTraversalMode.TOP_DOWN);
    
        // This field must be unique, as this container is handed over to Arquillian using unique system properties.
        // TODO: in case we ever need TWO application servers in an integration test, this might need extension
        if (baseContainerFields.size() != 1) {
            throw new IllegalStateException("Cannot find a unique static field of type GdccBaseContainer");
        }
        
        Field baseContainer = baseContainerFields.get(0);
        if (ModifierSupport.isNotStatic(baseContainer)) {
            throw new IllegalStateException("Field "+baseContainer.getName()+" of type GdccBaseContainer must be static");
        }
        if (!AnnotationSupport.isAnnotated(baseContainer, Container.class)) {
            throw new IllegalStateException("Field "+baseContainer.getName()+" must have annotation @Container");
        }
    }
}
