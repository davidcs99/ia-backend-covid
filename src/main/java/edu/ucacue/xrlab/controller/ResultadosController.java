package edu.ucacue.xrlab.controller;

import java.awt.image.ImagingOpException;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
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
	public List<Resultados> buscaResultados() {
		return resultadosRepositorio.findAll();
	}

	@PostMapping("/resultado")
	public ResponseEntity<?> create(@RequestBody Resultados resultado, BindingResult result) {
		Resultados resultadosNuevo = null;
		Map<String, Object> response = new HashMap<String, Object>();

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

		response.put("mensaje",
				"El ingreso de los resultados obtenidos por el IA se ha realizado con exito creado con éxito!");
		response.put("cliente", resultadosNuevo);
		return new ResponseEntity<Map<String, Object>>(response, HttpStatus.CREATED);

	}

	// @Secured({ "ROLE_ADMIN", "ROLE_USER" })
	@PostMapping("/upload")
	public ResponseEntity<?> upload(@RequestParam("archivo") MultipartFile archivo) {
		Map<String, Object> response = new HashMap<>();

		try {
			File targetFile = new File(archivo.getOriginalFilename());

			FileOutputStream fos = new FileOutputStream(targetFile);
			fos.write(archivo.getBytes());

			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_JSON);

			

			//File filee = new File("C:\\Users\\David\\eclipse-workspace\\ai-back-covid\\src\\main\\resources\\input.txt");
			//File filee = new File(archivo.getOriginalFilename());
			File temp = File.createTempFile
				      (archivo.getName(),".jpg");
				      temp.deleteOnExit();
			archivo.transferTo(temp);
			
			MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
			body.add("file", temp);
			HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

			// String archivo = null;
			String uri = "http://172.16.110.134:5000/file-upload";
			RestTemplate restTemplate = new RestTemplate();
			// headers.setContentType(MediaType.APPLICATION_JSON);
			// HttpEntity<MultipartFile> httpEntity = new HttpEntity<>(archivo, headers);

			// HttpEntity<MultipartFile> entity = new HttpEntity<>(archivo, headers);

			if (!archivo.isEmpty()) {

				//String nombreArchivo = null;

				// HttpEntity<MultipartFile> httpEntity = new HttpEntity<>(archivo, headers);

				// body.add("file", archivo);
				// nombreArchivo = uploadService.copiar(archivo);

				restTemplate.postForEntity(uri, requestEntity, String.class);
//				//restTemplate.postForObject(uri, requestEntity, String.class);
				response.put("mensaje", "Has subido correctamente la imagen: ");

			}

		} catch (ImagingOpException e) {
			response.put("mensaje", "Error al subir la imagen del producto");
			response.put("error", e.getMessage().concat(": ").concat(e.getCause().getMessage()));
			return new ResponseEntity<Map<String, Object>>(response, HttpStatus.INTERNAL_SERVER_ERROR);
		} catch (IOException io) {
			System.out.println(io.toString());
		}

		return new ResponseEntity<Map<String, Object>>(response, HttpStatus.CREATED);
	}

}
