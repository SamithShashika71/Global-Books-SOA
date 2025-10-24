package com.globalbooks.catalog.exception;

import javax.xml.ws.WebFault;

@WebFault(name = "CatalogFault", targetNamespace = "http://globalbooks.com/services/catalog/v1")
public class CatalogException extends Exception {

    private String errorCode;
    private String errorMessage;

    public CatalogException() {
        super();
    }

    public CatalogException(String errorCode, String errorMessage) {
        super(errorMessage);
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
    }

    public CatalogException(String errorCode, String errorMessage, Throwable cause) {
        super(errorMessage, cause);
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public String getFaultInfo() {
        return "Error Code: " + errorCode + ", Message: " + errorMessage;
    }
}