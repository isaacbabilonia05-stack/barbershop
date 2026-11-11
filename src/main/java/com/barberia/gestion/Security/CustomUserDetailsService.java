package com.barberia.gestion.Security;

import com.barberia.gestion.Repository.AdministradorRepository;
import com.barberia.gestion.Repository.BarberoRepository;
import com.barberia.gestion.Repository.ClienteRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/** Busca al usuario en MongoDB: primero administradores, luego barberos y por ultimo clientes. */
@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final AdministradorRepository administradorRepository;
    private final BarberoRepository barberoRepository;
    private final ClienteRepository clienteRepository;

    public CustomUserDetailsService(AdministradorRepository administradorRepository,
                                    BarberoRepository barberoRepository,
                                    ClienteRepository clienteRepository) {
        this.administradorRepository = administradorRepository;
        this.barberoRepository = barberoRepository;
        this.clienteRepository = clienteRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        String correo = email == null ? "" : email.trim();

        var admin = administradorRepository.findByEmail(correo);
        if (admin.isPresent()) {
            var a = admin.get();
            return new UsuarioPrincipal(a.getId(), a.getNombre(), a.getEmail(), a.getPassword(),
                    "ADMIN", !Boolean.FALSE.equals(a.getActivo()));
        }

        var barbero = barberoRepository.findByEmail(correo);
        if (barbero.isPresent()) {
            var b = barbero.get();
            return new UsuarioPrincipal(b.getId(), b.getNombre(), b.getEmail(), b.getPassword(),
                    "BARBERO", !Boolean.FALSE.equals(b.getActivo()));
        }

        var cliente = clienteRepository.findByEmail(correo);
        if (cliente.isPresent()) {
            var c = cliente.get();
            return new UsuarioPrincipal(c.getId(), c.getNombre(), c.getEmail(), c.getPassword(),
                    "CLIENTE", true);
        }

        throw new UsernameNotFoundException("Usuario no encontrado");
    }
}
