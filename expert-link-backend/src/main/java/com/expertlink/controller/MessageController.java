package com.expertlink.controller;

import com.expertlink.dto.ApiResponse;
import com.expertlink.dto.ApiResponses;
import com.expertlink.dto.PaginatedResponse;
import com.expertlink.domain.Message;
import com.expertlink.service.MessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/messages")
@RequiredArgsConstructor
public class MessageController {

    private final MessageService messageService;

    /**
     * 获取所有消息（分页）
     * GET /api/messages
     */
    @GetMapping
    public ResponseEntity<ApiResponse<PaginatedResponse<Message>>> getAllMessages(Pageable pageable) {
        Page<Message> messages = messageService.findAll(pageable);
        return ApiResponses.okPage(messages);
    }

    /**
     * 根据ID获取消息
     * GET /api/messages/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Message>> getMessageById(@PathVariable Long id) {
        Message message = messageService.findById(id);
        return ApiResponses.ok(message);
    }

    /**
     * 根据发送者ID获取消息
     * GET /api/messages/sender/{senderId}
     */
    @GetMapping("/sender/{senderId}")
    public ResponseEntity<ApiResponse<List<Message>>> getMessagesBySenderId(@PathVariable Long senderId) {
        List<Message> messages = messageService.findBySenderId(senderId);
        return ApiResponses.ok(messages);
    }

    /**
     * 根据接收者ID获取消息
     * GET /api/messages/receiver/{receiverId}
     */
    @GetMapping("/receiver/{receiverId}")
    public ResponseEntity<ApiResponse<List<Message>>> getMessagesByReceiverId(@PathVariable Long receiverId) {
        List<Message> messages = messageService.findByReceiverId(receiverId);
        return ApiResponses.ok(messages);
    }

    /**
     * 根据发送者和接收者ID获取消息
     * GET /api/messages/sender/{senderId}/receiver/{receiverId}
     */
    @GetMapping("/sender/{senderId}/receiver/{receiverId}")
    public ResponseEntity<ApiResponse<List<Message>>> getMessagesBySenderIdAndReceiverId(
            @PathVariable Long senderId,
            @PathVariable Long receiverId) {
        List<Message> messages = messageService.findBySenderIdAndReceiverId(senderId, receiverId);
        return ApiResponses.ok(messages);
    }

    /**
     * 查找用户相关的消息（发送或接收）
     * GET /api/messages/user/{userId1}/{userId2}
     */
    @GetMapping("/user/{userId1}/{userId2}")
    public ResponseEntity<ApiResponse<List<Message>>> getMessagesBySenderIdOrReceiverId(
            @PathVariable Long userId1,
            @PathVariable Long userId2) {
        List<Message> messages = messageService.findBySenderIdOrReceiverId(userId1, userId2);
        return ApiResponses.ok(messages);
    }

    /**
     * 获取用户的未读消息
     * GET /api/messages/user/{userId}/unread
     */
    @GetMapping("/user/{userId}/unread")
    public ResponseEntity<ApiResponse<List<Message>>> getUnreadMessagesForUser(@PathVariable Long userId) {
        List<Message> messages = messageService.findUnreadMessagesForUser(userId);
        return ApiResponses.ok(messages);
    }

    /**
     * 获取用户的未读消息数量
     * GET /api/messages/user/{userId}/unread-count
     */
    @GetMapping("/user/{userId}/unread-count")
    public ResponseEntity<ApiResponse<Long>> countUnreadMessagesForUser(@PathVariable Long userId) {
        long count = messageService.countUnreadMessagesForUser(userId);
        return ApiResponses.ok(count);
    }

    /**
     * 搜索用户的消息
     * GET /api/messages/user/{userId}/search
     */
    @GetMapping("/user/{userId}/search")
    public ResponseEntity<ApiResponse<PaginatedResponse<Message>>> searchMessagesForUser(
            @PathVariable Long userId,
            @RequestParam String keyword,
            Pageable pageable) {
        Page<Message> messages = messageService.searchMessagesForUser(userId, keyword, pageable);
        return ApiResponses.okPage(messages);
    }

    /**
     * 获取两个用户之间的对话（分页）
     * GET /api/messages/conversation/{senderId}/{receiverId}
     */
    @GetMapping("/conversation/{senderId}/{receiverId}")
    public ResponseEntity<ApiResponse<PaginatedResponse<Message>>> getConversationBetweenUsers(
            @PathVariable Long senderId,
            @PathVariable Long receiverId,
            Pageable pageable) {
        Page<Message> messages = messageService.findConversationBetweenUsers(senderId, receiverId, pageable);
        return ApiResponses.okPage(messages);
    }

    /**
     * 获取两个用户自某个时间点以来的对话
     * GET /api/messages/conversation/{senderId}/{receiverId}/since
     */
    @GetMapping("/conversation/{senderId}/{receiverId}/since")
    public ResponseEntity<ApiResponse<List<Message>>> getConversationBetweenUsersSince(
            @PathVariable Long senderId,
            @PathVariable Long receiverId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate) {
        List<Message> messages = messageService.findConversationBetweenUsersSince(senderId, receiverId, startDate);
        return ApiResponses.ok(messages);
    }

    /**
     * 获取用户的非归档消息
     * GET /api/messages/user/{userId}/non-archived
     */
    @GetMapping("/user/{userId}/non-archived")
    public ResponseEntity<ApiResponse<PaginatedResponse<Message>>> getNonArchivedMessagesForUser(
            @PathVariable Long userId,
            Pageable pageable) {
        Page<Message> messages = messageService.findNonArchivedMessagesForUser(userId, pageable);
        return ApiResponses.okPage(messages);
    }

    /**
     * 获取用户发送过的唯一接收者列表
     * GET /api/messages/user/{userId}/unique-receivers
     */
    @GetMapping("/user/{userId}/unique-receivers")
    public ResponseEntity<ApiResponse<List<Long>>> getUniqueReceiversForSender(@PathVariable Long userId) {
        List<Long> receivers = messageService.findUniqueReceiversForSender(userId);
        return ApiResponses.ok(receivers);
    }

    /**
     * 获取用户接收过的唯一发送者列表
     * GET /api/messages/user/{userId}/unique-senders
     */
    @GetMapping("/user/{userId}/unique-senders")
    public ResponseEntity<ApiResponse<List<Long>>> getUniqueSendersForReceiver(@PathVariable Long userId) {
        List<Long> senders = messageService.findUniqueSendersForReceiver(userId);
        return ApiResponses.ok(senders);
    }

    /**
     * 获取用户带有附件的消息
     * GET /api/messages/user/{userId}/with-attachments
     */
    @GetMapping("/user/{userId}/with-attachments")
    public ResponseEntity<ApiResponse<List<Message>>> getMessagesWithAttachmentsForUser(@PathVariable Long userId) {
        List<Message> messages = messageService.findMessagesWithAttachmentsForUser(userId);
        return ApiResponses.ok(messages);
    }

    /**
     * 获取用户特定类型的消息
     * GET /api/messages/user/{userId}/by-type/{messageType}
     */
    @GetMapping("/user/{userId}/by-type/{messageType}")
    public ResponseEntity<ApiResponse<List<Message>>> getMessagesByTypeForUser(
            @PathVariable Long userId,
            @PathVariable String messageType) {
        List<Message> messages = messageService.findMessagesByTypeForUser(userId, messageType);
        return ApiResponses.ok(messages);
    }

    /**
     * 获取用户的系统消息
     * GET /api/messages/user/{userId}/system
     */
    @GetMapping("/user/{userId}/system")
    public ResponseEntity<ApiResponse<List<Message>>> getSystemMessagesForUser(@PathVariable Long userId) {
        List<Message> messages = messageService.findSystemMessagesForUser(userId);
        return ApiResponses.ok(messages);
    }

    /**
     * 获取用户最近的消息
     * GET /api/messages/user/{userId}/recent
     */
    @GetMapping("/user/{userId}/recent")
    public ResponseEntity<ApiResponse<PaginatedResponse<Message>>> getRecentMessagesForUser(
            @PathVariable Long userId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            Pageable pageable) {
        Page<Message> messages = messageService.findRecentMessagesForUser(userId, startDate, pageable);
        return ApiResponses.okPage(messages);
    }

    /**
     * 获取用户在某个时间段内的消息
     * GET /api/messages/user/{userId}/between-dates
     */
    @GetMapping("/user/{userId}/between-dates")
    public ResponseEntity<ApiResponse<List<Message>>> getMessagesForUserBetweenDates(
            @PathVariable Long userId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        List<Message> messages = messageService.findMessagesForUserBetweenDates(userId, startDate, endDate);
        return ApiResponses.ok(messages);
    }

    /**
     * 检查两个用户之间是否存在消息
     * GET /api/messages/check-existence/{senderId}/{receiverId}
     */
    @GetMapping("/check-existence/{senderId}/{receiverId}")
    public ResponseEntity<ApiResponse<Boolean>> checkMessageExistence(
            @PathVariable Long senderId,
            @PathVariable Long receiverId) {
        boolean exists = messageService.existsBySenderIdAndReceiverId(senderId, receiverId);
        return ApiResponses.ok(exists);
    }

    /**
     * 创建消息
     * POST /api/messages
     */
    @PostMapping
    public ResponseEntity<ApiResponse<Message>> createMessage(
            @RequestBody Message message,
            @RequestParam Long senderId,
            @RequestParam Long receiverId) {
        Message createdMessage = messageService.create(message, senderId, receiverId);
        return ApiResponses.created(createdMessage);
    }

    /**
     * 更新消息
     * PUT /api/messages/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Message>> updateMessage(
            @PathVariable Long id,
            @RequestBody Message messageDetails) {
        Message updatedMessage = messageService.update(id, messageDetails);
        return ApiResponses.ok(updatedMessage);
    }

    /**
     * 删除消息（归档）
     * DELETE /api/messages/{id}/user/{userId}
     */
    @DeleteMapping("/{id}/user/{userId}")
    public ResponseEntity<ApiResponse<Void>> deleteMessageForUser(
            @PathVariable Long id,
            @PathVariable Long userId) {
        messageService.deleteForUser(id, userId);
        return ApiResponses.noContent("消息删除成功");
    }

    /**
     * 标记消息为已读
     * PUT /api/messages/{id}/mark-read/user/{userId}
     */
    @PutMapping("/{id}/mark-read/user/{userId}")
    public ResponseEntity<ApiResponse<Message>> markMessageAsRead(
            @PathVariable Long id,
            @PathVariable Long userId) {
        Message message = messageService.markAsRead(id, userId);
        return ApiResponses.ok(message);
    }

    /**
     * 标记消息为未读
     * PUT /api/messages/{id}/mark-unread/user/{userId}
     */
    @PutMapping("/{id}/mark-unread/user/{userId}")
    public ResponseEntity<ApiResponse<Message>> markMessageAsUnread(
            @PathVariable Long id,
            @PathVariable Long userId) {
        Message message = messageService.markAsUnread(id, userId);
        return ApiResponses.ok(message);
    }

    /**
     * 标记消息为星标（发送者侧）
     * PUT /api/messages/{id}/star-by-sender/user/{userId}
     */
    @PutMapping("/{id}/star-by-sender/user/{userId}")
    public ResponseEntity<ApiResponse<Message>> starMessageBySender(
            @PathVariable Long id,
            @PathVariable Long userId) {
        Message message = messageService.starBySender(id, userId);
        return ApiResponses.ok(message);
    }

    /**
     * 取消星标（发送者侧）
     * PUT /api/messages/{id}/unstar-by-sender/user/{userId}
     */
    @PutMapping("/{id}/unstar-by-sender/user/{userId}")
    public ResponseEntity<ApiResponse<Message>> unstarMessageBySender(
            @PathVariable Long id,
            @PathVariable Long userId) {
        Message message = messageService.unstarBySender(id, userId);
        return ApiResponses.ok(message);
    }

    /**
     * 标记消息为星标（接收者侧）
     * PUT /api/messages/{id}/star-by-receiver/user/{userId}
     */
    @PutMapping("/{id}/star-by-receiver/user/{userId}")
    public ResponseEntity<ApiResponse<Message>> starMessageByReceiver(
            @PathVariable Long id,
            @PathVariable Long userId) {
        Message message = messageService.starByReceiver(id, userId);
        return ApiResponses.ok(message);
    }

    /**
     * 取消星标（接收者侧）
     * PUT /api/messages/{id}/unstar-by-receiver/user/{userId}
     */
    @PutMapping("/{id}/unstar-by-receiver/user/{userId}")
    public ResponseEntity<ApiResponse<Message>> unstarMessageByReceiver(
            @PathVariable Long id,
            @PathVariable Long userId) {
        Message message = messageService.unstarByReceiver(id, userId);
        return ApiResponses.ok(message);
    }

    /**
     * 发送系统消息
     * POST /api/messages/system
     */
    @PostMapping("/system")
    public ResponseEntity<ApiResponse<Message>> sendSystemMessage(
            @RequestParam Long receiverId,
            @RequestParam String subject,
            @RequestParam String content,
            @RequestParam(required = false) String relatedEntityType,
            @RequestParam(required = false) Long relatedEntityId) {
        Message systemMessage = messageService.sendSystemMessage(
                receiverId, subject, content, relatedEntityType, relatedEntityId);
        return ApiResponses.created(systemMessage);
    }

    /**
     * 回复消息
     * POST /api/messages/{originalMessageId}/reply
     */
    @PostMapping("/{originalMessageId}/reply")
    public ResponseEntity<ApiResponse<Message>> replyToMessage(
            @PathVariable Long originalMessageId,
            @RequestParam Long senderId,
            @RequestParam String content) {
        Message reply = messageService.replyToMessage(originalMessageId, senderId, content);
        return ApiResponses.created(reply);
    }
}