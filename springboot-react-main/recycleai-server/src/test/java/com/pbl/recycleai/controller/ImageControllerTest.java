package com.pbl.recycleai.controller;

import com.pbl.recycleai.model.Bin;
import com.pbl.recycleai.model.Image;
import com.pbl.recycleai.repository.BinRepository;
import com.pbl.recycleai.repository.ImageRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ImageControllerTest {

    @Mock
    private ImageRepository imageRepository;

    @Mock
    private BinRepository binRepository;

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    @InjectMocks
    private ImageController imageController;

    @Test
    void whenGetLastImage_thenReturnImage() {
        Image image = new Image();
        image.setImageId(1);
        image.setPath("/path/to/image.jpg");

        when(imageRepository.findTopByBin_BinIdOrderByImageIdDesc(1)).thenReturn(Optional.of(image));

        ResponseEntity<Image> response = imageController.obtenerUltimaImagenPorBin(1);

        Assertions.assertEquals(200, response.getStatusCode().value());
        Assertions.assertEquals(image, response.getBody());
    }

    @Test
    void whenGetLastImageNotFound_thenReturn404() {
        when(imageRepository.findTopByBin_BinIdOrderByImageIdDesc(1)).thenReturn(Optional.empty());

        ResponseEntity<Image> response = imageController.obtenerUltimaImagenPorBin(1);

        Assertions.assertEquals(404, response.getStatusCode().value());
    }

    @Test
    void whenUploadImageBinNotFound_thenReturn404() {
        MultipartFile file = Mockito.mock(MultipartFile.class);
        when(binRepository.findById(1)).thenReturn(Optional.empty());

        ResponseEntity<String> response = imageController.subirImagen(file, 1);

        Assertions.assertEquals(404, response.getStatusCode().value());
        Assertions.assertTrue(response.getBody().contains("no existe"));
    }

    @Test
    void whenUploadImageSuccess_thenReturn200() throws IOException {
        MultipartFile file = Mockito.mock(MultipartFile.class);
        when(file.getOriginalFilename()).thenReturn("test.jpg");
        when(file.getBytes()).thenReturn(new byte[]{1, 2, 3});

        Bin bin = new Bin();
        bin.setBinId(1);
        when(binRepository.findById(1)).thenReturn(Optional.of(bin));

        try (MockedStatic<Files> mockedFiles = Mockito.mockStatic(Files.class);
             MockedStatic<Paths> mockedPaths = Mockito.mockStatic(Paths.class)) {
            
            Path path = Mockito.mock(Path.class);
            mockedPaths.when(() -> Paths.get(any(String.class))).thenReturn(path);
            
            // Stub resolve to return the same mock path or another mock
            when(path.resolve(any(String.class))).thenReturn(path);
            
            mockedFiles.when(() -> Files.exists(path)).thenReturn(true);
            mockedFiles.when(() -> Files.write(any(Path.class), any(byte[].class))).thenReturn(path);

            ResponseEntity<String> response = imageController.subirImagen(file, 1);

            Assertions.assertEquals(200, response.getStatusCode().value());
            Mockito.verify(imageRepository).save(any(Image.class));
            Mockito.verify(messagingTemplate).convertAndSend(any(String.class), any(Image.class));
        }
    }
    
    @Test
    void whenUploadImageIOException_thenReturn500() throws IOException {
        MultipartFile file = Mockito.mock(MultipartFile.class);
        when(file.getOriginalFilename()).thenReturn("test.jpg");
        when(file.getBytes()).thenReturn(new byte[]{1, 2, 3});

        Bin bin = new Bin();
        bin.setBinId(1);
        when(binRepository.findById(1)).thenReturn(Optional.of(bin));

        try (MockedStatic<Files> mockedFiles = Mockito.mockStatic(Files.class);
             MockedStatic<Paths> mockedPaths = Mockito.mockStatic(Paths.class)) {
            
            Path path = Mockito.mock(Path.class);
            mockedPaths.when(() -> Paths.get(any(String.class))).thenReturn(path);
            when(path.resolve(any(String.class))).thenReturn(path);
            
            mockedFiles.when(() -> Files.exists(path)).thenReturn(true);
            mockedFiles.when(() -> Files.write(any(Path.class), any(byte[].class))).thenThrow(new IOException("Disk error"));

            ResponseEntity<String> response = imageController.subirImagen(file, 1);

            Assertions.assertEquals(500, response.getStatusCode().value());
            Assertions.assertTrue(response.getBody().contains("Error al escribir archivo"));
        }
    }
}