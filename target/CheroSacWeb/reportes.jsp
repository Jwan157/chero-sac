<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<%@ taglib uri="jakarta.tags.fmt" prefix="fmt" %>
<c:set var="ctx" value="${pageContext.request.contextPath}"/>
<!doctype html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Chero.Sac | Reportes</title>
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css">
    <link rel="stylesheet" href="${ctx}/css/estilos_chero.css?v=20261127-owner">
</head>
<body class="cs-interno">

<%@ include file="/WEB-INF/vistas/menu-interno.jspf" %>

<main class="container cs-contenido">
    <section class="cs-hero">
        <p class="cs-eyebrow mb-2">Análisis de ventas</p>
        <h1>Reportes</h1>
        <p class="mb-0" style="color:#C9CED6">
            <c:out value="${periodo.etiqueta}"/>. Comparado con <c:out value="${anteriorPeriodo.etiqueta}"/>.
        </p>
    </section>

    <c:if test="${not empty error}">
        <div class="alert alert-danger"><c:out value="${error}"/></div>
    </c:if>

    <form method="get" action="${ctx}/admin/reportes" class="cs-panel mb-3">
        <div class="row g-2 align-items-end">
            <div class="col-md-4">
                <label class="form-label" for="tipo">Tipo de reporte</label>
                <select class="form-select" id="tipo" name="tipo">
                    <c:forEach var="t" items="${tipos}">
                        <option value="${t}" ${tipo == t ? 'selected' : ''}>${nombresTipo[t]}</option>
                    </c:forEach>
                </select>
            </div>
            <div class="col-md-4">
                <label class="form-label" for="fecha">Fecha de referencia</label>
                <input class="form-control" type="date" id="fecha" name="fecha" value="${fecha}">
            </div>
            <div class="col-md-4 d-grid">
                <button class="btn btn-cs" type="submit">Ver reporte</button>
            </div>
        </div>
    </form>

    <div class="d-flex flex-wrap gap-2 justify-content-between align-items-center mb-4">
        <div class="d-flex flex-wrap gap-2">
            <a class="btn btn-cs-outline btn-sm"
               href="${ctx}/admin/reportes?tipo=${tipo}&amp;fecha=${anteriorPeriodo.desde}">&larr; Periodo anterior</a>
            <a class="btn btn-cs-outline btn-sm" href="${ctx}/admin/reportes?tipo=${tipo}">Periodo actual</a>
            <a class="btn btn-cs-outline btn-sm"
               href="${ctx}/admin/reportes?tipo=${tipo}&amp;fecha=${siguientePeriodo.desde}">Periodo siguiente &rarr;</a>
        </div>
        <a class="btn btn-cs btn-sm"
           href="${ctx}/admin/reportes?tipo=${tipo}&amp;fecha=${periodo.desde}&amp;formato=csv">Descargar CSV</a>
    </div>

<section class="cs-panel mb-4"><h2>Resultado del periodo</h2><div class="row g-3"><div class="col-6 col-xl-3"><div class="cs-kpi"><div class="cs-kpi-etiqueta">Ventas netas con IGV</div><div class="cs-kpi-valor">S/ <fmt:formatNumber value="${actual.ventasNetas}" pattern="#,##0.00"/></div></div></div>
<div class="col-6 col-xl-3"><div class="cs-kpi"><div class="cs-kpi-etiqueta">IGV neto</div><div class="cs-kpi-valor">S/ <fmt:formatNumber value="${actual.igvNeto}" pattern="#,##0.00"/></div></div></div>
<div class="col-6 col-xl-3"><div class="cs-kpi"><div class="cs-kpi-etiqueta">Notas de crédito</div><div class="cs-kpi-valor">S/ <fmt:formatNumber value="${actual.creditos}" pattern="#,##0.00"/></div></div></div>
<div class="col-6 col-xl-3"><div class="cs-kpi"><div class="cs-kpi-etiqueta">Ganancia bruta</div><div class="cs-kpi-valor"><c:choose><c:when test="${actual.ganancia == null}">Costo pendiente</c:when><c:otherwise>S/ <fmt:formatNumber value="${actual.ganancia}" pattern="#,##0.00"/></c:otherwise></c:choose></div></div></div>
</div><p class="text-muted small mt-3 mb-0">La ganancia bruta descuenta el costo histórico de las unidades netas de la venta sin IGV. Las notas de crédito se reconocen en su fecha de emisión. Se excluyen pedidos cancelados, gastos operativos y mermas.</p></section>
    <div class="row g-3 mb-3">
        <div class="col-6 col-lg-3">
            <div class="cs-kpi">
                <div class="cs-kpi-etiqueta">Ventas brutas</div>
                <div class="cs-kpi-valor">S/ <fmt:formatNumber value="${actual.ventas}" pattern="#,##0.00"/></div>
                <div class="small ${empty actual.cambioVentas.texto ? 'text-muted' : (actual.cambioVentas.sube ? 'text-success' : 'text-danger')}">
                    <c:choose>
                        <c:when test="${empty actual.cambioVentas.texto}">Sin base de comparación</c:when>
                        <c:otherwise>${actual.cambioVentas.texto} frente al periodo anterior</c:otherwise>
                    </c:choose>
                </div>
            </div>
        </div>
        <div class="col-6 col-lg-3">
            <div class="cs-kpi">
                <div class="cs-kpi-etiqueta">Pedidos</div>
                <div class="cs-kpi-valor">${actual.pedidos}</div>
                <div class="small ${empty actual.cambioPedidos.texto ? 'text-muted' : (actual.cambioPedidos.sube ? 'text-success' : 'text-danger')}">
                    <c:choose>
                        <c:when test="${empty actual.cambioPedidos.texto}">Sin base de comparación</c:when>
                        <c:otherwise>${actual.cambioPedidos.texto} frente al periodo anterior</c:otherwise>
                    </c:choose>
                </div>
            </div>
        </div>
        <div class="col-6 col-lg-3">
            <div class="cs-kpi">
                <div class="cs-kpi-etiqueta">Ticket promedio</div>
                <div class="cs-kpi-valor">S/ <fmt:formatNumber value="${actual.ticket}" pattern="#,##0.00"/></div>
                <div class="small text-muted">Ventas entre pedidos</div>
            </div>
        </div>
        <div class="col-6 col-lg-3">
            <div class="cs-kpi">
                <div class="cs-kpi-etiqueta">Clientes distintos</div>
                <div class="cs-kpi-valor">${actual.clientes}</div>
                <div class="small text-muted">Con al menos un pedido</div>
            </div>
        </div>
    </div>

    <div class="row g-3 mb-4">
        <div class="col-6 col-lg-3">
            <div class="cs-kpi">
                <div class="cs-kpi-etiqueta">IGV cobrado</div>
                <div class="cs-kpi-valor">S/ <fmt:formatNumber value="${actual.igv}" pattern="#,##0.00"/></div>
            </div>
        </div>
        <div class="col-6 col-lg-3">
            <div class="cs-kpi">
                <div class="cs-kpi-etiqueta">Unidades vendidas</div>
                <div class="cs-kpi-valor">${totalUnidades}</div>
            </div>
        </div>
        <div class="col-6 col-lg-3">
            <div class="cs-kpi">
                <div class="cs-kpi-etiqueta">Pedidos cancelados</div>
                <div class="cs-kpi-valor">${actual.cancelados}</div>
            </div>
        </div>
        <div class="col-6 col-lg-3">
            <div class="cs-kpi">
                <div class="cs-kpi-etiqueta">Solicitudes postventa</div>
                <div class="cs-kpi-valor">${postventa.total}</div>
                <div class="small text-muted">Pendientes: ${postventa.pendientes}</div>
            </div>
        </div>
    </div>

    <section class="cs-panel p-0 overflow-hidden mb-4">
        <div class="p-3 border-bottom">
            <h2 class="h5 mb-0">
                <c:choose>
                    <c:when test="${tipo == 'ANUAL'}">Ventas por mes</c:when>
                    <c:otherwise>Ventas por día</c:otherwise>
                </c:choose>
            </h2>
        </div>
        <div class="cs-tabla-responsive">
            <table class="table cs-tabla mb-0">
                <thead>
                <tr>
                    <th>Periodo</th>
                    <th>Pedidos</th>
                    <th class="text-end">Ventas</th>
                    <th style="min-width:180px">Proporción</th>
                </tr>
                </thead>
                <tbody>
                <c:forEach var="f" items="${serie}">
                    <tr>
                        <td class="small"><c:out value="${f.etiqueta}"/></td>
                        <td class="cs-mono">${f.pedidos}</td>
                        <td class="text-end cs-mono">S/ <fmt:formatNumber value="${f.ventas}" pattern="#,##0.00"/></td>
                        <td>
                            <div class="cs-barra"><div class="cs-barra-fill" style="width:${f.porcentaje}%"></div></div>
                        </td>
                    </tr>
                </c:forEach>
                <c:if test="${empty serie}">
                    <tr><td colspan="4" class="text-center text-muted py-4">Sin ventas en este periodo.</td></tr>
                </c:if>
                </tbody>
            </table>
        </div>
    </section>

    <div class="row g-3 mb-4">
        <div class="col-lg-7">
            <section class="cs-panel p-0 overflow-hidden h-100">
                <div class="p-3 border-bottom">
                    <h2 class="h5 mb-0">Productos más vendidos</h2>
                </div>
                <div class="cs-tabla-responsive">
                    <table class="table cs-tabla mb-0">
                        <thead>
                        <tr>
                            <th>#</th>
                            <th>Producto</th>
                            <th class="text-end">Unidades</th>
                            <th class="text-end">Importe</th>
                        </tr>
                        </thead>
                        <tbody>
                        <c:forEach var="p" items="${top}" varStatus="st">
                            <tr>
                                <td class="cs-mono">${st.count}</td>
                                <td>
                                    <div class="fw-semibold small"><c:out value="${p.producto}"/></div>
                                    <div class="small text-muted"><c:out value="${p.categoria}"/></div>
                                </td>
                                <td class="text-end cs-mono">${p.unidades}</td>
                                <td class="text-end cs-mono">S/ <fmt:formatNumber value="${p.importe}" pattern="#,##0.00"/></td>
                            </tr>
                        </c:forEach>
                        <c:if test="${empty top}">
                            <tr><td colspan="4" class="text-center text-muted py-4">Sin productos vendidos.</td></tr>
                        </c:if>
                        </tbody>
                    </table>
                </div>
            </section>
        </div>

        <div class="col-lg-5">
            <section class="cs-panel h-100 mb-3">
                <h2 class="h6 mb-3">Ventas por categoría</h2>
                <c:forEach var="k" items="${categorias}">
                    <div class="mb-2">
                        <div class="d-flex justify-content-between small">
                            <span><c:out value="${k.categoria}"/> · ${k.unidades} u.</span>
                            <span class="cs-mono">S/ <fmt:formatNumber value="${k.importe}" pattern="#,##0.00"/></span>
                        </div>
                        <div class="cs-barra"><div class="cs-barra-fill" style="width:${k.porcentaje}%"></div></div>
                    </div>
                </c:forEach>
                <c:if test="${empty categorias}">
                    <p class="text-muted small mb-0">Sin ventas por categoría.</p>
                </c:if>
            </section>

            <section class="cs-panel h-100">
                <h2 class="h6 mb-3">Métodos de pago</h2>
                <c:forEach var="m" items="${metodos}">
                    <div class="mb-2">
                        <div class="d-flex justify-content-between small">
                            <span><c:out value="${m.metodo}"/> · ${m.pedidos} pedidos</span>
                            <span class="cs-mono">S/ <fmt:formatNumber value="${m.importe}" pattern="#,##0.00"/></span>
                        </div>
                        <div class="cs-barra"><div class="cs-barra-fill" style="width:${m.porcentaje}%"></div></div>
                    </div>
                </c:forEach>
                <c:if test="${empty metodos}">
                    <p class="text-muted small mb-0">Sin pagos registrados.</p>
                </c:if>
            </section>
        </div>
    </div>

    <div class="row g-3 mb-4">
        <div class="col-lg-6">
            <section class="cs-panel h-100">
                <h2 class="h6 mb-3">Pedidos por estado</h2>
                <c:forEach var="e" items="${conteoEstados}">
                    <div class="d-flex justify-content-between align-items-center mb-2">
                        <span class="cs-badge-estado cs-estado-${e.key}">${e.key}</span>
                        <span class="cs-mono fw-semibold">${e.value}</span>
                    </div>
                </c:forEach>
            </section>
        </div>
        <div class="col-lg-6">
            <section class="cs-panel h-100">
                <h2 class="h6 mb-3">Postventa del periodo</h2>
                <div class="d-flex justify-content-between small mb-1"><span>Solicitudes registradas</span><span class="cs-mono">${postventa.total}</span></div>
                <div class="d-flex justify-content-between small mb-1"><span>Devoluciones</span><span class="cs-mono">${postventa.devoluciones}</span></div>
                <div class="d-flex justify-content-between small mb-1"><span>Aprobadas</span><span class="cs-mono">${postventa.aprobadas}</span></div>
                <div class="d-flex justify-content-between small mb-1"><span>Rechazadas</span><span class="cs-mono">${postventa.rechazadas}</span></div>
                <div class="d-flex justify-content-between small"><span>Pendientes</span><span class="cs-mono">${postventa.pendientes}</span></div>
            </section>
        </div>
    </div>

    <section class="cs-panel p-0 overflow-hidden mb-4">
        <div class="p-3 border-bottom">
            <h2 class="h5 mb-0">Alertas de stock bajo</h2>
            <p class="small text-muted mb-0">Productos activos con ${umbralStock} unidades o menos disponibles.</p>
        </div>
        <div class="cs-tabla-responsive">
            <table class="table cs-tabla mb-0">
                <thead>
                <tr>
                    <th>Producto</th>
                    <th>Categoría</th>
                    <th class="text-end">Stock disponible</th>
                </tr>
                </thead>
                <tbody>
                <c:forEach var="s" items="${stockBajo}">
                    <tr>
                        <td><c:out value="${s.producto}"/></td>
                        <td class="small"><c:out value="${s.categoria}"/></td>
                        <td class="text-end cs-mono cs-stock-bajo">${s.stock}</td>
                    </tr>
                </c:forEach>
                <c:if test="${empty stockBajo}">
                    <tr><td colspan="3" class="text-center text-muted py-4">Ningún producto está por debajo del umbral.</td></tr>
                </c:if>
                </tbody>
            </table>
        </div>
    </section>
</main>

<footer class="cs-footer">
    <div class="container d-flex flex-wrap justify-content-between gap-2">
        <span>Chero.Sac · Base de Datos II · Grupo 1</span>
        <span class="cs-mono">PostgreSQL + Java 17</span>
    </div>
</footer>

<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>
