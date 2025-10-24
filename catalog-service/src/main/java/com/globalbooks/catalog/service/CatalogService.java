package com.globalbooks.catalog.service;

import com.globalbooks.catalog.model.*;
import com.globalbooks.catalog.exception.CatalogException;
import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;
import java.util.List;

@WebService(targetNamespace = "http://globalbooks.com/services/catalog/v1")
@SOAPBinding(style = SOAPBinding.Style.DOCUMENT, use = SOAPBinding.Use.LITERAL)
public interface CatalogService {

    @WebMethod
    @WebResult(name = "product")
    Product getProductById(@WebParam(name = "productId") String productId) throws CatalogException;

    @WebMethod
    @WebResult(name = "products")
    List<Product> searchProducts(@WebParam(name = "criteria") SearchCriteria criteria) throws CatalogException;

    @WebMethod
    @WebResult(name = "priceQuote")
    PriceQuote getProductPrice(
            @WebParam(name = "productId") String productId,
            @WebParam(name = "quantity") int quantity
    ) throws CatalogException;

    @WebMethod
    @WebResult(name = "inventoryStatus")
    InventoryStatus checkInventory(@WebParam(name = "productId") String productId) throws CatalogException;

    @WebMethod
    @WebResult(name = "success")
    boolean updateInventory(
            @WebParam(name = "productId") String productId,
            @WebParam(name = "quantity") int quantity,
            @WebParam(name = "operation") String operation
    ) throws CatalogException;
}