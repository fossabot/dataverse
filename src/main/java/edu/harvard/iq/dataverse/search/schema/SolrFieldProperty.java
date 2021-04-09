package edu.harvard.iq.dataverse.search.schema;

import java.util.Objects;

/**
 * A POJO class to hold information about {@link SolrFieldType} or {@link SolrField} properties, using typed responses.
 * This is currently Solr specific, which only allows for lots of boolean and some other properties (using String here
 * to reduce complexity).
 * @param <T> The factory methods will only allow for {@link Boolean} or {@link String} types.
 */
public class SolrFieldProperty<T> {
    
    private final String name;
    private final T defaultValue;
    private T value;
    
    private SolrFieldProperty(String name, T value, T defaultValue) {
        this.name = name;
        this.value = value;
        this.defaultValue = defaultValue;
    }
    
    /**
     * Method to create a property without an explicit default.
     * @param name The name of the property
     * @param value  The value of the property, in this case has to be typed {@link Boolean}.
     * @return A property field
     * @throws IllegalArgumentException if name or value is null.
     */
    public static SolrFieldProperty<Boolean> bool(String name, Boolean value) {
        return bool(name, value, null);
    }
    /**
     * Method to create a property with an explicit default. The value might be omitted.
     * @param name The name of the property
     * @param value  The value of the property, in this case has to be typed {@link Boolean}.
     * @param defaultValue  The default value of the property when no value present, in this case has to be typed {@link Boolean}.
     * @return A property field
     * @throws IllegalArgumentException if name or (value AND default value) is null.
     */
    public static SolrFieldProperty<Boolean> bool(String name, Boolean value, Boolean defaultValue) {
        if ( name == null )
            throw new IllegalArgumentException("Name cannot be null.");
        if ( value == null && defaultValue == null)
            throw new IllegalArgumentException("Value and defaultValue cannot both be null.");
        return new SolrFieldProperty<>(name, value, defaultValue);
    }
    
    /**
     * Method to create a property without an explicit default.
     * @param name The name of the property
     * @param value  The value of the property, in this case has to be typed {@link String}.
     * @return A property field
     * @throws IllegalArgumentException if name or value is null.
     */
    public static SolrFieldProperty<String> string(String name, String value) {
        return string(name, value, null);
    }
    /**
     * Method to create a property with an explicit default. The value might be omitted.
     * @param name The name of the property
     * @param value  The value of the property, in this case has to be typed {@link String}.
     * @param defaultValue  The default value of the property when no value present, in this case has to be typed {@link String}.
     * @return A property field
     * @throws IllegalArgumentException if name or (value AND default value) is null.
     */
    public static SolrFieldProperty<String> string(String name, String value, String defaultValue) {
        if ( name == null )
            throw new IllegalArgumentException("Name cannot be null.");
        if ( value == null && defaultValue == null)
            throw new IllegalArgumentException("Value and defaultValue cannot both be null.");
        return new SolrFieldProperty<>(name, value, defaultValue);
    }
    
    public String getName() {
        return name;
    }
    
    /**
     * Check if the value has been set (!= null).
     * @return true if set, false if not set.
     */
    public boolean isSet() {
        return value != null;
    }
    
    /**
     * Return value or defaultValue. Guaranteed not to be null, as not both can be null.
     * @return The value or the default value.
     */
    public T getValue() {
        return value == null ? defaultValue : value;
    }
    
    /**
     * Set the value of this property to a different value. Will not allow setting to null if
     * no default value is present.
     * @param value The new value (may be null)
     * @throws IllegalArgumentException if value is null and no default was set during creation.
     */
    public void setValue(T value) {
        if (value == null && defaultValue == null)
            throw new IllegalArgumentException("Value and defaultValue cannot both be null.");
        this.value = value;
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(name, value, defaultValue);
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SolrFieldProperty<?> that = (SolrFieldProperty<?>) o;
        // only compare the name - we want sets to contain unique properties, distinguished by their name!
        return Objects.equals(name, that.name);
    }
    
    @Override
    public String toString() {
        return "["+name+": "+getValue().toString()+"]";
    }
}
