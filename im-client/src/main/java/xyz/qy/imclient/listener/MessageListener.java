package xyz.qy.imclient.listener;


import xyz.qy.imcommon.model.IMSendResult;

public interface MessageListener<T> {

     void process(IMSendResult<T> result);

}
