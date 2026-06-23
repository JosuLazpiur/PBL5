package com.pbl.recycleai.controller;

import java.util.List;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.pbl.recycleai.model.Domain;
import com.pbl.recycleai.repository.DomainRepository;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api")
public class DomainController {

    private final DomainRepository domainRepository;

    public DomainController(DomainRepository domainRepository) {
        this.domainRepository = domainRepository;
    }

    @GetMapping("/domain")   
    public List<Domain> domainList(){
        return domainRepository.findAll();
    }

    @PostMapping(value = "/domain", 
        consumes = { MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE }, 
        produces = { MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE })
    public Domain saveUser(@RequestBody Domain domain){
        return domainRepository.save(domain);
    }
}
