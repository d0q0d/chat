package org.tpl.chat.service.annotation;

import org.tpl.chat.dal.model.SessionType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Permission {

  boolean member() default false;

  boolean p2pMember() default false;

  boolean groupMember() default false;
  boolean groupOwner() default false;
  boolean groupCreator() default false;

  boolean channelMember() default false;
  boolean channelOwner() default false;
  boolean channelCreator() default false;

  boolean sender() default false;

  boolean ignoreNotExistsSession() default false;

  SessionType[] acceptableTypes() default {SessionType.GROUP, SessionType.CHANNEL, SessionType.P2P};

  String sessionId() default "";
  String messageId() default "";
}
