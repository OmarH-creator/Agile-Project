package com.university.backend.repository;

import com.university.backend.entity.Announcement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AnnouncementRepository extends JpaRepository<Announcement, Long> {

    // Find all announcements ordered by timestamp descending (newest first)
    List<Announcement> findAllByOrderByTimestampDesc();
}
