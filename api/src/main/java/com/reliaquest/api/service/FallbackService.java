package com.reliaquest.api.service;

import com.reliaquest.api.model.Employee;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Provides fallback responses when the main service is unavailable.
 */
@Service
@Slf4j
public class FallbackService {

    /**
     * Fallback response for getAllEmployees when service is unavailable.
     */
    public List<Employee> getFallbackEmployees() {
        log.warn("Using fallback response for getAllEmployees");
        return Collections.emptyList();
    }

    /**
     * Fallback response for getEmployeeById when service is unavailable.
     */
    public Optional<Employee> getFallbackEmployee(String id) {
        log.warn("Using fallback response for getEmployeeById: {}", id);
        return Optional.empty();
    }

    /**
     * Fallback response for getHighestSalary when service is unavailable.
     */
    public Integer getFallbackHighestSalary() {
        log.warn("Using fallback response for getHighestSalary");
        return 0;
    }

    /**
     * Fallback response for getTop10HighestEarningEmployeeNames when service is unavailable.
     */
    public List<String> getFallbackTopEarners() {
        log.warn("Using fallback response for getTop10HighestEarningEmployeeNames");
        return Collections.emptyList();
    }

    /**
     * Fallback response for searchEmployeesByName when service is unavailable.
     */
    public List<Employee> getFallbackSearchResults(String searchString) {
        log.warn("Using fallback response for searchEmployeesByName: {}", searchString);
        return Collections.emptyList();
    }
}
