package com.globalbooks.catalog.model;

import javax.xml.bind.annotation.*;
import java.math.BigDecimal;
import java.util.Date;

@XmlRootElement(name = "PriceQuote")
@XmlAccessorType(XmlAccessType.FIELD)
public class PriceQuote {

    @XmlElement(required = true)
    private String productId;

    @XmlElement(required = true)
    private BigDecimal unitPrice;

    @XmlElement(required = true)
    private int quantity;

    @XmlElement(required = true)
    private BigDecimal subtotal;

    @XmlElement
    private BigDecimal discount;

    @XmlElement
    private BigDecimal tax;

    @XmlElement(required = true)
    private BigDecimal total;

    @XmlElement(required = true)
    private String currency = "USD";

    @XmlElement(required = true)
    @XmlSchemaType(name = "dateTime")
    private Date validUntil;

    // Default constructor
    public PriceQuote() {}

    // Calculation constructor
    public PriceQuote(String productId, BigDecimal unitPrice, int quantity) {
        this.productId = productId;
        this.unitPrice = unitPrice;
        this.quantity = quantity;
        calculateTotals();
    }

    private void calculateTotals() {
        this.subtotal = unitPrice.multiply(BigDecimal.valueOf(quantity));

        // Apply discount for bulk orders
        if (quantity >= 100) {
            this.discount = subtotal.multiply(BigDecimal.valueOf(0.15)); // 15% discount
        } else if (quantity >= 50) {
            this.discount = subtotal.multiply(BigDecimal.valueOf(0.10)); // 10% discount
        } else if (quantity >= 10) {
            this.discount = subtotal.multiply(BigDecimal.valueOf(0.05)); // 5% discount
        } else {
            this.discount = BigDecimal.ZERO;
        }

        BigDecimal afterDiscount = subtotal.subtract(discount);
        this.tax = afterDiscount.multiply(BigDecimal.valueOf(0.08)); // 8% tax
        this.total = afterDiscount.add(tax);

        // Quote valid for 7 days
        this.validUntil = new Date(System.currentTimeMillis() + (7 * 24 * 60 * 60 * 1000));
    }

    // Getters and Setters
    public String getProductId() { return productId; }
    public void setProductId(String productId) { this.productId = productId; }

    public BigDecimal getUnitPrice() { return unitPrice; }
    public void setUnitPrice(BigDecimal unitPrice) { this.unitPrice = unitPrice; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public BigDecimal getSubtotal() { return subtotal; }
    public void setSubtotal(BigDecimal subtotal) { this.subtotal = subtotal; }

    public BigDecimal getDiscount() { return discount; }
    public void setDiscount(BigDecimal discount) { this.discount = discount; }

    public BigDecimal getTax() { return tax; }
    public void setTax(BigDecimal tax) { this.tax = tax; }

    public BigDecimal getTotal() { return total; }
    public void setTotal(BigDecimal total) { this.total = total; }

    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }

    public Date getValidUntil() { return validUntil; }
    public void setValidUntil(Date validUntil) { this.validUntil = validUntil; }
}