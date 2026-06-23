package com.pbl.recycleai.repository;

import com.pbl.recycleai.model.Log;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface LogRepository extends JpaRepository<Log, Integer>{

    List<Log> findTop10ByBin_BinIdOrderByDatetimeDesc(Integer binId);
    Log findTopByBin_BinIdOrderByLogIdDesc(Integer binId);
}
