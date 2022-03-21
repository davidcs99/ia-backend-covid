package edu.ucacue.xrlab.infraestructura.repositorio;

import org.springframework.data.jpa.repository.JpaRepository;

import edu.ucacue.xrlab.modelo.Registro;

public interface RegistroRepositorio extends JpaRepository<Registro, Integer> {

}
