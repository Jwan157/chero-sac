\encoding UTF8
-- Aplicar sobre la base existente con su usuario de gestión:
-- psql -U postgres -d chero_sac -v propietario=admin -f migrar_desde_original.sql
-- Indique el nombre de un usuario interno activo que deba asumir el rol DUENO.
-- Una segunda ejecución se detiene antes de aplicar cambios.
\set ON_ERROR_STOP on
BEGIN;
LOCK TABLE usuarios, clientes, productos, pedidos, detalle_pedidos, movimientos_inventario
    IN SHARE ROW EXCLUSIVE MODE;
SELECT set_config('chero.migracion_propietario', :'propietario', true);
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM usuarios WHERE nombre_usuario=current_setting('chero.migracion_propietario')
                   AND activo AND rol='ADMINISTRADOR') THEN
        RAISE EXCEPTION 'Seleccione un usuario activo de la instalación original como propietario';
    END IF;
END $$;
ALTER TABLE usuarios DROP CONSTRAINT usuarios_rol_check;
UPDATE usuarios SET rol = CASE WHEN nombre_usuario=current_setting('chero.migracion_propietario') THEN 'DUENO' ELSE 'ADMIN' END;
ALTER TABLE usuarios ALTER COLUMN rol SET DEFAULT 'ADMIN', ADD CHECK (rol IN ('DUENO','ADMIN'));
ALTER TABLE clientes ADD COLUMN password_hash varchar(100), ADD COLUMN tiene_cuenta boolean NOT NULL DEFAULT false,
    ADD COLUMN hash_activacion varchar(64),
    ADD CONSTRAINT chk_cuenta_clave CHECK (tiene_cuenta = (password_hash IS NOT NULL));
ALTER TABLE clientes DROP CONSTRAINT chk_formato_documento;
ALTER TABLE clientes ADD CONSTRAINT chk_formato_documento CHECK (
    (tipo_documento='DNI' AND numero_documento ~ '^[0-9]{8}$') OR
    (tipo_documento='RUC' AND numero_documento ~ '^(10|20)[0-9]{9}$')
) NOT VALID;
ALTER TABLE productos ADD COLUMN costo_unitario numeric(10,2) CHECK (costo_unitario >= 0);
ALTER TABLE detalle_pedidos ADD COLUMN costo_unitario numeric(10,2) CHECK (costo_unitario >= 0);
ALTER TABLE movimientos_inventario DROP CONSTRAINT movimientos_inventario_tipo_movimiento_check,
    ALTER COLUMN tipo_movimiento TYPE varchar(32);
ALTER TABLE movimientos_inventario ADD CHECK (tipo_movimiento IN ('SALIDA_VENTA','INGRESO_DEVOLUCION_OK',
    'INGRESO_DEFECTUOSO','REPARACION_OK','BAJA_MERMA','AJUSTE'));
INSERT INTO historial_acciones(id_usuario,accion,detalle)
SELECT id_usuario,'MIGRACION','Migración del esquema y asignación de propietario'
FROM usuarios WHERE rol='DUENO';
COMMIT;
