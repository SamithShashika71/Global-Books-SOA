package com.globalbooks.catalog.security;

import javax.xml.namespace.QName;
import javax.xml.soap.*;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.soap.SOAPHandler;
import javax.xml.ws.handler.soap.SOAPMessageContext;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import java.util.Set;
import java.util.HashSet;
import java.util.Base64;
import java.security.MessageDigest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WSSecurityHandler implements SOAPHandler<SOAPMessageContext> {

    private static final Logger logger = LoggerFactory.getLogger(WSSecurityHandler.class);
    private static final String WSSE_NS = "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd";
    private static final String WSU_NS = "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd";
    private static final String WSSE_PREFIX = "wsse";
    private static final String WSU_PREFIX = "wsu";

    @Override
    public boolean handleMessage(SOAPMessageContext context) {
        Boolean outbound = (Boolean) context.get(MessageContext.MESSAGE_OUTBOUND_PROPERTY);

        if (!outbound) {
            // Inbound message - validate security
            try {
                SOAPMessage message = context.getMessage();
                SOAPHeader header = message.getSOAPHeader();

                if (header == null) {
                    logger.error("No SOAP header found in request");
                    generateSOAPFault(message, "Missing SOAP header");
                    return false;
                }

                // Check for Security element
                NodeList securityNodes = header.getElementsByTagNameNS(WSSE_NS, "Security");
                if (securityNodes.getLength() == 0) {
                    logger.error("No WS-Security header found");
                    generateSOAPFault(message, "Missing WS-Security header");
                    return false;
                }

                Element securityElement = (Element) securityNodes.item(0);

                // Check for UsernameToken
                NodeList usernameTokenNodes = securityElement.getElementsByTagNameNS(WSSE_NS, "UsernameToken");
                if (usernameTokenNodes.getLength() == 0) {
                    logger.error("No UsernameToken found in Security header");
                    generateSOAPFault(message, "Missing UsernameToken");
                    return false;
                }

                Element usernameToken = (Element) usernameTokenNodes.item(0);

                // Extract username and password
                NodeList usernameNodes = usernameToken.getElementsByTagNameNS(WSSE_NS, "Username");
                NodeList passwordNodes = usernameToken.getElementsByTagNameNS(WSSE_NS, "Password");

                if (usernameNodes.getLength() == 0 || passwordNodes.getLength() == 0) {
                    logger.error("Username or Password missing in UsernameToken");
                    generateSOAPFault(message, "Invalid UsernameToken");
                    return false;
                }

                String username = usernameNodes.item(0).getTextContent();
                Element passwordElement = (Element) passwordNodes.item(0);
                String password = passwordElement.getTextContent();
                String passwordType = passwordElement.getAttribute("Type");

                // Validate credentials
                if (!validateCredentials(username, password, passwordType)) {
                    logger.error("Invalid credentials for user: {}", username);
                    generateSOAPFault(message, "Authentication failed");
                    return false;
                }

                logger.info("User {} authenticated successfully", username);

                // Store username in context for potential use in service
                context.put("authenticated.user", username);
                context.setScope("authenticated.user", MessageContext.Scope.APPLICATION);

            } catch (Exception e) {
                logger.error("Error processing security header", e);
                return false;
            }
        }

        return true;
    }

    private boolean validateCredentials(String username, String password, String passwordType) {
        // Check if password is digest or plain text
        boolean isDigest = passwordType != null &&
                passwordType.contains("PasswordDigest");

        if (isDigest) {
            // For digest passwords, validate against hashed password
            return validateDigestPassword(username, password);
        } else {
            // For plain text passwords
            return validatePlainPassword(username, password);
        }
    }

    private boolean validatePlainPassword(String username, String password) {
        // In production, check against database or LDAP
        // For demo purposes, using hardcoded credentials
        if ("admin".equals(username) && "admin123".equals(password)) {
            return true;
        }
        if ("client1".equals(username) && "pass123".equals(password)) {
            return true;
        }
        if ("partner".equals(username) && "partner456".equals(password)) {
            return true;
        }
        return false;
    }

    private boolean validateDigestPassword(String username, String passwordDigest) {
        // In production, retrieve stored password hash from database
        // and compare with provided digest
        try {
            // For demo: create digest of known password and compare
            String knownPassword = getPasswordForUser(username);
            if (knownPassword == null) {
                return false;
            }

            MessageDigest digest = MessageDigest.getInstance("SHA-1");
            byte[] hash = digest.digest(knownPassword.getBytes());
            String expectedDigest = Base64.getEncoder().encodeToString(hash);

            return expectedDigest.equals(passwordDigest);
        } catch (Exception e) {
            logger.error("Error validating digest password", e);
            return false;
        }
    }

    private String getPasswordForUser(String username) {
        // In production, retrieve from database
        switch (username) {
            case "admin": return "admin123";
            case "client1": return "pass123";
            case "partner": return "partner456";
            default: return null;
        }
    }

    private void generateSOAPFault(SOAPMessage message, String reason) {
        try {
            SOAPBody body = message.getSOAPBody();
            body.removeContents();
            SOAPFault fault = body.addFault();
            fault.setFaultCode("SOAP-ENV:Client");
            fault.setFaultString("WS-Security Error: " + reason);
            fault.setFaultActor("CatalogService");

            // Add detail element
            Detail detail = fault.addDetail();
            SOAPElement detailEntry = detail.addChildElement("error", "sec",
                    "http://globalbooks.com/security");
            detailEntry.addTextNode(reason);

        } catch (SOAPException e) {
            logger.error("Error generating SOAP fault", e);
        }
    }

    @Override
    public boolean handleFault(SOAPMessageContext context) {
        return true;
    }

    @Override
    public void close(MessageContext context) {
        // Cleanup if needed
    }

    @Override
    public Set<QName> getHeaders() {
        // Fixed: Use HashSet instead of TreeSet
        Set<QName> headers = new HashSet<>();
        headers.add(new QName(WSSE_NS, "Security", WSSE_PREFIX));
        return headers;
    }
}