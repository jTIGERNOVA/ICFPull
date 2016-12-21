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

import org.apache.commons.lang3.StringUtils;
import org.bson.types.ObjectId;
import org.mongodb.morphia.mapping.Mapper;
import org.mongodb.morphia.query.Query;
import org.mongodb.morphia.query.UpdateOperations;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Class for handling user interface storage
 *
 * @author antonioj
 */
//https://github.com/mongodb/morphia/wiki/Updating
public class MongoStorage {

    private MongoDBAccess db;
    private ICFClassificationAnswerDA classificationAnswerDA;

    /**
     * Database properties are loaded and a connection is established
     */
    public MongoStorage() {
        try {
            db = new MongoDBAccess();
            classificationAnswerDA = new ICFClassificationAnswerDA(db);
        } catch (Exception e) {
            e.printStackTrace();

            throw new IllegalStateException("Could not connect to database");
        }
    }

    /**
     * Stores user interface. No fields can be null.
     *
     * @param answer Classification answer
     * @return The new identifier (ID). Null is returned if failure.
     */
    public ObjectId storeICFAnswer(ICFClassificationAnswer answer) {

        if (answer == null || StringUtils.isEmpty(answer.getCareGiver())
                || StringUtils.isEmpty(answer.getClassificationCode())
                || StringUtils.isEmpty(answer.getPatient())
                || StringUtils.isEmpty(answer.getValue())) {
            return null;
        }

        answer.setDateCreated(new Date());

        classificationAnswerDA.save(answer);

        return answer.getIdentifier();
    }

    public void updateInterface(ObjectId identifier, String pageName, String reportJson, String stat) {

        if (StringUtils.isEmpty(pageName) || StringUtils.isEmpty(stat) || StringUtils.isEmpty(reportJson) || StringUtils.isEmpty(stat))
            return;

        Query<ICFClassificationAnswer> updateQuery = db.getDatastore().createQuery(ICFClassificationAnswer.class).filter(Mapper.ID_KEY, identifier);
        UpdateOperations<ICFClassificationAnswer> ops = db.getDatastore().createUpdateOperations(ICFClassificationAnswer.class).set("reportJSONData", reportJson);
        db.getDatastore().update(updateQuery, ops);

        ops = db.getDatastore().createUpdateOperations(ICFClassificationAnswer.class).set("statisticData", stat);
        db.getDatastore().update(updateQuery, ops);

        ops = db.getDatastore().createUpdateOperations(ICFClassificationAnswer.class).set("pageName", pageName);
        db.getDatastore().update(updateQuery, ops);
    }

    /**
     * Gets the user interface results
     *
     * @param userName
     * @param platform
     * @param packageName
     * @return User interface results, ordered with the latest first
     */
    public List<ICFClassificationAnswer> getReportResults(String userName, String platform,
                                               String packageName) {
        List<ICFClassificationAnswer> found = db.getDatastore()
                .find(ICFClassificationAnswer.class).filter("userName", userName)
                .filter("platform", platform)
                .filter("packageName", packageName).order("pageNumber")
                .limit(MongoDBAccess.readLimit).asList();

        ArrayList<ICFClassificationAnswer> results = new ArrayList<ICFClassificationAnswer>();

        return results;
    }

    /**
     * Gets the user interface result
     *
     * @param userName
     * @param platform
     * @param packageName
     * @param pageNumber
     * @return User interface result, null if none is found. If more that one is
     * found, the latest is returned
     */
    public ICFClassificationAnswer getReportResult(String userName, String platform,
                                        String packageName, int pageNumber) {
        ICFClassificationAnswer found = db.getDatastore().find(ICFClassificationAnswer.class)
                .filter("userName", userName).filter("platform", platform)
                .filter("packageName", packageName)
                .filter("pageNumber", pageNumber).order("-dateCreated").get();

        if (found == null)
            return null;

        return null;
    }


}
