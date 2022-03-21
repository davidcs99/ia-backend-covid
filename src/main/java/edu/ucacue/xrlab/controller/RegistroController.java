package edu.ucacue.xrlab.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


import edu.ucacue.xrlab.infraestructura.repositorio.RegistroRepositorio;

import edu.ucacue.xrlab.modelo.Registro;


@RestController
@RequestMapping("/api")
public class RegistroController {


	@Autowired
	RegistroRepositorio registroRepositorio;
	
	@GetMapping("/registros")
	public List<Registro>buscaRegistros(){
		return registroRepositorio.findAll();
	}
	
	@PostMapping("/registro")
	public ResponseEntity<?> create(@RequestBody Registro registro, BindingResult result){
		Registro registroNuevo = null;
		Map<String, Object> response =new HashMap<String, Object>();
		
		if (result.hasErrors()) {

			List<String> errors = result.getFieldErrors().stream()
					.map(err -> "El campo '" + err.getField() + "' " + err.getDefaultMessage())
					.collect(Collectors.toList());

			response.put("errors", errors);
			return new ResponseEntity<Map<String, Object>>(response, HttpStatus.BAD_REQUEST);
		}

		try {
			registroNuevo = registroRepositorio.save(registro);
		} catch (DataAccessException e) {
			response.put("mensaje", "Error al realizar el insert en la base de datos");
			response.put("error", e.getMessage().concat(": ").concat(e.getMostSpecificCause().getMessage()));
			return new ResponseEntity<Map<String, Object>>(response, HttpStatus.INTERNAL_SERVER_ERROR);
		}

		response.put("mensaje", "El ingreso de personas se ha realizado con exito creado con éxito!");
		response.put("cliente", registroNuevo);
		return new ResponseEntity<Map<String, Object>>(response, HttpStatus.CREATED);
	
	}
	
}
