<%@ taglib tagdir="/WEB-INF/tags" prefix="ui" %>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<%@ taglib uri="jakarta.tags.fmt" prefix="fmt" %>
<%@ taglib uri="jakarta.tags.functions" prefix="fn" %>
<c:set var="ctx" value="${pageContext.request.contextPath}"/>
<!doctype html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Chero.Sac | Inventario</title>
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css">
    <link rel="stylesheet" href="${ctx}/css/estilos_chero.css?v=20261127-owner">
</head>
<body class="cs-interno">

<%@ include file="/WEB-INF/vistas/menu-interno.jspf" %>

<main class="container cs-contenido">
    <section class="cs-hero">
        <p class="cs-eyebrow mb-2">Almacén</p>
        <h1>Inventario de productos</h1>
        <p class="mb-0" style="color:#C9CED6">El stock de venta y el stock defectuoso se controlan por separado. Todo movimiento queda en el Kardex.</p>
    </section>

    <c:if test="${not empty param.ok}">
        <div class="alert alert-success"><c:out value="${param.ok}"/></div>
    </c:if>
    <c:if test="${not empty param.error}">
        <div class="alert alert-danger"><c:out value="${param.error}"/></div>
    </c:if>
    <c:if test="${not empty error}">
        <div class="alert alert-danger"><c:out value="${error}"/></div>
    </c:if>

    <div class="row g-3 mb-4">
        <div class="col-6 col-lg-4">
            <div class="cs-kpi">
                <div class="cs-kpi-etiqueta">Stock de venta (total)</div>
                <div class="cs-kpi-valor">${totalDisponible}</div>
            </div>
        </div>
        <div class="col-6 col-lg-4">
            <div class="cs-kpi">
                <div class="cs-kpi-etiqueta">Stock defectuoso (total)</div>
                <div class="cs-kpi-valor">${totalDefectuoso}</div>
            </div>
        </div>
        <div class="col-12 col-lg-4">
            <div class="cs-kpi">
                <div class="cs-kpi-etiqueta">Productos registrados</div>
                <div class="cs-kpi-valor">${fn:length(productos)}</div>
            </div>
        </div>
    </div>

    <details class="cs-panel mb-4">
        <summary class="fw-bold" style="cursor:pointer">+ Registrar nuevo producto</summary>
        <form method="post" action="${ctx}/admin/inventario" enctype="multipart/form-data" class="row g-2 mt-2">
            <input type="hidden" name="csrf" value="${sessionScope.csrf}">
            <input type="hidden" name="accion" value="crearProducto">
            <div class="col-md-6">
                <label class="form-label" for="nNombre">Nombre</label>
                <input class="form-control" id="nNombre" name="nombre" maxlength="150" required>
            </div>
            <div class="col-md-6">
                <label class="form-label" for="nCategoria">Categoría</label>
                <select class="form-select" id="nCategoria" name="idCategoria" required>
                    <c:forEach var="cat" items="${categorias}">
                        <option value="${cat.idCategoria}"><c:out value="${cat.nombreCategoria}"/></option>
                    </c:forEach>
                </select>
            </div>
            <div class="col-12">
                <label class="form-label" for="nDescripcion">Descripción</label>
                <input class="form-control" id="nDescripcion" name="descripcion" maxlength="500">
            </div>
            <div class="col-md-4">
                <label class="form-label" for="nPrecio">Precio (S/, con IGV)</label>
                <input class="form-control" type="number" step="0.01" min="0.01" id="nPrecio" name="precio" required>
            </div>
            <div class="col-md-4">
                <label class="form-label" for="nStock">Stock de venta inicial</label>
                <input class="form-control" type="number" min="0" id="nStock" name="stockDisponible" value="0" required>
            </div>
            <div class="col-md-4">
                <label class="form-label" for="nDef">Stock defectuoso inicial</label>
                <input class="form-control" type="number" min="0" id="nDef" name="stockDefectuoso" value="0" required>
            </div>
            <div class="col-12">
                <label class="form-label" for="nCosto">Costo unitario (S/, sin IGV)</label>
                <input class="form-control mb-2" type="number" min="0" step="0.01" name="costo" id="nCosto" required>
                <label class="form-label" for="fotoNueva">Foto del producto (hasta 5 MB)</label>
                <input class="form-control js-foto" type="file" name="imagenFile" id="fotoNueva" accept="image/*">
                <div class="my-2 js-previa"><ui:foto nombre="Vista previa"/></div>
                <label class="form-label" for="nImagen">URL de imagen (opcional)</label>
                <input class="form-control" id="nImagen" name="imagenUrl" maxlength="300" placeholder="https://...">
            </div>
            <div class="col-12">
                <button class="btn btn-cs" type="submit">Registrar producto</button>
            </div>
        </form>
    </details>

    <section class="cs-panel p-0 overflow-hidden mb-4">
        <div class="cs-tabla-responsive">
            <table class="table cs-tabla mb-0">
                <thead>
                <tr>
                    <th>Producto</th>
                    <th>Categoría</th>
                    <th class="text-center">Venta</th>
                    <th class="text-center">Defectuoso</th>
                    <th style="min-width:260px">Ajustar stock de venta</th>
                    <th style="min-width:200px">Precio (S/)</th>
                </tr>
                </thead>
                <tbody>
                <c:forEach var="p" items="${productos}">
                    <tr>
                        <td>
                            <div class="fw-semibold"><ui:foto ruta="${p.imagenUrl}" nombre=""/><c:out value="${p.nombreProducto}"/></div>
                            <details class="small mt-2"><summary>Actualizar foto</summary>
                            <form method="post" enctype="multipart/form-data" action="${ctx}/admin/inventario">
                                <input type="hidden" name="csrf" value="${sessionScope.csrf}">
                                <input type="hidden" name="idProducto" value="${p.idProducto}">
                                <input type="hidden" name="accion" value="cambiarFoto">
                                <input type="file" class="form-control form-control-sm js-foto" name="imagenFile" accept="image/*" aria-label="Nueva foto" required>
                                <div class="js-previa my-1"><ui:foto ruta="${p.imagenUrl}" nombre="Vista previa"/></div>
                                <button class="btn btn-sm btn-cs-outline">Guardar foto</button>
                            </form></details>
                            <c:if test="${!p.activo}"><span class="badge text-bg-secondary">Inactivo</span></c:if>
                        </td>
                        <td class="small"><c:out value="${p.nombreCategoria}"/></td>
                        <td class="text-center cs-mono ${p.stockDisponible eq 0 ? 'text-danger fw-bold' : ''}">${p.stockDisponible}</td>
                        <td class="text-center"><span class="cs-mono">${p.stockDefectuoso}</span>
                            <c:if test="${p.stockDefectuoso gt 0}"><details class="small"><summary>Gestionar</summary>
                            <form method="post" action="${ctx}/admin/inventario" class="d-grid gap-2 mt-2">
                            <input type="hidden" name="csrf" value="${sessionScope.csrf}">
                            <input type="hidden" name="idProducto" value="${p.idProducto}">
                            <input class="form-control form-control-sm" type="number" name="cantidad" aria-label="Cantidad defectuosa" value="1" min="1" max="${p.stockDefectuoso}" required>
                            <button class="btn btn-sm btn-cs-outline" name="accion" value="reparar">Reintegrar por reparación</button>
                            <button class="btn btn-sm btn-cs-negro" name="accion" value="merma">Dar de baja por merma</button></form></details></c:if></td>
                        <td>
                            <form method="post" action="${ctx}/admin/inventario" class="d-flex gap-1">
            <input type="hidden" name="csrf" value="${sessionScope.csrf}">
                                <input type="hidden" name="accion" value="ajustarStock">
                                <input type="hidden" name="idProducto" value="${p.idProducto}">
                                <input type="number" name="cantidad" class="form-control form-control-sm" style="width:90px"
                                       placeholder="± N" required>
                                <input type="text" name="motivo" class="form-control form-control-sm" maxlength="100"
                                       placeholder="Motivo">
                                <button class="btn btn-sm btn-cs-negro" type="submit">Aplicar</button>
                            </form>
                        </td>
                        <td>
                            <form method="post" action="${ctx}/admin/inventario" class="d-flex gap-1">
            <input type="hidden" name="csrf" value="${sessionScope.csrf}">
                                <input type="hidden" name="accion" value="actualizarPrecio">
                                <input type="hidden" name="idProducto" value="${p.idProducto}">
                                <input type="number" name="precio" step="0.01" min="0.01" class="form-control form-control-sm"
                                       style="width:110px" value="${p.precio}" required>
                                <button class="btn btn-sm btn-cs-outline" type="submit">Guardar</button>
                            </form>
                            <details class="small mt-2"><summary>Costo sin IGV</summary>
                            <form method="post" action="${ctx}/admin/inventario" class="d-flex gap-1 mt-2">
                            <input type="hidden" name="csrf" value="${sessionScope.csrf}"><input type="hidden" name="accion" value="actualizarCosto">
                            <input type="hidden" name="idProducto" value="${p.idProducto}">
                            <input class="form-control form-control-sm" aria-label="Costo unitario" type="number" name="costo" min="0" step="0.01" value="${p.costoUnitario}" required>
                            <button class="btn btn-sm btn-cs-outline">Guardar costo</button></form></details>
                        </td>
                    </tr>
                </c:forEach>
                </tbody>
            </table>
        </div>
    </section>

    <section class="cs-panel">
        <div class="d-flex flex-wrap justify-content-between align-items-center gap-2 mb-3">
            <h2 class="mb-0">Kardex de movimientos</h2>
            <form method="get" action="${ctx}/admin/inventario">
                <select class="form-select form-select-sm" name="producto" onchange="this.form.submit()" aria-label="Filtrar por producto">
                    <option value="">Todos los productos</option>
                    <c:forEach var="p" items="${productos}">
                        <option value="${p.idProducto}" ${filtroProducto == p.idProducto ? 'selected' : ''}>
                            <c:out value="${p.nombreProducto}"/>
                        </option>
                    </c:forEach>
                </select>
            </form>
        </div>
        <div class="cs-tabla-responsive">
            <table class="table cs-tabla mb-0">
                <thead>
                <tr>
                    <th>Fecha</th>
                    <th>Producto</th>
                    <th>Movimiento</th>
                    <th class="text-end">Cantidad</th>
                    <th>Referencia</th>
                </tr>
                </thead>
                <tbody>
                <c:forEach var="m" items="${kardex}">
                    <tr>
                        <td class="small cs-mono"><c:out value="${m.fechaMovimientoFormateada}"/></td>
                        <td class="small"><c:out value="${m.nombreProducto}"/></td>
                        <td><span class="badge text-bg-dark"><c:out value="${m.tipoMovimiento}"/></span></td>
                        <td class="text-end cs-mono">${m.cantidad}</td>
                        <td class="small cs-mono"><c:out value="${m.referenciaCodigo}"/></td>
                    </tr>
                </c:forEach>
                </tbody>
            </table>
        </div>
        <c:if test="${empty kardex}">
            <p class="text-center text-muted mb-0 mt-3">Sin movimientos registrados.</p>
        </c:if>
    </section>
</main>

<footer class="cs-footer">
    <div class="container d-flex flex-wrap justify-content-between gap-2">
        <span>Chero.Sac · Base de Datos II · Grupo 1</span>
        <span class="cs-mono">PostgreSQL + Java 17</span>
    </div>
</footer>

<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>
<script>
document.querySelectorAll('.js-foto').forEach(function (campo) {
    var foto = campo.closest('form').querySelector('.js-previa img');
    var original = foto.src;
    var previa;
    campo.addEventListener('change', function () {
        if (previa) URL.revokeObjectURL(previa);
        previa = campo.files.length ? URL.createObjectURL(campo.files[0]) : null;
        foto.src = previa || original;
    });
});
</script></body>
</html>
