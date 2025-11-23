// app.js - Lógica principal de la aplicación

const App = {
    accessibilityEnabled: false,

    async init() {
        await this.restoreSession();
        await this.loadComponents();
        await this.checkAuth();
        await this.updateCartBadge();
        this.bindGlobalEvents();
        this.initAccessibility();
    },

    async restoreSession() {
        const token = localStorage.getItem('accessToken');
        const usuario = localStorage.getItem('usuarioActual');

        if (token && usuario) {
            try {
                if (window.ApiService) {
                    const apiService = new window.ApiService();
                    apiService.setToken(token);
                }
            } catch (error) {
                console.warn('Error al restaurar sesión:', error);
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

    async loadComponents() {
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
                // Error silencioso
            }
        }

        if (footerElement) {
            try {
                const response = await fetch('/components/footer.html');
                if (response.ok) {
                    footerElement.innerHTML = await response.text();
                }
            } catch (error) {
                // Error silencioso
            }
        }
    },

    async updateCartBadge() {
        const carrito = await dataStore.getCarrito();
        const badges = document.querySelectorAll('.cart-badge');
        const cantidad = carrito.reduce((total, item) => total + item.cantidad, 0);

        badges.forEach(badge => {
            badge.textContent = cantidad;
            badge.style.display = cantidad > 0 ? 'inline-block' : 'none';
        });
    },

    async checkAuth() {
        const usuario = dataStore.getCurrentUser();
        const token = localStorage.getItem('accessToken');
        const path = window.location.pathname;

        const protectedPaths = [
            '/mis-pedidos.html',
            '/admin/',
            '/delivery/'
        ];

        const needsAuth = protectedPaths.some(p => path.includes(p));

        if (token && usuario) {
            if (usuario.rol !== 'DELIVERY') {
                try {
                    await AuthAPI.getCurrentUser();
                } catch (error) {
                    console.warn('Token inválido o expirado:', error.message);
                    if (needsAuth) {
                        this.showSessionExpiredModal();
                    } else {
                        dataStore.logout();
                    }
                    return false;
                }
            }
        }

        if (needsAuth && !usuario) {
            if (path.includes('/admin/') || path.includes('/delivery/')) {
                window.location.href = '/admin/index.html';
            } else {
                window.location.href = './login.html';
            }
            return false;
        }

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

        this.updateUserUI(usuario);
        return true;
    },

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

    bindGlobalEvents() {
        document.addEventListener('click', (e) => {
            if (e.target.classList.contains('btn-logout')) {
                e.preventDefault();
                this.logout();
            }
        });
    },

    logout() {
        this.showConfirmModal('¿Desea cerrar sesión?', () => {
            dataStore.logout();
            window.location.href = './index.html';
        });
    },

    formatPrice(precio) {
        return `S/. ${parseFloat(precio).toFixed(2)}`;
    },

    showNotification(message, type = 'success') {
        const notification = document.createElement('div');
        notification.className = `alert alert-${type} notification`;
        notification.innerHTML = `
            <div class="d-flex justify-content-between align-items-center">
                <span>${message}</span>
                <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
            </div>
        `;

        let container = document.getElementById('notification-container');
        if (!container) {
            container = document.createElement('div');
            container.id = 'notification-container';
            container.style.cssText = 'position: fixed; top: 80px; right: 20px; z-index: 9999; max-width: 350px;';
            document.body.appendChild(container);
        }

        container.appendChild(notification);

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
                window.location.href = '/login.html';
            });
        }
        
        // Mostrar modal
        const bsModal = new bootstrap.Modal(modal, {
            backdrop: 'static',
            keyboard: false
        });
        bsModal.show();
    },

    // Mostrar modal de confirmación
    showConfirmModal(message, onConfirm, onCancel = null) {
        // Crear modal si no existe
        let modal = document.getElementById('confirmModal');
        if (!modal) {
            modal = document.createElement('div');
            modal.id = 'confirmModal';
            modal.className = 'modal fade';
            modal.innerHTML = `
                <div class="modal-dialog modal-dialog-centered">
                    <div class="modal-content">
                        <div class="modal-header">
                            <h5 class="modal-title">Confirmar</h5>
                        </div>
                        <div class="modal-body">
                            <p id="confirmMessage"></p>
                        </div>
                        <div class="modal-footer">
                            <button type="button" class="btn btn-secondary" id="confirmCancelBtn">Cancelar</button>
                            <button type="button" class="btn btn-primary" id="confirmOkBtn">Aceptar</button>
                        </div>
                    </div>
                </div>
            `;
            document.body.appendChild(modal);
            
            // Eventos para los botones
            document.getElementById('confirmOkBtn').addEventListener('click', () => {
                const bsModal = bootstrap.Modal.getInstance(modal);
                bsModal.hide();
                if (onConfirm) onConfirm();
            });
            
            document.getElementById('confirmCancelBtn').addEventListener('click', () => {
                const bsModal = bootstrap.Modal.getInstance(modal);
                bsModal.hide();
                if (onCancel) onCancel();
            });
        }
        
        // Actualizar mensaje
        document.getElementById('confirmMessage').textContent = message;
        
        // Mostrar modal
        const bsModal = new bootstrap.Modal(modal);
        bsModal.show();
    },

    confirmAction(message, onConfirm, onCancel = null) {
        this.showConfirmModal(message, onConfirm, onCancel);
    },

    // Inicializar accesibilidad
    initAccessibility() {
        this.bindAccessibilityEvents();
    },

    // Crear botón de accesibilidad
    createAccessibilityButton() {
        // Ya no se crea flotante, se asume en HTML
    },

    // Eventos de accesibilidad
    bindAccessibilityEvents() {
        const button = document.getElementById('accessibility-btn');
        button.addEventListener('click', () => this.toggleAccessibility());
    },

    // Alternar modo accesibilidad
    toggleAccessibility() {
        this.accessibilityEnabled = !this.accessibilityEnabled;
        const button = document.getElementById('accessibility-btn');
        const icon = button.querySelector('i');
        if (icon) {
            icon.style.color = this.accessibilityEnabled ? '#28a745' : '';
        }
        button.title = this.accessibilityEnabled ? 'Desactivar modo accesibilidad' : 'Activar modo accesibilidad';
        
        if (this.accessibilityEnabled) {
            this.enableAccessibility();
        } else {
            this.disableAccessibility();
        }
    },

    // Habilitar accesibilidad
    enableAccessibility() {
        // Verificar soporte de síntesis de voz
        if (!('speechSynthesis' in window)) {
            alert('Tu navegador no soporta síntesis de voz. Usa Chrome, Edge o Safari para esta función.');
            return;
        }
        
        // Agregar event listeners
        document.addEventListener('focusin', this.handleFocus.bind(this), true);
        document.addEventListener('click', this.handleClick.bind(this), true);
        
        // Actualizar atributo aria-pressed del botón
        const button = document.getElementById('accessibility-btn');
        if (button) {
            button.setAttribute('aria-pressed', 'true');
        }
    },

    // Deshabilitar accesibilidad
    disableAccessibility() {
        document.removeEventListener('focusin', this.handleFocus.bind(this), true);
        document.removeEventListener('click', this.handleClick.bind(this), true);
        speechSynthesis.cancel(); // Detener cualquier habla en curso
        
        // Actualizar atributo aria-pressed del botón
        const button = document.getElementById('accessibility-btn');
        if (button) {
            button.setAttribute('aria-pressed', 'false');
        }
    },

    // Manejar foco (para PC con Tab)
    handleFocus(event) {
        if (!this.accessibilityEnabled) return;
        const element = event.target;
        const text = this.getReadableText(element);
        if (text) this.speak(text);
    },

    // Manejar click (para móvil)
    handleClick(event) {
        if (!this.accessibilityEnabled) return;
        const element = event.target;
        const text = this.getReadableText(element);
        if (text) {
            event.preventDefault();
            this.speak(text).then(() => {
                // Después de hablar, ejecutar el click
                setTimeout(() => {
                    element.click();
                }, 500); // Pequeño delay para asegurar que el habla termine
            });
        }
    },

    // Obtener texto legible del elemento
    getReadableText(element) {
        // Prioridad: aria-label, title, textContent, placeholder
        let text = '';
        if (element.getAttribute('aria-label')) {
            text = element.getAttribute('aria-label');
        } else if (element.title) {
            text = element.title;
        } else if (element.textContent && element.textContent.trim()) {
            text = element.textContent.trim();
        } else if (element.placeholder) {
            text = element.placeholder;
        }
        
        // Para inputs, usar label asociado
        if (['INPUT', 'SELECT', 'TEXTAREA'].includes(element.tagName)) {
            const label = document.querySelector(`label[for="${element.id}"]`);
            if (label && label.textContent.trim()) {
                text = label.textContent.trim();
            }
        }
        
        if (!text) {
            // Para botones sin texto, usar tipo
            if (element.tagName === 'BUTTON' && !element.textContent.trim()) {
                text = element.type === 'submit' ? 'Enviar' : 'Botón';
            } else {
                return null;
            }
        }
        
        // Agregar prefijo según tipo de elemento
        let prefix = '';
        const tag = element.tagName.toLowerCase();
        if (tag === 'a') {
            prefix = 'Enlace: ';
        } else if (tag === 'button') {
            prefix = 'Botón: ';
        } else if (tag === 'input') {
            if (element.type === 'text' || element.type === 'email' || element.type === 'password') {
                prefix = 'Campo de texto: ';
            } else if (element.type === 'checkbox') {
                prefix = 'Casilla de verificación: ';
            } else if (element.type === 'radio') {
                prefix = 'Botón de opción: ';
            } else {
                prefix = 'Campo: ';
            }
        } else if (tag === 'select') {
            prefix = 'Lista desplegable: ';
        } else if (tag === 'textarea') {
            prefix = 'Área de texto: ';
        } else if (tag === 'img') {
            prefix = 'Imagen: ';
        } else {
            prefix = 'Elemento: ';
        }
        
        return prefix + text;
    },

    // Función para hablar texto
    speak(text) {
        return new Promise(resolve => {
            const utterance = new SpeechSynthesisUtterance(text);
            utterance.lang = 'es-ES'; // Español de España, ajustar si es necesario
            utterance.rate = 0.8; // Un poco más lento para claridad
            utterance.onend = resolve;
            speechSynthesis.speak(utterance);
        });
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