<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<c:set var="ctx" value="${pageContext.request.contextPath}"/>
<!doctype html><html lang="es"><head><meta charset="UTF-8"><meta name="viewport" content="width=device-width,initial-scale=1">
<title>Trabajadores y auditoría | Chero.Sac</title>
<link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css">
<link rel="stylesheet" href="${ctx}/css/estilos_chero.css?v=20261127-owner"></head><body class="cs-interno">
<%@ include file="/WEB-INF/vistas/menu-interno.jspf" %>
<main class="container cs-contenido">
<section class="cs-hero"><h1>Trabajadores y auditoría</h1></section>
<c:if test="${not empty error or not empty param.error}"><div class="alert alert-danger" role="alert"><c:out value="${empty error ? param.error : error}"/></div></c:if>
<c:if test="${not empty param.ok}"><div class="alert alert-success" role="status"><c:out value="${param.ok}"/></div></c:if><div class="row g-4"><div class="col-lg-5"><section class="cs-panel"><h2>Crear trabajador</h2>
<form method="post" action="${ctx}/admin/usuarios">
            <input type="hidden" name="csrf" value="${sessionScope.csrf}"><input type="hidden" name="accion" value="crear">
<label class="form-label d-block mb-3">Nombre completo<input class="form-control" type="text" name="nombre" required maxlength="120"></label>
<label class="form-label d-block mb-3">Usuario<input class="form-control" type="text" name="usuario" required minlength="3" maxlength="50"></label>
<label class="form-label d-block mb-3">Correo<input class="form-control" type="email" name="correo" required maxlength="120"></label>
<label class="form-label d-block mb-3">Contraseña<input class="form-control" type="password" name="clave" required minlength="8" maxlength="72" autocomplete="new-password"></label>
<button class="btn btn-cs">Crear cuenta de empleado</button></form></section></div>
<div class="col-lg-7"><section class="cs-panel"><h2>Cuentas internas</h2>
<c:forEach var="u" items="${trabajadores}"><div class="d-flex justify-content-between gap-2 align-items-center py-3 border-bottom">
<div><strong><c:out value="${u.nombreCompleto}"/></strong><br><span class="small"><c:out value="${u.nombreUsuario}"/> · ${u.rol} · ${u.activo ? 'Activo' : 'Inactivo'}</span></div>
<c:if test="${u.rol == 'ADMIN'}"><form method="post" action="${ctx}/admin/usuarios">
            <input type="hidden" name="csrf" value="${sessionScope.csrf}">
<input type="hidden" name="idUsuario" value="${u.idUsuario}"><button class="btn btn-sm btn-cs-outline" name="accion" value="${u.activo ? 'desactivar' : 'activar'}">${u.activo ? 'Desactivar' : 'Activar'}</button>
</form></c:if></div></c:forEach></section></div></div>
<%@ include file="/WEB-INF/vistas/auditoria.jspf" %></main></body></html>
