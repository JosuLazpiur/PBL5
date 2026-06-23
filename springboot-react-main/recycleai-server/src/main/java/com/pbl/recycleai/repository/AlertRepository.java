package com.pbl.recycleai.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.pbl.recycleai.model.Alert;

public interface AlertRepository extends JpaRepository<Alert, Integer>{
    List<Alert> findByBin_BinIdOrderByDatetimeDesc(Integer binId);
    Alert findTopByBin_BinIdOrderByAlertIdDesc(Integer binId);
}
