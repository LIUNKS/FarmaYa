package com.farma_ya.model;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/**
 * Convierte entre el enum {@link OrderStatus} y el valor almacenado en la BD.
 *
 * Algunas bases de datos ya contienen valores capitalizados (por ejemplo
 * "Pendiente")
 * mientras que el enum en Java puede estar en mayúsculas (PENDIENTE). Este
 * converter
 * permite una lectura tolerante (case-insensitive) y escribe en la BD con la
 * forma
 * capitalizada (Primera letra mayúscula, resto minúscula) para mantener
 * compatibilidad.
 */
@Converter
public class OrderStatusConverter implements AttributeConverter<OrderStatus, String> {

    @Override
    public String convertToDatabaseColumn(OrderStatus attribute) {
        if (attribute == null)
            return null;
        // Escribir en DB como 'Pendiente', 'Procesando', etc.
        String name = attribute.name().toLowerCase();
        return Character.toUpperCase(name.charAt(0)) + name.substring(1);
    }

    @Override
    public OrderStatus convertToEntityAttribute(String dbData) {
        if (dbData == null)
            return null;
        // Intentar mapear ignorando mayúsculas/minúsculas
        for (OrderStatus status : OrderStatus.values()) {
            if (status.name().equalsIgnoreCase(dbData)) {
                return status;
            }
            // También comparar con la forma capitalizada usada en BD
            String capitalized = status.name().toLowerCase();
            capitalized = Character.toUpperCase(capitalized.charAt(0)) + capitalized.substring(1);
            if (capitalized.equals(dbData)) {
                return status;
            }
        }
        // Si no se encuentra, devolver null para evitar lanzar excepciones de
        // conversión
        return null;
    }
}
