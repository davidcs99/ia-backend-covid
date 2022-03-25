package edu.ucacue.xrlab.controller;


import java.util.ArrayList;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;



import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.oauth2.provider.approval.ApprovalStore;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import edu.ucacue.xrlab.infraestructura.repositorio.login.RoleRepositorio;
import edu.ucacue.xrlab.infraestructura.repositorio.login.UserRepository;
import edu.ucacue.xrlab.infraestructura.services.login.IUsuarioService;

import edu.ucacue.xrlab.modelo.Login.Role;
import edu.ucacue.xrlab.modelo.Login.Usuario;



@CrossOrigin(origins = { "http://localhost:4200" })
@RestController
@RequestMapping("/api")
public class UsuarioRestController {

	@Autowired
	private IUsuarioService usuarioService;

	@Autowired
	private RoleRepositorio roleRepositorio;

	@Autowired
	private BCryptPasswordEncoder passwordEncoder;

	
	@Autowired
	TokenStore tokenStore;
	
	@Autowired
	ApprovalStore approvalStore;

	// private final Logger log =
	// LoggerFactory.getLogger(ClienteRestController.class);

	
	@PostMapping("/usuario")
	public ResponseEntity<?> create(@RequestBody Usuario usuario, BindingResult result) throws Exception {
		Usuario usuarioNew = null;
		
		Map<String, Object> response = new HashMap<>();

		if (result.hasErrors()) {

			List<String> errors = result.getFieldErrors().stream()
					.map(err -> "El campo '" + err.getField() + "' " + err.getDefaultMessage())
					.collect(Collectors.toList());

			response.put("errors", errors);
			return new ResponseEntity<Map<String, Object>>(response, HttpStatus.BAD_REQUEST);
		}

		try {
			String passwordBcrypt = passwordEncoder.encode(usuario.getPassword());

			usuario.setEstado(true);
			usuario.setEstadoTokenRegistro(false);
			usuario.setRevocarToken(false);
			usuario.setPassword(passwordBcrypt);
			String emailEncripted = passwordEncoder.encode(usuario.getEmail());
			emailEncripted = emailEncripted.replace("/", "J");
			
			usuario.setEmailEncripted(emailEncripted);
			usuarioNew = usuarioService.save(usuario);
			
			

		} catch (DataAccessException e) {
			response.put("mensaje", "Su cédula o correo electrónico ya existen en nuestro sistema");
			response.put("error", e.getMessage().concat(": ").concat(e.getMostSpecificCause().getMessage()));
			return new ResponseEntity<Map<String, Object>>(response, HttpStatus.INTERNAL_SERVER_ERROR);
		}

		response.put("mensaje", "El cliente ha sido creado con éxito!");
		response.put("cliente", usuarioNew);
		

		return new ResponseEntity<Map<String, Object>>(response, HttpStatus.CREATED);
	}
	@Autowired
	UserRepository userRepository;
	
	@Secured({ "ROLE_ADMIN", "ROLE_USER" })
	@GetMapping("/userss")
	public List<Usuario> buscarUsuarios() {
		return userRepository.findAll();
	}
	@Secured({ "ROLE_ADMIN", "ROLE_USER" })
	@GetMapping("/usuario/{email}")
	public Usuario showFacturas(@PathVariable String email) {

		Usuario usuario = new Usuario();
		Map<String, Object> response = new HashMap<>();

		try {
			usuario = usuarioService.findByEmail(email);
		} catch (DataAccessException e) {
			response.put("mensaje", "Error al realizar la consulta en la base de datos");
			response.put("error", e.getMessage().concat(": ").concat(e.getMostSpecificCause().getMessage()));
		}

		return usuario;
	}

	@Secured({ "ROLE_ADMIN", "ROLE_USER" })
	@PutMapping("/usuario/{email}")
	public ResponseEntity<?> update(@RequestBody Usuario usuario, BindingResult result, @PathVariable String email) {

		Usuario usuarioActual = usuarioService.findByEmail(email);

		Usuario usuarioUpdated = null;

		Map<String, Object> response = new HashMap<>();

		if (result.hasErrors()) {

			List<String> errors = result.getFieldErrors().stream()
					.map(err -> "El campo '" + err.getField() + "' " + err.getDefaultMessage())
					.collect(Collectors.toList());

			response.put("errors", errors);
			return new ResponseEntity<Map<String, Object>>(response, HttpStatus.BAD_REQUEST);
		}

		if (usuarioActual == null) {
			response.put("mensaje", "Error: no se pudo editar, el cliente ID: "
					.concat(email.toString().concat(" no existe en la base de datos!")));
			return new ResponseEntity<Map<String, Object>>(response, HttpStatus.NOT_FOUND);
		}

		try {

			usuarioActual.setEmail(usuario.getEmail());
			usuarioActual.setApellido(usuario.getApellido());
			usuarioActual.setNombre(usuario.getNombre());
			
			usuarioActual.setTelefono(usuario.getTelefono());
			usuarioActual.setDireccion(usuario.getDireccion());
			usuarioActual.setEstado(usuario.getEstado());
			if(usuarioActual.getEstadoTokenRegistro()==null)
			{
				usuarioActual.setEstadoTokenRegistro(false);
			}
			if(!usuarioActual.getEstado())
			{
				usuarioActual.setRevocarToken(true);
			}

			usuarioUpdated = usuarioService.save(usuarioActual);

		} catch (DataAccessException e) {
			response.put("mensaje", "Error al actualizar el cliente en la base de datos");
			response.put("error", e.getMessage().concat(": ").concat(e.getMostSpecificCause().getMessage()));
			return new ResponseEntity<Map<String, Object>>(response, HttpStatus.INTERNAL_SERVER_ERROR);
		}

		response.put("mensaje", "El cliente ha sido actualizado con éxito!");
		response.put("cliente", usuarioUpdated);

		return new ResponseEntity<Map<String, Object>>(response, HttpStatus.CREATED);
	}

	/// actualizar roles del usuario
	@Secured({ "ROLE_ADMIN" })
	@PutMapping("/usuario/roles/{email}")
	public ResponseEntity<?> updateRolesUsuario(@RequestBody List<String> rolesString, BindingResult result,
			@PathVariable String email) {

		Usuario usuarioActual = usuarioService.findByEmail(email);


		Usuario usuarioUpdated = null;


		List<Role> allRoles = roleRepositorio.findAll();
		List<Role> selectedRoles = new ArrayList<Role>();

		Map<String, Object> response = new HashMap<>();

		if (result.hasErrors()) {

			List<String> errors = result.getFieldErrors().stream()
					.map(err -> "El campo '" + err.getField() + "' " + err.getDefaultMessage())
					.collect(Collectors.toList());

			response.put("errors", errors);
			return new ResponseEntity<Map<String, Object>>(response, HttpStatus.BAD_REQUEST);
		}

		if (usuarioActual == null) {
			response.put("mensaje", "Error: no se pudo editar, el cliente ID: "
					.concat(email.toString().concat(" no existe en la base de datos!")));
			return new ResponseEntity<Map<String, Object>>(response, HttpStatus.NOT_FOUND);
		}

		try {

			for (Role role : allRoles) {

				for (String roleString : rolesString) {

					if (role.getNombre().equals(roleString)) {
						selectedRoles.add(role);
					}

				}

			}
			usuarioActual.setRoles(selectedRoles);
			usuarioActual.setRevocarToken(true);

			usuarioUpdated = usuarioService.save(usuarioActual);
			// tokenServices.revokeToken(token);

		} catch (DataAccessException e) {
			response.put("mensaje", "Error al actualizar el cliente en la base de datos");
			response.put("error", e.getMessage().concat(": ").concat(e.getMostSpecificCause().getMessage()));
			return new ResponseEntity<Map<String, Object>>(response, HttpStatus.INTERNAL_SERVER_ERROR);
		}

		response.put("mensaje", "El cliente ha sido actualizado con éxito!");
		response.put("cliente", usuarioUpdated);

		return new ResponseEntity<Map<String, Object>>(response, HttpStatus.CREATED);
	}

	/// Este metodo es para confirmar el login del usuario
	@GetMapping("/usuario/actualizar/{emailCripted}")
	public ResponseEntity<?> updateUserEstado(@PathVariable String emailCripted) {
		Usuario usuarioActual = usuarioService.findByEmailCri(emailCripted);

		Usuario usuarioUpdated = null;

		Map<String, Object> response = new HashMap<>();

		if (usuarioActual == null) {
			response.put("mensaje",
					"Error: no se pudo editar, el cliente ID: ".concat(" no existe en la base de datos!"));
			return new ResponseEntity<Map<String, Object>>(response, HttpStatus.NOT_FOUND);
		}

		if (usuarioActual.getEstadoTokenRegistro()) {
			response.put("mensaje", "El Token está caducado");
			return new ResponseEntity<Map<String, Object>>(response, HttpStatus.NOT_FOUND);
		}

		try {

			usuarioActual.setEstado(true);
			usuarioActual.setEstadoTokenRegistro(true);
			usuarioUpdated = usuarioService.save(usuarioActual);

		} catch (DataAccessException e) {
			response.put("mensaje", "Error al actualizar el cUsuario en la base de datos");
			response.put("error", e.getMessage().concat(": ").concat(e.getMostSpecificCause().getMessage()));
			return new ResponseEntity<Map<String, Object>>(response, HttpStatus.INTERNAL_SERVER_ERROR);
		}

		response.put("mensaje", "El cliente ha sido actualizado con éxito!");
		response.put("cliente", usuarioUpdated);

		return new ResponseEntity<Map<String, Object>>(response, HttpStatus.CREATED);
	}

	/// Este metodo es para cambiar passwor del usuario
	@Secured({ "ROLE_ADMIN", "ROLE_USER" })
	@PutMapping("/usuario/{email}/{passwordNuevo}")
	public ResponseEntity<?> updatePassword(@RequestBody String passwordAnterior, BindingResult result,
			@PathVariable String email, @PathVariable String passwordNuevo) {

		Usuario usuarioActual = usuarioService.findByEmail(email);
		passwordNuevo = passwordEncoder.encode(passwordNuevo);
		// passwordAnterior= passwordAnterior;
		String passworUsuarioActual = usuarioActual.getPassword();

		Usuario usuarioUpdated = null;

		Map<String, Object> response = new HashMap<>();

		if (result.hasErrors()) {

			List<String> errors = result.getFieldErrors().stream()
					.map(err -> "El campo '" + err.getField() + "' " + err.getDefaultMessage())
					.collect(Collectors.toList());

			response.put("errors", errors);
			return new ResponseEntity<Map<String, Object>>(response, HttpStatus.BAD_REQUEST);
		}
		System.out.println(passwordAnterior);
		System.out.println(passworUsuarioActual);
		if (!passwordEncoder.matches(passwordAnterior, passworUsuarioActual)) {
			response.put("mensaje", "Password Incorrecto");
			return new ResponseEntity<Map<String, Object>>(response, HttpStatus.NOT_FOUND);
		}

		try {

			usuarioActual.setPassword(passwordNuevo);

			usuarioUpdated = usuarioService.save(usuarioActual);

		} catch (DataAccessException e) {
			response.put("mensaje", "Error al actualizar el cliente en la base de datos");
			response.put("error", e.getMessage().concat(": ").concat(e.getMostSpecificCause().getMessage()));
			return new ResponseEntity<Map<String, Object>>(response, HttpStatus.INTERNAL_SERVER_ERROR);
		}

		response.put("mensaje", "El cliente ha sido actualizado con éxito!");
		response.put("cliente", usuarioUpdated);

		return new ResponseEntity<Map<String, Object>>(response, HttpStatus.ACCEPTED);
	}

	//@Secured({ "ROLE_ADMIN" })
	@GetMapping("/usuarios/{terminoBusqueda}")
	@ResponseStatus(HttpStatus.OK)
	public List<Usuario> filtrarUsuarios(@PathVariable String terminoBusqueda) {
		return usuarioService.findByNombreAndApellido(terminoBusqueda);
	}

	@Secured({ "ROLE_ADMIN" })
	@GetMapping("/roles")
	@ResponseStatus(HttpStatus.OK)
	public List<Role> findAllRoles() {
		return roleRepositorio.findAll();
	}
	
	
	
	public static String alphaNumericString(int len) {
	    String AB = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
	    Random rnd = new Random();

	    StringBuilder sb = new StringBuilder(len);
	    for (int i = 0; i < len; i++) {
	        sb.append(AB.charAt(rnd.nextInt(AB.length())));
	    }
	    return sb.toString();
	}
	
	
	
}