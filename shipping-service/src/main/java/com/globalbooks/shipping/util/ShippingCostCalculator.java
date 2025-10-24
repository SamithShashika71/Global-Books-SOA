package com.globalbooks.shipping.util;

import com.globalbooks.shipping.model.ShippingMethod;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class ShippingCostCalculator {

    public BigDecimal calculate(BigDecimal weight, ShippingMethod method) {
        BigDecimal baseCost = switch (method) {
            case SAME_DAY -> new BigDecimal("25.00");
            case OVERNIGHT -> new BigDecimal("20.00");
            case TWO_DAY -> new BigDecimal("15.00");
            case EXPRESS -> new BigDecimal("12.00");
            case STANDARD -> new BigDecimal("8.00");
            case ECONOMY -> new BigDecimal("5.00");
        };

        // Add weight-based cost (e.g., $2 per pound)
        BigDecimal weightCost = weight.multiply(new BigDecimal("2.00"));

        return baseCost.add(weightCost);
    }
}