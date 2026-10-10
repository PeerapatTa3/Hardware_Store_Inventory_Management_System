package com.hardwarestore.dto.response;

import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
public class DashboardSummaryResponse {
    private long products;
    private long stockUnits;
    private long lowStock;
    private long purchases;
    private long orders;
    private long suppliers;
    private long customers;
    private List<SalesOrderResponse> recentOrders;
}