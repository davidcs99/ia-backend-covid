package edu.ucacue.xrlab.controller;

import java.awt.image.ImagingOpException;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
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


	
	@Secured({ "ROLE_ADMIN", "ROLE_USER" })
	@PostMapping("/upload")
	public String upload(@RequestParam("archivo") MultipartFile archivo)  {
		ResponseEntity<String> response;
		String respuesta="";

		   try {
	            RestTemplate restTemplate = new RestTemplate();
	            String url = "http://172.16.110.135:5000/file-upload";
	            HttpMethod requestMethod = HttpMethod.POST;

	            HttpHeaders headers = new HttpHeaders();
	            headers.setContentType(MediaType.MULTIPART_FORM_DATA);
	            
				//Convertir multipart file a file
				File convFile = new File(archivo.getOriginalFilename());
				convFile.createNewFile();
				FileOutputStream fos = new FileOutputStream(convFile);
				fos.write(archivo.getBytes());
				fos.close();
				////////////////////////////////////////


	            MultiValueMap<String, String> fileMap = new LinkedMultiValueMap<>();
	            ContentDisposition contentDisposition = ContentDisposition
	                    .builder("form-data")
	                    .name("file")
	                    .filename(convFile.getName())
	                    .build();

	            fileMap.add(HttpHeaders.CONTENT_DISPOSITION, contentDisposition.toString());
	            HttpEntity<byte[]> fileEntity = new HttpEntity<>(Files.readAllBytes(convFile.toPath()), fileMap);

	            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
	            body.add("file", fileEntity);

	            HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

	            response = restTemplate.exchange(url, requestMethod, requestEntity, String.class);
	            
	            
	            respuesta=response.toString();
	            System.out.println("file upload status code: " + response.getStatusCode());

	        } catch (IOException e) {
	            e.printStackTrace();
	        }

			return respuesta;

	}

}

