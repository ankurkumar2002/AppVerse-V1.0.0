
package com.appverse.order_service.repository;

import com.appverse.order_service.model.CustomerOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<CustomerOrder, String> {
    List<CustomerOrder> findByUserIdOrderByCreatedAtDesc(String userId);
    Optional<CustomerOrder> findByIdAndUserId(String orderId, String userId); // For operations needing ownership check
}