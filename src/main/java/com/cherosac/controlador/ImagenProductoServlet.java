package com.cherosac.controlador;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import javax.imageio.*;
import javax.imageio.stream.ImageInputStream;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.file.*;
import java.util.Iterator;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

@WebServlet("/img/productos/subidas/*")
public class ImagenProductoServlet extends HttpServlet {
    private static final Set<String> FORMATOS = Set.of("png","jpeg","jpg","gif","bmp");
    static Path directorio() {
        String base = System.getenv("CHERO_IMAGENES_DIR");
        return (base == null || base.isBlank()
                ? Path.of(System.getProperty("catalina.base"), "chero-data", "img", "productos")
                : Path.of(base)).toAbsolutePath().normalize();
    }

    static Path guardar(Part foto) throws IOException {
        if (foto == null || foto.getSize() == 0) return null;
        if (foto.getSize() > 5L*1024*1024) throw new IllegalArgumentException("La imagen supera 5 MB.");
        BufferedImage imagen;
        try (InputStream in = foto.getInputStream(); ImageInputStream datos = ImageIO.createImageInputStream(in)) {
            Iterator<ImageReader> lectores = ImageIO.getImageReaders(datos);
            if (!lectores.hasNext()) throw new IllegalArgumentException("Seleccione una foto PNG, JPEG, GIF o BMP.");
            ImageReader lector = lectores.next();
            try {
                lector.setInput(datos,true,true);
                if (!FORMATOS.contains(lector.getFormatName().toLowerCase(Locale.ROOT))
                        || (long)lector.getWidth(0)*lector.getHeight(0) > 12_000_000L)
                    throw new IllegalArgumentException("Formato incompatible o imagen mayor de 12 megapíxeles.");
                imagen = lector.read(0);
            } finally { lector.dispose(); }
        }
        Path carpeta = Files.createDirectories(directorio());
        Path ruta = carpeta.resolve(UUID.randomUUID()+".png");
        try (OutputStream salida = Files.newOutputStream(ruta, StandardOpenOption.CREATE_NEW)) {
            if (!ImageIO.write(imagen,"png",salida)) throw new IOException("Error al codificar la imagen.");
        } catch (IOException e) {
            Files.deleteIfExists(ruta);
            throw e;
        }
        return ruta;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String nombre = req.getPathInfo();
        if (nombre == null || !nombre.matches("/[0-9a-f-]{36}\\.png")) {
            resp.sendError(404);
            return;
        }
        Path foto = directorio().resolve(nombre.substring(1));
        if (!Files.isRegularFile(foto)) {
            resp.sendError(404);
            return;
        }
        resp.setContentType("image/png");
        resp.setHeader("X-Content-Type-Options","nosniff");
        resp.setHeader("Cache-Control","public, max-age=31536000, immutable");
        resp.setContentLengthLong(Files.size(foto));
        Files.copy(foto,resp.getOutputStream());
    }
}

