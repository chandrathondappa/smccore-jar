package com.penske.apps.smccore.unitdateutility.enums;

public enum DateFieldName {
    
    ORG_REQ_DATE,
    ORG_EST_DEL_DATE,
    EST_PROD_DATE,
    ACT_PROD_DATE,
    EST_DEL_DATE,
    ACT_DEL_DATE
    ;
    
    public static DateFieldName findByName(String name) {
        
        for(DateFieldName errorCode : values())
            if(errorCode.name().equals(name)) return errorCode;
        
        return null;
    }
}
