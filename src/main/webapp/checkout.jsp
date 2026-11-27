<%@ taglib tagdir="/WEB-INF/tags" prefix="ui" %>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<%@ taglib uri="jakarta.tags.fmt" prefix="fmt" %>
<c:set var="ctx" value="${pageContext.request.contextPath}"/>
<!doctype html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Chero.Sac | Finalizar compra</title>
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css">
    <link rel="stylesheet" href="${ctx}/css/estilos_chero.css">
</head>
<body>

<nav class="navbar navbar-expand-lg cs-navbar">
    <div class="container">
        <a class="navbar-brand" href="${ctx}/catalogo">CHERO<span class="cs-brand-punto">.SAC</span></a>
        <button class="navbar-toggler" type="button" data-bs-toggle="collapse" data-bs-target="#menuCheckout"
                aria-controls="menuCheckout" aria-expanded="false" aria-label="Abrir menú">
            <span class="navbar-toggler-icon"></span>
        </button>
        <div class="collapse navbar-collapse" id="menuCheckout">
            <ul class="navbar-nav ms-auto">
                <li class="nav-item"><a class="nav-link" href="${ctx}/catalogo">Catálogo</a></li>
                <li class="nav-item"><a class="nav-link activo" href="${ctx}/checkout">Checkout</a></li>
                <li class="nav-item"><a class="nav-link" href="${ctx}/seguimiento">Seguimiento</a></li>
            <%@ include file="/WEB-INF/vistas/cuenta-menu.jspf" %>
            </ul>
        </div>
    </div>
</nav>

<main class="container cs-contenido">
    <section class="cs-hero">
        <p class="cs-eyebrow mb-2">Paso final</p>
        <h1>Finalizar compra</h1>
        <p class="mb-0" style="color:#C9CED6">Confirme la entrega y el pago. El comprobante PDF es de demostración.</p>
    </section>

    <c:if test="${not empty error}">
        <div class="alert alert-danger"><c:out value="${error}"/></div>
    </c:if>

    <div class="row g-4">
        <div class="col-lg-7">
            <form method="post" action="${ctx}/checkout" class="cs-panel" id="formCompra">
            <input type="hidden" name="csrf" value="${sessionScope.csrf}">
                <h2>1. Datos del cliente</h2>
                <div class="row g-3">
                    <div class="col-md-6">
                        <label class="form-label" for="tipoDocumento">Tipo de documento</label>
                        <select class="form-select" id="tipoDocumento" name="tipoDocumento" ${not empty sessionScope.cliente ? 'disabled' : ''}>
                            <option value="DNI" ${(empty sessionScope.cliente ? param.tipoDocumento : sessionScope.cliente.tipoDocumento) == 'RUC' ? '' : 'selected'}>DNI (boleta)</option>
                            <option value="RUC" ${(empty sessionScope.cliente ? param.tipoDocumento : sessionScope.cliente.tipoDocumento) == 'RUC' ? 'selected' : ''}>RUC (factura)</option>
                        </select>
                    </div>
                    <div class="col-md-6">
                        <label class="form-label" for="numeroDocumento">Número de documento</label>
                        <input class="form-control cs-mono" id="numeroDocumento" name="numeroDocumento" ${not empty sessionScope.cliente ? 'readonly' : ''}
                               inputmode="numeric" maxlength="8" required
                               value="<c:out value='${not empty param.numeroDocumento ? param.numeroDocumento : sessionScope.cliente.numeroDocumento}'/>" placeholder="8 dígitos">
                    </div>
                    <div class="col-md-6">
                        <label class="form-label" for="nombres" id="lblNombres">Nombres</label>
                        <input class="form-control" id="nombres" name="nombres" maxlength="70" required
                               value="<c:out value='${not empty param.nombres ? param.nombres : sessionScope.cliente.nombreRazonSocial}'/>">
                    </div>
                    <div class="col-md-6" id="bloqueApellidos">
                        <label class="form-label" for="apellidos">Apellidos</label>
                        <input class="form-control" id="apellidos" name="apellidos" maxlength="79"
                               value="<c:out value='${not empty param.apellidos ? param.apellidos : sessionScope.cliente.apellido}'/>">
                    </div>
                    <div class="col-12">
                        <label class="form-label" for="direccion">Dirección de entrega</label>
                        <input class="form-control" id="direccion" name="direccion" maxlength="200" required
                               value="<c:out value='${not empty param.direccion ? param.direccion : sessionScope.cliente.direccion}'/>">
                    </div>
                    <div class="col-md-6">
                        <label class="form-label" for="telefono">Teléfono (opcional)</label>
                        <input class="form-control" id="telefono" name="telefono" maxlength="15"
                               value="<c:out value='${not empty param.telefono ? param.telefono : sessionScope.cliente.telefono}'/>">
                    </div>
                    <div class="col-md-6">
                        <label class="form-label" for="correo">Correo (opcional)</label>
                        <input class="form-control" type="email" id="correo" name="correo" ${not empty sessionScope.cliente ? 'readonly' : ''} maxlength="120"
                               value="<c:out value='${not empty param.correo ? param.correo : sessionScope.cliente.correo}'/>">
                    </div>
                </div>

                <h2 class="mt-4">2. Comprobante y pago</h2>
                <div class="row g-3">
                    <div class="col-md-6">
                        <span class="form-label d-block">Comprobante</span>
                        <div class="border rounded-3 p-2 small fw-semibold" id="etiquetaComprobante">
                            Boleta de venta electrónica (serie B001)
                        </div>
                    </div>
                    <div class="col-md-6">
                        <label class="form-label" for="idMetodoPago">Método de pago</label>
                        <select class="form-select" id="idMetodoPago" name="idMetodoPago" required>
                            <c:forEach var="m" items="${metodos}">
                                <option value="${m.idMetodoPago}" ${param.idMetodoPago == m.idMetodoPago ? 'selected' : ''}>
                                    <c:out value="${m.tipoPago}"/>
                                </option>
                            </c:forEach>
                        </select>
                    </div>
                </div>

                <input type="hidden" name="carrito" id="carritoOculto" value="">
                <button class="btn btn-cs btn-lg w-100 mt-4" type="submit" id="btnConfirmar">Confirmar pedido</button>
                <p class="small text-muted mt-2 mb-0">Al confirmar se genera su código de seguimiento y el comprobante electrónico.</p>
            </form>
        </div>

        <aside class="col-lg-5">
            <div class="cs-card cs-carrito p-3">
                <h2 class="h5 fw-bold mb-3">Resumen del pedido</h2>
                <div id="resumenVacio" class="text-muted d-none">
                    Su carrito está vacío. <a href="${ctx}/catalogo">Volver al catálogo</a>
                </div>
                <div id="resumenItems"></div>
                <div id="resumenTotales" class="cs-carrito-total d-none">
                    <div class="d-flex justify-content-between small">
                        <span>Subtotal (sin IGV)</span><span class="cs-mono" id="rSubtotal">S/ 0.00</span>
                    </div>
                    <div class="d-flex justify-content-between small">
                        <span>IGV (18%)</span><span class="cs-mono" id="rIgv">S/ 0.00</span>
                    </div>
                    <div class="d-flex justify-content-between align-items-center mt-2">
                        <span class="fw-bold">Total a pagar</span><span class="cs-precio-grande" id="rTotal">S/ 0.00</span>
                    </div>
                </div>
            </div>
        </aside>
    </div>
</main>

<footer class="cs-footer">
    <div class="container d-flex flex-wrap justify-content-between gap-2">
        <span>Chero.Sac · Base de Datos II · Grupo 1</span>
        <span class="cs-mono">PostgreSQL + Java 17</span>
    </div>
</footer>

<template id="plantillaFoto"><ui:foto nombre="" clase="cs-mini"/></template>
<script>
function miniatura(item) {
    var foto = document.getElementById('plantillaFoto').content.firstElementChild.cloneNode(true);
    if (item.imagen) foto.src = item.imagen;
    return foto;
}
</script>
<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>
<script>
(function () {
    'use strict';
    var CLAVE = 'chero_carrito';
    var FACTOR_IGV = 1.18;
    var nf = new Intl.NumberFormat('es-PE', { minimumFractionDigits: 2, maximumFractionDigits: 2 });

    function leer() {
        try {
            var datos = JSON.parse(localStorage.getItem(CLAVE) || '[]');
            return Array.isArray(datos) ? datos : [];
        } catch (e) {
            return [];
        }
    }

    function soles(valor) {
        return 'S/ ' + nf.format(valor);
    }

    function renderResumen() {
        var items = leer();
        var lista = document.getElementById('resumenItems');
        lista.textContent = '';
        var total = 0;
        items.forEach(function (it) {
            var importe = it.precio * it.cantidad;
            total += importe;
            var fila = document.createElement('div');
            fila.className = 'd-flex justify-content-between small py-1 border-bottom';
            var izquierda = document.createElement('span');
            izquierda.append(miniatura(it), document.createTextNode(it.cantidad + ' × ' + it.nombre));
            var derecha = document.createElement('span');
            derecha.className = 'cs-mono';
            derecha.textContent = soles(importe);
            fila.appendChild(izquierda);
            fila.appendChild(derecha);
            lista.appendChild(fila);
        });

        var subtotal = total / FACTOR_IGV;
        document.getElementById('rSubtotal').textContent = soles(subtotal);
        document.getElementById('rIgv').textContent = soles(total - subtotal);
        document.getElementById('rTotal').textContent = soles(total);
        document.getElementById('resumenVacio').classList.toggle('d-none', items.length > 0);
        document.getElementById('resumenTotales').classList.toggle('d-none', items.length === 0);
        document.getElementById('btnConfirmar').disabled = items.length === 0;
        document.getElementById('carritoOculto').value = items.map(function (it) {
            return it.id + ':' + it.cantidad;
        }).join(';');
    }

    function ajustarTipoDocumento() {
        var ruc = document.getElementById('tipoDocumento').value === 'RUC';
        var apellidos = document.getElementById('apellidos');
        var numero = document.getElementById('numeroDocumento');

        document.getElementById('lblNombres').textContent = ruc ? 'Razón social' : 'Nombres';
        document.getElementById('bloqueApellidos').classList.toggle('d-none', ruc);
        apellidos.required = !ruc;
        apellidos.disabled = ruc;

        numero.pattern = ruc ? "(10|20)[0-9]{9}" : "[0-9]{8}";
        numero.maxLength = ruc ? 11 : 8;
        numero.placeholder = ruc ? '11 dígitos' : '8 dígitos';
        document.getElementById('etiquetaComprobante').textContent = ruc
            ? 'Factura electrónica (serie F001)'
            : 'Boleta de venta electrónica (serie B001)';
    }

    document.addEventListener('DOMContentLoaded', function () {
        var numero = document.getElementById('numeroDocumento');
        numero.addEventListener('input', function () {
            numero.value = numero.value.replace(/\D/g, '');
        });
        document.getElementById('tipoDocumento').addEventListener('change', function () {
            numero.value = '';
            ajustarTipoDocumento();
        });
        document.getElementById('formCompra').addEventListener('submit', function (evento) {
            if (leer().length === 0) {
                evento.preventDefault();
                renderResumen();
            }
        });
        ajustarTipoDocumento();
        renderResumen();
    });
})();
</script>
</body>
</html>
