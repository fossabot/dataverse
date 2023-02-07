package edu.harvard.iq.dataverse.arquillian;

import org.jboss.arquillian.junit5.ArquillianExtension;
import org.junit.jupiter.api.extension.ExtendWith;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@ExtendWith(ArquillianExtension.class)
public @interface ArquillianTest {
}
