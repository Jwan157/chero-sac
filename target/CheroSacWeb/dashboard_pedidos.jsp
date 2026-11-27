<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="ui" %>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<%@ taglib uri="jakarta.tags.fmt" prefix="fmt" %>
<c:set var="ctx" value="${pageContext.request.contextPath}"/>
<!doctype html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Chero.Sac | Dashboard de pedidos</title>
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css">
    <link rel="stylesheet" href="${ctx}/css/estilos_chero.css?v=20261127-owner">
</head>
<body class="cs-interno">

<%@ include file="/WEB-INF/vistas/menu-interno.jspf" %>

<main class="container cs-contenido">
    <section class="cs-hero cs-owner-hero">
        <div class="cs-owner-hero-copy">
            <p class="cs-eyebrow mb-2">PANEL DEL PROPIETARIO</p>
            <h1>Gestión de pedidos y ventas</h1>
            <p class="mb-0">Bienvenido, <strong><c:out value="${sessionScope.usuarioAdmin.nombreCompleto}"/></strong>. Administra las compras, revisa los estados y atiende a tus clientes desde un solo lugar.</p>
        </div>
        <div class="cs-owner-hero-actions">
            <span class="cs-owner-date">Resumen actualizado</span>
            <a class="btn btn-light fw-bold" href="${ctx}/admin/pedidos">↻ Actualizar información</a>
        </div>
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

    <div class="cs-panel cs-owner-pending d-flex flex-wrap justify-content-between align-items-center gap-3 mb-3">
        <div class="d-flex align-items-center gap-3">
            <div class="cs-owner-pending-icon">↗</div>
            <div>
                <div class="fw-bold">Pedidos pendientes de atención</div>
                <div class="small text-muted">Las compras realizadas desde el catálogo se muestran aquí con sus productos y cantidades.</div>
            </div>
        </div>
        <div class="cs-owner-pending-number"><strong>${pedidosPendientes}</strong><span>por atender</span></div>
    </div>

    <section class="cs-admin-status-grid mb-4" aria-label="Resumen de pedidos por estado">
        <div class="cs-admin-status-item status-pendiente"><span class="cs-admin-status-dot"></span><span>Pendientes</span><strong>${conteoEstados['PENDIENTE']}</strong></div>
        <div class="cs-admin-status-item status-confirmado"><span class="cs-admin-status-dot"></span><span>Confirmados</span><strong>${conteoEstados['CONFIRMADO']}</strong></div>
        <div class="cs-admin-status-item status-enviado"><span class="cs-admin-status-dot"></span><span>Enviados</span><strong>${conteoEstados['ENVIADO']}</strong></div>
        <div class="cs-admin-status-item status-entregado"><span class="cs-admin-status-dot"></span><span>Entregados</span><strong>${conteoEstados['ENTREGADO']}</strong></div>
        <div class="cs-admin-status-item status-cancelado"><span class="cs-admin-status-dot"></span><span>Cancelados</span><strong>${conteoEstados['CANCELADO']}</strong></div>
    </section>

    <div class="cs-demo-note mb-3">
        <span class="cs-demo-note-icon">i</span>
        <div><strong>Registros de demostración:</strong> hay <b>${pedidosDemo}</b> pedidos identificados con la etiqueta <span class="cs-demo-tag">DEMO</span>. Sirven para revisar todos los estados durante la exposición; no son ventas reales ni modifican el stock.</div>
    </div>

    <div class="row g-3 mb-4">
        <div class="col-6 col-lg-3">
            <div class="cs-kpi">
                <div class="cs-kpi-etiqueta">Ventas de los últimos 7 días</div>
                <div class="cs-kpi-valor">S/ <fmt:formatNumber value="${resumen.ventas}" pattern="#,##0.00"/></div>
            </div>
        </div>
        <div class="col-6 col-lg-3">
            <div class="cs-kpi">
                <div class="cs-kpi-etiqueta">IGV de los últimos 7 días</div>
                <div class="cs-kpi-valor">S/ <fmt:formatNumber value="${resumen.igv}" pattern="#,##0.00"/></div>
            </div>
        </div>
        <div class="col-6 col-lg-3">
            <div class="cs-kpi">
                <div class="cs-kpi-etiqueta">Pedidos de los últimos 7 días</div>
                <div class="cs-kpi-valor">${resumen.pedidos}</div>
            </div>
        </div>
        <div class="col-6 col-lg-3">
            <div class="cs-kpi">
                <div class="cs-kpi-etiqueta">Devoluciones de los últimos 7 días</div>
                <div class="cs-kpi-valor">${devolucionesSemana}</div>
            </div>
        </div>
    </div>

    <form method="get" action="${ctx}/admin/pedidos" class="cs-panel mb-3">
        <div class="row g-2 align-items-end">
            <div class="col-md-5">
                <label class="form-label" for="q">Buscar</label>
                <input class="form-control" id="q" name="q" maxlength="100" placeholder="Código, documento o nombre"
                       value="<c:out value='${filtroQ}'/>">
            </div>
            <div class="col-md-4">
                <label class="form-label" for="estado">Estado</label>
                <select class="form-select" id="estado" name="estado">
                    <option value="">Todos</option>
                    <c:forEach var="e" items="${estados}">
                        <option value="${e}" ${filtroEstado == e ? 'selected' : ''}>${e}</option>
                    </c:forEach>
                </select>
            </div>
            <div class="col-md-3 d-grid">
                <button class="btn btn-cs" type="submit">Filtrar</button>
            </div>
        </div>
    </form>

    <section class="cs-panel p-0 overflow-hidden">
        <div class="cs-tabla-responsive">
            <table class="table cs-tabla mb-0">
                <thead>
                <tr>
                    <th>Código</th>
                    <th>Cliente</th>
                    <th>Productos solicitados</th>
                    <th>Pago</th>
                    <th>Fecha</th>
                    <th class="text-end">Total</th>
                    <th>Estado</th>
                    <th style="min-width:220px">Acciones</th>
                </tr>
                </thead>
                <tbody>
                <c:forEach var="p" items="${pedidos}">
                    <c:set var="bloqueado" value="${p.estado == 'CANCELADO' ? 'disabled' : ''}"/>
                    <tr class="${p.demostracion ? 'cs-row-demo' : ''}">
                        <td class="small">
                            <div class="cs-mono fw-bold"><c:out value="${p.codigoSeguimiento}"/></div>
                            <c:if test="${p.demostracion}"><span class="cs-demo-tag mt-1">DEMO</span></c:if>
                        </td>
                        <td>
                            <div class="fw-semibold"><c:out value="${p.clienteNombre}"/></div>
                            <div class="small text-muted cs-mono">
                                <c:out value="${p.clienteTipoDocumento}"/> <c:out value="${p.clienteNumeroDocumento}"/>
                            </div>
                        </td>
                        <td class="small">
                            <c:forEach var="d" items="${detallesPedidos[p.idPedido]}">
                                <div class="cs-order-product-line border-bottom py-1">
                                    <ui:foto ruta="${d.imagenUrl}" nombre="${d.nombreProducto}" clase="cs-order-thumb"/>
                                    <span><c:out value="${d.nombreProducto}"/></span>
                                    <strong class="cs-mono text-nowrap">×${d.cantidad}</strong>
                                </div>
                            </c:forEach>
                        </td>
                        <td class="small"><c:out value="${p.metodoPago}"/></td>
                        <td class="small"><c:out value="${p.fechaFormateada}"/></td>
                        <td class="text-end cs-mono">S/ <fmt:formatNumber value="${p.total}" pattern="#,##0.00"/></td>
                        <td><span class="cs-badge-estado cs-estado-${p.estado}">${p.estado}</span></td>
                        <td>
                            <form method="post" action="${ctx}/admin/pedidos" class="d-grid gap-1">
            <input type="hidden" name="csrf" value="${sessionScope.csrf}">
                                <input type="hidden" name="accion" value="actualizarEstado">
                                <input type="hidden" name="idPedido" value="${p.idPedido}">
                                <select name="estado" class="form-select form-select-sm" ${bloqueado}>
                                    <c:forEach var="e" items="${estados}">
                                        <option value="${e}" ${p.estado == e ? 'selected' : ''}>${e}</option>
                                    </c:forEach>
                                </select>
                                <input type="text" name="observaciones" maxlength="500" placeholder="Observaciones"
                                       class="form-control form-control-sm" ${bloqueado}
                                       value="<c:out value='${p.observaciones}'/>">
                                <button class="btn btn-sm btn-cs-negro" type="submit" ${bloqueado}>Guardar</button>
                            </form>
                        </td>
                    </tr>
                </c:forEach>
                </tbody>
            </table>
        </div>
        <c:if test="${empty pedidos}">
            <div class="cs-admin-empty p-4 text-center">
                <div class="cs-admin-empty-icon">▤</div>
                <strong>No hay pedidos para este filtro.</strong>
                <p class="text-muted mb-3">Cuando un cliente finalice una compra, el pedido aparecerá aquí automáticamente.</p>
                <a class="btn btn-cs-outline" href="${ctx}/catalogo">Abrir catálogo de la tienda</a>
            </div>
        </c:if>
    </section>

</main>

<footer class="cs-footer">
    <div class="container d-flex flex-wrap justify-content-between gap-2">
        <span>CHERO.SAC · Sistema de gestión y ventas · Grupo 1</span>
        <span class="cs-mono">Java 17+ · PostgreSQL</span>
    </div>
</footer>

<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>
