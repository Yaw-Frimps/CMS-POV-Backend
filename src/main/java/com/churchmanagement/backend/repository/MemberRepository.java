package com.churchmanagement.backend.repository;

import com.churchmanagement.backend.model.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

@Repository
public interface MemberRepository extends JpaRepository<Member, Long> {
    Optional<Member> findByUserId(Long userId);

    @Query("SELECT m FROM Member m WHERE m.dateOfBirth IS NOT NULL AND " +
           "EXTRACT(MONTH FROM m.dateOfBirth) = :month AND " +
           "EXTRACT(DAY FROM m.dateOfBirth) = :day")
    List<Member> findByBirthdayMonthAndDay(@Param("month") int month, @Param("day") int day);
}
