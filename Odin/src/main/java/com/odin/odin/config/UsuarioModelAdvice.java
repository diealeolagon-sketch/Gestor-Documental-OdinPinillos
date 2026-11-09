package com.odin.odin.config;

import com.odin.odin.model.Radicados;
import com.odin.odin.repository.RadicadosRepository;
import com.odin.odin.repository.NotificacionesRepository;
import com.odin.odin.model.Notificaciones;
import com.odin.odin.service.OdinUserDetails;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@ControllerAdvice
public class UsuarioModelAdvice {

    private final RadicadosRepository radicadosRepository;
    private final NotificacionesRepository notificacionesRepository;

    public UsuarioModelAdvice(RadicadosRepository radicadosRepository, NotificacionesRepository notificacionesRepository) {
        this.radicadosRepository = radicadosRepository;
        this.notificacionesRepository = notificacionesRepository;
    }

    @ModelAttribute("usuarioActual")
    public OdinUserDetails usuarioActual() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) return null;
        Object p = auth.getPrincipal();
        return (p instanceof OdinUserDetails oud) ? oud : null;
    }

    @ModelAttribute("nombreUsuario")
    public String nombreUsuario() {
        OdinUserDetails u = usuarioActual();
        if (u != null) return u.getNombreVisible();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && auth.getName() != null
                && !"anonymousUser".equals(auth.getName())) {
            return auth.getName();
        }
        return "Usuario";
    }

    @ModelAttribute("inicialUsuario")
    public String inicialUsuario() {
        OdinUserDetails u = usuarioActual();
        if (u != null) return u.getInicial();
        String n = nombreUsuario();
        return (n == null || n.isBlank()) ? "U" : n.substring(0, 1).toUpperCase();
    }

    @ModelAttribute("correoUsuario")
    public String correoUsuario() {
        OdinUserDetails u = usuarioActual();
        return u != null && u.getCorreo() != null ? u.getCorreo() : "";
    }

    @ModelAttribute("debeCambiarPassword")
    public boolean debeCambiarPassword() {
        OdinUserDetails u = usuarioActual();
        return u != null && u.isDebeCambiarPassword();
    }

    @ModelAttribute("notifCount")
    public long notifCount() {
        OdinUserDetails u = usuarioActual();
        if (u == null || u.getIdUsuario() == null) return 0L;
        try { return notificacionesRepository.countById_usuarioAndLeidaFalse(u.getIdUsuario()); }
        catch (Exception e) { return 0L; }
    }

    @ModelAttribute("notifItems")
    public List<String> notifItems() {
        OdinUserDetails u = usuarioActual();
        if (u == null || u.getIdUsuario() == null) return Collections.singletonList("No hay notificaciones nuevas");
        try {
            List<Notificaciones> ns = notificacionesRepository.findTop10ById_usuarioOrderByFechaDesc(u.getIdUsuario());
            if (ns == null || ns.isEmpty()) return Collections.singletonList("No hay notificaciones nuevas");
            List<String> items = new ArrayList<>();
            for (Notificaciones n : ns) items.add((n.getTitulo() == null ? "Aviso" : n.getTitulo()) + " — " + (n.getMensaje() == null ? "" : n.getMensaje()));
            return items;
        } catch (Exception e) { return Collections.singletonList("No hay notificaciones nuevas"); }
    }

    private boolean tieneAutoridad(String... authorities) {
        OdinUserDetails u = usuarioActual();
        if (u == null || u.getAuthorities() == null) return false;
        for (GrantedAuthority ga : u.getAuthorities()) {
            if (ga == null || ga.getAuthority() == null) continue;
            String a = ga.getAuthority();
            if ("ROLE_ADMIN".equals(a) || "ROLE_ROOT".equals(a) || "ROLE_ADMINISTRADOR".equals(a)) {
                return true;
            }
            for (String auth : authorities) {
                if (auth.equalsIgnoreCase(a)) {
                    return true;
                }
            }
        }
        return false;
    }

    @ModelAttribute("puedeUsuarios")
    public boolean puedeUsuarios() {
        return tieneAutoridad("admin_usuarios");
    }

    @ModelAttribute("esAdmin")
    public boolean esAdmin() {
        return tieneAutoridad("ROLE_ADMIN", "ROLE_ROOT", "ROLE_ADMINISTRADOR", "admin_usuarios");
    }

    @ModelAttribute("puedeRoles")
    public boolean puedeRoles() {
        return tieneAutoridad("admin_roles");
    }

    @ModelAttribute("puedeAcceso")
    public boolean puedeAcceso() {
        return puedeUsuarios() || puedeRoles();
    }

    @ModelAttribute("puedeDependencias")
    public boolean puedeDependencias() {
        return tieneAutoridad("ver_dependencias", "admin_ccd");
    }

    @ModelAttribute("puedeRadicados")
    public boolean puedeRadicados() {
        return tieneAutoridad("ver_radicados", "crear_radicado");
    }

    @ModelAttribute("puedeSeries")
    public boolean puedeSeries() {
        return tieneAutoridad("admin_ccd");
    }

    @ModelAttribute("puedeSubseries")
    public boolean puedeSubseries() {
        return tieneAutoridad("admin_ccd");
    }

    @ModelAttribute("puedeDocumentos")
    public boolean puedeDocumentos() {
        return tieneAutoridad("gestionar_documentos", "admin_ccd");
    }

    @ModelAttribute("puedeGestionDocumental")
    public boolean puedeGestionDocumental() {
        return puedeSeries() || puedeSubseries() || puedeDocumentos();
    }

    @ModelAttribute("puedeTramites")
    public boolean puedeTramites() {
        return tieneAutoridad("ver_tramites", "gestionar_tramites");
    }

    @ModelAttribute("puedeConsulta")
    public boolean puedeConsulta() {
        return tieneAutoridad("ver_radicados");
    }

    @ModelAttribute("puedeBitacora")
    public boolean puedeBitacora() {
        return tieneAutoridad("ver_bitacora");
    }

    @ModelAttribute("puedeReportes")
    public boolean puedeReportes() {
        return tieneAutoridad("ver_reportes");
    }
}
