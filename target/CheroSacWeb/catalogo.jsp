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
    <title>Chero.Sac | Catálogo</title>
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css">
    <link rel="stylesheet" href="${ctx}/css/estilos_chero.css">
</head>
<body>

<nav class="navbar navbar-expand-lg cs-navbar">
    <div class="container">
        <a class="navbar-brand" href="${ctx}/catalogo">CHERO<span class="cs-brand-punto">.SAC</span></a>
        <button class="navbar-toggler" type="button" data-bs-toggle="collapse" data-bs-target="#menuCliente"
                aria-controls="menuCliente" aria-expanded="false" aria-label="Abrir menú">
            <span class="navbar-toggler-icon"></span>
        </button>
        <div class="collapse navbar-collapse" id="menuCliente">
            <ul class="navbar-nav ms-auto">
                <li class="nav-item"><a class="nav-link activo" href="${ctx}/catalogo">Catálogo</a></li>
                <li class="nav-item"><a class="nav-link" href="${ctx}/seguimiento">Seguimiento</a></li>
                <li class="nav-item"><a class="nav-link" href="${ctx}/login">Administración</a></li>
            <%@ include file="/WEB-INF/vistas/cuenta-menu.jspf" %>
            </ul>
        </div>
    </div>
</nav>

<main class="container cs-contenido">
    <section class="cs-hero">
        <p class="cs-eyebrow mb-2">Tienda en línea</p>
        <h1>Hardware y equipos originales</h1>
        <p class="mb-0" style="color:#C9CED6">Compra sin registrarte. Al confirmar recibirás un código de seguimiento CH-YYYY-XXXXXX.</p>
    </section>

    <c:if test="${not empty error}">
        <div class="alert alert-danger"><c:out value="${error}"/></div>
    </c:if>

    <div class="row g-4">
        <div class="col-lg-8">
            <form method="get" action="${ctx}/catalogo" class="cs-panel mb-3">
                <div class="row g-2 align-items-end">
                    <div class="col-sm-7">
                        <label class="form-label" for="q">Buscar producto</label>
                        <input class="form-control" type="search" id="q" name="q" maxlength="100"
                               value="<c:out value='${q}'/>" placeholder="Ej. laptop, teclado, RAM">
                    </div>
                    <div class="col-sm-3">
                        <label class="form-label" for="cat">Categoría</label>
                        <select class="form-select" id="cat" name="cat">
                            <option value="">Todas</option>
                            <c:forEach var="categoria" items="${categorias}">
                                <option value="${categoria.idCategoria}"
                                        ${categoria.idCategoria == cat ? 'selected' : ''}>
                                    <c:out value="${categoria.nombreCategoria}"/>
                                </option>
                            </c:forEach>
                        </select>
                    </div>
                    <div class="col-sm-2 d-grid">
                        <button class="btn btn-cs" type="submit">Buscar</button>
                    </div>
                </div>
            </form>

            <div class="mb-3">
                <a class="cs-pill ${empty cat ? 'activa' : ''}" href="${ctx}/catalogo">Todos</a>
                <c:forEach var="categoria" items="${categorias}">
                    <a class="cs-pill ${categoria.idCategoria == cat ? 'activa' : ''}"
                       href="${ctx}/catalogo?cat=${categoria.idCategoria}">
                        <c:out value="${categoria.nombreCategoria}"/>
                    </a>
                </c:forEach>
            </div>

            <div class="d-flex justify-content-between align-items-center flex-wrap gap-2 mb-3">
                <span class="small text-muted">Mostrando <strong>${inicioResultados}-${finResultados}</strong> de
                    <strong>${totalProductos}</strong> productos</span>
                <span class="small text-muted">Página ${paginaActual} de ${totalPaginas}</span>
            </div>

            <div class="row row-cols-1 row-cols-sm-2 row-cols-xl-3 g-3">
                <c:forEach var="p" items="${productos}">
                    <div class="col">
                        <article class="cs-card cs-producto">
                            <ui:foto ruta="${p.imagenUrl}" nombre="${p.nombreProducto}" clase="cs-producto-img"/>
                            <div class="cs-producto-cuerpo">
                                <span class="cs-categoria-tag mb-2 align-self-start">
                                    <c:out value="${p.nombreCategoria}"/>
                                </span>
                                <h2 class="cs-producto-nombre"><c:out value="${p.nombreProducto}"/></h2>
                                <p class="small text-muted flex-grow-1 mb-2"><c:out value="${p.descripcion}"/></p>
                                <div class="d-flex justify-content-between align-items-center mb-2">
                                    <span class="cs-precio">S/ <fmt:formatNumber value="${p.precio}" pattern="#,##0.00"/></span>
                                    <c:choose>
                                        <c:when test="${p.stockDisponible gt 0}">
                                            <span class="small">Stock: ${p.stockDisponible}</span>
                                        </c:when>
                                        <c:otherwise>
                                            <span class="cs-stock-bajo">Agotado</span>
                                        </c:otherwise>
                                    </c:choose>
                                </div>
                                <button type="button" class="btn btn-cs w-100 js-agregar"
                                        data-id="${p.idProducto}"
                                        data-nombre="<c:out value='${p.nombreProducto}'/>"
                                        data-precio="${p.precio}"
                                        data-stock="${p.stockDisponible}"
                                        ${p.stockDisponible gt 0 ? '' : 'disabled'}>
                                    Agregar al carrito
                                </button>
                            </div>
                        </article>
                    </div>
                </c:forEach>
            </div>

            <c:if test="${empty productos and empty error}">
                <div class="alert alert-light border mt-3">No hay productos que coincidan con la búsqueda.</div>
            </c:if>
            <c:if test="${totalPaginas gt 1}">
                <nav class="cs-paginacion mt-4" aria-label="Paginación del catálogo">
                    <ul class="pagination justify-content-center flex-wrap gap-1">
                        <li class="page-item ${paginaActual == 1 ? 'disabled' : ''}">
                            <c:url var="urlAnterior" value="/catalogo">
                                <c:param name="pagina" value="${paginaActual - 1}"/>
                                <c:if test="${not empty q}"><c:param name="q" value="${q}"/></c:if>
                                <c:if test="${not empty cat}"><c:param name="cat" value="${cat}"/></c:if>
                            </c:url>
                            <a class="page-link" href="${urlAnterior}" aria-label="Página anterior">Anterior</a>
                        </li>
                        <c:forEach begin="1" end="${totalPaginas}" var="n">
                            <li class="page-item ${n == paginaActual ? 'active' : ''}">
                                <c:url var="urlPagina" value="/catalogo">
                                    <c:param name="pagina" value="${n}"/>
                                    <c:if test="${not empty q}"><c:param name="q" value="${q}"/></c:if>
                                    <c:if test="${not empty cat}"><c:param name="cat" value="${cat}"/></c:if>
                                </c:url>
                                <a class="page-link" href="${urlPagina}" aria-current="${n == paginaActual ? 'page' : ''}">${n}</a>
                            </li>
                        </c:forEach>
                        <li class="page-item ${paginaActual == totalPaginas ? 'disabled' : ''}">
                            <c:url var="urlSiguiente" value="/catalogo">
                                <c:param name="pagina" value="${paginaActual + 1}"/>
                                <c:if test="${not empty q}"><c:param name="q" value="${q}"/></c:if>
                                <c:if test="${not empty cat}"><c:param name="cat" value="${cat}"/></c:if>
                            </c:url>
                            <a class="page-link" href="${urlSiguiente}" aria-label="Página siguiente">Siguiente</a>
                        </li>
                    </ul>
                </nav>
            </c:if>
        </div>

        <aside class="col-lg-4">
            <div class="cs-card cs-carrito p-3">
                <h2 class="h5 fw-bold mb-3">Tu carrito</h2>
                <div id="carritoVacio" class="text-muted">Aún no agregas productos.</div>
                <div id="carritoItems"></div>
                <div id="carritoTotales" class="cs-carrito-total d-none">
                    <div class="d-flex justify-content-between small">
                        <span>Subtotal (sin IGV)</span><span class="cs-mono" id="cSubtotal">S/ 0.00</span>
                    </div>
                    <div class="d-flex justify-content-between small">
                        <span>IGV (18%)</span><span class="cs-mono" id="cIgv">S/ 0.00</span>
                    </div>
                    <div class="d-flex justify-content-between align-items-center mt-2">
                        <span class="fw-bold">Total</span><span class="cs-precio-grande" id="cTotal">S/ 0.00</span>
                    </div>
                    <a class="btn btn-cs w-100 mt-3" href="${ctx}/checkout">Finalizar compra</a>
                    <button type="button" class="btn btn-link btn-sm text-decoration-none w-100 mt-1" id="btnVaciar"
                            style="color:#9AA3B2">Vaciar carrito</button>
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

    function guardar(items) {
        try {
            localStorage.setItem(CLAVE, JSON.stringify(items));
        } catch (e) {
            // Almacenamiento no disponible: el carrito solo dura mientras la pagina este abierta.
        }
    }

    function soles(valor) {
        return 'S/ ' + nf.format(valor);
    }

    function agregar(data, imagen) {
        var items = leer();
        var id = parseInt(data.id, 10);
        var stock = parseInt(data.stock, 10);
        var existente = items.find(function (it) { return it.id === id; });
        if (existente) {
            existente.imagen = imagen;
            if (existente.cantidad < stock) {
                existente.cantidad += 1;
            }
        } else {
            items.push({ id: id, nombre: data.nombre, imagen: imagen, precio: parseFloat(data.precio), stock: stock, cantidad: 1 });
        }
        guardar(items);
        render();
    }

    function cambiar(id, delta) {
        var items = leer();
        items.forEach(function (it) {
            if (it.id === id) {
                it.cantidad = Math.min(it.cantidad + delta, it.stock);
            }
        });
        guardar(items.filter(function (it) { return it.cantidad > 0; }));
        render();
    }

    function boton(etiqueta, accion) {
        var b = document.createElement('button');
        b.type = 'button';
        b.className = 'btn btn-sm btn-cs-outline';
        b.textContent = etiqueta;
        b.addEventListener('click', accion);
        return b;
    }

    function filaCarrito(it) {
        var fila = document.createElement('div');
        fila.className = 'cs-carrito-item';

        var nombre = document.createElement('div');
        nombre.className = 'cs-carrito-nombre';
        nombre.append(miniatura(it), document.createTextNode(it.nombre));

        var linea = document.createElement('div');
        linea.className = 'd-flex justify-content-between align-items-center mt-1';

        var control = document.createElement('div');
        control.className = 'cs-cantidad-control';
        control.appendChild(boton('-', function () { cambiar(it.id, -1); }));
        var cant = document.createElement('span');
        cant.className = 'cs-mono';
        cant.textContent = it.cantidad;
        control.appendChild(cant);
        control.appendChild(boton('+', function () { cambiar(it.id, 1); }));

        var importe = document.createElement('span');
        importe.className = 'cs-mono';
        importe.textContent = soles(it.precio * it.cantidad);

        linea.appendChild(control);
        linea.appendChild(importe);
        fila.appendChild(nombre);
        fila.appendChild(linea);
        return fila;
    }

    function render() {
        var items = leer();
        var lista = document.getElementById('carritoItems');
        if (!lista) {
            return;
        }
        lista.textContent = '';
        var total = 0;
        items.forEach(function (it) {
            total += it.precio * it.cantidad;
            lista.appendChild(filaCarrito(it));
        });
        var subtotal = total / FACTOR_IGV;
        document.getElementById('carritoVacio').classList.toggle('d-none', items.length > 0);
        document.getElementById('carritoTotales').classList.toggle('d-none', items.length === 0);
        document.getElementById('cSubtotal').textContent = soles(subtotal);
        document.getElementById('cIgv').textContent = soles(total - subtotal);
        document.getElementById('cTotal').textContent = soles(total);
    }

    document.addEventListener('DOMContentLoaded', function () {
        document.querySelectorAll('.js-agregar').forEach(function (btn) {
            btn.addEventListener('click', function () { agregar(btn.dataset, btn.closest('article').querySelector('img').src); });
        });
        var vaciar = document.getElementById('btnVaciar');
        if (vaciar) {
            vaciar.addEventListener('click', function () { guardar([]); render(); });
        }
        render();
    });
})();
</script>
</body>
</html>
