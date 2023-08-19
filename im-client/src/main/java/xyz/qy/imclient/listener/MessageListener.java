package xyz.qy.imclient.listener;

import xyz.qy.imcommon.model.SendResult;

public interface MessageListener {
     void process(SendResult result);
}
