package com.penske.apps.smccore.unitdateutility.enums;

public enum DateErrorCode {

    /* Null Checks ****************************************************************************************************/
    NULL_ORG_REQ_DATE(DateFieldName.ORG_REQ_DATE, "Requested Production Date must not be null"),
    NULL_ACT_PROD_DATE(DateFieldName.ACT_PROD_DATE, "Actual Production Date must not be null if there is an Actual Delivery Date"),
    NULL_EST_PROD_DATE(DateFieldName.EST_PROD_DATE, "Estimated Production Date must not be null"),
    NULL_ORG_EST_DEL_DATE(DateFieldName.ORG_EST_DEL_DATE, "Requested Delivery Date must not be null"),
    NULL_EST_DEL_DATE(DateFieldName.EST_DEL_DATE, "Estimated Delivery Date must not be null"),

    /* Issue Date Checks **********************************************************************************************/
    ORG_REQ_DATE_BEFORE_ISSUE(DateFieldName.ORG_REQ_DATE, "Requested Production Date must be after issue date"),
    EST_PROD_DATE_BEFORE_ISSUE(DateFieldName.EST_PROD_DATE, "Estimated Production Date must be after issue date"),
    ACT_PROD_DATE_BEFORE_ISSUE(DateFieldName.ACT_PROD_DATE, "Actual Production Date must be after issue date"),
    ORG_EST_DEL_DATE_BEFORE_ISSUE(DateFieldName.ORG_EST_DEL_DATE, "Requested Delivery Date must be after issue date"),
    EST_DEL_DATE_BEFORE_ISSUE(DateFieldName.EST_DEL_DATE, "Estimated Delivery Date must be after issue date"),
    ACT_DEL_DATE_BEFORE_ISSUE(DateFieldName.ACT_DEL_DATE, "Actual Delivery Date must be after issue date"),

    /* Min Max Date Checks ********************************************************************************************/
    ORG_REQ_DATE_AFTER_ORG_EST_DEL_DATE(DateFieldName.ORG_REQ_DATE, "Requested Production Date must be less than Requested Delivery Date"),
    ACT_PROD_DATE_AFTER_ACT_DELV_DATE(DateFieldName.ACT_PROD_DATE, "Actual Production Date must be less than Actual Delivery Date"),
    ACT_PROD_DATE_AFTER_TODAY(DateFieldName.ACT_PROD_DATE, "Actual Production Date must not be greater than today"),
    ACT_DEL_DATE_AFTER_TODAY(DateFieldName.ACT_DEL_DATE, "Actual Delivery Date must not be greater than today"),
    EST_DEL_DATE_BEFORE_ACT_PROD_DATE(DateFieldName.EST_DEL_DATE, "Estimated Delivery Date must be greater or equal to the Actual Production Date"),
    EST_DEL_DATE_BEFORE_EST_PROD_DATE(DateFieldName.EST_DEL_DATE, "Estimated Delivery Date must be greater or equal to the Estimated Production Date"),
    ;

    private final DateFieldName fieldName;
    private final String errorMessage;


    private DateErrorCode(DateFieldName fieldName, String errorMessage) {
        this.fieldName = fieldName;
        this.errorMessage = errorMessage;
    }

    public static DateErrorCode findByName(String name) {

        for(DateErrorCode errorCode : values())
            if(errorCode.name().equals(name)) return errorCode;

        return null;
    }

    public String getErrorMessage() {
        return errorMessage;
    }
    public DateFieldName getFieldName() {
        return fieldName;
    }

}
