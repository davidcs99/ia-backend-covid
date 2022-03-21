package edu.ucacue.xrlab.controller;

import java.awt.image.ImagingOpException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import edu.ucacue.xrlab.infraestructura.repositorio.ResultadosRepositorio;
import edu.ucacue.xrlab.modelo.Resultados;


@RestController
@RequestMapping("/api")
public class ResultadosController {


	@Autowired
	ResultadosRepositorio resultadosRepositorio;
	
	@Secured("ROLE_USER")
	@GetMapping("/resultados")
	public List<Resultados>buscaResultados(){
		return resultadosRepositorio.findAll();
	}
	
	@PostMapping("/resultado")
	public ResponseEntity<?> create(@RequestBody Resultados resultado, BindingResult result){
		Resultados resultadosNuevo = null;
		Map<String, Object> response =new HashMap<String, Object>();
		
		if (result.hasErrors()) {

			List<String> errors = result.getFieldErrors().stream()
					.map(err -> "El campo '" + err.getField() + "' " + err.getDefaultMessage())
					.collect(Collectors.toList());

			response.put("errors", errors);
			return new ResponseEntity<Map<String, Object>>(response, HttpStatus.BAD_REQUEST);
		}

		try {
			resultadosNuevo = resultadosRepositorio.save(resultado);
		} catch (DataAccessException e) {
			response.put("mensaje", "Error al realizar el insert en la base de datos");
			response.put("error", e.getMessage().concat(": ").concat(e.getMostSpecificCause().getMessage()));
			return new ResponseEntity<Map<String, Object>>(response, HttpStatus.INTERNAL_SERVER_ERROR);
		}

		response.put("mensaje", "El ingreso de los resultados obtenidos por el IA se ha realizado con exito creado con éxito!");
		response.put("cliente", resultadosNuevo);
		return new ResponseEntity<Map<String, Object>>(response, HttpStatus.CREATED);
	
	}
	//@Secured({ "ROLE_ADMIN", "ROLE_USER" })
		@PostMapping("/upload")
		public ResponseEntity<?> upload(@RequestParam("archivo") MultipartFile archivo ){
			Map<String, Object> response = new HashMap<>();

			

			if (!archivo.isEmpty()) {

				String nombreArchivo = null;
				try {
					// nombreArchivo = uploadService.copiar(archivo);
					
				} catch (ImagingOpException e) {
					response.put("mensaje", "Error al subir la imagen del producto");
					response.put("error", e.getMessage().concat(": ").concat(e.getCause().getMessage()));
					return new ResponseEntity<Map<String, Object>>(response, HttpStatus.INTERNAL_SERVER_ERROR);
				}

				// String nombreFotoAnterior = tipoNegocio.getFoto();

				// uploadService.eliminar(nombreFotoAnterior);

				

			
				
				response.put("mensaje", "Has subido correctamente la imagen: " + nombreArchivo);

			}

			return new ResponseEntity<Map<String, Object>>(response, HttpStatus.CREATED);
		}
}