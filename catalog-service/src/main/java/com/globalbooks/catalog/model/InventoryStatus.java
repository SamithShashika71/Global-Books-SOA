package com.globalbooks.catalog.model;

import javax.xml.bind.annotation.*;
import java.util.Date;

@XmlRootElement(name = "InventoryStatus")
@XmlAccessorType(XmlAccessType.FIELD)
public class InventoryStatus {

    @XmlElement(required = true)
    private String productId;

    @XmlElement(required = true)
    private int availableQuantity;

    @XmlElement(required = true)
    private int reservedQuantity;

    @XmlElement(required = true)
    private boolean inStock;

    @XmlElement
    private String warehouseLocation;

    @XmlElement
    @XmlSchemaType(name = "date")
    private Date restockDate;

    @XmlElement(required = true)
    @XmlSchemaType(name = "dateTime")
    private Date lastUpdated;

    // Default constructor
    public InventoryStatus() {
        this.lastUpdated = new Date();
    }

    // Full constructor
    public InventoryStatus(String productId, int availableQuantity, int reservedQuantity) {
        this.productId = productId;
        this.availableQuantity = availableQuantity;
        this.reservedQuantity = reservedQuantity;
        this.inStock = availableQuantity > 0;
        this.lastUpdated = new Date();
    }

    // Getters and Setters
    public String getProductId() { return productId; }
    public void setProductId(String productId) { this.productId = productId; }

    public int getAvailableQuantity() { return availableQuantity; }
    public void setAvailableQuantity(int availableQuantity) {
        this.availableQuantity = availableQuantity;
        this.inStock = availableQuantity > 0;
    }

    public int getReservedQuantity() { return reservedQuantity; }
    public void setReservedQuantity(int reservedQuantity) { this.reservedQuantity = reservedQuantity; }

    public boolean isInStock() { return inStock; }
    public void setInStock(boolean inStock) { this.inStock = inStock; }

    public String getWarehouseLocation() { return warehouseLocation; }
    public void setWarehouseLocation(String warehouseLocation) { this.warehouseLocation = warehouseLocation; }

    public Date getRestockDate() { return restockDate; }
    public void setRestockDate(Date restockDate) { this.restockDate = restockDate; }

    public Date getLastUpdated() { return lastUpdated; }
    public void setLastUpdated(Date lastUpdated) { this.lastUpdated = lastUpdated; }
}