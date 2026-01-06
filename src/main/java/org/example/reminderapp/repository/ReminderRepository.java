package org.example.reminderapp.repository;

import org.example.reminderapp.entity.Reminder;
import org.example.reminderapp.entity.enums.Status;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.util.List;

public interface ReminderRepository extends JpaRepository<Reminder, Long>,
        JpaSpecificationExecutor<Reminder> {
    List<Reminder> findAllByRemindAtBeforeAndStatus(OffsetDateTime dateTime,
                                                    Status status);

    Page<Reminder> findByUserIdAndTitleContainingOrDescriptionContaining(Long userId,
                                                                         String title,
                                                                         String description,
                                                                         Pageable pageable);

    Page<Reminder> findByUserId(Long userId, Pageable pageable);


    @Query("SELECT r " +
           "FROM Reminder r " +
           "WHERE r.user.id = :userId AND CAST(r.remindAt AS LocalDate) = :date")
    Page<Reminder> findByUserIdAndRemindAtDate(@Param("userId") Long userId, @Param("date") LocalDate date, Pageable pageable);

    @Query("SELECT r " +
           "FROM Reminder r " +
           "WHERE r.user.id = :userId AND CAST(r.remindAt AS LocalTime) = :time")
    Page<Reminder> findByUserIdAndRemindAtTime(@Param("userId") Long userId, @Param("time") LocalTime time, Pageable pageable);

    @Query("SELECT r " +
           "FROM Reminder r " +
           "WHERE r.user.id = :userId AND CAST(r.remindAt AS LocalDate) = :date AND CAST(r.remindAt AS LocalTime) = :time")
    Page<Reminder> findByUserIdAndRemindAtDateAndTime(@Param("userId") Long userId, @Param("date") LocalDate date, @Param("time") LocalTime time, Pageable pageable);

}
