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
    <title>Chero.Sac | Seguimiento de pedido</title>
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css">
    <link rel="stylesheet" href="${ctx}/css/estilos_chero.css">
</head>
<body>

<nav class="navbar navbar-expand-lg cs-navbar cs-no-imprimir">
    <div class="container">
        <a class="navbar-brand" href="${ctx}/catalogo">CHERO<span class="cs-brand-punto">.SAC</span></a>
        <button class="navbar-toggler" type="button" data-bs-toggle="collapse" data-bs-target="#menuSeguimiento"
                aria-controls="menuSeguimiento" aria-expanded="false" aria-label="Abrir menú">
            <span class="navbar-toggler-icon"></span>
        </button>
        <div class="collapse navbar-collapse" id="menuSeguimiento">
            <ul class="navbar-nav ms-auto">
                <li class="nav-item"><a class="nav-link" href="${ctx}/catalogo">Catálogo</a></li>
                <li class="nav-item"><a class="nav-link activo" href="${ctx}/seguimiento">Seguimiento</a></li>
                <li class="nav-item"><a class="nav-link" href="${ctx}/login">Administración</a></li>
            <%@ include file="/WEB-INF/vistas/cuenta-menu.jspf" %>
            </ul>
        </div>
    </div>
</nav>

<main class="container cs-contenido">
    <section class="cs-hero cs-no-imprimir">
        <p class="cs-eyebrow mb-2">Seguimiento</p>
        <h1>Estado de su pedido</h1>
        <p class="mb-0" style="color:#C9CED6">${empty sessionScope.cliente ? "Ingrese su código de seguimiento y su número de documento." : "Seleccione una compra para revisar sus productos, comprobantes y devoluciones."}</p>
    </section>

    <c:if test="${not empty param.ok}">
        <div class="alert alert-success cs-no-imprimir"><c:out value="${param.ok}"/></div>
    </c:if>
    <c:if test="${not empty error}">
        <div class="alert alert-danger cs-no-imprimir"><c:out value="${error}"/></div>
    </c:if>
    <c:if test="${not empty param.error}">
        <div class="alert alert-danger cs-no-imprimir"><c:out value="${param.error}"/></div>
    </c:if>

    <c:if test="${not empty sessionScope.claveVinculo and empty sessionScope.cliente}"><div class="alert alert-info">Documento <c:out value="${sessionScope.documentoVinculo}"/>. Guarde la clave de su primera compra para vincular el historial al <a href="${ctx}/cliente">crear su cuenta</a>.
        <code><c:out value="${sessionScope.claveVinculo}"/></code></div></c:if>
    <c:choose><c:when test="${not empty sessionScope.cliente}"><section class="cs-panel mb-4"><h2>Mis pedidos</h2>
    <c:if test="${empty misPedidos}"><p>Todavía no tiene pedidos. <a href="${ctx}/catalogo">Ver catálogo</a></p></c:if>
    <c:forEach var="compra" items="${misPedidos}"><a class="d-flex flex-wrap gap-3 align-items-center border-bottom py-2" href="${ctx}/seguimiento?codigo=${compra.codigoSeguimiento}"><span class="cs-mono">${compra.codigoSeguimiento}</span><span>${compra.fechaFormateada} · ${compra.estado}</span><strong>S/ ${compra.total}</strong></a>
    <div class="d-flex flex-wrap gap-2 mb-2"><c:forEach var="doc" items="${documentosPorPedido[compra.idPedido]}">
        <a class="small cs-mono" href="${ctx}/comprobante-pdf?codigo=${compra.codigoSeguimiento}&amp;id=${doc.idComprobante}">PDF · ${doc.numeroCompleto}</a>
    </c:forEach></div>
    <c:forEach var="dev" items="${postventaPorPedido[compra.idPedido]}"><p class="small">${dev.tipoSolicitud} · <c:out value="${dev.nombreProducto}"/> × ${dev.cantidadDevuelta} · ${dev.estado}</p></c:forEach>
    </c:forEach>
    </section></c:when><c:otherwise>
    <form method="get" action="${ctx}/seguimiento" class="cs-panel mb-4 cs-no-imprimir">
        <div class="row g-2 align-items-end">
            <div class="col-md-5">
                <label class="form-label" for="codigo">Código de seguimiento</label>
                <input class="form-control cs-mono" id="codigo" name="codigo" maxlength="20" required
                       placeholder="CH-2026-000001" value="<c:out value='${param.codigo}'/>">
            </div>
            <div class="col-md-4">
                <label class="form-label" for="documento">Número de documento</label>
                <input class="form-control cs-mono" id="documento" name="documento" maxlength="11"
                       inputmode="numeric" required value="<c:out value='${param.documento}'/>">
            </div>
            <div class="col-md-3 d-grid">
                <button class="btn btn-cs" type="submit">Consultar</button>
            </div>
        </div>
    </form></c:otherwise></c:choose>

    <c:if test="${not empty pedido}">
        <div class="row g-4">
            <div class="col-lg-7">
                <section class="cs-panel mb-4">
                    <div class="d-flex flex-wrap justify-content-between align-items-center gap-2 mb-3">
                        <span class="cs-codigo"><c:out value="${pedido.codigoSeguimiento}"/></span>
                        <span class="cs-badge-estado cs-estado-${pedido.estado}">${pedido.estado}</span>
                    </div>
                    <p class="small text-muted mb-3">
                        Pedido realizado el <c:out value="${pedido.fechaFormateada}"/>
                        · Pago: <c:out value="${pedido.metodoPago}"/>
                    </p>

                    <c:choose>
                        <c:when test="${indicePaso lt 0}">
                            <div class="alert alert-secondary mb-0">Este pedido fue cancelado. Si tiene dudas, comuníquese con la tienda.</div>
                        </c:when>
                        <c:otherwise>
                            <ol class="cs-timeline">
                                <c:forEach var="paso" items="${pasos}" varStatus="st">
                                    <li class="${st.index lt indicePaso ? 'completado' : ''} ${st.index eq indicePaso ? 'activo' : ''}">
                                        <c:choose>
                                            <c:when test="${paso == 'PENDIENTE'}">Pedido recibido</c:when>
                                            <c:when test="${paso == 'CONFIRMADO'}">Pedido confirmado</c:when>
                                            <c:when test="${paso == 'ENVIADO'}">Pedido enviado</c:when>
                                            <c:otherwise>Pedido entregado</c:otherwise>
                                        </c:choose>
                                    </li>
                                </c:forEach>
                            </ol>
                        </c:otherwise>
                    </c:choose>
                </section>

                <section class="cs-panel mb-4">
                    <h2>Detalle del pedido</h2>
                    <div class="cs-tabla-responsive">
                        <table class="table cs-tabla align-middle mb-3">
                            <thead>
                            <tr>
                                <th>Producto</th>
                                <th class="text-center">Cant.</th>
                                <th class="text-end">Importe</th>
                            </tr>
                            </thead>
                            <tbody>
                            <c:forEach var="d" items="${detalles}">
                                <tr>
                                    <td><ui:foto ruta="${d.imagenUrl}" nombre=""/><c:out value="${d.nombreProducto}"/></td>
                                    <td class="text-center">${d.cantidad}</td>
                                    <td class="text-end cs-mono">S/ <fmt:formatNumber value="${d.subtotalLinea}" pattern="#,##0.00"/></td>
                                </tr>
                            </c:forEach>
                            </tbody>
                        </table>
                    </div>
                    <div class="d-flex justify-content-between small"><span>Subtotal (sin IGV)</span>
                        <span class="cs-mono">S/ <fmt:formatNumber value="${pedido.subtotal}" pattern="#,##0.00"/></span></div>
                    <div class="d-flex justify-content-between small"><span>IGV (18%)</span>
                        <span class="cs-mono">S/ <fmt:formatNumber value="${pedido.igv}" pattern="#,##0.00"/></span></div>
                    <div class="d-flex justify-content-between align-items-center border-top pt-2 mt-2">
                        <span class="fw-bold">Total</span>
                        <span class="cs-precio-grande" style="color:var(--cs-rojo)">S/ <fmt:formatNumber value="${pedido.total}" pattern="#,##0.00"/></span>
                    </div>
                </section>

                <c:if test="${pedido.estado == 'ENTREGADO'}">
                    <form method="post" action="${ctx}/devolucion" class="cs-panel mb-4 cs-no-imprimir">
            <input type="hidden" name="csrf" value="${sessionScope.csrf}">
                        <input type="hidden" name="codigo" value="${fn:escapeXml(pedido.codigoSeguimiento)}">
                        <input type="hidden" name="documento" value="${fn:escapeXml(pedido.clienteNumeroDocumento)}">
                        <h2>Solicitar cambio o devolución</h2>
                        <p class="small text-muted">El administrador revisará su solicitud. Al aprobarla se emitirá una nota de crédito.</p>
                        <div class="row g-3">
                            <div class="col-md-6">
                                <fieldset class="m-0"><legend class="form-label">Producto a devolver</legend>
                                    <c:forEach var="d" items="${detalles}" varStatus="fila">
                                        <label class="d-flex align-items-center gap-2 py-1"><input type="radio" name="idProducto" required value="${d.idProducto}" ${fila.first ? 'checked' : ''}>
                                            <ui:foto ruta="${d.imagenUrl}" nombre=""/><span><c:out value="${d.nombreProducto}"/> (×${d.cantidad})</span>
                                        </label>
                                    </c:forEach>
                                </fieldset>
                            </div>
                            <div class="col-md-3">
                                <label class="form-label" for="cantidad">Cantidad</label>
                                <input class="form-control" type="number" id="cantidad" name="cantidad" min="1" value="1" required>
                            </div>
                            <div class="col-md-3">
                                <label class="form-label" for="tipoSolicitud">Tipo</label>
                                <select class="form-select" id="tipoSolicitud" name="tipoSolicitud" required>
                                    <option value="DEVOLUCION">Devolución</option>
                                    <option value="CAMBIO">Cambio</option>
                                    <option value="GARANTIA">Garantía</option>
                                </select>
                            </div>
                            <div class="col-12">
                                <label class="form-label" for="motivo">Motivo</label>
                                <textarea class="form-control" id="motivo" name="motivo" rows="3" minlength="10" maxlength="500" required></textarea>
                            </div>
                        </div>
                        <button class="btn btn-cs mt-3" type="submit">Enviar solicitud</button>
                    </form>
                </c:if>
            </div>

            <div class="col-lg-5">
                <section class="cs-panel mb-4">
                    <div class="d-flex justify-content-between align-items-center mb-2">
                        <h2 class="mb-0">Comprobantes</h2>
                        <button type="button" class="btn btn-sm btn-cs-negro cs-no-imprimir" onclick="window.print()">Imprimir</button>
                    </div>
                    <c:forEach var="comp" items="${comprobantes}">
                        <div class="d-flex flex-wrap justify-content-between align-items-center gap-2 border-bottom py-2">
                            <div>
                                <span class="cs-badge-estado cs-tipo-${comp.tipoComprobante}">${comp.tipoComprobante}</span>
                                <span class="cs-mono small ms-1"><c:out value="${comp.numeroCompleto}"/></span>
                                <span class="cs-badge-estado cs-estado-${comp.estadoSunat} ms-1">${comp.estadoSunat} · simulado</span>
                            </div>
                            <a class="btn btn-sm btn-cs-outline cs-no-imprimir" target="_blank" rel="noopener"
                               href="${ctx}/comprobante-pdf?codigo=${fn:escapeXml(pedido.codigoSeguimiento)}&amp;documento=${fn:escapeXml(pedido.clienteNumeroDocumento)}&amp;id=${comp.idComprobante}">Ver PDF</a>
                        </div>
                    </c:forEach>
                    <c:if test="${empty comprobantes}">
                        <p class="small text-muted mb-0">Aún no hay comprobantes emitidos para este pedido.</p>
                    </c:if>
                </section>

                <section class="cs-panel">
                    <h2>Solicitudes de postventa</h2>
                    <ul class="list-unstyled mb-0">
                        <c:forEach var="s" items="${solicitudes}">
                            <li class="border-bottom py-2">
                                <div class="d-flex justify-content-between align-items-center gap-2">
                                    <span class="small fw-semibold">
                                        <c:out value="${s.nombreProducto}"/> · ${s.tipoSolicitud} · x${s.cantidadDevuelta}
                                    </span>
                                    <span class="cs-badge-estado cs-estado-${s.estado}">${s.estado}</span>
                                </div>
                                <c:if test="${not empty s.respuestaAdmin}">
                                    <div class="small text-muted mt-1">Respuesta: <c:out value="${s.respuestaAdmin}"/></div>
                                </c:if>
                            </li>
                        </c:forEach>
                    </ul>
                    <c:if test="${empty solicitudes}">
                        <p class="small text-muted mb-0">No ha registrado solicitudes para este pedido.</p>
                    </c:if>
                </section>
            </div>
        </div>
    </c:if>
</main>

<footer class="cs-footer cs-no-imprimir">
    <div class="container d-flex flex-wrap justify-content-between gap-2">
        <span>Chero.Sac · Base de Datos II · Grupo 1</span>
        <span class="cs-mono">PostgreSQL + Java 17</span>
    </div>
</footer>

<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>
<script>
(function () {
    'use strict';
    // Tras una compra exitosa se vacia el carrito guardado en el navegador.
    if (new URLSearchParams(window.location.search).has('nuevo')) {
        try {
            localStorage.removeItem('chero_carrito');
        } catch (e) {
            // Sin acceso a localStorage no hay carrito que limpiar.
        }
    }
})();
</script>
</body>
</html>
