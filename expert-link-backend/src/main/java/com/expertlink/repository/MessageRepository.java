package com.expertlink.repository;

import com.expertlink.domain.Message;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {
    
    List<Message> findBySenderId(Long senderId);
    
    List<Message> findByReceiverId(Long receiverId);
    
    List<Message> findBySenderIdAndReceiverId(Long senderId, Long receiverId);
    
    List<Message> findBySenderIdOrReceiverId(Long userId1, Long userId2);
    
    List<Message> findBySenderIdAndIsArchivedBySender(Long senderId, Boolean isArchivedBySender);
    
    List<Message> findByReceiverIdAndIsArchivedByReceiver(Long receiverId, Boolean isArchivedByReceiver);
    
    List<Message> findBySenderIdAndIsStarredBySender(Long senderId, Boolean isStarredBySender);
    
    List<Message> findByReceiverIdAndIsStarredByReceiver(Long receiverId, Boolean isStarredByReceiver);
    
    List<Message> findByIsRead(Boolean isRead);
    
    List<Message> findByMessageType(String messageType);
    
    List<Message> findByRelatedEntityTypeAndRelatedEntityId(String relatedEntityType, Long relatedEntityId);
    
    List<Message> findByReplyToMessageId(Long replyToMessageId);
    
    @Query("SELECT m FROM Message m WHERE " +
           "(m.sender.id = :userId OR m.receiver.id = :userId) AND " +
           "m.isRead = false")
    List<Message> findUnreadMessagesForUser(@Param("userId") Long userId);
    
    @Query("SELECT m FROM Message m WHERE " +
           "(m.sender.id = :userId OR m.receiver.id = :userId) AND " +
           "m.createdAt >= :startDate AND m.createdAt <= :endDate")
    List<Message> findMessagesForUserBetweenDates(
            @Param("userId") Long userId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT m FROM Message m WHERE " +
           "(m.sender.id = :userId OR m.receiver.id = :userId) AND " +
           "LOWER(m.subject) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(m.content) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<Message> searchMessagesForUser(
            @Param("userId") Long userId,
            @Param("keyword") String keyword,
            Pageable pageable);
    
    @Query("SELECT m FROM Message m WHERE " +
           "((m.sender.id = :senderId AND m.receiver.id = :receiverId) OR " +
           "(m.sender.id = :receiverId AND m.receiver.id = :senderId)) AND " +
           "m.createdAt >= :startDate")
    List<Message> findConversationBetweenUsersSince(
            @Param("senderId") Long senderId,
            @Param("receiverId") Long receiverId,
            @Param("startDate") LocalDateTime startDate);
    
    @Query("SELECT m FROM Message m WHERE " +
           "((m.sender.id = :senderId AND m.receiver.id = :receiverId) OR " +
           "(m.sender.id = :receiverId AND m.receiver.id = :senderId)) " +
           "ORDER BY m.createdAt DESC")
    Page<Message> findConversationBetweenUsers(
            @Param("senderId") Long senderId,
            @Param("receiverId") Long receiverId,
            Pageable pageable);
    
    @Query("SELECT DISTINCT m.sender.id FROM Message m WHERE m.receiver.id = :userId")
    List<Long> findUniqueSendersForReceiver(@Param("userId") Long userId);
    
    @Query("SELECT DISTINCT m.receiver.id FROM Message m WHERE m.sender.id = :userId")
    List<Long> findUniqueReceiversForSender(@Param("userId") Long userId);
    
    @Query("SELECT COUNT(m) FROM Message m WHERE m.receiver.id = :userId AND m.isRead = false")
    long countUnreadMessagesForUser(@Param("userId") Long userId);
    
    @Query("SELECT m FROM Message m WHERE " +
           "(m.sender.id = :userId AND m.isArchivedBySender = false) OR " +
           "(m.receiver.id = :userId AND m.isArchivedByReceiver = false)")
    Page<Message> findNonArchivedMessagesForUser(
            @Param("userId") Long userId,
            Pageable pageable);
    
    @Query("SELECT m FROM Message m WHERE " +
           "m.hasAttachments = true AND " +
           "(m.sender.id = :userId OR m.receiver.id = :userId)")
    List<Message> findMessagesWithAttachmentsForUser(@Param("userId") Long userId);
    
    @Query("SELECT m FROM Message m WHERE " +
           "m.messageType = :messageType AND " +
           "(m.sender.id = :userId OR m.receiver.id = :userId)")
    List<Message> findMessagesByTypeForUser(
            @Param("userId") Long userId,
            @Param("messageType") String messageType);
    
    @Query("SELECT m FROM Message m WHERE " +
           "m.messageType = 'SYSTEM' AND " +
           "m.receiver.id = :userId")
    List<Message> findSystemMessagesForUser(@Param("userId") Long userId);
    
    @Query("SELECT m FROM Message m WHERE " +
           "(m.sender.id = :userId OR m.receiver.id = :userId) AND " +
           "m.createdAt >= :startDate " +
           "ORDER BY m.createdAt DESC")
    Page<Message> findRecentMessagesForUser(
            @Param("userId") Long userId,
            @Param("startDate") LocalDateTime startDate,
            Pageable pageable);
    
    boolean existsBySenderIdAndReceiverId(Long senderId, Long receiverId);
}