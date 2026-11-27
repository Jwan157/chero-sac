<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<c:set var="ctx" value="${pageContext.request.contextPath}"/>
<!doctype html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Chero.Sac | Postventa</title>
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css">
    <link rel="stylesheet" href="${ctx}/css/estilos_chero.css?v=20261127-owner">
</head>
<body class="cs-interno">

<%@ include file="/WEB-INF/vistas/menu-interno.jspf" %>

<main class="container cs-contenido">
    <section class="cs-hero">
        <p class="cs-eyebrow mb-2">Control interno</p>
        <h1>Solicitudes de postventa</h1>
        <p class="mb-0" style="color:#C9CED6">Revise el motivo y apruebe o rechace. Al aprobar se genera una nota de crédito y el producto vuelve al stock según su condición.</p>
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

    <div class="mb-3">
        <a class="cs-pill ${empty filtroEstado ? 'activa' : ''}" href="${ctx}/admin/devoluciones">Todas</a>
        <a class="cs-pill ${filtroEstado == 'PENDIENTE' ? 'activa' : ''}" href="${ctx}/admin/devoluciones?estado=PENDIENTE">Pendientes</a>
        <a class="cs-pill ${filtroEstado == 'APROBADO' ? 'activa' : ''}" href="${ctx}/admin/devoluciones?estado=APROBADO">Aprobadas</a>
        <a class="cs-pill ${filtroEstado == 'RECHAZADO' ? 'activa' : ''}" href="${ctx}/admin/devoluciones?estado=RECHAZADO">Rechazadas</a>
    </div>

    <section class="cs-panel p-0 overflow-hidden">
        <div class="cs-tabla-responsive">
            <table class="table cs-tabla mb-0">
                <thead>
                <tr>
                    <th>#</th>
                    <th>Pedido</th>
                    <th>Producto</th>
                    <th class="text-center">Cant.</th>
                    <th>Tipo y motivo</th>
                    <th>Estado</th>
                    <th style="min-width:270px">Resolución</th>
                </tr>
                </thead>
                <tbody>
                <c:forEach var="s" items="${solicitudes}">
                    <tr>
                        <td class="cs-mono">${s.idSolicitud}</td>
                        <td>
                            <div class="cs-mono small"><c:out value="${s.codigoPedido}"/></div>
                            <div class="small text-muted"><c:out value="${s.fechaSolicitudFormateada}"/></div>
                        </td>
                        <td><c:out value="${s.nombreProducto}"/></td>
                        <td class="text-center">${s.cantidadDevuelta}</td>
                        <td style="max-width:260px">
                            <span class="badge text-bg-dark mb-1">${s.tipoSolicitud}</span>
                            <div class="small"><c:out value="${s.motivo}"/></div>
                        </td>
                        <td>
                            <span class="cs-badge-estado cs-estado-${s.estado}">${s.estado}</span>
                            <c:if test="${not empty s.condicionProducto}">
                                <div class="small text-muted mt-1">${s.condicionProducto}</div>
                            </c:if>
                            <c:if test="${not empty s.respuestaAdmin}">
                                <div class="small text-muted mt-1">Resp.: <c:out value="${s.respuestaAdmin}"/></div>
                            </c:if>
                        </td>
                        <td>
                            <c:if test="${s.estado == 'PENDIENTE'}">
                                <form method="post" action="${ctx}/admin/devoluciones" class="d-grid gap-1">
            <input type="hidden" name="csrf" value="${sessionScope.csrf}">
                                    <input type="hidden" name="accion" value="resolver">
                                    <input type="hidden" name="idSolicitud" value="${s.idSolicitud}">
                                    <select name="decision" class="form-select form-select-sm" required>
                                        <option value="APROBAR">Aprobar</option>
                                        <option value="RECHAZAR">Rechazar</option>
                                    </select>
                                    <select name="condicion" class="form-select form-select-sm">
                                        <option value="">Condición (solo si aprueba)</option>
                                        <option value="BUEN_ESTADO">Buen estado: stock de venta</option>
                                        <option value="DEFECTUOSO">Defectuoso: stock defectuoso</option>
                                    </select>
                                    <input type="text" name="respuesta" maxlength="500" class="form-control form-control-sm"
                                           placeholder="Respuesta para el cliente">
                                    <button class="btn btn-sm btn-cs-negro" type="submit">Resolver</button>
                                </form>
                            </c:if>
                        </td>
                    </tr>
                </c:forEach>
                </tbody>
            </table>
        </div>
        <c:if test="${empty solicitudes}">
            <p class="p-4 text-center text-muted mb-0">No hay solicitudes para este filtro.</p>
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
document.querySelectorAll('[name="decision"]').forEach(function (campo) {
    var condicion = campo.form.elements.condicion;
    function actualizar() {
        condicion.required = campo.value === 'APROBAR';
        condicion.disabled = !condicion.required;
    }
    campo.addEventListener('change',actualizar);
    actualizar();
});
</script></body>
</html>
