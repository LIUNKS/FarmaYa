// data.js - Gestión de datos con integración al backend
// Este archivo maneja los datos locales (carrito) y se comunica con el backend vía API

// Clase para manejar los datos con integración al backend
class DataStore {
    constructor() {
        // Cache local solo para carrito (temporal mientras el usuario navega)
        this.carritoLocal = this.getCarritoLocal();
    }

    // ============ PRODUCTOS ============
    
    // Obtener productos desde el backend
    async getProductos() {
        try {
            return await ProductAPI.getAllProducts();
        } catch (error) {
            console.error('Error al obtener productos:', error);
            return [];
        }
    }

    // Obtener producto por ID desde el backend
    async getProductoById(id) {
        try {
            return await ProductAPI.getProductById(id);
        } catch (error) {
            console.error('Error al obtener producto:', error);
            return null;
        }
    }

    // Buscar productos
    async searchProductos(keyword) {
        try {
            return await ProductAPI.searchProducts(keyword);
        } catch (error) {
            console.error('Error al buscar productos:', error);
            return [];
        }
    }

    // Obtener productos por categoría
    async getProductosByCategoria(categoria) {
        try {
            return await ProductAPI.getProductsByCategory(categoria);
        } catch (error) {
            console.error('Error al filtrar productos:', error);
            return [];
        }
    }

    // ============ CATEGORÍAS ============
    
    // Obtener categorías únicas desde los productos
    async getCategorias() {
        try {
            const productos = await this.getProductos();
            const categoriasSet = new Set(productos.map(p => p.categoria).filter(c => c));
            return ['Todos', ...Array.from(categoriasSet).sort()];
        } catch (error) {
            console.error('Error al obtener categorías:', error);
            return ['Todos'];
        }
    }

    // ============ CARRITO (LOCAL) ============
    
    // Obtener carrito local (para usuarios no autenticados)
    getCarritoLocal() {
        const carrito = localStorage.getItem('carrito_local');
        return carrito ? JSON.parse(carrito) : [];
    }

    // Guardar carrito local
    saveCarritoLocal(carrito) {
        localStorage.setItem('carrito_local', JSON.stringify(carrito));
        this.carritoLocal = carrito;
    }

    // Obtener carrito (desde backend si está autenticado, local si no)
    async getCarrito() {
        const usuario = this.getCurrentUser();
        
        if (usuario) {
            // Usuario autenticado: obtener carrito del backend
            try {
                const carritoBackend = await CartAPI.getCart();
                // Convertir formato del backend al formato local
                return carritoBackend.items?.map(item => ({
                    id: item.product.id,
                    nombre: item.product.nombre,
                    precio: item.product.precio,
                    cantidad: item.quantity,
                    imagen: item.product.imagenUrl
                })) || [];
            } catch (error) {
                console.error('Error al obtener carrito del backend:', error);
                return this.carritoLocal;
            }
        } else {
            // Usuario no autenticado: usar carrito local
            return this.carritoLocal;
        }
    }

    // Agregar al carrito
    async addToCarrito(productoId, cantidad = 1) {
        const usuario = this.getCurrentUser();
        
        if (usuario) {
            // Usuario autenticado: agregar al carrito del backend
            try {
                await CartAPI.addToCart(productoId, cantidad);
                return true;
            } catch (error) {
                console.error('Error al agregar al carrito:', error);
                return false;
            }
        } else {
            // Usuario no autenticado: agregar al carrito local
            try {
                const producto = await this.getProductoById(productoId);
                if (!producto) return false;

                const itemExistente = this.carritoLocal.find(item => item.id === productoId);
                
                if (itemExistente) {
                    itemExistente.cantidad += cantidad;
                } else {
                    this.carritoLocal.push({
                        id: producto.id,
                        nombre: producto.nombre,
                        precio: producto.precio,
                        cantidad: cantidad,
                        imagen: producto.imagenUrl
                    });
                }
                
                this.saveCarritoLocal(this.carritoLocal);
                return true;
            } catch (error) {
                console.error('Error al agregar al carrito local:', error);
                return false;
            }
        }
    }

    // Actualizar cantidad en carrito
    async updateCarritoItem(productoId, cantidad) {
        const usuario = this.getCurrentUser();
        
        if (usuario) {
            // Usuario autenticado: actualizar en backend
            try {
                if (cantidad <= 0) {
                    await CartAPI.removeFromCart(productoId);
                } else {
                    // Remover y volver a agregar con la nueva cantidad
                    await CartAPI.removeFromCart(productoId);
                    await CartAPI.addToCart(productoId, cantidad);
                }
            } catch (error) {
                console.error('Error al actualizar carrito:', error);
            }
        } else {
            // Usuario no autenticado: actualizar carrito local
            const item = this.carritoLocal.find(item => item.id === productoId);
            
            if (item) {
                if (cantidad <= 0) {
                    this.carritoLocal = this.carritoLocal.filter(item => item.id !== productoId);
                } else {
                    item.cantidad = cantidad;
                }
                this.saveCarritoLocal(this.carritoLocal);
            }
        }
    }

    // Limpiar carrito
    async clearCarrito() {
        const usuario = this.getCurrentUser();
        
        if (usuario) {
            // Usuario autenticado: limpiar carrito del backend
            try {
                await CartAPI.clearCart();
            } catch (error) {
                console.error('Error al limpiar carrito:', error);
            }
        } else {
            // Usuario no autenticado: limpiar carrito local
            this.carritoLocal = [];
            this.saveCarritoLocal(this.carritoLocal);
        }
    }

    // ============ USUARIO ============

    // Obtener usuario actual (desde localStorage)
    getCurrentUser() {
        const usuario = localStorage.getItem('usuarioActual');
        return usuario ? JSON.parse(usuario) : null;
    }

    // Logout
    logout() {
        AuthAPI.logout();
        this.carritoLocal = [];
        this.saveCarritoLocal(this.carritoLocal);
    }

    // ============ PEDIDOS ============

    // Obtener pedidos del usuario actual
    async getMisPedidos() {
        try {
            return await OrderAPI.getUserOrders();
        } catch (error) {
            console.error('Error al obtener pedidos:', error);
            return [];
        }
    }

    // Obtener todos los pedidos (admin)
    async getAllPedidos() {
        try {
            // TODO: Implementar endpoint de admin para obtener todos los pedidos
            return await OrderAPI.getUserOrders(); // Por ahora usa el mismo
        } catch (error) {
            console.error('Error al obtener todos los pedidos:', error);
            return [];
        }
    }

    // Crear nuevo pedido
    async createPedido(datosCliente) {
        try {
            const orderData = {
                shippingAddress: {
                    direccionLinea: datosCliente.direccion,
                    distrito: datosCliente.distrito || '',
                    ciudad: datosCliente.ciudad || 'Lima',
                    referencia: datosCliente.referencia || ''
                }
            };
            
            return await OrderAPI.createOrder(orderData);
        } catch (error) {
            console.error('Error al crear pedido:', error);
            return null;
        }
    }

    // Actualizar estado de pedido (admin)
    async updatePedidoEstado(pedidoId, nuevoEstado) {
        try {
            await OrderAPI.updateOrderStatus(pedidoId, nuevoEstado);
            return true;
        } catch (error) {
            console.error('Error al actualizar estado del pedido:', error);
            return false;
        }
    }
}

// Crear instancia global
const dataStore = new DataStore();