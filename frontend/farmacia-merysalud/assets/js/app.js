// app.js - Lógica principal de la aplicación

// Objeto principal de la aplicación
const App = {
    // Inicializar la aplicación
    async init() {
        
        // Restaurar sesión si existe
        await this.restoreSession();
        
        // Cargar componentes HTML
        await this.loadComponents();
        
        // Verificar autenticación
        await this.checkAuth();
        
        // Actualizar badge del carrito
        await this.updateCartBadge();
        
        // ⚠️ DESACTIVADO: Causaba modal de sesión expirada en modo público
        // this.startTokenValidationTimer();
        
        // Eventos globales
        this.bindGlobalEvents();
        
    },

    // Restaurar sesión desde localStorage
    async restoreSession() {
        const token = localStorage.getItem('accessToken');
        const usuario = localStorage.getItem('usuarioActual');
        
        if (token && usuario) {
            try {
                // Configurar token en ApiService
                if (window.ApiService) {
                    const apiService = new window.ApiService();
                    apiService.setToken(token);
                }
                
            } catch (error) {
                console.warn('Error al restaurar sesión:', error);
                // Limpiar sesión corrupta
                this.clearSession();
            }
        }
    },

    // Limpiar sesión completamente
    clearSession() {
        localStorage.removeItem('accessToken');
        localStorage.removeItem('refreshToken');
        localStorage.removeItem('usuarioActual');
        localStorage.removeItem('userData');
    },

    // ⚠️ MÉTODO DESACTIVADO - Causaba problemas en modo público
    // Iniciar timer para validar token periódicamente
    startTokenValidationTimer() {
        // NO hacer nada - método desactivado
    },

    // Cargar componentes HTML reutilizables
    async loadComponents() {
        // Solo cargar si existe el elemento
        const navbarElement = document.getElementById('navbar-container');
        const footerElement = document.getElementById('footer-container');
        
        if (navbarElement) {
            const usuario = dataStore.getCurrentUser();
            let navbarFile = 'navbar-public.html';
            
            if (usuario) {
                if (usuario.rol === 'ADMIN') navbarFile = 'navbar-admin.html';
                else if (usuario.rol === 'DELIVERY') navbarFile = 'navbar-delivery.html';
            }
            
            try {
                const response = await fetch(`/components/${navbarFile}`);
                if (response.ok) {
                    navbarElement.innerHTML = await response.text();
                }
            } catch (error) {
            }
        }
        
        if (footerElement) {
            try {
                const response = await fetch('/components/footer.html');
                if (response.ok) {
                    footerElement.innerHTML = await response.text();
                }
            } catch (error) {
            }
        }
    },

    // Actualizar badge del carrito
    async updateCartBadge() {
        const carrito = await dataStore.getCarrito();
        const badges = document.querySelectorAll('.cart-badge');
        const cantidad = carrito.reduce((total, item) => total + item.cantidad, 0);
        
        badges.forEach(badge => {
            badge.textContent = cantidad;
            badge.style.display = cantidad > 0 ? 'inline-block' : 'none';
        });
    },

    // Verificar autenticación
    async checkAuth() {
        const usuario = dataStore.getCurrentUser();
        const token = localStorage.getItem('accessToken');
        const path = window.location.pathname;
        
        // Páginas que requieren autenticación
        const protectedPaths = [
            '/mis-pedidos.html',
            '/admin/',
            '/delivery/'
        ];
        
        // Verificar si la página actual requiere autenticación
        const needsAuth = protectedPaths.some(p => path.includes(p));
        
        // ✅ CORREGIDO: Solo validar token si el usuario ESTÁ logueado
        if (token && usuario) {
            // Para usuarios delivery, no validar token con API call (puede causar problemas)
            if (usuario.rol !== 'DELIVERY') {
                try {
                    // Validar token llamando a una API que requiere autenticación
                    await AuthAPI.getCurrentUser();
                } catch (error) {
                    console.warn('Token inválido o expirado:', error.message);
                    
                    // ✅ SOLO mostrar modal si estamos en página protegida
                    if (needsAuth) {
                        this.showSessionExpiredModal();
                    } else {
                        // En página pública, solo limpiar sesión sin mostrar modal
                        dataStore.logout();
                    }
                    return false;
                }
            } else {
            }
        }
        
        if (needsAuth && !usuario) {
            // Redirigir al login apropiado
            if (path.includes('/admin/') || path.includes('/delivery/')) {
                window.location.href = '/admin/index.html';
            } else {
                window.location.href = './login.html';
            }
            return false;
        }
        
        // Verificar roles
        if (usuario) {
            if (path.includes('/admin/') && usuario.rol !== 'ADMIN') {
                window.location.href = '/';
                return false;
            }
            if (path.includes('/delivery/') && usuario.rol !== 'DELIVERY') {
                window.location.href = '/';
                return false;
            }
        }
        
        // Actualizar UI con info del usuario
        this.updateUserUI(usuario);
        return true;
    },

    // Actualizar UI según usuario
    updateUserUI(usuario) {
        const userNameElements = document.querySelectorAll('.user-name');
        const loginButtons = document.querySelectorAll('.btn-login');
        const logoutButtons = document.querySelectorAll('.btn-logout');
        const userMenus = document.querySelectorAll('.user-menu');
        
        if (usuario) {
            userNameElements.forEach(el => el.textContent = usuario.nombre);
            loginButtons.forEach(el => el.style.display = 'none');
            logoutButtons.forEach(el => el.style.display = 'block');
            userMenus.forEach(el => el.style.display = 'block');
        } else {
            loginButtons.forEach(el => el.style.display = 'block');
            logoutButtons.forEach(el => el.style.display = 'none');
            userMenus.forEach(el => el.style.display = 'none');
        }
    },

    // Eventos globales
    bindGlobalEvents() {
        // Logout
        document.addEventListener('click', (e) => {
            if (e.target.classList.contains('btn-logout')) {
                e.preventDefault();
                this.logout();
            }
        });
    },

    // Cerrar sesión
    logout() {
        if (confirm('¿Desea cerrar sesión?')) {
            dataStore.logout();
            window.location.href = './index.html';
        }
    },

    // Formatear precio
    formatPrice(precio) {
        return `S/. ${parseFloat(precio).toFixed(2)}`;
    },

    // Mostrar notificación
    showNotification(message, type = 'success') {
        // Crear elemento de notificación
        const notification = document.createElement('div');
        notification.className = `alert alert-${type} notification`;
        notification.innerHTML = `
            <div class="d-flex justify-content-between align-items-center">
                <span>${message}</span>
                <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
            </div>
        `;
        
        // Contenedor de notificaciones
        let container = document.getElementById('notification-container');
        if (!container) {
            container = document.createElement('div');
            container.id = 'notification-container';
            container.style.cssText = 'position: fixed; top: 80px; right: 20px; z-index: 9999; max-width: 350px;';
            document.body.appendChild(container);
        }
        
        container.appendChild(notification);
        
        // Auto-cerrar después de 3 segundos
        setTimeout(() => {
            notification.remove();
        }, 3000);
    },

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
                            <p>El tiempo de sesión ha expirado. Por favor, inicia sesión nuevamente.</p>
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
                dataStore.logout();
                window.location.href = './login.html';
            });
        }
        
        // Mostrar modal
        const bsModal = new bootstrap.Modal(modal, {
            backdrop: 'static',
            keyboard: false
        });
        bsModal.show();
    },
};

// Utilidades globales
const Utils = {
    // Validar email
    validateEmail(email) {
        const re = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
        return re.test(email);
    },

    // Validar teléfono (Perú)
    validatePhone(phone) {
        const re = /^9\d{8}$/;
        return re.test(phone);
    },

    // Validar formulario
    validateForm(formElement) {
        const inputs = formElement.querySelectorAll('input[required], select[required], textarea[required]');
        let isValid = true;
        
        inputs.forEach(input => {
            if (!input.value.trim()) {
                input.classList.add('is-invalid');
                isValid = false;
            } else {
                input.classList.remove('is-invalid');
                input.classList.add('is-valid');
            }
            
            // Validaciones específicas
            if (input.type === 'email' && !this.validateEmail(input.value)) {
                input.classList.add('is-invalid');
                isValid = false;
            }
            
            if (input.type === 'tel' && !this.validatePhone(input.value)) {
                input.classList.add('is-invalid');
                isValid = false;
            }
        });
        
        return isValid;
    },

    // Obtener parámetros de URL
    getUrlParam(param) {
        const urlParams = new URLSearchParams(window.location.search);
        return urlParams.get(param);
    },

    // Debounce para búsquedas
    debounce(func, wait) {
        let timeout;
        return function executedFunction(...args) {
            const later = () => {
                clearTimeout(timeout);
                func(...args);
            };
            clearTimeout(timeout);
            timeout = setTimeout(later, wait);
        };
    }
};

// Inicializar cuando el DOM esté listo
document.addEventListener('DOMContentLoaded', async () => {
    await App.init();
});

// Hacer disponibles globalmente
window.App = App;
window.Utils = Utils;