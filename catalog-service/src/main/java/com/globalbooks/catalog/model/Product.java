package com.globalbooks.catalog.model;

import javax.xml.bind.annotation.*;
import java.math.BigDecimal;
import java.util.Date;

@XmlRootElement(name = "Product")
@XmlType(propOrder = {"productId", "title", "author", "isbn", "description",
        "category", "price", "currency", "stockQuantity", "publishDate", "imageUrl"})
@XmlAccessorType(XmlAccessType.FIELD)
public class Product {

    @XmlElement(required = true)
    private String productId;

    @XmlElement(required = true)
    private String title;

    @XmlElement(required = true)
    private String author;

    @XmlElement(required = true)
    private String isbn;

    @XmlElement
    private String description;

    @XmlElement(required = true)
    private String category;

    @XmlElement(required = true)
    private BigDecimal price;

    @XmlElement(defaultValue = "USD")
    private String currency = "USD";

    @XmlElement(required = true)
    private int stockQuantity;

    @XmlElement
    @XmlSchemaType(name = "date")
    private Date publishDate;

    @XmlElement
    private String imageUrl;

    // Default constructor
    public Product() {}

    // Full constructor
    public Product(String productId, String title, String author, String isbn,
                   String category, BigDecimal price, int stockQuantity) {
        this.productId = productId;
        this.title = title;
        this.author = author;
        this.isbn = isbn;
        this.category = category;
        this.price = price;
        this.stockQuantity = stockQuantity;
    }

    // Getters and Setters
    public String getProductId() { return productId; }
    public void setProductId(String productId) { this.productId = productId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getAuthor() { return author; }
    public void setAuthor(String author) { this.author = author; }

    public String getIsbn() { return isbn; }
    public void setIsbn(String isbn) { this.isbn = isbn; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }

    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }

    public int getStockQuantity() { return stockQuantity; }
    public void setStockQuantity(int stockQuantity) { this.stockQuantity = stockQuantity; }

    public Date getPublishDate() { return publishDate; }
    public void setPublishDate(Date publishDate) { this.publishDate = publishDate; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
}