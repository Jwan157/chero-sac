\encoding UTF8
-- Emitir una nueva clave para un cliente invitado después de verificar su identidad.
-- psql -U postgres -d chero_sac -v documento=12345678 -f vincular_cliente.sql
\set ON_ERROR_STOP on
WITH clave AS MATERIALIZED (SELECT gen_random_uuid()::text AS valor)
UPDATE clientes c
SET hash_activacion=encode(sha256(convert_to(clave.valor,'UTF8')),'hex')
FROM clave
WHERE c.numero_documento=:'documento' AND NOT c.tiene_cuenta
RETURNING c.numero_documento, clave.valor AS clave_privada_de_vinculacion;

