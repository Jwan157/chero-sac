<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<c:set var="ctx" value="${pageContext.request.contextPath}"/>
<!doctype html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Chero.Sac | Acceso administrador</title>
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css">
    <link rel="stylesheet" href="${ctx}/css/estilos_chero.css">
</head>
<body>

<nav class="navbar cs-navbar">
    <div class="container">
        <a class="navbar-brand" href="${ctx}/catalogo">CHERO<span class="cs-brand-punto">.SAC</span></a>
        <a class="nav-link" href="${ctx}/catalogo">Volver a la tienda</a>
    </div>
</nav>

<main class="container cs-login-wrap cs-contenido">
    <div class="cs-card cs-login-card p-4">
        <p class="cs-eyebrow mb-1">Acceso restringido</p>
        <h1 class="h3 fw-bold mb-1">Panel administrativo</h1>
        <p class="small mb-4" style="color:#9AA3B2">Ingrese con su usuario y contraseña de administrador.</p>

        <c:if test="${not empty aviso}">
            <div class="alert alert-warning py-2"><c:out value="${aviso}"/></div>
        </c:if>
        <c:if test="${not empty error}">
            <div class="alert alert-danger py-2"><c:out value="${error}"/></div>
        </c:if>

        <form method="post" action="${ctx}/login">
            <input type="hidden" name="csrf" value="${sessionScope.csrf}">
            <div class="mb-3">
                <label class="form-label text-white" for="usuario">Usuario</label>
                <input class="form-control" id="usuario" name="usuario" maxlength="50" autocomplete="username"
                       required value="<c:out value='${param.usuario}'/>">
            </div>
            <div class="mb-4">
                <label class="form-label text-white" for="clave">Contraseña</label>
                <input class="form-control" type="password" id="clave" name="clave" autocomplete="current-password" required>
            </div>
            <button class="btn btn-cs w-100 py-2" type="submit">Ingresar</button>
        </form>
    </div>
</main>

<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>
