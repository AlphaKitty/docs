package com.expertlink.service;

import com.expertlink.domain.Message;
import com.expertlink.domain.User;
import com.expertlink.repository.MessageRepository;
import com.expertlink.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MessageService {

    private final MessageRepository messageRepository;
    private final UserRepository userRepository;

    /**
     * 获取所有消息（分页）
     */
    public Page<Message> findAll(Pageable pageable) {
        return messageRepository.findAll(pageable);
    }

    /**
     * 根据ID查找消息
     */
    public Message findById(Long id) {
        return messageRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("消息不存在，ID: " + id));
    }

    /**
     * 根据发送者ID查找消息
     */
    public List<Message> findBySenderId(Long senderId) {
        return messageRepository.findBySenderId(senderId);
    }

    /**
     * 根据接收者ID查找消息
     */
    public List<Message> findByReceiverId(Long receiverId) {
        return messageRepository.findByReceiverId(receiverId);
    }

    /**
     * 根据发送者和接收者ID查找消息
     */
    public List<Message> findBySenderIdAndReceiverId(Long senderId, Long receiverId) {
        return messageRepository.findBySenderIdAndReceiverId(senderId, receiverId);
    }

    /**
     * 查找用户相关的消息（发送或接收）
     */
    public List<Message> findBySenderIdOrReceiverId(Long userId1, Long userId2) {
        return messageRepository.findBySenderIdOrReceiverId(userId1, userId2);
    }

    /**
     * 创建消息
     */
    @Transactional
    public Message create(Message message, Long senderId, Long receiverId) {
        // 验证发送者存在
        User sender = userRepository.findById(senderId)
                .orElseThrow(() -> new RuntimeException("发送者不存在，ID: " + senderId));
        message.setSender(sender);

        // 验证接收者存在
        User receiver = userRepository.findById(receiverId)
                .orElseThrow(() -> new RuntimeException("接收者不存在，ID: " + receiverId));
        message.setReceiver(receiver);

        // 设置默认值
        if (message.getIsRead() == null) {
            message.setIsRead(false);
        }
        if (message.getIsArchivedBySender() == null) {
            message.setIsArchivedBySender(false);
        }
        if (message.getIsArchivedByReceiver() == null) {
            message.setIsArchivedByReceiver(false);
        }
        if (message.getIsStarredBySender() == null) {
            message.setIsStarredBySender(false);
        }
        if (message.getIsStarredByReceiver() == null) {
            message.setIsStarredByReceiver(false);
        }
        if (message.getHasAttachments() == null) {
            message.setHasAttachments(false);
        }
        if (message.getMessageType() == null) {
            message.setMessageType("PRIVATE");
        }
        
        // 如果是回复消息，验证父消息存在
        if (message.getReplyToMessageId() != null) {
            Message parentMessage = messageRepository.findById(message.getReplyToMessageId())
                    .orElseThrow(() -> new RuntimeException("父消息不存在，ID: " + message.getReplyToMessageId()));
            message.setReplyTo(parentMessage);
        }

        message.setCreatedAt(LocalDateTime.now());
        message.setUpdatedAt(LocalDateTime.now());

        return messageRepository.save(message);
    }

    /**
     * 更新消息
     */
    @Transactional
    public Message update(Long id, Message messageDetails) {
        Message existingMessage = findById(id);
        
        // 更新基本信息
        if (messageDetails.getSubject() != null) {
            existingMessage.setSubject(messageDetails.getSubject());
        }
        if (messageDetails.getContent() != null) {
            existingMessage.setContent(messageDetails.getContent());
        }
        if (messageDetails.getMessageType() != null) {
            existingMessage.setMessageType(messageDetails.getMessageType());
        }
        if (messageDetails.getRelatedEntityType() != null) {
            existingMessage.setRelatedEntityType(messageDetails.getRelatedEntityType());
        }
        if (messageDetails.getRelatedEntityId() != null) {
            existingMessage.setRelatedEntityId(messageDetails.getRelatedEntityId());
        }
        if (messageDetails.getHasAttachments() != null) {
            existingMessage.setHasAttachments(messageDetails.getHasAttachments());
        }
        if (messageDetails.getAttachmentsJson() != null) {
            existingMessage.setAttachmentsJson(messageDetails.getAttachmentsJson());
        }
        
        existingMessage.setUpdatedAt(LocalDateTime.now());

        return messageRepository.save(existingMessage);
    }

    /**
     * 删除消息（软删除：归档）
     */
    @Transactional
    public void deleteForUser(Long id, Long userId) {
        Message message = findById(id);
        
        // 检查用户是发送者还是接收者
        boolean isSender = message.getSender().getId().equals(userId);
        boolean isReceiver = message.getReceiver().getId().equals(userId);
        
        if (!isSender && !isReceiver) {
            throw new RuntimeException("用户无权删除此消息");
        }
        
        // 如果是发送者，归档发送者侧
        if (isSender) {
            message.setIsArchivedBySender(true);
        }
        
        // 如果是接收者，归档接收者侧
        if (isReceiver) {
            message.setIsArchivedByReceiver(true);
        }
        
        message.setUpdatedAt(LocalDateTime.now());
        messageRepository.save(message);
        
        log.info("用户 {} 归档消息 ID: {}", userId, id);
    }

    /**
     * 标记消息为已读
     */
    @Transactional
    public Message markAsRead(Long id, Long userId) {
        Message message = findById(id);
        
        // 检查用户是否是接收者
        if (!message.getReceiver().getId().equals(userId)) {
            throw new RuntimeException("只有接收者可以标记消息为已读");
        }
        
        if (!message.getIsRead()) {
            message.markAsRead();
            message.setUpdatedAt(LocalDateTime.now());
            return messageRepository.save(message);
        }
        
        return message;
    }

    /**
     * 标记消息为未读
     */
    @Transactional
    public Message markAsUnread(Long id, Long userId) {
        Message message = findById(id);
        
        // 检查用户是否是接收者
        if (!message.getReceiver().getId().equals(userId)) {
            throw new RuntimeException("只有接收者可以标记消息为未读");
        }
        
        if (message.getIsRead()) {
            message.setIsRead(false);
            message.setUpdatedAt(LocalDateTime.now());
            return messageRepository.save(message);
        }
        
        return message;
    }

    /**
     * 标记消息为星标（发送者侧）
     */
    @Transactional
    public Message starBySender(Long id, Long userId) {
        Message message = findById(id);
        
        // 检查用户是否是发送者
        if (!message.getSender().getId().equals(userId)) {
            throw new RuntimeException("只有发送者可以标记此消息为星标（发送者侧）");
        }
        
        message.setIsStarredBySender(true);
        message.setUpdatedAt(LocalDateTime.now());
        return messageRepository.save(message);
    }

    /**
     * 取消星标（发送者侧）
     */
    @Transactional
    public Message unstarBySender(Long id, Long userId) {
        Message message = findById(id);
        
        // 检查用户是否是发送者
        if (!message.getSender().getId().equals(userId)) {
            throw new RuntimeException("只有发送者可以取消此消息的星标（发送者侧）");
        }
        
        message.setIsStarredBySender(false);
        message.setUpdatedAt(LocalDateTime.now());
        return messageRepository.save(message);
    }

    /**
     * 标记消息为星标（接收者侧）
     */
    @Transactional
    public Message starByReceiver(Long id, Long userId) {
        Message message = findById(id);
        
        // 检查用户是否是接收者
        if (!message.getReceiver().getId().equals(userId)) {
            throw new RuntimeException("只有接收者可以标记此消息为星标（接收者侧）");
        }
        
        message.setIsStarredByReceiver(true);
        message.setUpdatedAt(LocalDateTime.now());
        return messageRepository.save(message);
    }

    /**
     * 取消星标（接收者侧）
     */
    @Transactional
    public Message unstarByReceiver(Long id, Long userId) {
        Message message = findById(id);
        
        // 检查用户是否是接收者
        if (!message.getReceiver().getId().equals(userId)) {
            throw new RuntimeException("只有接收者可以取消此消息的星标（接收者侧）");
        }
        
        message.setIsStarredByReceiver(false);
        message.setUpdatedAt(LocalDateTime.now());
        return messageRepository.save(message);
    }

    /**
     * 获取用户的未读消息
     */
    public List<Message> findUnreadMessagesForUser(Long userId) {
        return messageRepository.findUnreadMessagesForUser(userId);
    }

    /**
     * 获取用户的未读消息数量
     */
    public long countUnreadMessagesForUser(Long userId) {
        return messageRepository.countUnreadMessagesForUser(userId);
    }

    /**
     * 搜索用户的消息
     */
    public Page<Message> searchMessagesForUser(Long userId, String keyword, Pageable pageable) {
        return messageRepository.searchMessagesForUser(userId, keyword, pageable);
    }

    /**
     * 获取两个用户之间的对话
     */
    public Page<Message> findConversationBetweenUsers(Long senderId, Long receiverId, Pageable pageable) {
        return messageRepository.findConversationBetweenUsers(senderId, receiverId, pageable);
    }

    /**
     * 获取两个用户自某个时间点以来的对话
     */
    public List<Message> findConversationBetweenUsersSince(Long senderId, Long receiverId, LocalDateTime startDate) {
        return messageRepository.findConversationBetweenUsersSince(senderId, receiverId, startDate);
    }

    /**
     * 获取用户的非归档消息
     */
    public Page<Message> findNonArchivedMessagesForUser(Long userId, Pageable pageable) {
        return messageRepository.findNonArchivedMessagesForUser(userId, pageable);
    }

    /**
     * 获取用户发送过的唯一接收者列表
     */
    public List<Long> findUniqueReceiversForSender(Long userId) {
        return messageRepository.findUniqueReceiversForSender(userId);
    }

    /**
     * 获取用户接收过的唯一发送者列表
     */
    public List<Long> findUniqueSendersForReceiver(Long userId) {
        return messageRepository.findUniqueSendersForReceiver(userId);
    }

    /**
     * 获取用户带有附件的消息
     */
    public List<Message> findMessagesWithAttachmentsForUser(Long userId) {
        return messageRepository.findMessagesWithAttachmentsForUser(userId);
    }

    /**
     * 获取用户特定类型的消息
     */
    public List<Message> findMessagesByTypeForUser(Long userId, String messageType) {
        return messageRepository.findMessagesByTypeForUser(userId, messageType);
    }

    /**
     * 获取用户的系统消息
     */
    public List<Message> findSystemMessagesForUser(Long userId) {
        return messageRepository.findSystemMessagesForUser(userId);
    }

    /**
     * 获取用户最近的消息
     */
    public Page<Message> findRecentMessagesForUser(Long userId, LocalDateTime startDate, Pageable pageable) {
        return messageRepository.findRecentMessagesForUser(userId, startDate, pageable);
    }

    /**
     * 获取用户在某个时间段内的消息
     */
    public List<Message> findMessagesForUserBetweenDates(Long userId, LocalDateTime startDate, LocalDateTime endDate) {
        return messageRepository.findMessagesForUserBetweenDates(userId, startDate, endDate);
    }

    /**
     * 检查两个用户之间是否存在消息
     */
    public boolean existsBySenderIdAndReceiverId(Long senderId, Long receiverId) {
        return messageRepository.existsBySenderIdAndReceiverId(senderId, receiverId);
    }

    /**
     * 发送系统消息
     */
    @Transactional
    public Message sendSystemMessage(Long receiverId, String subject, String content, 
                                      String relatedEntityType, Long relatedEntityId) {
        // 系统消息的发送者可以是空或系统用户，这里假设发送者为null
        Message systemMessage = new Message();
        systemMessage.setSubject(subject);
        systemMessage.setContent(content);
        systemMessage.setMessageType("SYSTEM");
        systemMessage.setRelatedEntityType(relatedEntityType);
        systemMessage.setRelatedEntityId(relatedEntityId);
        systemMessage.setIsRead(false);
        systemMessage.setIsArchivedBySender(false);
        systemMessage.setIsArchivedByReceiver(false);
        systemMessage.setIsStarredBySender(false);
        systemMessage.setIsStarredByReceiver(false);
        systemMessage.setHasAttachments(false);
        systemMessage.setCreatedAt(LocalDateTime.now());
        systemMessage.setUpdatedAt(LocalDateTime.now());
        
        // 设置接收者
        User receiver = userRepository.findById(receiverId)
                .orElseThrow(() -> new RuntimeException("接收者不存在，ID: " + receiverId));
        systemMessage.setReceiver(receiver);
        
        // 系统消息可能没有具体发送者，或者可以设置为系统用户
        // 这里设置为null，需要在实体中允许发送者为空
        
        return messageRepository.save(systemMessage);
    }

    /**
     * 回复消息
     */
    @Transactional
    public Message replyToMessage(Long originalMessageId, Long senderId, String content) {
        Message originalMessage = findById(originalMessageId);
        
        // 验证发送者是否有权回复（通常是原始消息的接收者或发送者）
        boolean canReply = originalMessage.getSender().getId().equals(senderId) || 
                          originalMessage.getReceiver().getId().equals(senderId);
        
        if (!canReply) {
            throw new RuntimeException("无权回复此消息");
        }
        
        // 确定接收者（如果是原始发送者回复，则接收者是原始接收者，反之亦然）
        Long receiverId = originalMessage.getSender().getId().equals(senderId) 
                ? originalMessage.getReceiver().getId() 
                : originalMessage.getSender().getId();
        
        Message reply = new Message();
        reply.setSubject("Re: " + (originalMessage.getSubject() != null ? originalMessage.getSubject() : ""));
        reply.setContent(content);
        reply.setMessageType("PRIVATE");
        reply.setReplyToMessageId(originalMessageId);
        reply.setIsRead(false);
        reply.setCreatedAt(LocalDateTime.now());
        reply.setUpdatedAt(LocalDateTime.now());
        
        return create(reply, senderId, receiverId);
    }
}