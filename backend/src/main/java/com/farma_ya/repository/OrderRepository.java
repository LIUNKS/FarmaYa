package com.farma_ya.repository;

import com.farma_ya.model.Order;
import com.farma_ya.model.OrderStatus;
import com.farma_ya.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByUser(User user);

    List<Order> findByUserId(Long userId);

    List<Order> findByStatus(String status);

    // Método para obtener pedidos entregados por rango de fechas
    @Query("SELECT o FROM Order o WHERE o.status = :status AND o.createdAt >= :fechaInicio AND o.createdAt <= :fechaFin")
    List<Order> findByStatusAndCreatedAtBetween(@Param("status") OrderStatus status,
            @Param("fechaInicio") LocalDateTime fechaInicio,
            @Param("fechaFin") LocalDateTime fechaFin);

    // Método específico para pedidos entregados por rango de fechas
    default List<Order> findPedidosEntregadosPorRangoFechas(java.time.LocalDate fechaInicio,
            java.time.LocalDate fechaFin) {
        LocalDateTime inicioDateTime = fechaInicio.atStartOfDay();
        LocalDateTime finDateTime = fechaFin.atTime(23, 59, 59);
        return findByStatusAndCreatedAtBetween(OrderStatus.DELIVERED, inicioDateTime, finDateTime);
    }
}