package com.pbl.recycleai.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.pbl.recycleai.model.Bin;
import com.pbl.recycleai.model.Domain;

public interface BinRepository extends JpaRepository<Bin, Integer>{
    List<Bin> findByDomain(Domain domain);
}
