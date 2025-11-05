Feature: Order Management
  Como farmacia FarmaYa
  Quiero gestionar pedidos de medicamentos
  Para brindar un servicio eficiente a mis clientes

  Background:
    Given the system is running
    And the database is initialized

  Scenario: Successfully process a customer order
    Given a customer has placed an order with ID "ORD-001"
    When the admin processes the order
    Then the order status should be "PROCESANDO"
    When the admin assigns a delivery person
    Then the order should be assigned to a delivery person
    When the delivery person marks the order as "ENVIADO"
    Then the order status should be "ENVIADO"
    When the delivery person marks the order as "ENTREGADO"
    Then the order status should be "ENTREGADO"
    And the delivery person should receive payment

  Scenario: Customer views their order history
    Given a customer is logged in
    And the customer has placed multiple orders
    When the customer requests their order history
    Then they should see all their orders
    And each order should show its status and details

  Scenario: Delivery person manages assigned orders
    Given a delivery person is logged in
    And they have assigned orders
    When they view their assigned orders
    Then they should see only orders assigned to them
    When they update an order status
    Then the order status should be updated
    And they should see updated statistics

  Scenario: Admin manages all orders
    Given an admin is logged in
    When they view all orders in the system
    Then they should see orders from all customers
    When they update any order status
    Then the order status should be updated
    When they assign delivery persons
    Then orders should be properly assigned

  Scenario: Order status validation
    Given an order exists with status "PENDIENTE"
    When trying to mark it as "ENTREGADO" directly
    Then the operation should be rejected
    And the order status should remain "ENTREGADO"

  Scenario: Delivery statistics
    Given a delivery person is logged in
    And a delivery person has completed deliveries
    When they request their statistics
    Then they should see:
      | pending_orders | in_process_orders | delivered_orders | total_earnings |
      | 0             | 1                 | 2               | 75.0          |