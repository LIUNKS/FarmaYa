// app.js - Lógica principal de la aplicación

// Objeto principal de la aplicación
const App = {
    // Inicializar la aplicación
    init() {
        this.loadComponents();
        this.updateCartBadge();
        this.checkAuth();
        this.bindGlobalEvents();
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
                else if (usuario.rol === 'REPARTIDOR') navbarFile = 'navbar-delivery.html';
            }
            
            try {
                const response = await fetch(`/components/${navbarFile}`);
                if (response.ok) {
                    navbarElement.innerHTML = await response.text();
                }
            } catch (error) {
                console.log('Navbar será incluido directamente en HTML');
            }
        }
        
        if (footerElement) {
            try {
                const response = await fetch('/components/footer.html');
                if (response.ok) {
                    footerElement.innerHTML = await response.text();
                }
            } catch (error) {
                console.log('Footer será incluido directamente en HTML');
            }
        }
    },

    // Actualizar badge del carrito
    updateCartBadge() {
        const carrito = dataStore.getCarrito();
        const badges = document.querySelectorAll('.cart-badge');
        const cantidad = carrito.reduce((total, item) => total + item.cantidad, 0);
        
        badges.forEach(badge => {
            badge.textContent = cantidad;
            badge.style.display = cantidad > 0 ? 'inline-block' : 'none';
        });
    },

    // Verificar autenticación
    checkAuth() {
        const usuario = dataStore.getCurrentUser();
        const path = window.location.pathname;
        
        // Páginas que requieren autenticación
        const protectedPaths = [
            '/mis-pedidos.html',
            '/admin/',
            '/delivery/'
        ];
        
        // Verificar si la página actual requiere autenticación
        const needsAuth = protectedPaths.some(p => path.includes(p));
        
        if (needsAuth && !usuario) {
            // Redirigir al login apropiado
            if (path.includes('/admin/') || path.includes('/delivery/')) {
                window.location.href = '/admin/index.html';
            } else {
                window.location.href = '/login.html';
            }
            return false;
        }
        
        // Verificar roles
        if (usuario) {
            if (path.includes('/admin/') && usuario.rol !== 'ADMIN') {
                window.location.href = '/';
                return false;
            }
            if (path.includes('/delivery/') && usuario.rol !== 'REPARTIDOR') {
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
            window.location.href = '/';
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

    // Agregar al carrito (función global)
    addToCart(productoId, cantidad = 1) {
        const success = dataStore.addToCarrito(productoId, cantidad);
        
        if (success) {
            this.updateCartBadge();
            this.showNotification('Producto agregado al carrito', 'success');
            
            // Animar el botón del carrito
            const cartIcon = document.querySelector('.navbar .fa-shopping-cart');
            if (cartIcon) {
                cartIcon.parentElement.classList.add('animate-bounce');
                setTimeout(() => {
                    cartIcon.parentElement.classList.remove('animate-bounce');
                }, 500);
            }
        } else {
            this.showNotification('Error al agregar el producto', 'danger');
        }
        
        return success;
    }
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
document.addEventListener('DOMContentLoaded', () => {
    App.init();
});

// Hacer disponibles globalmente
window.App = App;
window.Utils = Utils;