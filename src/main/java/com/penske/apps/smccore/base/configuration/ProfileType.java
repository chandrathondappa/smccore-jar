package com.penske.apps.smccore.base.configuration;

/**
 * Static content class uses specifically for Spring Profile naming.
 */
public final class ProfileType {
    
    //Prevent instantiation of this class. This is just a holder for constants.
    private ProfileType() {}
	
    public static final String PRODUCTION = "prod";
    public static final String QA = "qa";
    public static final String QA2 = "qa2";
    public static final String DEVELOPMENT = "dev";
    public static final String LOCAL = "local";
    public static final String TEST = "test";
    
    private static final String NOT = "!";

    public static final String NOT_PRODUCTION = NOT + PRODUCTION;
    public static final String NOT_QA = NOT + QA;
    public static final String NOT_QA2 = NOT + QA2;
    public static final String NOT_DEVELOPMENT = NOT + DEVELOPMENT;
    public static final String NOT_LOCAL = NOT + LOCAL;
    public static final String NOT_TEST = NOT + TEST;
}
