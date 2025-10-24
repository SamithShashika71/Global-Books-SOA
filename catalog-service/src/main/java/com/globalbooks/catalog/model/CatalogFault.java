package com.globalbooks.catalog.model;

import javax.xml.bind.annotation.*;
import java.util.Date;

@XmlRootElement(name = "CatalogFault")
@XmlAccessorType(XmlAccessType.FIELD)
public class CatalogFault {

    @XmlElement(required = true)
    private String errorCode;

    @XmlElement(required = true)
    private String errorMessage;

    @XmlElement
    private String detail;

    @XmlElement(required = true)
    @XmlSchemaType(name = "dateTime")
    private Date timestamp;

    // Default constructor
    public CatalogFault() {
        this.timestamp = new Date();
    }

    // Constructor with error details
    public CatalogFault(String errorCode, String errorMessage, String detail) {
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
        this.detail = detail;
        this.timestamp = new Date();
    }

    // Getters and Setters
    public String getErrorCode() { return errorCode; }
    public void setErrorCode(String errorCode) { this.errorCode = errorCode; }

    public String getErrorMessage() { return errorMessage; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }

    public String getDetail() { return detail; }
    public void setDetail(String detail) { this.detail = detail; }

    public Date getTimestamp() { return timestamp; }
    public void setTimestamp(Date timestamp) { this.timestamp = timestamp; }
}