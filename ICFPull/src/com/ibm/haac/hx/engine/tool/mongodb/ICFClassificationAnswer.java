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

import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;

import java.util.Date;

/**
 * Classification answer class
 *
 * @author antonioj
 *
 */
@Entity("ICFClassificationAnswer")
public class ICFClassificationAnswer {

    @Id
    private ObjectId identifier = null;

    private String careGiver;
    private String patient;
    private String classificationCode;
    private String value;

    private Date dateCreated;

    public ObjectId getIdentifier() {
        return identifier;
    }

    public void setIdentifier(ObjectId identifier) {
        this.identifier = identifier;
    }

    public String getCareGiver() {
        return careGiver;
    }

    public void setCareGiver(String careGiver) {
        this.careGiver = careGiver;
    }

    public String getPatient() {
        return patient;
    }

    public void setPatient(String patient) {
        this.patient = patient;
    }

    public String getClassificationCode() {
        return classificationCode;
    }

    public void setClassificationCode(String classificationCode) {
        this.classificationCode = classificationCode;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public Date getDateCreated() {
        return dateCreated;
    }

    public void setDateCreated(Date dateCreated) {
        this.dateCreated = dateCreated;
    }
}
