package com.pbl.recycleai.controller;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import com.pbl.recycleai.model.Bin;
import com.pbl.recycleai.model.Image;
import com.pbl.recycleai.repository.ImageRepository;
import com.pbl.recycleai.repository.BinRepository; 
import org.springframework.messaging.simp.SimpMessagingTemplate;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api")
public class ImageController {

    private static final String CARPETA_SUBIDAS = "C:/RecyclAI_Images/";
    private final ImageRepository imageRepository;
    private final BinRepository binRepository; 
    private final SimpMessagingTemplate messagingTemplate;

    public ImageController(ImageRepository imageRepository, BinRepository binRepository, SimpMessagingTemplate messagingTemplate) {
        this.imageRepository = imageRepository;
        this.binRepository = binRepository;
        this.messagingTemplate = messagingTemplate;
    }

    @PostMapping("/upload")
    public ResponseEntity<String> subirImagen(
            @RequestPart("file") MultipartFile file,
            @RequestParam(value = "binId", defaultValue = "1") Integer binId) {
        try {
            Bin binReal = binRepository.findById(binId).orElse(null);
            
            if (binReal == null) {
                return ResponseEntity.status(404).body("Error: El Bin con ID " + binId + " no existe en la base de datos.");
            }

            Path directorio = Paths.get(CARPETA_SUBIDAS);
            if (!Files.exists(directorio)) {
                Files.createDirectories(directorio);
            }

            String nombreOriginal = file.getOriginalFilename();
            String nombreUnico = System.currentTimeMillis() + "_" + nombreOriginal;
            Path rutaArchivo = directorio.resolve(nombreUnico);
            Files.write(rutaArchivo, file.getBytes());

            Image imagen = new Image();
            imagen.setPath("/images/" + nombreUnico);
            
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            imagen.setUploadDate(LocalDateTime.now().format(formatter));
            
            imagen.setBin(binReal); 

            imageRepository.save(imagen);
            messagingTemplate.convertAndSend("/topic/images/" + binId, imagen); 

            return ResponseEntity.ok("Imagen guardada correctamente en Bin ID: " + binId);

        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Error al escribir archivo: " + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Error de Base de Datos: " + e.getMessage());
        }
    }

    @GetMapping("/images/latest/{binId}")
    public ResponseEntity<Image> obtenerUltimaImagenPorBin(@PathVariable("binId") Integer binId) {
        return imageRepository.findTopByBin_BinIdOrderByImageIdDesc(binId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}