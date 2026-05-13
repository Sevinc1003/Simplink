package com.simplink.main.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.simplink.main.entity.IpLog;

@Repository
public interface IpLogRepository extends JpaRepository<IpLog, Long> {

    List<IpLog> findByUrlId(Long urlId);

    long countByUrlId(Long urlId);

}
