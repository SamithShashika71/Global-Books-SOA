package com.globalbooks.catalog.model;

import javax.xml.bind.annotation.*;
import java.math.BigDecimal;

@XmlRootElement(name = "SearchCriteria")
@XmlAccessorType(XmlAccessType.FIELD)
public class SearchCriteria {

    @XmlElement
    private String keyword;

    @XmlElement
    private String category;

    @XmlElement
    private String author;

    @XmlElement
    private BigDecimal minPrice;

    @XmlElement
    private BigDecimal maxPrice;

    @XmlElement(defaultValue = "false")
    private boolean inStockOnly = false;

    @XmlElement(defaultValue = "100")
    private int maxResults = 100;

    // Default constructor
    public SearchCriteria() {}

    // Getters and Setters
    public String getKeyword() { return keyword; }
    public void setKeyword(String keyword) { this.keyword = keyword; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getAuthor() { return author; }
    public void setAuthor(String author) { this.author = author; }

    public BigDecimal getMinPrice() { return minPrice; }
    public void setMinPrice(BigDecimal minPrice) { this.minPrice = minPrice; }

    public BigDecimal getMaxPrice() { return maxPrice; }
    public void setMaxPrice(BigDecimal maxPrice) { this.maxPrice = maxPrice; }

    public boolean isInStockOnly() { return inStockOnly; }
    public void setInStockOnly(boolean inStockOnly) { this.inStockOnly = inStockOnly; }

    public int getMaxResults() { return maxResults; }
    public void setMaxResults(int maxResults) { this.maxResults = maxResults; }
}
