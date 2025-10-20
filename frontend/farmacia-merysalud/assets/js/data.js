// data.js - Datos simulados para el sistema de la farmacia
// Este archivo simula la base de datos usando localStorage

const INITIAL_DATA = {
    // Usuarios del sistema
    usuarios: [
        {
            id: 1,
            email: "maria@gmail.com",
            password: "123456",
            rol: "CLIENTE",
            nombre: "María García",
            telefono: "999888777",
            direccion: "Av. Brasil 123, Breña"
        },
        {
            id: 2,
            email: "admin@farmacia.com",
            password: "admin123",
            rol: "ADMIN",
            nombre: "Juan Administrador",
            telefono: "999777666"
        },
        {
            id: 3,
            email: "delivery@farmacia.com",
            password: "delivery123",
            rol: "REPARTIDOR",
            nombre: "Pedro Repartidor",
            telefono: "999666555"
        }
    ],

    // Catálogo de productos
    productos: [
        {
            id: 1,
            nombre: "Paracetamol 500mg",
            precio: 5.50,
            stock: 100,
            categoria: "Analgésicos",
            imagen: "paracetamol.jpg",
            descripcion: "Caja x 10 tabletas"
        },
        {
            id: 2,
            nombre: "Ibuprofeno 400mg",
            precio: 8.00,
            stock: 75,
            categoria: "Analgésicos",
            imagen: "ibuprofeno.jpg",
            descripcion: "Caja x 10 cápsulas"
        },
        {
            id: 3,
            nombre: "Vitamina C 1000mg",
            precio: 12.00,
            stock: 50,
            categoria: "Vitaminas",
            imagen: "vitaminac.jpg",
            descripcion: "Frasco x 30 tabletas"
        },
        {
            id: 4,
            nombre: "Complejo B",
            precio: 15.00,
            stock: 40,
            categoria: "Vitaminas",
            imagen: "complejob.jpg",
            descripcion: "Frasco x 30 cápsulas"
        },
        {
            id: 5,
            nombre: "Alcohol 70°",
            precio: 6.00,
            stock: 200,
            categoria: "Primeros Auxilios",
            imagen: "alcohol.jpg",
            descripcion: "Frasco x 250ml"
        },
        {
            id: 6,
            nombre: "Gasas estériles",
            precio: 3.50,
            stock: 150,
            categoria: "Primeros Auxilios",
            imagen: "gasas.jpg",
            descripcion: "Paquete x 10 unidades"
        },
        {
            id: 7,
            nombre: "Mascarillas KN95",
            precio: 2.00,
            stock: 500,
            categoria: "Protección",
            imagen: "mascarilla.jpg",
            descripcion: "Unidad"
        },
        {
            id: 8,
            nombre: "Termómetro Digital",
            precio: 25.00,
            stock: 20,
            categoria: "Equipos",
            imagen: "termometro.jpg",
            descripcion: "Unidad"
        },
        {
            id: 9,
            nombre: "Jarabe para la tos",
            precio: 18.00,
            stock: 30,
            categoria: "Medicamentos",
            imagen: "jarabe.jpg",
            descripcion: "Frasco x 120ml"
        },
        {
            id: 10,
            nombre: "Crema antibiótica",
            precio: 22.00,
            stock: 25,
            categoria: "Medicamentos",
            imagen: "crema.jpg",
            descripcion: "Tubo x 15g"
        },
        {
            id: 11,
            nombre: "Pañales para adulto",
            precio: 35.00,
            stock: 15,
            categoria: "Cuidado Personal",
            imagen: "panales.jpg",
            descripcion: "Paquete x 10 unidades"
        },
        {
            id: 12,
            nombre: "Glucómetro",
            precio: 85.00,
            stock: 10,
            categoria: "Equipos",
            imagen: "glucometro.jpg",
            descripcion: "Kit completo"
        }
    ],

    // Categorías disponibles
    categorias: [
        "Todos",
        "Analgésicos",
        "Vitaminas",
        "Primeros Auxilios",
        "Protección",
        "Equipos",
        "Medicamentos",
        "Cuidado Personal"
    ],

    // Pedidos (simulados)
    pedidos: [
        {
            id: 1,
            numero: "ORD-001",
            clienteId: 1,
            clienteNombre: "María García",
            telefono: "999888777",
            direccion: "Av. Brasil 123, Breña",
            productos: [
                {id: 1, nombre: "Paracetamol 500mg", cantidad: 2, precio: 5.50, subtotal: 11.00},
                {id: 3, nombre: "Vitamina C 1000mg", cantidad: 1, precio: 12.00, subtotal: 12.00}
            ],
            total: 23.00,
            estado: "PENDIENTE",
            fecha: "2024-12-20 10:30",
            repartidorId: 3
        },
        {
            id: 2,
            numero: "ORD-002",
            clienteId: 1,
            clienteNombre: "María García",
            telefono: "999888777",
            direccion: "Av. Brasil 123, Breña",
            productos: [
                {id: 5, nombre: "Alcohol 70°", cantidad: 1, precio: 6.00, subtotal: 6.00}
            ],
            total: 6.00,
            estado: "ENTREGADO",
            fecha: "2024-12-19 15:45",
            repartidorId: 3
        }
    ],

    // Carrito actual (vacío al inicio)
    carrito: [],

    // Usuario logueado actual (null al inicio)
    usuarioActual: null
};

// Clase para manejar los datos
class DataStore {
    constructor() {
        this.initializeData();
    }

    // Inicializar datos en localStorage si no existen
    initializeData() {
        if (!localStorage.getItem('farmacia_data')) {
            localStorage.setItem('farmacia_data', JSON.stringify(INITIAL_DATA));
        }
    }

    // Obtener todos los datos
    getData() {
        return JSON.parse(localStorage.getItem('farmacia_data')) || INITIAL_DATA;productImages
    }

    // Guardar datos
    saveData(data) {
        localStorage.setItem('farmacia_data', JSON.stringify(data));
    }

    // Obtener productos
    getProductos() {
        const data = this.getData();
        return data.productos;
    }

    // Obtener producto por ID
    getProductoById(id) {
        const productos = this.getProductos();
        return productos.find(p => p.id === parseInt(id));
    }

    // Obtener categorías
    getCategorias() {
        const data = this.getData();
        return data.categorias;
    }

    // Obtener carrito
    getCarrito() {
        const data = this.getData();
        return data.carrito || [];
    }

    // Agregar al carrito
    addToCarrito(productoId, cantidad = 1) {
        const data = this.getData();
        const producto = this.getProductoById(productoId);
        
        if (!producto) return false;

        const itemExistente = data.carrito.find(item => item.id === productoId);
        
        if (itemExistente) {
            itemExistente.cantidad += cantidad;
        } else {
            data.carrito.push({
                id: producto.id,
                nombre: producto.nombre,
                precio: producto.precio,
                cantidad: cantidad,
                imagen: producto.imagen
            });
        }
        
        this.saveData(data);
        return true;
    }

    // Actualizar cantidad en carrito
    updateCarritoItem(productoId, cantidad) {
        const data = this.getData();
        const item = data.carrito.find(item => item.id === productoId);
        
        if (item) {
            if (cantidad <= 0) {
                data.carrito = data.carrito.filter(item => item.id !== productoId);
            } else {
                item.cantidad = cantidad;
            }
            this.saveData(data);
        }
    }

    // Limpiar carrito
    clearCarrito() {
        const data = this.getData();
        data.carrito = [];
        this.saveData(data);
    }

    // Login
    login(email, password) {
        const data = this.getData();
        const usuario = data.usuarios.find(u => 
            u.email === email && u.password === password
        );
        
        if (usuario) {
            data.usuarioActual = usuario;
            this.saveData(data);
            return usuario;
        }
        return null;
    }

    // Logout
    logout() {
        const data = this.getData();
        data.usuarioActual = null;
        this.saveData(data);
    }

    // Obtener usuario actual
    getCurrentUser() {
        const data = this.getData();
        return data.usuarioActual;
    }

    // Obtener pedidos del usuario actual
    getMisPedidos() {
        const data = this.getData();
        const usuario = data.usuarioActual;
        
        if (!usuario || usuario.rol !== 'CLIENTE') return [];
        
        return data.pedidos.filter(p => p.clienteId === usuario.id);
    }

    // Obtener todos los pedidos (admin/repartidor)
    getAllPedidos() {
        const data = this.getData();
        return data.pedidos;
    }

    // Crear nuevo pedido
    createPedido(datosCliente) {
        const data = this.getData();
        const carrito = data.carrito;
        
        if (carrito.length === 0) return null;

        const total = carrito.reduce((sum, item) => sum + (item.precio * item.cantidad), 0);
        
        const nuevoPedido = {
            id: data.pedidos.length + 1,
            numero: `ORD-${String(data.pedidos.length + 1).padStart(3, '0')}`,
            clienteId: data.usuarioActual?.id || null,
            clienteNombre: datosCliente.nombre,
            telefono: datosCliente.telefono,
            direccion: datosCliente.direccion,
            productos: carrito.map(item => ({
                ...item,
                subtotal: item.precio * item.cantidad
            })),
            total: total,
            estado: "PENDIENTE",
            fecha: new Date().toLocaleString('es-PE'),
            repartidorId: 3 // Auto-asignar al único repartidor
        };

        data.pedidos.push(nuevoPedido);
        data.carrito = []; // Limpiar carrito después de crear pedido
        this.saveData(data);
        
        return nuevoPedido;
    }

    // Actualizar estado de pedido
    updatePedidoEstado(pedidoId, nuevoEstado) {
        const data = this.getData();
        const pedido = data.pedidos.find(p => p.id === pedidoId);
        
        if (pedido) {
            pedido.estado = nuevoEstado;
            this.saveData(data);
            return true;
        }
        return false;
    }
}

// Crear instancia global
const dataStore = new DataStore();