<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<c:set var="ctx" value="${pageContext.request.contextPath}"/>
<!doctype html><html lang="es"><head><meta charset="UTF-8"><meta name="viewport" content="width=device-width,initial-scale=1">
<title>Mi cuenta | Chero.Sac</title>
<link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css">
<link rel="stylesheet" href="${ctx}/css/estilos_chero.css"></head><body class="">
<nav class="cs-navbar py-3"><div class="container d-flex align-items-center justify-content-between">
<a class="cs-side-brand" href="${ctx}/catalogo">CHERO<span>.SAC</span></a><a class="text-white" href="${ctx}/catalogo">Volver al catálogo</a></div></nav>
<main class="container cs-contenido"><section class="cs-hero"><h1>Mi cuenta</h1><p class="mb-0">Consulta tus pedidos, comprobantes y devoluciones.</p></section>
<c:if test="${not empty error or not empty param.error}"><div class="alert alert-danger" role="alert"><c:out value="${empty error ? param.error : error}"/></div></c:if>
<c:if test="${not empty param.ok}"><div class="alert alert-success" role="status"><c:out value="${param.ok}"/></div></c:if><div class="row g-4 align-items-start"><div class="col-lg-5"><section class="cs-panel"><h2>Iniciar sesión</h2>
<form method="post" action="${ctx}/cliente">
            <input type="hidden" name="csrf" value="${sessionScope.csrf}"><input type="hidden" name="accion" value="entrar">
<label class="form-label d-block mb-3">DNI o RUC<input class="form-control" type="text" name="documento" required inputmode="numeric" maxlength="11" autocomplete="username"></label>
<label class="form-label d-block mb-3">Contraseña<input class="form-control" type="password" name="clave" required autocomplete="current-password"></label>
<button class="btn btn-cs">Entrar</button></form></section></div>
<div class="col-lg-7"><section class="cs-panel"><h2>Crear cuenta</h2><form method="post" action="${ctx}/cliente">
            <input type="hidden" name="csrf" value="${sessionScope.csrf}">
<input type="hidden" name="accion" value="registrar"><label class="form-label d-block mb-3">Tipo de documento<select name="tipoDocumento" class="form-select"><option>DNI</option><option>RUC</option></select></label>
<label class="form-label d-block mb-3">Número de documento<input class="form-control" type="text" name="documento" required inputmode="numeric" maxlength="11"></label>
<label class="form-label d-block mb-3">Nombres o razón social<input class="form-control" type="text" name="nombre" required minlength="2" maxlength="70"></label>
<label class="form-label d-block mb-3">Apellidos (para DNI)<input class="form-control" type="text" name="apellido" maxlength="79"></label>
<label class="form-label d-block mb-3">Correo<input class="form-control" type="email" name="correo" required maxlength="120" autocomplete="email"></label>
<label class="form-label d-block mb-3">Dirección de entrega<input class="form-control" type="text" name="direccion" required minlength="5" maxlength="200"></label>
<label class="form-label d-block mb-3">Contraseña (mínimo 8 caracteres)<input class="form-control" type="password" name="clave" required minlength="8" maxlength="72" autocomplete="new-password"></label>
<label class="form-label d-block mb-3">Clave de vinculación de tu primera compra como invitado (si corresponde)<input class="form-control" type="text" name="vinculo" maxlength="60" autocomplete="off" value="<c:out value='${sessionScope.claveVinculo}'/>"></label>
<button class="btn btn-cs">Crear cuenta</button>
</form></section></div></div></main></body></html>
