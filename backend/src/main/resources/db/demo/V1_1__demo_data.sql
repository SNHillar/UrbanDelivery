-- Datos de demostración (no se cargan en producción: ver spring.flyway.locations).
-- Contraseña de todos los usuarios: demo1234

INSERT INTO users (email, password_hash, full_name, phone, role) VALUES
    ('admin@urbandelivery.local',    '$2a$10$gXYmtFtomU48LbHtFbE9g./SqwwakYkD.K.VUhwcK2aG2cqzyRxD.', 'Administración',     NULL,         'ADMIN'),
    ('cliente@urbandelivery.local',  '$2a$10$gXYmtFtomU48LbHtFbE9g./SqwwakYkD.K.VUhwcK2aG2cqzyRxD.', 'Lucía Cliente',      '3400000001', 'CLIENT'),
    ('cliente2@urbandelivery.local', '$2a$10$gXYmtFtomU48LbHtFbE9g./SqwwakYkD.K.VUhwcK2aG2cqzyRxD.', 'Martín Cliente',     '3400000002', 'CLIENT'),
    ('rotiseria@urbandelivery.local','$2a$10$gXYmtFtomU48LbHtFbE9g./SqwwakYkD.K.VUhwcK2aG2cqzyRxD.', 'Dueño Rotisería',    '3400000010', 'MERCHANT'),
    ('farmacia@urbandelivery.local', '$2a$10$gXYmtFtomU48LbHtFbE9g./SqwwakYkD.K.VUhwcK2aG2cqzyRxD.', 'Dueña Farmacia',     '3400000020', 'MERCHANT'),
    ('pendiente@urbandelivery.local','$2a$10$gXYmtFtomU48LbHtFbE9g./SqwwakYkD.K.VUhwcK2aG2cqzyRxD.', 'Dueño Verdulería',   '3400000030', 'MERCHANT');

INSERT INTO merchants (owner_user_id, name, category, address, phone, opening_hours,
                       offers_pickup, offers_delivery, delivery_fee, approval_status, approved_by, approved_at)
SELECT u.id, m.name, m.category, m.address, m.phone, m.opening_hours,
       TRUE, m.offers_delivery, m.delivery_fee, m.approval_status,
       CASE WHEN m.approval_status = 'APPROVED' THEN a.id END,
       CASE WHEN m.approval_status = 'APPROVED' THEN now() END
FROM (VALUES
    ('rotiseria@urbandelivery.local', 'Rotisería La Esquina', 'Rotisería', 'San Martín 450', '3400000010',
     'Mar a Dom 11-14 y 19-23', TRUE, 1500.00, 'APPROVED'),
    ('farmacia@urbandelivery.local', 'Farmacia Central', 'Farmacia', 'Belgrano 120', '3400000020',
     'Lun a Sáb 8-12:30 y 16-20:30', FALSE, 0.00, 'APPROVED'),
    ('pendiente@urbandelivery.local', 'Verdulería Don José', 'Verdulería', 'Moreno 89', '3400000030',
     'Lun a Sáb 8-13', FALSE, 0.00, 'PENDING')
) AS m(email, name, category, address, phone, opening_hours, offers_delivery, delivery_fee, approval_status)
JOIN users u ON u.email = m.email
CROSS JOIN (SELECT id FROM users WHERE email = 'admin@urbandelivery.local') a;

INSERT INTO products (merchant_id, name, description, price, available)
SELECT mer.id, p.name, p.description, p.price, p.available
FROM (VALUES
    ('Rotisería La Esquina', 'Pollo al spiedo', 'Entero, con papas', 12500.00, TRUE),
    ('Rotisería La Esquina', 'Empanadas de carne (docena)', 'Cortadas a cuchillo', 9800.00, TRUE),
    ('Rotisería La Esquina', 'Empanadas de jamón y queso (docena)', NULL, 9200.00, TRUE),
    ('Rotisería La Esquina', 'Tarta de verduras', 'Porción familiar', 7400.00, TRUE),
    ('Rotisería La Esquina', 'Matambre a la pizza', 'Solo fines de semana', 14000.00, FALSE),
    ('Farmacia Central', 'Alcohol en gel 250 ml', NULL, 2300.00, TRUE),
    ('Farmacia Central', 'Protector solar FPS 50', NULL, 15800.00, TRUE),
    ('Verdulería Don José', 'Cajón de tomates', NULL, 8000.00, TRUE)
) AS p(merchant_name, name, description, price, available)
JOIN merchants mer ON mer.name = p.merchant_name;
