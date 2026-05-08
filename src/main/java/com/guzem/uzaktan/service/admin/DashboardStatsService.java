package com.guzem.uzaktan.service.admin;

import java.util.Map;

/**
 * Aggregates dashboard statistics from multiple services.
 */
public interface DashboardStatsService {

    Map<String, Object> getDashboardStats();
}
