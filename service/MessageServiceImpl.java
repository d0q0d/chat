package org.tpl.chat.service;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.tpl.chat.dal.model.*;
import org.tpl.chat.dal.repository.MessageRepository;
import org.tpl.chat.service.mapper.MessageServiceMapper;
import org.tpl.chat.service.messagebroker.ProducerHandler;
import org.tpl.chat.service.model.FileUploadModel;
import org.tpl.chat.service.model.IterativePageState;
import org.tpl.chat.service.model.MemberModel;
import org.tpl.chat.service.remote.FmAdapter;
import org.tpl.chat.service.remote.model.MultiPartFileUploadModel;
import org.tpl.chat.util.UserUtil;
import org.tpl.util.common.config.LocaleConfig;
import org.tpl.util.common.service.exception.NotFoundException;
import org.tpl.util.common.service.exception.PreconditionFailedException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static org.tpl.chat.dal.model.MessageStatus.SEEN;
import static org.tpl.chat.dal.model.MessageStatus.SENT;
import static org.tpl.chat.dal.model.SessionType.P2P;

@Service
@Slf4j
public class MessageServiceImpl implements MessageService {

    private final MessageRepository repository;
    private final SessionService sessionService;
    private final UserService userService;
    private final MessageServiceMapper mapper;
    private final StoryService storyService;
    private final FmAdapter fmAdapter;
    private final UserUtil userUtil;
    private final ProducerHandler producerHandler;
    @Value("${message.valid.file.p2p}")
    private List<MessageType> VALID_FILE_P2P;
    @Value("${message.valid.file.group}")
    private List<MessageType> VALID_FILE_GROUP;
    @Value("${message.valid.file.channel}")
    private List<MessageType> VALID_FILE_CHANNEL;

    @Value(value = "${story.expiration.hours}")
    private int StoryExpireHours;

    @PostConstruct
    public void init(){
      System.out.println();
    }

    public MessageServiceImpl(
            MessageRepository repository,
            @Lazy SessionService sessionService,
            UserService userService,
            MessageServiceMapper mapper,
            StoryService storyService, FmAdapter fmAdapter,
            UserUtil userUtil,
            ProducerHandler producerHandler
            ) {
        this.repository = repository;
        this.sessionService = sessionService;
        this.userService = userService;
        this.mapper = mapper;
        this.storyService = storyService;
        this.fmAdapter = fmAdapter;
        this.userUtil = userUtil;
        this.producerHandler = producerHandler;
    }

  @Override
  public Message save(Message message) {
    message = repository.save(message);
    fillExtraInfoOfMessages(List.of(message));
    return message;
  }

  @Override
  public Message getLastMessage(String sessionId) {
    return repository.findTopBySessionIdOrderByCreatedDateDesc(sessionId);
  }

  @Override
  public Page<Message> getMessages(String sessionId, Pageable pageable) {
    var session = sessionService.getById(sessionId);
    if (session.getSessionDeleteModelList() != null
        && session.getSessionDeleteModelList().stream()
            .anyMatch(deleteModel -> deleteModel.getUserId().equals(userUtil.getUserId()))) {
      var lastDeletedDate =
          session.getSessionDeleteModelList().stream()
              .filter(deleteModel -> deleteModel.getUserId().equals(userUtil.getUserId()))
              .findAny()
              .get()
              .getLastDeletedDate();
      Page<Message> messagePage = repository.findAllBySessionIdAndCreatedDateAfter(sessionId, lastDeletedDate, pageable);
      fillExtraInfoOfMessages(messagePage.getContent());
      return messagePage;
    } else {
      Page<Message> messagePage = repository.findAllBySessionId(sessionId, pageable);
      fillExtraInfoOfMessages(messagePage.getContent());
      return messagePage;
    }
  }

    @Override
    public List<Message> getMessagesIterative(String sessionId, String messageId, IterativePageState state, int limit) {
      var session = sessionService.getById(sessionId);
      LocalDateTime messageDateTime = null;
      if (Objects.nonNull(messageId)) {
        messageDateTime = getMessage(messageId).getCreatedDate();
      }
      if (session.getSessionDeleteModelList() != null
              && session.getSessionDeleteModelList().stream()
              .anyMatch(deleteModel -> deleteModel.getUserId().equals(userUtil.getUserId()))) {
        var lastDeletedDate =
                session.getSessionDeleteModelList().stream()
                        .filter(deleteModel -> deleteModel.getUserId().equals(userUtil.getUserId()))
                        .findAny()
                        .get()
                        .getLastDeletedDate();
        List<Message> messages = repository.findMessagesIterative(sessionId, lastDeletedDate, messageDateTime, state, limit);
        fillExtraInfoOfMessages(messages);
        return messages;
      } else {
        List<Message> messages = repository.findMessagesIterative(sessionId, null, messageDateTime, state, limit);
        fillExtraInfoOfMessages(messages);
        return messages;
      }
    }

    @Override
  public Message editMessage(String messageId, String content) {
    var message = repository.findById(messageId).orElseThrow(NotFoundException::new);
    if (CollectionUtils.isEmpty(message.getReceiverIds())) {
      message.setContent(content);
      message = repository.save(message);
    }else {
      throw new PreconditionFailedException(
              LocaleConfig.getLocaleMessage("message.can.not.be.edited"));
    }
    fillExtraInfoOfMessages(List.of(message));
    Session session = sessionService.getById(message.getSessionId());
    if (session != null) sendEditedMessageOnSocket(message, session.getMembers());
    return message;
  }

  @Override
  public Message getById(String messageId) {
    Message message = getMessage(messageId);
    fillExtraInfoOfMessages(List.of(message));
    return message;
  }

  @Override
  public Optional<Message> getOptionalById(String messageId) {
    Optional<Message> opt = repository.findById(messageId);
    opt.ifPresent(message -> fillExtraInfoOfMessages(List.of(message)));
    return opt;
  }

  @Retryable(retryFor = {DuplicateKeyException.class},
          maxAttempts = 20,
          backoff = @Backoff(delay = 20))
  @Override
  public Message send(Message message, MultipartFile file) {
    var session = sessionService.getById(message.getSessionId());
    if (session == null || session.getSessionType().equals(P2P)) {
      if (Objects.nonNull(file) && !VALID_FILE_P2P.contains(message.getType())){
        throw new AccessDeniedException(LocaleConfig.getLocaleMessage("permission.denied"));
      }
      return sendToP2PSession(session, message, file);
    } else if (session.getSessionType().equals(SessionType.GROUP)) {
      if (Objects.nonNull(file) && !VALID_FILE_GROUP.contains(message.getType())){
        throw new AccessDeniedException(LocaleConfig.getLocaleMessage("permission.denied"));
      }
      return saveMessage(message, file, session);
    } else if (session.getSessionType().equals(SessionType.CHANNEL)) {
      if (Objects.nonNull(file) && !VALID_FILE_CHANNEL.contains(message.getType())){
        throw new AccessDeniedException(LocaleConfig.getLocaleMessage("permission.denied"));
      }
      return saveMessage(message, file, session);
    } else {
      throw new AccessDeniedException(LocaleConfig.getLocaleMessage("permission.denied"));
    }
  }

  @Override
  public String seenMessage(String messageId) {
    var message = getMessage(messageId);
    repository.seenMessage(message.getSessionId(), userUtil.getUserId(), message.getCreatedDate());
    if (!userUtil.getUserId().equals(message.getSenderId())){
      message.setMessageStatus(SEEN);
      producerHandler.sendAndSaveInMemory(
              message.getSenderId(),
              mapper.messageToSocketSeenModel(message));
    }
    return message.getSessionId();
  }

  @Override
  public int getUnreadMessageCount(String userId, Session session) {
    if (session.getSessionDeleteModelList() != null
            && session.getSessionDeleteModelList().stream()
            .anyMatch(deleteModel -> deleteModel.getUserId().equals(userUtil.getUserId()))) {
      var lastDeletedDate =
              session.getSessionDeleteModelList().stream()
                      .filter(deleteModel -> deleteModel.getUserId().equals(userUtil.getUserId()))
                      .findAny()
                      .get()
                      .getLastDeletedDate();
      return repository.countBySessionIdAndSenderIdNotAndReceiverIdsNotContainingAndCreatedDateAfter(session.getId(), userId, userId, lastDeletedDate);
    } else return repository.countBySessionIdAndSenderIdNotAndReceiverIdsNotContaining(session.getId(), userId, userId);
  }

  @Override
  public void pin(String messageId) {
    var message = getMessage(messageId);
    var session = sessionService.getById(message.getSessionId());
    session.setPinedMessageId(messageId);
    sessionService.save(session);
  }

  @Override
  public void unpin(String messageId) {
    var message = getMessage(messageId);
    var session = sessionService.getById(message.getSessionId());
    session.setPinedMessageId(null);
    sessionService.save(session);
  }

  @Override
  public void sendTypingStatus(String sessionId, String userId) {
    var session = sessionService.getById(sessionId);
    sendTypingStatusOnSocket(userId, sessionId, session.getMembers());
  }

  @Override
  public Optional<Message> getFirstUnreadMessage(Session session, String userId) {
    if (session.getSessionDeleteModelList() != null
            && session.getSessionDeleteModelList().stream()
            .anyMatch(deleteModel -> deleteModel.getUserId().equals(userId))) {
      var lastDeletedDate =
              session.getSessionDeleteModelList().stream()
                      .filter(deleteModel -> deleteModel.getUserId().equals(userId))
                      .findAny()
                      .get()
                      .getLastDeletedDate();
      return repository.getFirstUnreadMessage(session.getId(), userId, lastDeletedDate);
    } else return repository.getFirstUnreadMessage(session.getId(), userId, null);
  }

  @Override
  public void sendReaction(String messageId, String reaction, String userId) {
    var message = getMessage(messageId);
    if (message.getReactions() == null) message.setReactions(new HashMap<>());
    message.getReactions().put(userId, reaction);
    repository.save(message);
    var session = sessionService.getById(message.getSessionId());
    sendReactionOnSocket(userId, reaction, message, session.getMembers());
  }

  public void deleteById(String messageId) {
    var message =
            repository
                    .findById(messageId)
                    .orElseThrow(
                            () -> new NotFoundException(LocaleConfig.getLocaleMessage("entity.not.found")));
    if (!CollectionUtils.isEmpty(message.getReceiverIds()))
      throw new PreconditionFailedException(
              LocaleConfig.getLocaleMessage("message.can.not.be.deleted"));
    repository.deleteById(messageId);
    Session session = sessionService.getById(message.getSessionId());
    if (session == null) return;
    Message lastMessage = getLastMessage(session.getId());
    sendDeletedMessagedOnSocket(lastMessage, message, session.getMembers());
  }

  private void sendDeletedMessagedOnSocket(Message lastMessage, Message message, Set<String> members){
    members.remove(userUtil.getUserId());
    members.parallelStream().forEach(member -> {
      producerHandler.sendAndSaveInMemory(member, mapper.messageToSocketDeletedModel(message, lastMessage));
    });
  }

  private void sendReactionOnSocket(String userId, String reaction, Message message, Set<String> members){
    members.remove(userUtil.getUserId());
    members.parallelStream().forEach(member -> producerHandler.sendAndSaveInMemory(member, mapper.messageToSocketReactionModel(userId, reaction, message.getId(), message.getSessionId())));
  }

  private void sendTypingStatusOnSocket(String userId, String sessionId, Set<String> members){
    var user = userService.getById(userId);
    members.remove(userUtil.getUserId());
    members.parallelStream()
            .forEach(member -> producerHandler.sendAndSaveInMemory(
                    member,
                    mapper.messageToSocketTypingModel(
                            userId,
                            sessionId,
                            user == null ? null : user.getFullName(),
                            user == null ? null : user.getPersonnelPhotoUrl()
                    )
            ));
  }

  private void sendEditedMessageOnSocket(Message message, Set<String> members){
    members.remove(userUtil.getUserId());
    members.parallelStream().forEach(member -> {
      producerHandler.sendAndSaveInMemory(member, mapper.messageToSocketEditedModel(message));
    });
  }

  private Message getMessage(String messageId) {
    return repository
        .findById(messageId)
        .orElseThrow(
            () -> new NotFoundException(LocaleConfig.getLocaleMessage("entity.not.found")));
  }

  private Message sendToP2PSession(Session session, Message message, MultipartFile file) {
    session = checkP2PSession(session, message.getSessionId(), message);
    if (session != null) {
      var sessionDeleteModelList = session.getSessionDeleteModelList();
      if (preconditionForRestoreP2PSession(sessionDeleteModelList)) {
        session.setSessionDeleteModelList(restoreP2PSession(session));
        sessionService.save(session);
      }
      return saveMessage(message, file, session);
    } else return initializeNewP2PSession(message, file);
  }

  private List<SessionDeleteModel> restoreP2PSession(Session session) {
    checkHasAccessToSendMessage(session.getMembers().stream().filter(id -> !id.equals(userUtil.getUserId())).findFirst().orElse(null));
    return session.getSessionDeleteModelList().stream()
        .map(
            deleteModel -> {
              deleteModel.setIsDeleted(false);
              return deleteModel;
            })
        .toList();
  }

  private void checkHasAccessToSendMessage(String userId){
    if (userId != null && !userService.hasAccessBasedOnRole(userId))
      throw new AccessDeniedException(LocaleConfig.getLocaleMessage("permission.denied"));
  }

  private boolean preconditionForRestoreP2PSession(
      List<SessionDeleteModel> sessionDeleteModelList) {
    return sessionDeleteModelList != null
        && sessionDeleteModelList.stream().anyMatch(SessionDeleteModel::getIsDeleted);
  }

  private Message saveMessage(Message message, MultipartFile file, Session session) {
    if (file != null) {
      var fileUploadModel = fmAdapter.upload(message.getType(), new MultiPartFileUploadModel(file));
      setMessageMetaData(message, fileUploadModel, file);
      message.setUrl(fileUploadModel.getUrl());
    }
    message.setSessionId(session.getId());
    message.setMessageStatus(SENT);
    message = repository.save(message);
    fillExtraInfoOfMessages(List.of(message));
    sendMessageOnSocketForMembers(message, session.getMembers());
    return message;
  }

  private void sendMessageOnSocketForMembers(Message message, Set<String> members) {
    if (!Set.of(MessageType.CREATED, MessageType.NEW_MEMBERS, MessageType.REMOVE_MEMBERS).contains(message.getType())){
      members.remove(userUtil.getUserId());
    }
      members.parallelStream().forEach(member -> {
        producerHandler.sendAndSaveInMemory(member, mapper.messageToSocketMessageModel(message));
      });
  }

  private Session checkP2PSession(Session session, String sessionId, Message message) {
    if (session == null){
      Set<String> members = new HashSet<>(List.of(message.getSenderId(), message.getSessionId()));
      Session existedSession =  sessionService.getByMembersAndType(members, P2P);
      if (existedSession != null) return existedSession;
      if (userService.getById(sessionId) == null)
        throw new PreconditionFailedException(LocaleConfig.getLocaleMessage("entity.not.found"));
    }
    return session;
  }

  private Message initializeNewP2PSession(Message message, MultipartFile file) {
    checkHasAccessToSendMessage(message.getSessionId());
    var newSession =
            sessionService.save(
                    new Session(new HashSet<>(List.of(message.getSenderId(), message.getSessionId())), P2P, message.getSenderId().equals(message.getSessionId()) ? Boolean.TRUE : null));
    return saveMessage(message, file, newSession);
  }

  private void fillExtraInfoOfMessages(List<Message> messages){
    if (CollectionUtils.isEmpty(messages)) return;
    fillSenderOfMessages(messages);
    fillRepliedMessagePreview(messages);
    fillStoryOfMessages(messages);
  }

  private void fillRepliedMessagePreview(List<Message> messages) {
    for (Message message : messages) {
      if (Objects.nonNull(message.getRepliedId())){
        message.setRepliedMessagePreview(
                repository.findById(message.getRepliedId()).map(mapper::getMessagePreviewModelFromMessage).orElse(null)
        );
      }
    }
  }

  private void fillSenderOfMessages(List<Message> messages) {
    Map<String, User> usersMap = null;
    try {
      usersMap = userService.getMapByIds(messages.stream().map(Message::getSenderId).collect(Collectors.toSet()));
    } catch (Exception e) {
      e.printStackTrace();
    }
    if (usersMap != null) {
      for (Message message : messages) {
        User user = usersMap.get(message.getSenderId());
        if (user != null) {
          message.setSender(new MemberModel(message.getSenderId(), user.getPersonnelPhotoUrl(), user.getFullName(), null));
        }
      }
    }
  }

    private void fillStoryOfMessages(List<Message> messages) {
        if (CollectionUtils.isEmpty(messages)) return;
        for (Message message : messages) {
            // check ExpireDate for story
            var storyRepliedId = message.getStoryRepliedId();
            if (storyRepliedId != null) {
                Optional<Story> optStory = storyService.getOptionalById(storyRepliedId);
                if (optStory.isPresent()) {
                    Story story = optStory.get();
                    var storyDate = story.getCreatedDate();
                    var storyExpireDate = storyDate.plusHours(StoryExpireHours);
                    var nowDate = LocalDateTime.now();
                    if (storyExpireDate.isAfter(nowDate)) {
                        message.setStory(story);
                    }
                }
            }
        }
    }

  private void setMessageMetaData(Message message, FileUploadModel fileUploadModel, MultipartFile file) {
    setFileSize(message, file);
    if (message.getType().equals(MessageType.VIDEO)){
      setFileLength(message, fileUploadModel);
      setFilePreview(message, fileUploadModel);
    }
  }

  private void setFileSize(Message message, MultipartFile file) {
    var messageMetaData = message.getMessageMetaData();
    if (messageMetaData == null) messageMetaData = new MessageMetaData();
    messageMetaData.setSize(file.getSize());
    message.setMessageMetaData(messageMetaData);
  }

  private void setFilePreview(Message message, FileUploadModel fileUploadModel) {
    var messageMetaData = message.getMessageMetaData();
    if (messageMetaData == null) messageMetaData = new MessageMetaData();
    messageMetaData.setPreviewUrl(fileUploadModel.getPreviewUrl());
    message.setMessageMetaData(messageMetaData);
  }

  private void setFileLength(Message message, FileUploadModel fileUploadModel) {
    var messageMetaData = message.getMessageMetaData();
    if (messageMetaData == null) messageMetaData = new MessageMetaData();
    messageMetaData.setLength(fileUploadModel.getDuration());
    message.setMessageMetaData(messageMetaData);
  }
}
