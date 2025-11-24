// api-config.js - Configuración y utilidades para la API

// Configuración base de la API
const API_CONFIG = {
    BASE_URL: 'https://81k7338z.brs.devtunnels.ms/api',
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

    // Mostrar modal de sesión expirada
    showSessionExpiredModal() {
        // Crear modal si no existe
        let modal = document.getElementById('sessionExpiredModal');
        if (!modal) {
            modal = document.createElement('div');
            modal.id = 'sessionExpiredModal';
            modal.className = 'modal fade';
            modal.innerHTML = `
                <div class="modal-dialog modal-dialog-centered">
                    <div class="modal-content">
                        <div class="modal-header">
                            <h5 class="modal-title">Sesión Expirada</h5>
                        </div>
                        <div class="modal-body">
                            <p>Tu sesión ha expirado. Serás redirigido al login.</p>
                        </div>
                        <div class="modal-footer">
                            <button type="button" class="btn btn-primary" id="sessionExpiredBtn">Aceptar</button>
                        </div>
                    </div>
                </div>
            `;
            document.body.appendChild(modal);
            
            // Evento para el botón aceptar
            document.getElementById('sessionExpiredBtn').addEventListener('click', () => {
                const bsModal = bootstrap.Modal.getInstance(modal);
                bsModal.hide();
                // Limpiar sesión y redirigir al login
                this.clearToken();
                const path = window.location.pathname;
                if (path.includes('/admin/')) {
                    window.location.href = '/admin/index.html';
                } else {
                    window.location.href = '/login.html';
                }
            });
        }
        
        // Mostrar modal
        const bsModal = new bootstrap.Modal(modal, {
            backdrop: 'static',
            keyboard: false
        });
        bsModal.show();
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
            let parsed;
            try {
                parsed = await response.json();
            } catch (e) {
                throw new Error(`Error ${response.status}: ${response.statusText}`);
            }

            let message = null;
            if (parsed) {
                if (parsed.errors && Array.isArray(parsed.errors)) {
                    message = parsed.errors.map(err => err.defaultMessage || err.message || JSON.stringify(err)).join('; ');
                }
                else if (parsed.fieldErrors && Array.isArray(parsed.fieldErrors)) {
                    message = parsed.fieldErrors.map(err => `${err.field}: ${err.defaultMessage || err.message || ''}`).join('; ');
                }
                else if (parsed.message) {
                    message = parsed.message;
                }
                else if (typeof parsed === 'string') {
                    message = parsed;
                } else {
                    message = JSON.stringify(parsed);
                }
            }

            throw new Error(message || `Error ${response.status}: ${response.statusText}`);
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
        
        // Si es error 401, verificar si es necesario mostrar modal
        if (error.message.includes('401')) {
            const path = window.location.pathname;
            
            // Páginas protegidas que SÍ deben mostrar modal
            const protectedPaths = [
                '/admin/',
                '/delivery/',
                '/mis-pedidos.html'
            ];
            
            const isProtectedPage = protectedPaths.some(p => path.includes(p));
            
            // Limpiar sesión siempre
            this.clearToken();
            
            // SOLO mostrar modal si estamos en página protegida
            if (isProtectedPage) {
                if (window.App && window.App.showSessionExpiredModal) {
                    window.App.showSessionExpiredModal();
                } else {
                    // Fallback para páginas protegidas
                    this.showSessionExpiredModal();
                }
            } else {
                // En páginas públicas, solo limpiar sesión sin mostrar modal
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

    // ============ MÉTODOS PÚBLICOS ============

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
    },

    // ============ MÉTODOS DE ADMINISTRACIÓN (requieren auth) ============

    // Crear nuevo producto (Admin)
    async createProduct(productData) {
        return await this.api.post('/products', productData, true);
    },

    // Actualizar producto (Admin)
    async updateProduct(id, productData) {
        return await this.api.put(`/products/${id}`, productData, true);
    },

    // Eliminar producto (Admin)
    async deleteProduct(id) {
        return await this.api.delete(`/products/${id}`, true);
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

    // ============ MÉTODOS DE USUARIO ============

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

    // ============ MÉTODOS DE ADMINISTRACIÓN (requieren auth ADMIN) ============

    // Obtener TODOS los pedidos (Admin)
    async getAllOrders() {
        return await this.api.get('/orders/admin/all', true);
    },

    // Actualizar estado de orden (Admin)
    // Estados válidos: PENDING, PROCESSING, DELIVERED, CANCELLED
    async updateOrderStatus(orderId, status) {
        return await this.api.put(`/orders/${orderId}/status?status=${status}`, null, true);
    },

    // Asignar repartidor a pedido (Admin)
    async assignDelivery(orderId, repartidorId) {
        return await this.api.put(`/orders/${orderId}/assign-delivery?repartidorId=${repartidorId}`, null, true);
    },

    // Obtener repartidores disponibles (Admin)
    async getAvailableDeliveryUsers() {
        return await this.api.get('/orders/delivery/available', true);
    },

    // Obtener pedidos sin asignar (Admin)
    async getUnassignedOrders() {
        return await this.api.get('/orders/unassigned', true);
    },

    // ============ MÉTODOS DE DELIVERY (requieren auth DELIVERY) ============

    // Obtener pedidos asignados al repartidor
    async getMyAssignedOrders() {
        return await this.api.get('/orders/delivery/my-orders', true);
    },

    // Actualizar estado de pedido asignado (Delivery)
    async updateDeliveryStatus(orderId, status) {
        return await this.api.put(`/orders/${orderId}/delivery-status?status=${status}`, null, true);
    },

    // Obtener detalle de pedido asignado (Delivery)
    async getAssignedOrderDetail(orderId) {
        return await this.api.get(`/orders/delivery/order/${orderId}`, true);
    },

    // Obtener estadísticas del repartidor
    async getDeliveryStats() {
        return await this.api.get('/orders/delivery/stats', true);
    }
};

const UserAPI = {
    api: new ApiService(),

    // ============ MÉTODOS DE ADMINISTRACIÓN (requieren auth Admin) ============

    // Obtener todos los usuarios (Admin)
    async getAllUsers() {
        return await this.api.get('/users', true);
    },

    // Obtener usuario por ID (Admin)
    async getUserById(id) {
        return await this.api.get(`/users/${id}`, true);
    },

    // ============ MÉTODOS DE PERFIL (requieren auth) ============

    // Obtener perfil del usuario actual
    async getCurrentProfile() {
        return await this.api.get('/users/profile', true);
    },

    // Actualizar información personal del perfil
    async updateProfile(profileData) {
        return await this.api.put('/users/profile', profileData, true);
    },

    // Cambiar contraseña
    async changePassword(currentPassword, newPassword) {
        return await this.api.put('/users/profile/password', {
            currentPassword,
            newPassword
        }, true);
    }
};

const DashboardAPI = {
    api: new ApiService(),

    // Obtener datos del dashboard (Admin)
    async getDashboardData() {
        return await this.api.get('/dashboard/data', true);
    }
};

const ReporteAPI = {
    api: new ApiService(),

    // ============ REPORTES DIARIOS ============

    // Generar reporte diario de ganancias
    async generarReporteDiario(fecha) {
        return await this.api.get(`/reportes/diario-ganancias?fecha=${fecha}`, true);
    },

    // Exportar reporte diario de ganancias a Excel
    async exportarReporteDiario(fecha) {
        const response = await fetch(`${this.api.baseURL}/reportes/exportar-diario-ganancias?fecha=${fecha}`, {
            method: 'GET',
            headers: this.api.getHeaders(true)
        });

        if (!response.ok) {
            throw new Error(`Error al descargar reporte: ${response.status}`);
        }

        // Crear blob y descargar
        const blob = await response.blob();
        const url = window.URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = url;
        a.download = `reporte_ganancias_${fecha}.xlsx`;
        document.body.appendChild(a);
        a.click();
        window.URL.revokeObjectURL(url);
        document.body.removeChild(a);
    },

    // ============ REPORTES SEMANALES ============

    // Generar reporte semanal
    async generarReporteSemanal(fechaInicio, fechaFin) {
        return await this.api.post('/reportes/generar-semanal', {
            fechaInicio: fechaInicio,
            fechaFin: fechaFin
        }, true);
    },

    // Obtener reportes por año
    async getReportesPorAno(ano) {
        return await this.api.get(`/reportes/por-año/${ano}`, true);
    },

    // Obtener últimos reportes
    async getUltimosReportes(limite = 5) {
        return await this.api.get(`/reportes/ultimos/${limite}`, true);
    },

    // Exportar reporte semanal a Excel
    async exportarReporteSemanal(reporteId) {
        const response = await fetch(`${this.api.baseURL}/reportes/exportar-semanal/${reporteId}`, {
            method: 'GET',
            headers: this.api.getHeaders(true)
        });

        if (!response.ok) {
            throw new Error(`Error al descargar reporte: ${response.status}`);
        }

        // Crear blob y descargar
        const blob = await response.blob();
        const url = window.URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = url;
        a.download = `reporte_semanal_${reporteId}.xlsx`;
        document.body.appendChild(a);
        a.click();
        window.URL.revokeObjectURL(url);
        document.body.removeChild(a);
    }
};

// Exportar servicios
window.API_CONFIG = API_CONFIG;
window.ApiService = ApiService;
window.AuthAPI = AuthAPI;
window.ProductAPI = ProductAPI;
window.CartAPI = CartAPI;
window.OrderAPI = OrderAPI;
window.UserAPI = UserAPI;
window.DashboardAPI = DashboardAPI;
window.ReporteAPI = ReporteAPI;