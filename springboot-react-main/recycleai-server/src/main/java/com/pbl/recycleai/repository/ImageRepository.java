package com.pbl.recycleai.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.pbl.recycleai.model.Image;

@Repository
public interface ImageRepository extends JpaRepository<Image, Integer> {
    Optional<Image> findTopByBin_BinIdOrderByImageIdDesc(Integer binId);
}
