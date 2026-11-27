<%@ tag pageEncoding="UTF-8" body-content="empty" %>
<%@ attribute name="ruta" required="false" %>
<%@ attribute name="nombre" required="false" %>
<%@ attribute name="clase" required="false" %>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<c:url var="urlFoto" value="${empty ruta ? '/img/productos/default-hardware.png' : ruta}"/>
<img loading="lazy" src="<c:out value='${urlFoto}'/>" alt="<c:out value='${nombre}'/>" class="<c:out value='${empty clase ? "cs-mini" : clase}'/>"
     onerror="this.onerror=null; this.src='${pageContext.request.contextPath}/img/productos/default-hardware.png';">
