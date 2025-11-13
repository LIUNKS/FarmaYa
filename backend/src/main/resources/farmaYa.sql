-- Roles
CREATE TABLE IF NOT EXISTS rol (
  rol_id INT AUTO_INCREMENT PRIMARY KEY,
  nombre VARCHAR(50) NOT NULL UNIQUE
);

-- Usuarios (clientes/admin)
CREATE TABLE IF NOT EXISTS usuario (
  usuario_id INT AUTO_INCREMENT PRIMARY KEY,
  rol_id INT NOT NULL,
  nombre VARCHAR(100) NOT NULL,
  email VARCHAR(150) NOT NULL UNIQUE,
  telefono VARCHAR(30),
  password_hash VARCHAR(255) NOT NULL,
  creado_en TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (rol_id) REFERENCES rol(rol_id)
);

-- Direcciones
CREATE TABLE IF NOT EXISTS direccion (
  direccion_id INT AUTO_INCREMENT PRIMARY KEY,
  usuario_id INT NULL,
  direccion_linea VARCHAR(255) NOT NULL,
  distrito VARCHAR(100),
  ciudad VARCHAR(100) DEFAULT 'Lima',
  referencia TEXT,
  creado_en TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (usuario_id) REFERENCES usuario(usuario_id) ON DELETE SET NULL
);

-- proveedores 
CREATE TABLE IF NOT EXISTS proveedor (
  proveedor_id INT AUTO_INCREMENT PRIMARY KEY,
  nombre VARCHAR(150) NOT NULL UNIQUE
);

-- productos 
CREATE TABLE IF NOT EXISTS producto (
  producto_id INT AUTO_INCREMENT PRIMARY KEY,
  sku VARCHAR(100) UNIQUE,
  nombre VARCHAR(200) NOT NULL,
  descripcion TEXT,
  presentacion VARCHAR(100),
  principio_activo VARCHAR(200),
  precio DECIMAL(10,2) NOT NULL,
  categoria VARCHAR(100),
  imagen_url VARCHAR(500),  
  stock INT NOT NULL DEFAULT 0,  
  proveedor_id INT NULL,
  activo BOOLEAN DEFAULT TRUE,
  creado_en TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (proveedor_id) REFERENCES proveedor(proveedor_id)
);

-- Stock por lote
CREATE TABLE IF NOT EXISTS loteproducto (
  lote_id INT AUTO_INCREMENT PRIMARY KEY,
  producto_id INT NOT NULL,
  numero_lote VARCHAR(100),
  cantidad INT NOT NULL DEFAULT 0,
  fecha_vencimiento DATE NULL,
  creado_en TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (producto_id) REFERENCES producto(producto_id) ON DELETE CASCADE,
  UNIQUE KEY uk_producto_lote (producto_id, numero_lote)
);

-- Pedidos
CREATE TABLE IF NOT EXISTS pedido (
  pedido_id INT AUTO_INCREMENT PRIMARY KEY,
  numero_pedido VARCHAR(50) NOT NULL UNIQUE,
  usuario_id INT NULL,
  direccion_entrega_id INT NULL,
  repartidor_id INT NULL,
  estado ENUM('Pendiente','Procesando','Entregado','Cancelado') DEFAULT 'Pendiente',
  subtotal DECIMAL(10,2) DEFAULT 0,
  total DECIMAL(10,2) DEFAULT 0,
  creado_en TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (usuario_id) REFERENCES usuario(usuario_id) ON DELETE SET NULL,
  FOREIGN KEY (direccion_entrega_id) REFERENCES direccion(direccion_id) ON DELETE SET NULL,
  FOREIGN KEY (repartidor_id) REFERENCES usuario(usuario_id) ON DELETE SET NULL
);

-- Detalle de pedido
CREATE TABLE IF NOT EXISTS detallepedido (
  detalle_pedido_id INT AUTO_INCREMENT PRIMARY KEY,
  pedido_id INT NOT NULL,
  producto_id INT NOT NULL,
  cantidad INT NOT NULL,
  precio_unit DECIMAL(10,2) NOT NULL,
  subtotal DECIMAL(10,2) NOT NULL,
  lote_id INT NULL,
  creado_en TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (pedido_id) REFERENCES pedido(pedido_id) ON DELETE CASCADE,
  FOREIGN KEY (producto_id) REFERENCES producto(producto_id),
  FOREIGN KEY (lote_id) REFERENCES loteproducto(lote_id) ON DELETE SET NULL
);

-- Pagos
CREATE TABLE IF NOT EXISTS pago (
  pago_id INT AUTO_INCREMENT PRIMARY KEY,
  pedido_id INT NOT NULL,
  metodo VARCHAR(50),
  monto DECIMAL(10,2) NOT NULL,
  estado_pago ENUM('Pagado','Pendiente','Fallido') DEFAULT 'Pendiente',
  creado_en TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (pedido_id) REFERENCES pedido(pedido_id) ON DELETE CASCADE
);

-- Carrito 
CREATE TABLE IF NOT EXISTS carrito (
  carrito_id INT AUTO_INCREMENT PRIMARY KEY,
  usuario_id INT NULL,
  session_token VARCHAR(255),
  creado_en TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (usuario_id) REFERENCES usuario(usuario_id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS itemcarrito (
  item_carrito_id INT AUTO_INCREMENT PRIMARY KEY,
  carrito_id INT NOT NULL,
  producto_id INT NOT NULL,
  cantidad INT NOT NULL DEFAULT 1,
  creado_en TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (carrito_id) REFERENCES carrito(carrito_id) ON DELETE CASCADE,
  FOREIGN KEY (producto_id) REFERENCES producto(producto_id) ON DELETE CASCADE,
  UNIQUE KEY uk_carrito_producto (carrito_id, producto_id)
);

-- Reportes de Ventas Semanales
CREATE TABLE IF NOT EXISTS reporteventasemanal (
  reporte_id INT AUTO_INCREMENT PRIMARY KEY,
  semana_inicio DATE NOT NULL,
  semana_fin DATE NOT NULL,
  year_semana VARCHAR(10) NOT NULL, -- Formato: "2025-W01"
  total_pedidos INT DEFAULT 0,
  total_productos_vendidos INT DEFAULT 0,
  total_ingresos DECIMAL(12,2) DEFAULT 0.00,
  producto_mas_vendido_id INT NULL,
  categoria_mas_vendida VARCHAR(100),
  generado_en TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (producto_mas_vendido_id) REFERENCES producto(producto_id),
  UNIQUE KEY uk_year_semana (year_semana)
);

-- Detalle de Ventas por producto Semanal
CREATE TABLE IF NOT EXISTS detalleventasemanalproducto (
  detalle_id INT AUTO_INCREMENT PRIMARY KEY,
  reporte_id INT NOT NULL,
  producto_id INT NOT NULL,
  cantidad_vendida INT NOT NULL,
  total_ingresos DECIMAL(10,2) NOT NULL,
  FOREIGN KEY (reporte_id) REFERENCES reporteventasemanal(reporte_id) ON DELETE CASCADE,
  FOREIGN KEY (producto_id) REFERENCES producto(producto_id),
  UNIQUE KEY uk_reporte_producto (reporte_id, producto_id)
);

-- Datos iniciales
INSERT IGNORE INTO rol (nombre) VALUES ('ADMIN'), ('CLIENTE');

COMMIT;

-- ####################################################################################
-- INSERTAR LOS proveedorES
-- ####################################################################################
INSERT IGNORE INTO proveedor (nombre) VALUES
('GENOMMA LAB'), ('IQFARMA'), ('ROEMMERS S.A.'), ('AC FARMA'), ('PORTUGAL'),
('CONFINASA'), ('NATURGEN'), ('NATURGEN-PQ'), ('UNIMED'), ('SHERFARMA'),
('FARMINDUSTRIA'), ('CAFERMA S.A.'), ('LUSA'), ('LANSIER'), ('LUKOLL'),
('OTHON'), ('PHARMAGEN'), ('GENFAR'), ('MEDIFARMA S.A.'), ('TEVA PERU'),
('GSK-LAB'), ('DEUTSCHE PHARMA'), ('BIOTOSCANA'), ('DEXCEL'), ('PERUFARMA'),
('DRONNVELS'), ('FARMAKONSUMA'), ('SAVAL'), ('VITA PHARMA'), ('ALBIS'),
('GRUNENTHAL'), ('INDURETA'), ('BAGO'), ('MEGA LABS'), ('SIEGFRIED'),
('REFASA'), ('ROPSOHN LABS'), ('RB HEALTH'), ('DISTRIBUIDORA'), ('LABORATORIO'),
('BAYER-PERUFARMA'), ('GLAXOSMITHKLINE'), ('CLASS'), ('SANOFI AVENTIS'),
('LABORATORIOS AC'), ('GEOFMAN'), ('LAFRANCOL'), ('EUROFARMA'), ('INTI'),
('TECNOFARMA'), ('AZ PHARMA'), ('HERSIL'), ('PFIZER');
COMMIT;


-- ####################################################################################
-- INSERTAR LOS 120 PRIMEROS productoS
-- ####################################################################################
INSERT IGNORE INTO producto (sku, nombre, presentacion, principio_activo, precio, categoria, imagen_url, stock, proveedor_id, activo) VALUES
('PROD-TXT-001', '3-GEL SUSP', 'Caja x 20 Sobres', 'MAGALDRATO+SIMETICONA', 55.00, 'Gastroenterología', '/img_1.png', 55, (SELECT proveedor_id FROM proveedor WHERE nombre = 'GENOMMA LAB'), TRUE),
('PROD-TXT-002', 'ABRILAR E.F. JARABE', 'Frasco x 200 ML', 'HEDERA HELIX', 42.00, 'Respiratorio', '/img_2.png', 42, (SELECT proveedor_id FROM proveedor WHERE nombre = 'ROEMMERS S.A.'), TRUE),
('PROD-TXT-003', 'ACIBELMAX COMPRIMIDOS', 'Caja x 100', 'MAGALDRATO+SIMETICONA', 80.00, 'Gastroenterología', '/img_3.png', 80, (SELECT proveedor_id FROM proveedor WHERE nombre = 'ROEMMERS S.A.'), TRUE),
('PROD-TXT-004', 'ACICLOVIR 200 MG', 'Caja x 35 tabletas', 'ACICLOVIR', 9.00, 'Antivirales', '/img_4.png', 9, (SELECT proveedor_id FROM proveedor WHERE nombre = 'IQFARMA'), TRUE),
('PROD-TXT-005', 'ACICLOVIR 5% CREMA', 'Tubo x 15 GR', 'ACICLOVIR', 8.50, 'Dermatología', '/img_5.png', 8, (SELECT proveedor_id FROM proveedor WHERE nombre = 'PORTUGAL'), TRUE),
('PROD-TXT-006', 'ACIDO FUSIDICO 2% CREMA', 'Tubo x 15 GR', 'ACIDO FUSIDICO', 20.00, 'Dermatología', '/img_6.png', 20, (SELECT proveedor_id FROM proveedor WHERE nombre = 'CONFINASA'), TRUE),
('PROD-TXT-007', 'ACIDO FOLICO 0.5 MG', 'Caja x 100 tabletas', 'ACIDO FOLICO', 4.00, 'Vitaminas', '/img_7.png', 4, (SELECT proveedor_id FROM proveedor WHERE nombre = 'SHERFARMA'), TRUE),
('PROD-TXT-008', 'ACIDO TRANEXAMICO 500 MG', 'Caja x 20 tabletas', 'ACIDO TRANEXAMICO', 69.00, 'Hematología', '/img_8.png', 69, (SELECT proveedor_id FROM proveedor WHERE nombre = 'HERSIL'), TRUE),
('PROD-TXT-009', 'ACROBRONQUIOL COMPUESTO JARABE', 'Frasco x 120 ML', 'AMBROXOL+CLEMBUTEROL', 19.50, 'Respiratorio', '/img_9.png', 19, (SELECT proveedor_id FROM proveedor WHERE nombre = 'PORTUGAL'), TRUE),
('PROD-TXT-010', 'ACTIMEN 500MG', 'Caja x 100 Tabletas', 'L-ARGININA', 114.00, 'Vitaminas', '/img_10.png', 114, (SELECT proveedor_id FROM proveedor WHERE nombre = 'DISTRIBUIDORA'), TRUE),
('PROD-TXT-011', 'ADALAT OROS 30MG', 'Caja x 30 Comprimidos', 'NIFEDIPINO', 56.50, 'Cardiovasculares', '/img_11.png', 56, (SELECT proveedor_id FROM proveedor WHERE nombre = 'BAYER-PERUFARMA'), TRUE),
('PROD-TXT-012', 'AERO-OM 40 MG/ML GOTAS', 'Frasco x 15 ML', 'SIMETICONA', 20.00, 'Gastroenterología', '/img_12.png', 20, (SELECT proveedor_id FROM proveedor WHERE nombre = 'UNIMED'), TRUE),
('PROD-TXT-013', 'AFLAREX 0.1% OFTALMICO', 'Frasco x 5 ML', 'FLUOROMETOLONA ACETATO', 29.50, 'Oftalmológicos', '/img_13.png', 29, (SELECT proveedor_id FROM proveedor WHERE nombre = 'AC FARMA'), TRUE),
('PROD-TXT-014', 'ALBENDAZOL 200 MG', 'Caja x 100 tabletas', 'ALBENDAZOL', 35.00, 'Antiparasitarios', '/img_14.png', 35, (SELECT proveedor_id FROM proveedor WHERE nombre = 'IQFARMA'), TRUE),
('PROD-TXT-015', 'ALERCET 10 MG', 'Caja x 100 tabletas', 'CETIRIZINA', 28.50, 'Antialérgicos', '/img_15.png', 28, (SELECT proveedor_id FROM proveedor WHERE nombre = 'CAFERMA S.A.'), TRUE),
('PROD-TXT-016', 'ALERCAS 5 MG', 'Caja x 30 tabletas', 'LEVOCETIRIZINA', 48.00, 'Antialérgicos', '/img_16.png', 48, (SELECT proveedor_id FROM proveedor WHERE nombre = 'LUSA'), TRUE),
('PROD-TXT-017', 'ALERFAST 180 MG', 'Caja x 10 tabletas', 'FEXOFENADINA', 38.90, 'Antialérgicos', '/img_17.png', 38, (SELECT proveedor_id FROM proveedor WHERE nombre = 'LANSIER'), TRUE),
('PROD-TXT-018', 'ALERGICAL D NF JARABE', 'Frasco x 60 ML', 'CLORFENAMINA+PSEUDOEFEDRINA', 12.00, 'Antigripales', '/img_18.png', 12, (SELECT proveedor_id FROM proveedor WHERE nombre = 'LUKOLL'), TRUE),
('PROD-TXT-019', 'ALERLIV 5 MG', 'Caja x 10 tabletas', 'LEVOCETIRIZINA', 20.00, 'Antialérgicos', '/img_19.png', 20, (SELECT proveedor_id FROM proveedor WHERE nombre = 'OTHON'), TRUE),
('PROD-TXT-020', 'ALERPRIV 10 MG', 'Caja x 100 tabletas', 'LORATADINA', 48.50, 'Antialérgicos', '/img_20.png', 48, (SELECT proveedor_id FROM proveedor WHERE nombre = 'PHARMAGEN'), TRUE),
('PROD-TXT-021', 'ALFATIL 250 MG/5ML SUSP.', 'Frasco x 100 ML', 'CEFACLOR', 42.00, 'Antibióticos', '/img_21.png', 42, (SELECT proveedor_id FROM proveedor WHERE nombre = 'GENFAR'), TRUE),
('PROD-TXT-022', 'ALIVIAMAX RAPIDA ACCION', 'Caja x 100 cápsulas', 'IBUPROFENO+CAFEINA', 45.00, 'Analgésicos', '/img_22.png', 45, (SELECT proveedor_id FROM proveedor WHERE nombre = 'MEDIFARMA S.A.'), TRUE),
('PROD-TXT-023', 'AMBROMED', 'Caja x 100 comprimidos', 'CLONIXINATO DE LISINA', 70.00, 'Analgésicos', '/img_23.png', 70, (SELECT proveedor_id FROM proveedor WHERE nombre = 'AZ PHARMA'), TRUE),
('PROD-TXT-024', 'AMOVAL 500 MG', 'Caja x 100 cápsulas', 'AMOXICILINA', 42.50, 'Antibióticos', '/img_24.png', 42, (SELECT proveedor_id FROM proveedor WHERE nombre = 'TEVA PERU'), TRUE),
('PROD-TXT-025', 'AMOXICILINA 500 MG + ACIDO CLAVULANICO 125 MG', 'Caja x 100 tabletas', 'AMOXICILINA+AC.CLAVULANICO', 150.00, 'Antibióticos', '/img_25.png', 150, (SELECT proveedor_id FROM proveedor WHERE nombre = 'IQFARMA'), TRUE),
('PROD-TXT-026', 'ANDROCUR 50MG', 'Caja x 20 Tabletas', 'CIPROTERONA', 112.50, 'Hormonales', '/img_26.png', 112, (SELECT proveedor_id FROM proveedor WHERE nombre = 'BAYER-PERUFARMA'), TRUE),
('PROD-TXT-027', 'ANFIBOL 200MG', 'Caja x 10 Tabletas', 'CEFIXIMA', 70.00, 'Antibióticos', '/img_27.png', 70, (SELECT proveedor_id FROM proveedor WHERE nombre = 'REFASA'), TRUE),
('PROD-TXT-028', 'APIRON 250MG/5ML JBE', 'Frasco x 60 ML', 'NAPROXENO', 12.00, 'Analgésicos', '/img_28.png', 12, (SELECT proveedor_id FROM proveedor WHERE nombre = 'SIEGFRIED'), TRUE),
('PROD-TXT-029', 'ARTRILASE 37.5/325MG', 'Caja x 20 Tabletas', 'TRAMADOL+PARACETAMOL', 42.50, 'Analgésicos', '/img_29.png', 42, (SELECT proveedor_id FROM proveedor WHERE nombre = 'MEGA LABS'), TRUE),
('PROD-TXT-030', 'ARTRONIL FORTE CAPSULAS', 'Caja x 100', 'IBUPROFENO+ORFENADRINA', 45.00, 'Analgésicos', '/img_30.png', 45, (SELECT proveedor_id FROM proveedor WHERE nombre = 'GENOMMA LAB'), TRUE),
('PROD-TXT-031', 'ARTROSAMIN 1.5GR/2.5GR SOBRES', 'Caja x 15', 'GLUCOSAMINA+CONDROITINA', 85.00, 'Suplementos', '/img_31.png', 85, (SELECT proveedor_id FROM proveedor WHERE nombre = 'BAGO'), TRUE),
('PROD-TXT-032', 'ASAWIN 81MG', 'Caja x 100 Tabletas', 'ACIDO ACETILSALICILICO', 13.50, 'Cardiovasculares', '/img_32.png', 13, (SELECT proveedor_id FROM proveedor WHERE nombre = 'INDURETA'), TRUE),
('PROD-TXT-033', 'ATARAX 10 MG', 'Caja x 30 tabletas', 'HIDROXIZINA', 28.50, 'Ansiolíticos', '/img_33.png', 28, (SELECT proveedor_id FROM proveedor WHERE nombre = 'TECNOFARMA'), TRUE),
('PROD-TXT-034', 'ATEPLAX 75MG', 'Caja x 100 Tabletas', 'CLOPIDOGREL', 140.00, 'Cardiovasculares', '/img_34.png', 140, (SELECT proveedor_id FROM proveedor WHERE nombre = 'GRUNENTHAL'), TRUE),
('PROD-TXT-035', 'ATIZOR 200MG', 'Caja x 100 Tabletas', 'ITRACONAZOL', 100.00, 'Antifúngicos', '/img_35.png', 100, (SELECT proveedor_id FROM proveedor WHERE nombre = 'ALBIS'), TRUE),
('PROD-TXT-036', 'ATORVASTATINA 20 MG', 'Caja x 30 tabletas', 'ATORVASTATINA', 37.00, 'Cardiovasculares', '/img_36.png', 37, (SELECT proveedor_id FROM proveedor WHERE nombre = 'CAFERMA S.A.'), TRUE),
('PROD-TXT-037', 'ATURAL 850MG', 'Caja x 30 Tabletas', 'METFORMINA', 14.50, 'Antidiabéticos', '/img_37.png', 14, (SELECT proveedor_id FROM proveedor WHERE nombre = 'VITA PHARMA'), TRUE),
('PROD-TXT-038', 'AZITRIN 200MG/5ML JBE', 'Frasco x 15 ML', 'AZITROMICINA', 10.00, 'Antibióticos', '/img_38.png', 10, (SELECT proveedor_id FROM proveedor WHERE nombre = 'SAVAL'), TRUE),
('PROD-TXT-039', 'AZITROX 200MG/5ML JBE', 'Frasco x 30 ML', 'AZITROMICINA', 20.00, 'Antibióticos', '/img_39.png', 20, (SELECT proveedor_id FROM proveedor WHERE nombre = 'FARMAKONSUMA'), TRUE),
('PROD-TXT-040', 'AZITROMICINA 500 MG', 'Caja x 15 tabletas', 'AZITROMICINA', 42.50, 'Antibióticos', '/img_40.png', 42, (SELECT proveedor_id FROM proveedor WHERE nombre = 'IQFARMA'), TRUE),
('PROD-TXT-041', 'AMBROXOL 30 MG', 'Caja x 100 Tabletas', 'AMBROXOL', 30.00, 'Respiratorio', '/img_41.png', 30, (SELECT proveedor_id FROM proveedor WHERE nombre = 'GENFAR'), TRUE),
('PROD-TXT-042', 'AMIKACINA 100MG/2ML', 'Caja x 1 Ampolla', 'AMIKACINA', 4.50, 'Antibióticos', '/img_42.png', 4, (SELECT proveedor_id FROM proveedor WHERE nombre = 'MEDIFARMA S.A.'), TRUE),
('PROD-TXT-043', 'AMIKACINA 500MG/2ML', 'Caja x 100 Ampollas', 'AMIKACINA', 380.00, 'Antibióticos', '/img_43.png', 380, (SELECT proveedor_id FROM proveedor WHERE nombre = 'GENFAR'), TRUE),
('PROD-TXT-044', 'AMINOFILINA 250MG/10ML', 'Caja x 100 Ampollas', 'AMINOFILINA', 80.00, 'Respiratorio', '/img_44.png', 80, (SELECT proveedor_id FROM proveedor WHERE nombre = 'GENFAR'), TRUE),
('PROD-TXT-045', 'AMIODARONA 200 MG', 'Caja x 20 Tabletas', 'AMIODARONA', 35.00, 'Cardiovasculares', '/img_45.png', 35, (SELECT proveedor_id FROM proveedor WHERE nombre = 'MEDIFARMA S.A.'), TRUE),
('PROD-TXT-046', 'AMITRIPTILINA 25 MG', 'Caja x 30 Tabletas', 'AMITRIPTILINA', 15.00, 'Antidepresivos', '/img_46.png', 15, (SELECT proveedor_id FROM proveedor WHERE nombre = 'GENFAR'), TRUE),
('PROD-TXT-047', 'AMLODIPINO 10 MG', 'Caja x 100 Tabletas', 'AMLODIPINO', 45.00, 'Cardiovasculares', '/img_47.png', 45, (SELECT proveedor_id FROM proveedor WHERE nombre = 'GENFAR'), TRUE),
('PROD-TXT-048', 'AMOXICILINA 125 MG/5ML', 'Frasco x 60 ML', 'AMOXICILINA', 4.50, 'Antibióticos', '/img_48.png', 4, (SELECT proveedor_id FROM proveedor WHERE nombre = 'GENFAR'), TRUE),
('PROD-TXT-049', 'AMOXICILINA 250 MG/5ML', 'Frasco x 60 ML', 'AMOXICILINA', 5.50, 'Antibióticos', '/img_49.png', 5, (SELECT proveedor_id FROM proveedor WHERE nombre = 'GENFAR'), TRUE),
('PROD-TXT-050', 'AMOXICILINA 500 MG', 'Caja x 500 Cápsulas', 'AMOXICILINA', 120.00, 'Antibióticos', '/img_50.png', 120, (SELECT proveedor_id FROM proveedor WHERE nombre = 'GENFAR'), TRUE),
('PROD-TXT-051', 'AMOXICILINA 875 MG + ACIDO CLAVULANICO 125 MG', 'Caja x 14 Tabletas', 'AMOXICILINA+AC.CLAVULANICO', 85.00, 'Antibióticos', '/img_51.png', 85, (SELECT proveedor_id FROM proveedor WHERE nombre = 'GENFAR'), TRUE),
('PROD-TXT-052', 'AMPICILINA 1 G', 'Caja x 100 Frascos', 'AMPICILINA', 350.00, 'Antibióticos', '/img_52.png', 350, (SELECT proveedor_id FROM proveedor WHERE nombre = 'GENFAR'), TRUE),
('PROD-TXT-053', 'AMPICILINA 500 MG', 'Caja x 100 Cápsulas', 'AMPICILINA', 40.00, 'Antibióticos', '/img_53.png', 40, (SELECT proveedor_id FROM proveedor WHERE nombre = 'GENFAR'), TRUE),
('PROD-TXT-054', 'APRONAX 550 MG', 'Caja x 60 Tabletas', 'NAPROXENO', 120.00, 'Analgésicos', '/img_54.png', 120, (SELECT proveedor_id FROM proveedor WHERE nombre = 'BAYER-PERUFARMA'), TRUE),
('PROD-TXT-055', 'ARADOI 50 MG', 'Caja x 30 Tabletas', 'LOSARTAN', 45.00, 'Cardiovasculares', '/img_55.png', 45, (SELECT proveedor_id FROM proveedor WHERE nombre = 'ROEMMERS S.A.'), TRUE),
('PROD-TXT-056', 'ARTRICAM 15 MG', 'Caja x 100 Tabletas', 'MELOXICAM', 80.00, 'Analgésicos', '/img_56.png', 80, (SELECT proveedor_id FROM proveedor WHERE nombre = 'ROEMMERS S.A.'), TRUE),
('PROD-TXT-057', 'ARTRIDEN 4 MG', 'Caja x 10 Tabletas', 'TIOCOLCHICOSIDO', 30.00, 'Relajantes Musculares', '/img_57.png', 30, (SELECT proveedor_id FROM proveedor WHERE nombre = 'ROEMMERS S.A.'), TRUE),
('PROD-TXT-058', 'ARTRINEO 200 MG', 'Caja x 20 Cápsulas', 'CELECOXIB', 60.00, 'Analgésicos', '/img_58.png', 60, (SELECT proveedor_id FROM proveedor WHERE nombre = 'ROEMMERS S.A.'), TRUE),
('PROD-TXT-059', 'ARTROFLEX 1.5 G', 'Caja x 30 Sobres', 'GLUCOSAMINA', 90.00, 'Suplementos', '/img_59.png', 90, (SELECT proveedor_id FROM proveedor WHERE nombre = 'ROEMMERS S.A.'), TRUE),
('PROD-TXT-060', 'ARTROX 90 MG', 'Caja x 14 Tabletas', 'ETORICOXIB', 70.00, 'Analgésicos', '/img_60.png', 70, (SELECT proveedor_id FROM proveedor WHERE nombre = 'ROEMMERS S.A.'), TRUE),
('PROD-TXT-061', 'ASAWIN 81 MG', 'Caja x 100 Tabletas', 'ACIDO ACETILSALICILICO', 13.50, 'Cardiovasculares', '/img_61.png', 13, (SELECT proveedor_id FROM proveedor WHERE nombre = 'INDURETA'), TRUE),
('PROD-TXT-062', 'ATEPLAX 75 MG', 'Caja x 100 Tabletas', 'CLOPIDOGREL', 140.00, 'Cardiovasculares', '/img_62.png', 140, (SELECT proveedor_id FROM proveedor WHERE nombre = 'GRUNENTHAL'), TRUE),
('PROD-TXT-063', 'ATIZOR 200 MG', 'Caja x 100 Tabletas', 'ITRACONAZOL', 100.00, 'Antifúngicos', '/img_63.png', 100, (SELECT proveedor_id FROM proveedor WHERE nombre = 'ALBIS'), TRUE),
('PROD-TXT-064', 'ATORVASTATINA 20 MG', 'Caja x 30 tabletas', 'ATORVASTATINA', 37.00, 'Cardiovasculares', '/img_64.png', 37, (SELECT proveedor_id FROM proveedor WHERE nombre = 'CAFERMA S.A.'), TRUE),
('PROD-TXT-065', 'ATURAL 850 MG', 'Caja x 30 Tabletas', 'METFORMINA', 14.50, 'Antidiabéticos', '/img_65.png', 14, (SELECT proveedor_id FROM proveedor WHERE nombre = 'VITA PHARMA'), TRUE),
('PROD-TXT-066', 'AZITRIN 200MG/5ML JBE', 'Frasco x 15 ML', 'AZITROMICINA', 10.00, 'Antibióticos', '/img_66.png', 10, (SELECT proveedor_id FROM proveedor WHERE nombre = 'SAVAL'), TRUE),
('PROD-TXT-067', 'AZITROX 200MG/5ML JBE', 'Frasco x 30 ML', 'AZITROMICINA', 20.00, 'Antibióticos', '/img_67.png', 20, (SELECT proveedor_id FROM proveedor WHERE nombre = 'FARMAKONSUMA'), TRUE),
('PROD-TXT-068', 'AZITROMICINA 500 MG', 'Caja x 15 tabletas', 'AZITROMICINA', 42.50, 'Antibióticos', '/img_68.png', 42, (SELECT proveedor_id FROM proveedor WHERE nombre = 'IQFARMA'), TRUE),
('PROD-TXT-069', 'ALCOHOL 70%', 'Botella x 1 L', 'ALCOHOL ETILICO', 10.00, 'Primeros Auxilios', '/img_69.png', 200, (SELECT proveedor_id FROM proveedor WHERE nombre = 'LABORATORIO'), TRUE),
('PROD-TXT-070', 'ALGODON', 'Bolsa x 100 G', 'ALGODON', 5.00, 'Primeros Auxilios', '/img_70.png', 300, (SELECT proveedor_id FROM proveedor WHERE nombre = 'LABORATORIO'), TRUE),
('PROD-TXT-071', 'AGUA OXIGENADA 10 VOL', 'Botella x 120 ML', 'PEROXIDO DE HIDROGENO', 3.00, 'Primeros Auxilios', '/img_71.png', 250, (SELECT proveedor_id FROM proveedor WHERE nombre = 'LABORATORIO'), TRUE),
('PROD-TXT-072', 'APRACUR FORTE', 'Caja x 100 Tabletas', 'PARACETAMOL+CLORFENAMINA+CAFEINA', 40.00, 'Antigripales', '/img_72.png', 150, (SELECT proveedor_id FROM proveedor WHERE nombre = 'ROEMMERS S.A.'), TRUE),
('PROD-TXT-073', 'ACETAMINOFEN 500 MG', 'Caja x 100 Tabletas', 'ACETAMINOFEN', 10.00, 'Analgésicos', '/img_73.png', 500, (SELECT proveedor_id FROM proveedor WHERE nombre = 'GENFAR'), TRUE),
('PROD-TXT-074', 'ACICLOVIR 800 MG', 'Caja x 35 Tabletas', 'ACICLOVIR', 50.00, 'Antivirales', '/img_74.png', 80, (SELECT proveedor_id FROM proveedor WHERE nombre = 'GENFAR'), TRUE),
('PROD-TXT-075', 'AEROFLUX JARABE', 'Frasco x 120 ML', 'SALBUTAMOL+AMBROXOL', 25.00, 'Respiratorio', '/img_75.png', 100, (SELECT proveedor_id FROM proveedor WHERE nombre = 'ROEMMERS S.A.'), TRUE),
('PROD-TXT-076', 'ALBENDAZOL 400 MG', 'Caja x 50 Tabletas', 'ALBENDAZOL', 60.00, 'Antiparasitarios', '/img_76.png', 120, (SELECT proveedor_id FROM proveedor WHERE nombre = 'GENFAR'), TRUE),
('PROD-TXT-077', 'ALERGINA 10 MG', 'Caja x 100 Tabletas', 'LORATADINA', 30.00, 'Antialérgicos', '/img_77.png', 200, (SELECT proveedor_id FROM proveedor WHERE nombre = 'ROEMMERS S.A.'), TRUE),
('PROD-TXT-078', 'ALIVIUM 400 MG', 'Caja x 20 Cápsulas', 'IBUPROFENO', 15.00, 'Analgésicos', '/img_78.png', 180, (SELECT proveedor_id FROM proveedor WHERE nombre = 'ROEMMERS S.A.'), TRUE),
('PROD-TXT-079', 'ALOPURINOL 300 MG', 'Caja x 100 Tabletas', 'ALOPURINOL', 40.00, 'Analgésicos', '/img_79.png', 90, (SELECT proveedor_id FROM proveedor WHERE nombre = 'GENFAR'), TRUE),
('PROD-TXT-080', 'ALPRAZOLAM 0.5 MG', 'Caja x 30 Tabletas', 'ALPRAZOLAM', 25.00, 'Ansiolíticos', '/img_80.png', 110, (SELECT proveedor_id FROM proveedor WHERE nombre = 'ROEMMERS S.A.'), TRUE),
('PROD-TXT-081', 'AMBROXOL 15 MG/5ML JARABE', 'Frasco x 120 ML', 'AMBROXOL', 8.00, 'Respiratorio', '/img_81.png', 300, (SELECT proveedor_id FROM proveedor WHERE nombre = 'GENFAR'), TRUE),
('PROD-TXT-082', 'AMIKACINA 500 MG/2ML INYECTABLE', 'Caja x 1 Ampolla', 'AMIKACINA', 7.00, 'Antibióticos', '/img_82.png', 150, (SELECT proveedor_id FROM proveedor WHERE nombre = 'GENFAR'), TRUE),
('PROD-TXT-083', 'AMINOFILINA 100 MG', 'Caja x 20 Tabletas', 'AMINOFILINA', 12.00, 'Respiratorio', '/img_83.png', 130, (SELECT proveedor_id FROM proveedor WHERE nombre = 'ROEMMERS S.A.'), TRUE),
('PROD-TXT-084', 'AMIODARONA 200 MG', 'Caja x 20 Tabletas', 'AMIODARONA', 35.00, 'Cardiovasculares', '/img_84.png', 80, (SELECT proveedor_id FROM proveedor WHERE nombre = 'ROEMMERS S.A.'), TRUE),
('PROD-TXT-085', 'AMITRIPTILINA 25 MG', 'Caja x 30 Tabletas', 'AMITRIPTILINA', 15.00, 'Antidepresivos', '/img_85.png', 100, (SELECT proveedor_id FROM proveedor WHERE nombre = 'ROEMMERS S.A.'), TRUE),
('PROD-TXT-086', 'AMLODIPINO 5 MG', 'Caja x 30 Tabletas', 'AMLODIPINO', 20.00, 'Cardiovasculares', '/img_86.png', 160, (SELECT proveedor_id FROM proveedor WHERE nombre = 'ROEMMERS S.A.'), TRUE),
('PROD-TXT-087', 'AMOXICILINA 500 MG', 'Caja x 100 Cápsulas', 'AMOXICILINA', 30.00, 'Antibióticos', '/img_87.png', 400, (SELECT proveedor_id FROM proveedor WHERE nombre = 'GENFAR'), TRUE),
('PROD-TXT-088', 'AMOXICILINA 250 MG/5ML SUSPENSION', 'Frasco x 60 ML', 'AMOXICILINA', 6.00, 'Antibióticos', '/img_88.png', 250, (SELECT proveedor_id FROM proveedor WHERE nombre = 'GENFAR'), TRUE),
('PROD-TXT-089', 'AMPICILINA 500 MG', 'Caja x 100 Cápsulas', 'AMPICILINA', 40.00, 'Antibióticos', '/img_89.png', 350, (SELECT proveedor_id FROM proveedor WHERE nombre = 'GENFAR'), TRUE),
('PROD-TXT-090', 'ANTALGINA 500 MG', 'Caja x 100 Tabletas', 'METAMIZOL SODICO', 20.00, 'Analgésicos', '/img_90.png', 300, (SELECT proveedor_id FROM proveedor WHERE nombre = 'ROEMMERS S.A.'), TRUE),
('PROD-TXT-091', 'ASPIRINA 100 MG', 'Caja x 100 Tabletas', 'ACIDO ACETILSALICILICO', 15.00, 'Cardiovasculares', '/img_91.png', 280, (SELECT proveedor_id FROM proveedor WHERE nombre = 'BAYER-PERUFARMA'), TRUE),
('PROD-TXT-092', 'ATENOLOL 100 MG', 'Caja x 30 Tabletas', 'ATENOLOL', 18.00, 'Cardiovasculares', '/img_92.png', 140, (SELECT proveedor_id FROM proveedor WHERE nombre = 'ROEMMERS S.A.'), TRUE),
('PROD-TXT-093', 'ATORVASTATINA 10 MG', 'Caja x 30 Tabletas', 'ATORVASTATINA', 30.00, 'Cardiovasculares', '/img_93.png', 110, (SELECT proveedor_id FROM proveedor WHERE nombre = 'ROEMMERS S.A.'), TRUE),
('PROD-TXT-094', 'AZITROMICINA 500 MG', 'Caja x 3 Tabletas', 'AZITROMICINA', 15.00, 'Antibióticos', '/img_94.png', 200, (SELECT proveedor_id FROM proveedor WHERE nombre = 'ROEMMERS S.A.'), TRUE),
('PROD-TXT-095', 'AZITROMICINA 200 MG/5ML SUSPENSION', 'Frasco x 15 ML', 'AZITROMICINA', 12.00, 'Antibióticos', '/img_95.png', 130, (SELECT proveedor_id FROM proveedor WHERE nombre = 'ROEMMERS S.A.'), TRUE),
('PROD-TXT-096', 'ACETILCISTEINA 600 MG', 'Caja x 10 Sobres', 'ACETILCISTEINA', 25.00, 'Respiratorio', '/img_96.png', 100, (SELECT proveedor_id FROM proveedor WHERE nombre = 'ROEMMERS S.A.'), TRUE),
('PROD-TXT-097', 'ACIDO ALENDRONICO 70 MG', 'Caja x 4 Tabletas', 'ACIDO ALENDRONICO', 40.00, 'Metabolismo Oseo', '/img_97.png', 70, (SELECT proveedor_id FROM proveedor WHERE nombre = 'ROEMMERS S.A.'), TRUE),
('PROD-TXT-098', 'ACIDO MICOFENOLICO 500 MG', 'Caja x 50 Tabletas', 'ACIDO MICOFENOLICO', 250.00, 'Inmunosupresores', '/img_98.png', 30, (SELECT proveedor_id FROM proveedor WHERE nombre = 'ROEMMERS S.A.'), TRUE),
('PROD-TXT-099', 'ACIDO URSODEOXICOLICO 300 MG', 'Caja x 20 Tabletas', 'ACIDO URSODEOXICOLICO', 80.00, 'Gastroenterología', '/img_99.png', 50, (SELECT proveedor_id FROM proveedor WHERE nombre = 'ROEMMERS S.A.'), TRUE),
('PROD-TXT-100', 'ACIDO VALPROICO 250 MG', 'Caja x 30 Tabletas', 'ACIDO VALPROICO', 35.00, 'Neurología', '/img_100.png', 90, (SELECT proveedor_id FROM proveedor WHERE nombre = 'ROEMMERS S.A.'), TRUE),
('PROD-TXT-101', 'ACTRON 600 MG', 'Caja x 10 Cápsulas', 'IBUPROFENO', 12.00, 'Analgésicos', '/img_101.png', 180, (SELECT proveedor_id FROM proveedor WHERE nombre = 'BAYER-PERUFARMA'), TRUE),
('PROD-TXT-102', 'ADACEL', 'Jeringa Prellenada', 'VACUNA DTPA', 150.00, 'Vacunas', '/img_102.png', 40, (SELECT proveedor_id FROM proveedor WHERE nombre = 'SANOFI AVENTIS'), TRUE),
('PROD-TXT-103', 'ADRENALINA 1 MG/ML', 'Caja x 100 Ampollas', 'ADRENALINA', 180.00, 'Cardiovasculares', '/img_103.png', 60, (SELECT proveedor_id FROM proveedor WHERE nombre = 'GENFAR'), TRUE),
('PROD-TXT-104', 'AGRASTAT 0.25 MG/ML', 'Frasco x 50 ML', 'TIROFIBAN', 450.00, 'Cardiovasculares', '/img_104.png', 20, (SELECT proveedor_id FROM proveedor WHERE nombre = 'ROEMMERS S.A.'), TRUE),
('PROD-TXT-105', 'ALDACTONE-A 25 MG', 'Caja x 20 Tabletas', 'ESPIRONOLACTONA', 22.00, 'Cardiovasculares', '/img_105.png', 110, (SELECT proveedor_id FROM proveedor WHERE nombre = 'ROEMMERS S.A.'), TRUE),
('PROD-TXT-106', 'ALENDRONATO 70 MG', 'Caja x 4 Tabletas', 'ALENDRONATO', 35.00, 'Metabolismo Oseo', '/img_106.png', 80, (SELECT proveedor_id FROM proveedor WHERE nombre = 'GENFAR'), TRUE),
('PROD-TXT-107', 'ALERFORT', 'Frasco x 120 ML', 'DEXCLORFENIRAMINA+BETAMETASONA', 28.00, 'Antialérgicos', '/img_107.png', 100, (SELECT proveedor_id FROM proveedor WHERE nombre = 'ROEMMERS S.A.'), TRUE),
('PROD-TXT-108', 'ALFADOM 250 MG', 'Caja x 30 Tabletas', 'METILDOPA', 40.00, 'Cardiovasculares', '/img_108.png', 90, (SELECT proveedor_id FROM proveedor WHERE nombre = 'ROEMMERS S.A.'), TRUE),
('PROD-TXT-109', 'ALIZAPRIDA 50 MG', 'Caja x 20 Tabletas', 'ALIZAPRIDA', 30.00, 'Antieméticos', '/img_109.png', 120, (SELECT proveedor_id FROM proveedor WHERE nombre = 'ROEMMERS S.A.'), TRUE),
('PROD-TXT-110', 'ALPROSTADIL 20 MCG', 'Caja x 1 Ampolla', 'ALPROSTADIL', 80.00, 'Urología', '/img_110.png', 50, (SELECT proveedor_id FROM proveedor WHERE nombre = 'ROEMMERS S.A.'), TRUE),
('PROD-TXT-111', 'ALTACEF 500 MG', 'Caja x 10 Tabletas', 'CEFUROXIMA', 60.00, 'Antibióticos', '/img_111.png', 100, (SELECT proveedor_id FROM proveedor WHERE nombre = 'ROEMMERS S.A.'), TRUE),
('PROD-TXT-112', 'AMBROXOL CLENBUTEROL JARABE', 'Frasco x 120 ML', 'AMBROXOL+CLENBUTEROL', 18.00, 'Respiratorio', '/img_112.png', 150, (SELECT proveedor_id FROM proveedor WHERE nombre = 'GENFAR'), TRUE),
('PROD-TXT-113', 'AMIKACINA 500 MG/2ML', 'Caja x 10 Ampollas', 'AMIKACINA', 60.00, 'Antibióticos', '/img_113.png', 120, (SELECT proveedor_id FROM proveedor WHERE nombre = 'GENFAR'), TRUE),
('PROD-TXT-114', 'AMOXIDAL DUO 1 G', 'Caja x 14 Tabletas', 'AMOXICILINA', 50.00, 'Antibióticos', '/img_114.png', 180, (SELECT proveedor_id FROM proveedor WHERE nombre = 'ROEMMERS S.A.'), TRUE),
('PROD-TXT-115', 'AMPICILINA + SULBACTAM 1.5 G', 'Caja x 1 Frasco', 'AMPICILINA+SULBACTAM', 25.00, 'Antibióticos', '/img_115.png', 200, (SELECT proveedor_id FROM proveedor WHERE nombre = 'GENFAR'), TRUE),
('PROD-TXT-116', 'ANALGAN 1 G', 'Caja x 10 Tabletas', 'PARACETAMOL', 8.00, 'Analgésicos', '/img_116.png', 300, (SELECT proveedor_id FROM proveedor WHERE nombre = 'ROEMMERS S.A.'), TRUE),
('PROD-TXT-117', 'ANSAID 100 MG', 'Caja x 20 Tabletas', 'FLURBIPROFENO', 30.00, 'Analgésicos', '/img_117.png', 120, (SELECT proveedor_id FROM proveedor WHERE nombre = 'ROEMMERS S.A.'), TRUE),
('PROD-TXT-118', 'APIDRA 100 UI/ML', 'Pluma Precargada', 'INSULINA GLULISINA', 90.00, 'Antidiabéticos', '/img_118.png', 70, (SELECT proveedor_id FROM proveedor WHERE nombre = 'SANOFI AVENTIS'), TRUE),
('PROD-TXT-119', 'APRONAX 275 MG', 'Caja x 20 Tabletas', 'NAPROXENO', 35.00, 'Analgésicos', '/img_119.png', 150, (SELECT proveedor_id FROM proveedor WHERE nombre = 'BAYER-PERUFARMA'), TRUE),
('PROD-TXT-120', 'ARAVA 20 MG', 'Caja x 30 Tabletas', 'LEFLUNOMIDA', 300.00, 'Antirreumáticos', '/img_120.png', 40, (SELECT proveedor_id FROM proveedor WHERE nombre = 'SANOFI AVENTIS'), TRUE);
COMMIT;


-- ####################################################################################
-- INSERTAR LOS LOTES PARA LOS 120 productoS
-- ####################################################################################
INSERT IGNORE INTO loteproducto (producto_id, numero_lote, cantidad, fecha_vencimiento) VALUES
((SELECT producto_id FROM producto WHERE sku = 'PROD-TXT-001'), 'LOTE-A001', 55, '2025-12-30'),
((SELECT producto_id FROM producto WHERE sku = 'PROD-TXT-002'), 'LOTE-A002', 42, '2026-01-30'),
((SELECT producto_id FROM producto WHERE sku = 'PROD-TXT-003'), 'LOTE-A003', 80, '2027-02-28'),
((SELECT producto_id FROM producto WHERE sku = 'PROD-TXT-004'), 'LOTE-A004', 9, '2026-03-30'),
((SELECT producto_id FROM producto WHERE sku = 'PROD-TXT-005'), 'LOTE-A005', 8, '2027-04-30'),
((SELECT producto_id FROM producto WHERE sku = 'PROD-TXT-006'), 'LOTE-A006', 20, '2026-05-30'),
((SELECT producto_id FROM producto WHERE sku = 'PROD-TXT-007'), 'LOTE-A007', 4, '2025-11-30'),
((SELECT producto_id FROM producto WHERE sku = 'PROD-TXT-008'), 'LOTE-A008', 69, '2026-12-30'),
((SELECT producto_id FROM producto WHERE sku = 'PROD-TXT-009'), 'LOTE-A009', 19, '2027-01-30'),
((SELECT producto_id FROM producto WHERE sku = 'PROD-TXT-010'), 'LOTE-A010', 114, '2026-02-28'),
((SELECT producto_id FROM producto WHERE sku = 'PROD-TXT-011'), 'LOTE-A011', 56, '2027-03-30'),
((SELECT producto_id FROM producto WHERE sku = 'PROD-TXT-012'), 'LOTE-A012', 20, '2026-04-30'),
((SELECT producto_id FROM producto WHERE sku = 'PROD-TXT-013'), 'LOTE-A013', 29, '2027-05-30'),
((SELECT producto_id FROM producto WHERE sku = 'PROD-TXT-014'), 'LOTE-A014', 35, '2026-06-30'),
((SELECT producto_id FROM producto WHERE sku = 'PROD-TXT-015'), 'LOTE-A015', 28, '2027-07-30'),
((SELECT producto_id FROM producto WHERE sku = 'PROD-TXT-016'), 'LOTE-A016', 48, '2026-08-30'),
((SELECT producto_id FROM producto WHERE sku = 'PROD-TXT-017'), 'LOTE-A017', 38, '2027-09-30'),
((SELECT producto_id FROM producto WHERE sku = 'PROD-TXT-018'), 'LOTE-A018', 12, '2025-10-30'),
((SELECT producto_id FROM producto WHERE sku = 'PROD-TXT-019'), 'LOTE-A019', 20, '2026-11-30'),
((SELECT producto_id FROM producto WHERE sku = 'PROD-TXT-020'), 'LOTE-A020', 48, '2027-12-30'),
((SELECT producto_id FROM producto WHERE sku = 'PROD-TXT-021'), 'LOTE-A021', 42, '2026-01-30'),
((SELECT producto_id FROM producto WHERE sku = 'PROD-TXT-022'), 'LOTE-A022', 45, '2027-02-28'),
((SELECT producto_id FROM producto WHERE sku = 'PROD-TXT-023'), 'LOTE-A023', 70, '2026-03-30'),
((SELECT producto_id FROM producto WHERE sku = 'PROD-TXT-024'), 'LOTE-A024', 42, '2027-04-30'),
((SELECT producto_id FROM producto WHERE sku = 'PROD-TXT-025'), 'LOTE-A025', 150, '2026-05-30'),
((SELECT producto_id FROM producto WHERE sku = 'PROD-TXT-026'), 'LOTE-A026', 112, '2027-06-30'),
((SELECT producto_id FROM producto WHERE sku = 'PROD-TXT-027'), 'LOTE-A027', 70, '2026-07-30'),
((SELECT producto_id FROM producto WHERE sku = 'PROD-TXT-028'), 'LOTE-A028', 12, '2027-08-30'),
((SELECT producto_id FROM producto WHERE sku = 'PROD-TXT-029'), 'LOTE-A029', 42, '2026-09-30'),
((SELECT producto_id FROM producto WHERE sku = 'PROD-TXT-030'), 'LOTE-A030', 45, '2027-10-30'),
((SELECT producto_id FROM producto WHERE sku = 'PROD-TXT-031'), 'LOTE-A031', 85, '2026-11-30'),
((SELECT producto_id FROM producto WHERE sku = 'PROD-TXT-032'), 'LOTE-A032', 13, '2027-12-30'),
((SELECT producto_id FROM producto WHERE sku = 'PROD-TXT-033'), 'LOTE-A033', 28, '2026-01-30'),
((SELECT producto_id FROM producto WHERE sku = 'PROD-TXT-034'), 'LOTE-A034', 140, '2027-02-28'),
((SELECT producto_id FROM producto WHERE sku = 'PROD-TXT-035'), 'LOTE-A035', 100, '2026-03-30'),
((SELECT producto_id FROM producto WHERE sku = 'PROD-TXT-036'), 'LOTE-A036', 37, '2027-04-30'),
((SELECT producto_id FROM producto WHERE sku = 'PROD-TXT-037'), 'LOTE-A037', 14, '2026-05-30'),
((SELECT producto_id FROM producto WHERE sku = 'PROD-TXT-038'), 'LOTE-A038', 10, '2027-06-30'),
((SELECT producto_id FROM producto WHERE sku = 'PROD-TXT-039'), 'LOTE-A039', 20, '2026-07-30'),
((SELECT producto_id FROM producto WHERE sku = 'PROD-TXT-040'), 'LOTE-A040', 42, '2027-08-30'),
((SELECT producto_id FROM producto WHERE sku = 'PROD-TXT-041'), 'LOTE-A041', 30, '2026-09-30'),
((SELECT producto_id FROM producto WHERE sku = 'PROD-TXT-042'), 'LOTE-A042', 4, '2027-10-30'),
((SELECT producto_id FROM producto WHERE sku = 'PROD-TXT-043'), 'LOTE-A043', 380, '2026-11-30'),
((SELECT producto_id FROM producto WHERE sku = 'PROD-TXT-044'), 'LOTE-A044', 80, '2027-12-30'),
((SELECT producto_id FROM producto WHERE sku = 'PROD-TXT-045'), 'LOTE-A045', 35, '2026-01-30'),
((SELECT producto_id FROM producto WHERE sku = 'PROD-TXT-046'), 'LOTE-A046', 15, '2027-02-28'),
((SELECT producto_id FROM producto WHERE sku = 'PROD-TXT-047'), 'LOTE-A047', 45, '2026-03-30'),
((SELECT producto_id FROM producto WHERE sku = 'PROD-TXT-048'), 'LOTE-A048', 4, '2027-04-30'),
((SELECT producto_id FROM producto WHERE sku = 'PROD-TXT-049'), 'LOTE-A049', 5, '2026-05-30'),
((SELECT producto_id FROM producto WHERE sku = 'PROD-TXT-050'), 'LOTE-A050', 120, '2027-06-30'),
((SELECT producto_id FROM producto WHERE sku = 'PROD-TXT-051'), 'LOTE-A051', 85, '2026-07-30'),
((SELECT producto_id FROM producto WHERE sku = 'PROD-TXT-052'), 'LOTE-A052', 350, '2027-08-30'),
((SELECT producto_id FROM producto WHERE sku = 'PROD-TXT-053'), 'LOTE-A053', 40, '2026-09-30'),
((SELECT producto_id FROM producto WHERE sku = 'PROD-TXT-054'), 'LOTE-A054', 120, '2027-10-30'),
((SELECT producto_id FROM producto WHERE sku = 'PROD-TXT-055'), 'LOTE-A055', 45, '2026-11-30'),
((SELECT producto_id FROM producto WHERE sku = 'PROD-TXT-056'), 'LOTE-A056', 80, '2027-12-30'),
((SELECT producto_id FROM producto WHERE sku = 'PROD-TXT-057'), 'LOTE-A057', 30, '2026-01-30'),
((SELECT producto_id FROM producto WHERE sku = 'PROD-TXT-058'), 'LOTE-A058', 60, '2027-02-28'),
((SELECT producto_id FROM producto WHERE sku = 'PROD-TXT-059'), 'LOTE-A059', 90, '2026-03-30'),
((SELECT producto_id FROM producto WHERE sku = 'PROD-TXT-060'), 'LOTE-A060', 70, '2027-04-30'),
((SELECT producto_id FROM producto WHERE sku = 'PROD-TXT-061'), 'LOTE-A061', 13, '2026-05-30'),
((SELECT producto_id FROM producto WHERE sku = 'PROD-TXT-062'), 'LOTE-A062', 140, '2027-06-30'),
((SELECT producto_id FROM producto WHERE sku = 'PROD-TXT-063'), 'LOTE-A063', 100, '2026-07-30'),
((SELECT producto_id FROM producto WHERE sku = 'PROD-TXT-064'), 'LOTE-A064', 37, '2027-08-30'),
((SELECT producto_id FROM producto WHERE sku = 'PROD-TXT-065'), 'LOTE-A065', 14, '2026-09-30'),
((SELECT producto_id FROM producto WHERE sku = 'PROD-TXT-066'), 'LOTE-A066', 10, '2027-10-30'),
((SELECT producto_id FROM producto WHERE sku = 'PROD-TXT-067'), 'LOTE-A067', 20, '2026-11-30'),
((SELECT producto_id FROM producto WHERE sku = 'PROD-TXT-068'), 'LOTE-A068', 42, '2027-12-30'),
((SELECT producto_id FROM producto WHERE sku = 'PROD-TXT-069'), 'LOTE-A069', 200, '2026-01-30'),
((SELECT producto_id FROM producto WHERE sku = 'PROD-TXT-070'), 'LOTE-A070', 300, '2027-02-28'),
((SELECT producto_id FROM producto WHERE sku = 'PROD-TXT-071'), 'LOTE-A071', 250, '2026-03-30'),
((SELECT producto_id FROM producto WHERE sku = 'PROD-TXT-072'), 'LOTE-A072', 150, '2027-04-30'),
((SELECT producto_id FROM producto WHERE sku = 'PROD-TXT-073'), 'LOTE-A073', 500, '2026-05-30'),
((SELECT producto_id FROM producto WHERE sku = 'PROD-TXT-074'), 'LOTE-A074', 80, '2027-06-30'),
((SELECT producto_id FROM producto WHERE sku = 'PROD-TXT-075'), 'LOTE-A075', 100, '2026-07-30'),
((SELECT producto_id FROM producto WHERE sku = 'PROD-TXT-076'), 'LOTE-A076', 120, '2027-08-30'),
((SELECT producto_id FROM producto WHERE sku = 'PROD-TXT-077'), 'LOTE-A077', 200, '2026-09-30'),
((SELECT producto_id FROM producto WHERE sku = 'PROD-TXT-078'), 'LOTE-A078', 180, '2027-10-30'),
((SELECT producto_id FROM producto WHERE sku = 'PROD-TXT-079'), 'LOTE-A079', 90, '2026-11-30'),
((SELECT producto_id FROM producto WHERE sku = 'PROD-TXT-080'), 'LOTE-A080', 110, '2027-12-30'),
((SELECT producto_id FROM producto WHERE sku = 'PROD-TXT-081'), 'LOTE-A081', 300, '2026-01-30'),
((SELECT producto_id FROM producto WHERE sku = 'PROD-TXT-082'), 'LOTE-A082', 150, '2027-02-28'),
((SELECT producto_id FROM producto WHERE sku = 'PROD-TXT-083'), 'LOTE-A083', 130, '2026-03-30'),
((SELECT producto_id FROM producto WHERE sku = 'PROD-TXT-084'), 'LOTE-A084', 80, '2027-04-30'),
((SELECT producto_id FROM producto WHERE sku = 'PROD-TXT-085'), 'LOTE-A085', 100, '2026-05-30'),
((SELECT producto_id FROM producto WHERE sku = 'PROD-TXT-086'), 'LOTE-A086', 160, '2027-06-30'),
((SELECT producto_id FROM producto WHERE sku = 'PROD-TXT-087'), 'LOTE-A087', 400, '2026-07-30'),
((SELECT producto_id FROM producto WHERE sku = 'PROD-TXT-088'), 'LOTE-A088', 250, '2027-08-30'),
((SELECT producto_id FROM producto WHERE sku = 'PROD-TXT-089'), 'LOTE-A089', 350, '2026-09-30'),
((SELECT producto_id FROM producto WHERE sku = 'PROD-TXT-090'), 'LOTE-A090', 300, '2027-10-30'),
((SELECT producto_id FROM producto WHERE sku = 'PROD-TXT-091'), 'LOTE-A091', 280, '2026-11-30'),
((SELECT producto_id FROM producto WHERE sku = 'PROD-TXT-092'), 'LOTE-A092', 140, '2027-12-30'),
((SELECT producto_id FROM producto WHERE sku = 'PROD-TXT-093'), 'LOTE-A093', 110, '2026-01-30'),
((SELECT producto_id FROM producto WHERE sku = 'PROD-TXT-094'), 'LOTE-A094', 200, '2027-02-28'),
((SELECT producto_id FROM producto WHERE sku = 'PROD-TXT-095'), 'LOTE-A095', 130, '2026-03-30'),
((SELECT producto_id FROM producto WHERE sku = 'PROD-TXT-096'), 'LOTE-A096', 100, '2027-04-30'),
((SELECT producto_id FROM producto WHERE sku = 'PROD-TXT-097'), 'LOTE-A097', 70, '2026-05-30'),
((SELECT producto_id FROM producto WHERE sku = 'PROD-TXT-098'), 'LOTE-A098', 30, '2027-06-30'),
((SELECT producto_id FROM producto WHERE sku = 'PROD-TXT-099'), 'LOTE-A099', 50, '2026-07-30'),
((SELECT producto_id FROM producto WHERE sku = 'PROD-TXT-100'), 'LOTE-A100', 90, '2027-08-30'),
((SELECT producto_id FROM producto WHERE sku = 'PROD-TXT-101'), 'LOTE-A101', 180, '2026-09-30'),
((SELECT producto_id FROM producto WHERE sku = 'PROD-TXT-102'), 'LOTE-A102', 40, '2027-10-30'),
((SELECT producto_id FROM producto WHERE sku = 'PROD-TXT-103'), 'LOTE-A103', 60, '2026-11-30'),
((SELECT producto_id FROM producto WHERE sku = 'PROD-TXT-104'), 'LOTE-A104', 20, '2027-12-30'),
((SELECT producto_id FROM producto WHERE sku = 'PROD-TXT-105'), 'LOTE-A105', 110, '2026-01-30'),
((SELECT producto_id FROM producto WHERE sku = 'PROD-TXT-106'), 'LOTE-A106', 80, '2027-02-28'),
((SELECT producto_id FROM producto WHERE sku = 'PROD-TXT-107'), 'LOTE-A107', 100, '2026-03-30'),
((SELECT producto_id FROM producto WHERE sku = 'PROD-TXT-108'), 'LOTE-A108', 90, '2027-04-30'),
((SELECT producto_id FROM producto WHERE sku = 'PROD-TXT-109'), 'LOTE-A109', 120, '2026-05-30'),
((SELECT producto_id FROM producto WHERE sku = 'PROD-TXT-110'), 'LOTE-A110', 50, '2027-06-30'),
((SELECT producto_id FROM producto WHERE sku = 'PROD-TXT-111'), 'LOTE-A111', 100, '2026-07-30'),
((SELECT producto_id FROM producto WHERE sku = 'PROD-TXT-112'), 'LOTE-A112', 150, '2027-08-30'),
((SELECT producto_id FROM producto WHERE sku = 'PROD-TXT-113'), 'LOTE-A113', 120, '2026-09-30'),
((SELECT producto_id FROM producto WHERE sku = 'PROD-TXT-114'), 'LOTE-A114', 180, '2027-10-30'),
((SELECT producto_id FROM producto WHERE sku = 'PROD-TXT-115'), 'LOTE-A115', 200, '2026-11-30'),
((SELECT producto_id FROM producto WHERE sku = 'PROD-TXT-116'), 'LOTE-A116', 300, '2027-12-30'),
((SELECT producto_id FROM producto WHERE sku = 'PROD-TXT-117'), 'LOTE-A117', 120, '2026-01-30'),
((SELECT producto_id FROM producto WHERE sku = 'PROD-TXT-118'), 'LOTE-A118', 70, '2027-02-28'),
((SELECT producto_id FROM producto WHERE sku = 'PROD-TXT-119'), 'LOTE-A119', 150, '2026-03-30'),
((SELECT producto_id FROM producto WHERE sku = 'PROD-TXT-120'), 'LOTE-A120', 40, '2027-04-30');
COMMIT;
