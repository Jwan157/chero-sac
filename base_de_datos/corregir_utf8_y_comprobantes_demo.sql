\encoding UTF8
\set ON_ERROR_STOP on

-- CHERO.SAC | Correccion de textos y comprobantes internos de demostracion
-- Ejecutar una sola vez o volver a ejecutar con seguridad sobre chero_sac.
-- No elimina pedidos, clientes, productos ni movimientos de inventario.

BEGIN;

-- 1) Repara cadenas que se guardaron con doble codificacion (por ejemplo, un apellido con acento).
-- Solo actua sobre valores que contienen los indicadores habituales de mojibake.
DO $$
DECLARE
    columna RECORD;
BEGIN
    FOR columna IN
        SELECT table_name, column_name
        FROM information_schema.columns
        WHERE table_schema = 'public'
          AND data_type IN ('text', 'character varying', 'character')
          AND table_name IN (
              'usuarios', 'clientes', 'categorias', 'productos', 'pedidos',
              'comprobantes_electronicos', 'solicitudes_devolucion',
              'historial_acciones', 'metodos_pago'
          )
    LOOP
        EXECUTE format(
            'UPDATE public.%I SET %I = convert_from(convert_to(%I, %L), %L) '
            'WHERE %I LIKE %L OR %I LIKE %L',
            columna.table_name, columna.column_name, columna.column_name,
            'WIN1252', 'UTF8', columna.column_name, '%Ã%', columna.column_name, '%Â%'
        );
    END LOOP;
END $$;

-- Normaliza el nombre de muestra para que se vea correctamente en catálogo y seguimiento.
UPDATE clientes
SET nombre_razon_social = 'José', apellido = 'Luján'
WHERE tipo_documento = 'DNI' AND numero_documento = '78029039';

-- 2) Crea documentos internos para los pedidos de demostración que aún no tienen uno.
-- El estado ACEPTADO de la estructura heredada es solo un estado interno de muestra;
-- la interfaz y el PDF indican expresamente que no existe envío ni autorización SUNAT.
WITH candidatos AS (
    SELECT
        p.id_pedido,
        p.codigo_seguimiento,
        p.fecha_pedido,
        p.estado,
        p.subtotal,
        p.igv,
        p.total,
        cl.tipo_documento,
        cl.numero_documento,
        CASE WHEN cl.tipo_documento = 'RUC' THEN 'FACTURA' ELSE 'BOLETA' END AS tipo_comprobante,
        CASE WHEN cl.tipo_documento = 'RUC' THEN 'F001' ELSE 'B001' END AS serie,
        CASE
            WHEN cl.tipo_documento = 'RUC' THEN cl.nombre_razon_social
            ELSE trim(coalesce(cl.nombre_razon_social, '') || ' ' || coalesce(cl.apellido, ''))
        END AS nombre_cliente,
        row_number() OVER (
            PARTITION BY CASE WHEN cl.tipo_documento = 'RUC' THEN 'F001' ELSE 'B001' END
            ORDER BY p.fecha_pedido, p.id_pedido
        ) AS orden
    FROM pedidos p
    JOIN clientes cl ON cl.id_cliente = p.id_cliente
    WHERE p.es_demo = TRUE
      AND NOT EXISTS (
          SELECT 1
          FROM comprobantes_electronicos ce
          WHERE ce.id_pedido = p.id_pedido
            AND ce.tipo_comprobante <> 'NOTA_CREDITO'
      )
), correlativos AS (
    SELECT serie, coalesce(max(numero_correlativo), 0) AS ultimo
    FROM comprobantes_electronicos
    GROUP BY serie
)
INSERT INTO comprobantes_electronicos (
    id_pedido, tipo_comprobante, serie, numero_correlativo,
    ruc_dni_cliente, razon_social_cliente, subtotal, igv_18, total,
    hash_sunat, estado_sunat, fecha_emision
)
SELECT
    c.id_pedido,
    c.tipo_comprobante,
    c.serie,
    coalesce(k.ultimo, 0) + c.orden,
    c.numero_documento,
    c.nombre_cliente,
    c.subtotal,
    c.igv,
    c.total,
    md5('CHERO-DEMO|' || c.codigo_seguimiento || '|' || c.total::text)
      || md5('CHERO-INTERNO|' || c.codigo_seguimiento || '|' || c.numero_documento),
    CASE WHEN c.estado = 'CANCELADO' THEN 'ANULADO' ELSE 'ACEPTADO' END,
    c.fecha_pedido
FROM candidatos c
LEFT JOIN correlativos k ON k.serie = c.serie
ON CONFLICT (serie, numero_correlativo) DO NOTHING;

COMMIT;

-- Resultado de control: pedidos demo y documentos internos generados.
SELECT
    count(*) FILTER (WHERE p.es_demo) AS pedidos_demo,
    count(ce.id_comprobante) FILTER (WHERE p.es_demo) AS documentos_demo
FROM pedidos p
LEFT JOIN comprobantes_electronicos ce ON ce.id_pedido = p.id_pedido;
