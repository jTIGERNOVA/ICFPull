/******************************************************************************
 * Licensed Materials - Property of IBM
 * "Restricted Materials of IBM"
 * © Copyright IBM Corp. 2014 All Rights Reserved.
 * <p/>
 * Copying, redistribution and/or modification is prohibited.
 * U.S. Government Users Restricted Rights - Use, duplication or
 * disclosure restricted by GSA ADP Schedule Contract with IBM Corp.
 *****************************************************************************/
package com.ibm.haac.hx.engine.tool.mongodb;

/**
 * Class to handle ICF Classification Answer storage
 *
 * @author antonioj
 *
 */
public class ICFClassificationAnswerDA {

    private final IDBAccess dbAccess;

    /**
     * Constructor
     *
     * @param dbAccess
     *            DB access
     */
    public ICFClassificationAnswerDA(IDBAccess dbAccess) {
        this.dbAccess = dbAccess;
        // ensure db collection exists
        this.dbAccess.mapEntity(ICFClassificationAnswer.class);
    }

    public void save(ICFClassificationAnswer result) {
        if (result == null)
            return;

        dbAccess.getDatastore().save(result);
    }
}
