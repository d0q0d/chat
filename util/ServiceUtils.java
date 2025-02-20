package org.tpl.chat.util;

import java.util.concurrent.ThreadLocalRandom;

public class ServiceUtils {

    public static String getRandomStringByTemplateAndLength(String fromAB, int len) {
        StringBuilder sb = new StringBuilder(len);
        for (int i = 0; i < len; i++)
            sb.append(fromAB.charAt(ThreadLocalRandom.current().nextInt(fromAB.length())));
        return sb.toString();
    }

    public static String getOrganizationParentCodePrefix(String code){
        if (code.length() != 11) throw new IllegalArgumentException("organization code is invalid.");
        String first = code.substring(0, 2);
        String second = code.substring(2, 5);
        String third = code.substring(5, 8);
        String forth = code.substring(8);
        if (second.equals("000") && third.equals("000") && forth.equals("000")) return first;
        else if (third.equals("000") && forth.equals("000")) return first + second;
        else if (forth.equals("000")) return first + second + third;
        else return first + second + third + forth;
    }

    public static String getFormationParentCodePrefix(String code){
        if (code.length() != 6) throw new IllegalArgumentException("formation code is invalid.");
        String first = code.substring(0, 2);
        String second = code.substring(2, 4);
        String third = code.substring(4, 5);
        String forth = code.substring(5);
        if (second.equals("00") && third.equals("0") && forth.equals("0")) return first;
        else if (third.equals("0") && forth.equals("0")) return first + second;
        else if (forth.equals("0")) return first + second + third;
        else return first + second + third + forth;
    }

 /*   public static boolean isEmpty(String str) {
        return str == null || str.length() == 0;
    }
    private ArrayList<Session> addPinedSessions(String userId, List<Session> sessionList) {
        var userInfo = userInfoService.getByUserId(userId);
        var sortedPinedSessions = userInfo.getPinedSessionsSet().stream().sorted(Comparator.comparing(PinedSession::getPinedDateTime).reversed()).toList();
        var pinedIds = sortedPinedSessions.stream().map(PinedSession::getSessionId).toList();
        var unPinedSessions = sessionList.stream().filter(session -> !pinedIds.contains(session.getId())).toList();
        var pinedSessions = sessionList.stream().filter(session -> pinedIds.contains(session.getId())).toList();
        var finalSessionList = new ArrayList<Session>();
        for (PinedSession sortedPinedSession : sortedPinedSessions) {
            var sessionId = sortedPinedSession.getSessionId();
            var pinedSession = pinedSessions.stream().filter(session -> session.getId().equals(sessionId)).toList().get(0);
            finalSessionList.add(pinedSession);
        }
        finalSessionList.addAll(unPinedSessions);
        return finalSessionList;
    }*/
}
