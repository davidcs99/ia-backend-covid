package edu.ucacue.xrlab.infraestructura.services.login;

import java.util.List;

import edu.ucacue.xrlab.modelo.Login.Usuario;


public interface IUsuarioService {

	public Usuario findByEmail(String email);
	public Usuario findByEmailCri(String emailCipted);
	public Usuario save(Usuario usuario);
	public List<Usuario> findByNombreAndApellido(String terminoBusqueda);
	
    public Usuario findUsuarioByCedulaAndEmail(String cedula, String email);
}
