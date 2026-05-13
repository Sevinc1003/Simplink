package com.simplink.main.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.simplink.main.entity.IpLogsEntity;

@Repository
public interface IpLogsRepository extends JpaRepository<IpLogsEntity, Long> {

    List<IpLogsEntity> findByUrlId(Long urlId);

    long countByUrlId(Long urlId);

}
