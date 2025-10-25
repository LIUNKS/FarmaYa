// api-config.js - Configuración y utilidades para la API

// Configuración base de la API
const API_CONFIG = {
    BASE_URL: 'http://localhost:8080/api',
    TIMEOUT: 30000, // 30 segundos
    HEADERS: {
        'Content-Type': 'application/json',
        'Accept': 'application/json'
    }
};

// Clase para manejar las peticiones a la API
class ApiService {
    constructor() {
        this.baseURL = API_CONFIG.BASE_URL;
        this.token = this.getStoredToken();
    }

    // Obtener token almacenado
    getStoredToken() {
        return localStorage.getItem('accessToken');
    }

    // Guardar token
    setToken(token) {
        this.token = token;
        localStorage.setItem('accessToken', token);
    }

    // Eliminar token
    clearToken() {
        this.token = null;
        localStorage.removeItem('accessToken');
        localStorage.removeItem('refreshToken');
        localStorage.removeItem('usuarioActual');
    }

    // Headers con autenticación
    getHeaders(includeAuth = true) {
        const headers = { ...API_CONFIG.HEADERS };
        if (includeAuth && this.token) {
            headers['Authorization'] = `Bearer ${this.token}`;
        }
        return headers;
    }

    // Método GET
    async get(endpoint, includeAuth = true) {
        try {
            const response = await fetch(`${this.baseURL}${endpoint}`, {
                method: 'GET',
                headers: this.getHeaders(includeAuth)
            });
            return await this.handleResponse(response);
        } catch (error) {
            return this.handleError(error);
        }
    }

    // Método POST
    async post(endpoint, data, includeAuth = false) {
        try {
            const response = await fetch(`${this.baseURL}${endpoint}`, {
                method: 'POST',
                headers: this.getHeaders(includeAuth),
                body: JSON.stringify(data)
            });
            return await this.handleResponse(response);
        } catch (error) {
            return this.handleError(error);
        }
    }

    // Método PUT
    async put(endpoint, data, includeAuth = true) {
        try {
            const response = await fetch(`${this.baseURL}${endpoint}`, {
                method: 'PUT',
                headers: this.getHeaders(includeAuth),
                body: JSON.stringify(data)
            });
            return await this.handleResponse(response);
        } catch (error) {
            return this.handleError(error);
        }
    }

    // Método DELETE
    async delete(endpoint, includeAuth = true) {
        try {
            const response = await fetch(`${this.baseURL}${endpoint}`, {
                method: 'DELETE',
                headers: this.getHeaders(includeAuth)
            });
            return await this.handleResponse(response);
        } catch (error) {
            return this.handleError(error);
        }
    }

    // Manejar respuesta
    async handleResponse(response) {
        if (!response.ok) {
            const error = await response.json().catch(() => ({ message: response.statusText }));
            throw new Error(error.message || `Error ${response.status}: ${response.statusText}`);
        }

        const contentType = response.headers.get('content-type');
        if (contentType && contentType.includes('application/json')) {
            return await response.json();
        }
        return response.text();
    }

    // Manejar error
    handleError(error) {
        console.error('API Error:', error);
        
        // Si es error 401, mostrar modal de expiración de sesión
        if (error.message.includes('401')) {
            // Limpiar sesión
            this.clearToken();
            
            // Mostrar modal de expiración de sesión
            if (window.App && window.App.showSessionExpiredModal) {
                window.App.showSessionExpiredModal();
            } else {
                // Fallback si App no está disponible
                alert('Tu sesión ha expirado. Serás redirigido al login.');
                setTimeout(() => {
                    window.location.href = '/login.html';
                }, 2000);
            }
        }
        
        throw error;
    }
}

// Servicios específicos de la API
const AuthAPI = {
    api: new ApiService(),

    // Login
    async login(username, password) {
        const response = await this.api.post('/auth/login', { username, password });
        
        if (response.accessToken) {
            // Guardar tokens
            this.api.setToken(response.accessToken);
            localStorage.setItem('refreshToken', response.refreshToken);
            
            // Obtener información completa del usuario
            const userInfo = await this.getCurrentUser();
            
            // Guardar usuario
            localStorage.setItem('usuarioActual', JSON.stringify({
                id: userInfo.id,
                nombre: userInfo.username,
                email: userInfo.email,
                rol: response.role,
                telefono: userInfo.telefono
            }));
        }
        
        return response;
    },

    // Registro
    async register(userData) {
        return await this.api.post('/auth/register', userData);
    },

    // Obtener usuario actual
    async getCurrentUser() {
        return await this.api.get('/auth/me', true);
    },

    // Logout
    logout() {
        this.api.clearToken();
    }
};

const ProductAPI = {
    api: new ApiService(),

    // Obtener todos los productos
    async getAllProducts() {
        return await this.api.get('/products', false);
    },

    // Obtener producto por ID
    async getProductById(id) {
        return await this.api.get(`/products/${id}`, false);
    },

    // Buscar productos
    async searchProducts(keyword) {
        return await this.api.get(`/products/search?keyword=${encodeURIComponent(keyword)}`, false);
    },

    // Filtrar por categoría
    async getProductsByCategory(category) {
        return await this.api.get(`/products/category/${encodeURIComponent(category)}`, false);
    }
};

const CartAPI = {
    api: new ApiService(),

    // Obtener carrito del usuario
    async getCart() {
        return await this.api.get('/cart', true);
    },

    // Agregar producto al carrito
    async addToCart(productId, quantity = 1) {
        return await this.api.post(`/cart/add?productId=${productId}&quantity=${quantity}`, null, true);
    },

    // Remover producto del carrito
    async removeFromCart(productId) {
        return await this.api.delete(`/cart/remove/${productId}`, true);
    },

    // Vaciar carrito
    async clearCart() {
        return await this.api.delete('/cart/clear', true);
    }
};

const OrderAPI = {
    api: new ApiService(),

    // Crear orden
    async createOrder(orderData = null) {
        if (orderData) {
            return await this.api.post('/orders', orderData, true);
        } else {
            return await this.api.post('/orders', null, true);
        }
    },

    // Obtener órdenes del usuario
    async getUserOrders() {
        return await this.api.get('/orders', true);
    },

    // Obtener orden por ID
    async getOrderById(orderId) {
        return await this.api.get(`/orders/${orderId}`, true);
    },

    // Actualizar estado de orden (Admin)
    async updateOrderStatus(orderId, status) {
        return await this.api.put(`/orders/${orderId}/status?status=${status}`, null, true);
    }
};

// Exportar servicios
window.API_CONFIG = API_CONFIG;
window.ApiService = ApiService;
window.AuthAPI = AuthAPI;
window.ProductAPI = ProductAPI;
window.CartAPI = CartAPI;
window.OrderAPI = OrderAPI;
