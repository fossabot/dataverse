package edu.harvard.iq.dataverse.settings;

/**
 * Contains short-lived toggles to enable certain functionality that is under active development and might be unsafe
 * to use in production from a security view.
 */
public final class EmergingFeature {
    
    /*
     * Please add flags here as e.g. "public static final EmergingFeature NAME = new EmergingFeature("name", false);"
     */
    
    /* Comes from microprofile-config.properties by default, if empty from other source must throw error! */
    private static final String fusedBranchName = JvmSettings.FEATURE_PROTECTED_BRANCH_NAME.lookup();
    private static final String buildBranchName = JvmSettings.FEATURE_BUILD_BRANCH_NAME.lookup();
    
    private final String flagName;
    private final boolean isSecurityRelevant;
    
    EmergingFeature(String flagName, boolean isSecurityRelevant) {
        this.flagName = flagName;
        this.isSecurityRelevant = isSecurityRelevant;
    }
    
    /**
     * <p>
     *     Lookup the configured status of this feature flag. Remember, these feature might not be safe to use in
     *     production. As a consequence, you need to blow a fuse to use them on a production build.
     * </p><p>
     *     The current implementation reuses {@link JvmSettings} to interpret any
     *     <a href="https://download.eclipse.org/microprofile/microprofile-config-3.0/microprofile-config-spec-3.0.html#_built_in_converters">boolean values</a>
     *     (true == case-insensitive one of "true", "1", "YES", "Y", "ON") and hook into the usual settings system
     *     (any MicroProfile Config Source available). All other values will result in "false".
     *  * </p
     * @return True or false depending on the configuration, the security relevancy, and if running on a release build
     */
    public boolean enabled() {
        System.out.println(fusedBranchName);
        System.out.println(buildBranchName);
        
        return JvmSettings.FEATURE_EMERGING_FLAG.lookupOptional(Boolean.class, flagName)
            .orElse(false)
            /*
             * This boolean logic here means:
             * 1. If this flag is security relevant (=true), determine if this is a release branch build
             *    (negation, becomes false, means second condition is evaluated).
             *    If it is a release build (=true), do not allow the flag to be activated (negate, becomes false, fails
             *    AND operation)
             * 2. In all other cases, this will become true and with the AND operator go with what's configured
             */
            && (!this.isSecurityRelevant || !runningOnReleaseBranch());
    }
    
    public static boolean runningOnReleaseBranch() {
        return buildBranchName.equals(fusedBranchName);
    }
    
}
