package com.penske.apps.smccore.unitdateutility;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.penske.apps.smccore.unitdateutility.enums.DateErrorCode;
import com.penske.apps.smccore.unitdateutility.enums.DateFieldName;

public class UnitDateValidator {

    public static boolean validateUnitDates(Date issueDate, Date orgReqDate, Date estProdDate, Date actProdDate, Date orgEstDelDate, Date estDelDate, Date actDelDate) {

        List<DateErrorCode> unitDateErrorCodes = getUnitDateErrorCodes(issueDate, orgReqDate, estProdDate, actProdDate, orgEstDelDate, estDelDate, actDelDate);

        return unitDateErrorCodes.size() == 0;
    }

    public static Map<DateFieldName, List<DateErrorCode>> getUnitDateErrorCodesAsMap(Date issueDate, Date orgReqDate, Date estProdDate, Date actProdDate, Date orgEstDelDate, Date estDelDate, Date actDelDate) {

        List<DateErrorCode> unitDateErrorCodes = getUnitDateErrorCodes(issueDate, orgReqDate, estProdDate, actProdDate, orgEstDelDate, estDelDate, actDelDate);

        Map<DateFieldName, List<DateErrorCode>> errorCodesByFieldName = new HashMap<DateFieldName, List<DateErrorCode>>();

        for (DateErrorCode errorCode : unitDateErrorCodes) {

            DateFieldName fieldName = errorCode.getFieldName();

            boolean hasField = errorCodesByFieldName.containsKey(fieldName);

            if (hasField) {
                errorCodesByFieldName.get(fieldName).add(errorCode);
            } else {
                errorCodesByFieldName.put(fieldName, new ArrayList<DateErrorCode>(Arrays.asList(errorCode)));
            }

        }

        return errorCodesByFieldName;

    }

    public static List<DateErrorCode> getUnitDateErrorCodes(Date issueDate, Date orgReqDate, Date estProdDate, Date actProdDate, Date orgEstDelDate, Date estDelDate, Date actDelDate) {

        if (issueDate == null)
            throw new IllegalArgumentException("Issue Date must be provided for date valdiation");

        List<DateErrorCode> errorCodes = new ArrayList<>();

        boolean notNullActProdDate = actProdDate != null;
        boolean notNullActDelDate = actDelDate != null;

        boolean nullEstDelDate = estDelDate == null;

        // Null checks and Issue Date Checks **********************************************
        if (orgReqDate == null)
            errorCodes.add(DateErrorCode.NULL_ORG_REQ_DATE);
        else if (orgReqDate.before(issueDate))
            errorCodes.add(DateErrorCode.ORG_REQ_DATE_BEFORE_ISSUE);

        if (estProdDate == null)
            errorCodes.add(DateErrorCode.NULL_EST_PROD_DATE);
        else if (estProdDate.before(issueDate))
            errorCodes.add(DateErrorCode.EST_PROD_DATE_BEFORE_ISSUE);

        if (notNullActProdDate && actProdDate.before(issueDate))
            errorCodes.add(DateErrorCode.ACT_PROD_DATE_BEFORE_ISSUE);

        if (orgEstDelDate == null)
            errorCodes.add(DateErrorCode.NULL_ORG_EST_DEL_DATE);
        else if (orgEstDelDate.before(issueDate))
            errorCodes.add(DateErrorCode.ORG_EST_DEL_DATE_BEFORE_ISSUE);

        if (nullEstDelDate)
            errorCodes.add(DateErrorCode.NULL_EST_DEL_DATE);
        else if (estDelDate.before(issueDate))
            errorCodes.add(DateErrorCode.EST_DEL_DATE_BEFORE_ISSUE);

        if (notNullActDelDate && actDelDate.before(issueDate))
            errorCodes.add(DateErrorCode.ACT_DEL_DATE_BEFORE_ISSUE);

        // If Act Del date is supplied, then Act Prod date better not be null
        if (notNullActDelDate && !notNullActProdDate)
            errorCodes.add(DateErrorCode.NULL_ACT_PROD_DATE);

        // Min/Max checks **********************************************
        if (orgReqDate != null && orgEstDelDate != null && orgReqDate.after(orgEstDelDate))
            errorCodes.add(DateErrorCode.ORG_REQ_DATE_AFTER_ORG_EST_DEL_DATE);

        if (notNullActProdDate && !nullEstDelDate && estDelDate.before(actProdDate))// If there is a actual prod date, est del better not be before it
            errorCodes.add(DateErrorCode.EST_DEL_DATE_BEFORE_ACT_PROD_DATE);
        else if (!notNullActProdDate && estProdDate != null && !nullEstDelDate && estDelDate.before(estProdDate))// Else if there is just a est prod date, est del better not be before it
            errorCodes.add(DateErrorCode.EST_DEL_DATE_BEFORE_EST_PROD_DATE);

        if (notNullActProdDate && notNullActDelDate && actProdDate.after(actDelDate))
            errorCodes.add(DateErrorCode.ACT_PROD_DATE_AFTER_ACT_DELV_DATE);
        if (notNullActProdDate && actProdDate.after(new Date()))
            errorCodes.add(DateErrorCode.ACT_PROD_DATE_AFTER_TODAY);
        if (notNullActDelDate && actDelDate.after(new Date()))
            errorCodes.add(DateErrorCode.ACT_DEL_DATE_AFTER_TODAY);


        return errorCodes;
    }
}
