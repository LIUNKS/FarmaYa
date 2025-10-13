package com.farma_ya.controller;

import com.farma_ya.model.Cart;
import com.farma_ya.model.User;
import com.farma_ya.service.ICartService;
import com.farma_ya.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Carrito", description = "Gestión del carrito de compras")
@SecurityRequirement(name = "Bearer Authentication")
@RestController
@RequestMapping("/api/cart")
public class CartController {

    private final ICartService cartService;

    private final UserService userService;

    public CartController(ICartService cartService, UserService userService) {
        this.cartService = cartService;
        this.userService = userService;
    }

    @Operation(summary = "Obtener carrito del usuario", description = "Obtiene el carrito actual del usuario autenticado")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Carrito obtenido exitosamente", content = @Content(schema = @Schema(implementation = Cart.class))),
            @ApiResponse(responseCode = "401", description = "Usuario no autenticado"),
            @ApiResponse(responseCode = "404", description = "Carrito no encontrado")
    })
    @GetMapping
    public ResponseEntity<Cart> getUserCart(@AuthenticationPrincipal UserDetails userDetails) {
        User currentUser = userService.getUserByUsername(userDetails.getUsername());
        Cart cart = cartService.getCartByUser(currentUser);
        return ResponseEntity.ok(cart);
    }

    @Operation(summary = "Agregar producto al carrito", description = "Agrega un producto al carrito del usuario con la cantidad especificada")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Producto agregado al carrito exitosamente", content = @Content(schema = @Schema(implementation = Cart.class))),
            @ApiResponse(responseCode = "400", description = "Parámetros inválidos"),
            @ApiResponse(responseCode = "401", description = "Usuario no autenticado"),
            @ApiResponse(responseCode = "404", description = "Producto no encontrado")
    })
    @PostMapping("/add")
    public ResponseEntity<Cart> addProductToCart(
            @AuthenticationPrincipal UserDetails userDetails,
            @Parameter(description = "ID del producto a agregar", required = true) @RequestParam Long productId,
            @Parameter(description = "Cantidad del producto (por defecto 1)") @RequestParam(defaultValue = "1") int quantity) {
        User currentUser = userService.getUserByUsername(userDetails.getUsername());
        Cart updatedCart = cartService.addToCart(currentUser, productId, quantity);
        return new ResponseEntity<>(updatedCart, HttpStatus.OK);
    }

    @Operation(summary = "Remover producto del carrito", description = "Elimina un producto específico del carrito")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Producto removido del carrito exitosamente", content = @Content(schema = @Schema(implementation = Cart.class))),
            @ApiResponse(responseCode = "401", description = "Usuario no autenticado"),
            @ApiResponse(responseCode = "404", description = "Producto no encontrado en el carrito")
    })
    @DeleteMapping("/remove/{productId}")
    public ResponseEntity<Cart> removeProductFromCart(
            @AuthenticationPrincipal UserDetails userDetails,
            @Parameter(description = "ID del producto a remover", required = true) @PathVariable Long productId) {
        User currentUser = userService.getUserByUsername(userDetails.getUsername());
        Cart updatedCart = cartService.removeFromCart(currentUser, productId);
        return ResponseEntity.ok(updatedCart);
    }

    @Operation(summary = "Vaciar carrito", description = "Elimina todos los productos del carrito del usuario")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Carrito vaciado exitosamente", content = @Content(schema = @Schema(implementation = Cart.class))),
            @ApiResponse(responseCode = "401", description = "Usuario no autenticado")
    })
    @DeleteMapping("/clear")
    public ResponseEntity<Cart> clearCart(@AuthenticationPrincipal UserDetails userDetails) {
        User currentUser = userService.getUserByUsername(userDetails.getUsername());
        Cart clearedCart = cartService.clearCart(currentUser);
        return ResponseEntity.ok(clearedCart);
    }
}