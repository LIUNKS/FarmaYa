package com.farma_ya.controller;

import com.farma_ya.model.Product;
import com.farma_ya.service.IProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Productos", description = "Gestión de productos farmacéuticos")
@RestController
@RequestMapping("/api/products")
public class ProductController {

        @Autowired
        private final IProductService productService;

        public ProductController(IProductService productService) {
                this.productService = productService;
        }

        @Operation(summary = "Obtener todos los productos", description = "Retorna una lista de todos los productos disponibles")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Lista de productos obtenida exitosamente", content = @Content(schema = @Schema(implementation = Product.class)))
        })
        @GetMapping
        public ResponseEntity<List<Product>> getAllProducts() {
                List<Product> products = productService.getAllProducts();
                return ResponseEntity.ok(products);
        }

        @Operation(summary = "Obtener producto por ID", description = "Retorna un producto específico por su ID")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Producto encontrado", content = @Content(schema = @Schema(implementation = Product.class))),
                        @ApiResponse(responseCode = "404", description = "Producto no encontrado")
        })
        @GetMapping("/{id}")
        public ResponseEntity<Product> getProductById(
                        @Parameter(description = "ID del producto", required = true) @PathVariable Integer id) {
                Product product = productService.getProductById(id);
                return ResponseEntity.ok(product);
        }

        @Operation(summary = "Crear nuevo producto", description = "Crea un nuevo producto (solo administradores)")
        @SecurityRequirement(name = "Bearer Authentication")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "201", description = "Producto creado exitosamente", content = @Content(schema = @Schema(implementation = Product.class))),
                        @ApiResponse(responseCode = "400", description = "Datos inválidos"),
                        @ApiResponse(responseCode = "401", description = "No autorizado"),
                        @ApiResponse(responseCode = "403", description = "Acceso denegado - Solo administradores")
        })
        @PostMapping
        @PreAuthorize("hasRole('ADMIN')")
        public ResponseEntity<Product> createProduct(@Valid @RequestBody Product product) {
                Product createdProduct = productService.createProduct(product);
                return new ResponseEntity<>(createdProduct, HttpStatus.CREATED);
        }

        @Operation(summary = "Actualizar producto", description = "Actualiza un producto existente (solo administradores)")
        @SecurityRequirement(name = "Bearer Authentication")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Producto actualizado exitosamente", content = @Content(schema = @Schema(implementation = Product.class))),
                        @ApiResponse(responseCode = "400", description = "Datos inválidos"),
                        @ApiResponse(responseCode = "401", description = "No autorizado"),
                        @ApiResponse(responseCode = "403", description = "Acceso denegado - Solo administradores"),
                        @ApiResponse(responseCode = "404", description = "Producto no encontrado")
        })
        @PutMapping("/{id}")
        @PreAuthorize("hasRole('ADMIN')")
        public ResponseEntity<Product> updateProduct(
                        @Parameter(description = "ID del producto", required = true) @PathVariable Integer id,
                        @Valid @RequestBody Product productDetails) {
                Product updatedProduct = productService.updateProduct(id, productDetails);
                return ResponseEntity.ok(updatedProduct);
        }

        @Operation(summary = "Eliminar producto", description = "Elimina un producto del sistema (solo administradores)")
        @SecurityRequirement(name = "Bearer Authentication")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "204", description = "Producto eliminado exitosamente"),
                        @ApiResponse(responseCode = "401", description = "No autorizado"),
                        @ApiResponse(responseCode = "403", description = "Acceso denegado - Solo administradores"),
                        @ApiResponse(responseCode = "404", description = "Producto no encontrado")
        })
        @DeleteMapping("/{id}")
        @PreAuthorize("hasRole('ADMIN')")
        public ResponseEntity<Void> deleteProduct(
                        @Parameter(description = "ID del producto", required = true) @PathVariable Integer id) {
                productService.deleteProduct(id);
                return ResponseEntity.noContent().build();
        }

        @Operation(summary = "Buscar productos", description = "Busca productos por nombre")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Búsqueda realizada exitosamente", content = @Content(schema = @Schema(implementation = Product.class)))
        })
        @GetMapping("/search")
        public ResponseEntity<List<Product>> searchProducts(
                        @Parameter(description = "Nombre del producto a buscar") @RequestParam(required = false) String name) {
                List<Product> products = productService.searchProducts(name);
                return ResponseEntity.ok(products);
        }

        @Operation(summary = "Obtener productos por categoría", description = "Retorna productos de una categoría específica")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Productos de la categoría obtenidos exitosamente", content = @Content(schema = @Schema(implementation = Product.class)))
        })
        @GetMapping("/category/{category}")
        public ResponseEntity<List<Product>> getProductsByCategory(
                        @Parameter(description = "Nombre de la categoría", required = true) @PathVariable String category) {
                List<Product> products = productService.getProductsByCategory(category);
                return ResponseEntity.ok(products);
        }
}