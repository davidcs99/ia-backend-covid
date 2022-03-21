package edu.ucacue.xrlab.infraestructura.repositorio.login;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import edu.ucacue.xrlab.modelo.Login.Usuario;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<Usuario, Integer> {

    //Optional<Usuario> findByUsername(String username);
    Optional<Usuario> findByEmail(String email);
    Optional<Usuario> findByEmailEncripted(String email);
    
  
	@Query("SELECT u FROM Usuario u  WHERE "
			+ "LOWER(u.nombre) LIKE LOWER(CONCAT('%',:terminoBusqueda, '%')) OR "
			+ "LOWER(u.apellido) LIKE LOWER(CONCAT('%',:terminoBusqueda, '%')) OR "
			+ "LOWER(u.cedula) LIKE LOWER(CONCAT('%',:terminoBusqueda, '%')) OR "
			+ "LOWER(u.email) LIKE LOWER(CONCAT('%',:terminoBusqueda, '%'))")
    List<Usuario> findByNombreAndApellido(String terminoBusqueda);
	
	@Query("select NEW Usuario (u.nombre, u.apellido, u.email, u.telefono) FROM Usuario u  WHERE u.cedula = :cedula and u.estado=true")
    List<Usuario> getUsuarioByCedula(String cedula);
	
	@Query("select u FROM Usuario u  WHERE u.cedula=:cedula and u.email=:email")
    Usuario findUsuarioByCedulaAndEmail(String cedula, String email);
	
	
	
}