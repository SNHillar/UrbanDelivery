-- V1 de la entrega final: solo retiro en el local (PICKUP).
-- Los datos de demo (db/demo/V1_1) dejaron un comercio con envío habilitado y tarifa;
-- como esa migración ya puede estar aplicada, no se edita: se corrige acá.
-- Las columnas quedan en el modelo como preparación para un futuro DELIVERY.
UPDATE merchants
SET offers_delivery = FALSE,
    delivery_fee    = 0
WHERE offers_delivery OR delivery_fee <> 0;
