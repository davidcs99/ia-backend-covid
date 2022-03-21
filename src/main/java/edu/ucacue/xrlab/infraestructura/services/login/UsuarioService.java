package edu.ucacue.xrlab.infraestructura.services.login;

import java.util.Date;
import java.util.List;

import java.util.stream.Collectors;



import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import edu.ucacue.xrlab.infraestructura.repositorio.login.RoleRepositorio;
import edu.ucacue.xrlab.infraestructura.repositorio.login.UserRepository;
import edu.ucacue.xrlab.modelo.Login.Role;
import edu.ucacue.xrlab.modelo.Login.Usuario;




@Service
public class UsuarioService implements IUsuarioService, UserDetailsService{
	
	private Logger logger = LoggerFactory.getLogger(UsuarioService.class);

	@Autowired
	private UserRepository userRepository;
	
	@Autowired
	private RoleRepositorio roleRepositorio;
	
	
	//En este método utilizamos Spring security para consultar en la BD si el Usuario Existe
	// Y si existe lo combertimos en un UserDetails Que permite trabajar con las seguridades de SpringBoot
	@Override
	@Transactional
	public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
		
		Usuario usuario = userRepository.findByEmail(email).get();
		
		if(usuario == null) {
			logger.error("Error en el login: no existe el usuario '"+email+"' en el sistema!");
			throw new UsernameNotFoundException("Error en el login: no existe el usuario '"+email+"' en el sistema!");
		}
		
		List<GrantedAuthority> authorities = usuario.getRoles()
				.stream()
				.map(role -> new SimpleGrantedAuthority(role.getNombre()))
				.peek(authority -> logger.info("Role: " + authority.getAuthority()))
				.collect(Collectors.toList());
				//Ojo este User de Spring security, me permite administrar la seguridad de acceso a los recursos web
		     //new User(username, password, enabled, accountNonExpired, credentialsNonExpired, accountNonLocked, authorities)
		if(usuario.getRevocarToken()==true)
		{
			usuario.setRevocarToken(false);
			userRepository.save(usuario);
		}
		return new User(usuario.getEmail(), usuario.getPassword(), usuario.getEstado(), true, true, true, authorities);
	}

	@Override
	@Transactional(readOnly=true)
	public Usuario findByEmail(String email) {
		return userRepository.findByEmail(email).get();
	}

	@Override
	public Usuario save(Usuario usuario)   {
		
		if(usuario.getFechaCreacion()== null) {
			usuario.setFechaCreacion(new Date());
		}
		
		if(usuario.getRoles().size()== 0)
		{
		Role role = roleRepositorio.findRoleByName("ROLE_USER");
		usuario.addRole(role);
		}

		return userRepository.save(usuario);
	}
	
	@SuppressWarnings("unused")
	private Usuario usuarioByMail(String email)
	{
		return userRepository.findByEmail(email).get();
	}

	@Override
	public Usuario findByEmailCri(String emailCipted) {
		// TODO Auto-generated method stub
		return userRepository.findByEmailEncripted(emailCipted).get();
	}

	@Override
	public List<Usuario> findByNombreAndApellido(String terminoBusqueda) {
		// TODO Auto-generated method stub
		return userRepository.findByNombreAndApellido(terminoBusqueda);
	}

	
	

	@Override
	public Usuario findUsuarioByCedulaAndEmail(String cedula, String email) {
		// TODO Auto-generated method stub
		return userRepository.findUsuarioByCedulaAndEmail(cedula, email);
	}

}
