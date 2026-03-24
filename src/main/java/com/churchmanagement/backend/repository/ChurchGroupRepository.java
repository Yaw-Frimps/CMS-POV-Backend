package com.churchmanagement.backend.repository;

import com.churchmanagement.backend.model.ChurchGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ChurchGroupRepository extends JpaRepository<ChurchGroup, Long> {
}
