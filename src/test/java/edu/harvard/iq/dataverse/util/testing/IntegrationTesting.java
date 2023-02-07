package edu.harvard.iq.dataverse.util.testing;

import edu.harvard.iq.dataverse.arquillian.ArquillianTest;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Tag("arq")
@Tag("tc")
@Tag("it")
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
// REMEMBER: Order is crucial below to keep the order of initialization intact!
@ExtendWith(ITChecksExtension.class)
@Testcontainers
@RemoteJvmSettings
@ArquillianTest
public @interface IntegrationTesting {
}
