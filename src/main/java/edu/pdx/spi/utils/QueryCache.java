package edu.pdx.spi.utils;

import io.vertx.core.Vertx;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public final class QueryCache {
  Vertx vertx;
  Map<String, Long> channelTimers;
  Map<String, List<String>> channelClients;
  Map<String, String> statusUrls;

  public QueryCache(Vertx vertx) {
    this.vertx = vertx;
    channelTimers = new HashMap<>();
    channelClients = new HashMap<>();
    statusUrls = new HashMap<>();
  }

  public boolean isTimerCached(long timerId) {
    return channelTimers.containsValue(timerId);
  }

  public boolean isChannelActive(String channelName) {
    return channelTimers.containsKey(channelName);
  }

  public boolean isClientListening(String channelName, String clientIp) {
    List<String> clients = channelClients.get(channelName);
    return Objects.nonNull(clients) && clients.contains(clientIp);
  }

  public void addChannel(String channelName, long timerId) {
    channelTimers.putIfAbsent(channelName, timerId);
    channelClients.putIfAbsent(channelName, new ArrayList<>());
  }

  public void addChannel(String channelName, String statusUrl) {
    channelClients.putIfAbsent(channelName, new ArrayList<>());
    statusUrls.putIfAbsent(channelName, statusUrl);
  }

  public void addClient(String channelName, String clientIp) {
    if (!channelClients.containsKey(channelName)) {
      throw new IllegalArgumentException("Channel is not active. Can not add client.");
    }
    channelClients.get(channelName).add(clientIp);
  }

  public void removeClient(String clientIp) {
    channelClients.entrySet().forEach(entry -> {
      entry.getValue().remove(clientIp);
      if (entry.getValue().isEmpty()) {
        long timerId = channelTimers.get(entry.getKey());
        unregisterTimer(entry.getKey(), timerId);
      }
    });
    channelClients.entrySet().removeIf(e -> e.getValue().isEmpty());
  }

  public Map<String, String> getStatusUrls() {
    return statusUrls;
  }

  public int getTimerCount() {
    return channelTimers.size();
  }

  public int getStatusCount() {
    return statusUrls.size();
  }

  public int getClientCount(String channelName) {
    List<String> clients = channelClients.get(channelName);
    if (Objects.isNull(clients)) {
      throw new IllegalArgumentException("Channel does not exist");
    }
    return clients.size();
  }

  private void unregisterTimer(String channelName, long timerId) {
    vertx.cancelTimer(timerId);
    channelTimers.remove(channelName);
    //channelClients.remove(channelName);
    statusUrls.remove(channelName);
  }
}
