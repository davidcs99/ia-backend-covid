package edu.ucacue.xrlab.infraestructura.repositorio.login;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import edu.ucacue.xrlab.modelo.Login.Role;



public interface RoleRepositorio extends JpaRepository<Role, Long>{

	
    @Query("select r from Role r where r.nombre like :roleName")
    Role findRoleByName(@Param("roleName") String roleName);
}
