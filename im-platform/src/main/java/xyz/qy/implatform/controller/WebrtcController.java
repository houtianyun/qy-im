package xyz.qy.implatform.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import xyz.qy.imclient.IMClient;
import xyz.qy.imcommon.model.IMPrivateMessage;
import xyz.qy.imcommon.model.PrivateMessageInfo;
import xyz.qy.implatform.config.ICEServerConfig;
import xyz.qy.implatform.enums.MessageType;
import xyz.qy.implatform.exception.GlobalException;
import xyz.qy.implatform.result.Result;
import xyz.qy.implatform.result.ResultUtils;
import xyz.qy.implatform.session.SessionContext;
import xyz.qy.implatform.session.UserSession;

import java.util.Arrays;

@Api(tags = "webrtc视频单人通话")
@RestController
@RequestMapping("/webrtc/private")
public class WebrtcController {

    @Autowired
    private IMClient imClient;

    @Autowired
    private ICEServerConfig iceServerConfig;

    @ApiOperation(httpMethod = "POST", value = "呼叫视频通话")
    @PostMapping("/call")
    public Result call(@RequestParam Long uid, @RequestBody String offer) {
        if(!imClient.isOnline(uid)){
            throw new GlobalException("对方目前不在线");
        }
        imClient.sendPrivateMessage(buildSendMessage(MessageType.RTC_CALL,uid,offer));
        return ResultUtils.success();
    }

    @ApiOperation(httpMethod = "POST", value = "接受视频通话")
    @PostMapping("/accept")
    public Result accept(@RequestParam Long uid,@RequestBody String answer) {
        imClient.sendPrivateMessage(buildSendMessage(MessageType.RTC_ACCEPT,uid,answer));
        return ResultUtils.success();
    }


    @ApiOperation(httpMethod = "POST", value = "拒绝视频通话")
    @PostMapping("/reject")
    public Result reject(@RequestParam Long uid) {
        imClient.sendPrivateMessage(buildSendMessage(MessageType.RTC_REJECT,uid,null));
        return ResultUtils.success();
    }

    @ApiOperation(httpMethod = "POST", value = "取消呼叫")
    @PostMapping("/cancel")
    public Result cancel(@RequestParam Long uid) {
        imClient.sendPrivateMessage(buildSendMessage(MessageType.RTC_CANCEL,uid,null));
        return ResultUtils.success();
    }

    @ApiOperation(httpMethod = "POST", value = "呼叫失败")
    @PostMapping("/failed")
    public Result failed(@RequestParam Long uid,@RequestParam String reason) {
        imClient.sendPrivateMessage(buildSendMessage(MessageType.RTC_FAILED,uid,reason));
        return ResultUtils.success();
    }

    @ApiOperation(httpMethod = "POST", value = "挂断")
    @PostMapping("/handup")
    public Result leave(@RequestParam Long uid) {
        imClient.sendPrivateMessage(buildSendMessage(MessageType.RTC_HANDUP,uid,null));
        return ResultUtils.success();
    }


    @PostMapping("/candidate")
    @ApiOperation(httpMethod = "POST", value = "同步candidate")
    public Result candidate(@RequestParam Long uid,@RequestBody String candidate ) {
        imClient.sendPrivateMessage(buildSendMessage(MessageType.RTC_CANDIDATE,uid,candidate));
        return ResultUtils.success();
    }

    @GetMapping("/iceservers")
    @ApiOperation(httpMethod = "GET", value = "获取iceservers")
    public Result iceservers() {
        return ResultUtils.success(iceServerConfig.getIceServers());
    }

    private IMPrivateMessage buildSendMessage(MessageType messageType, Long uid, String content){
        UserSession session  = SessionContext.getSession();
        PrivateMessageInfo messageInfo = new PrivateMessageInfo();
        messageInfo.setType(messageType.code());
        messageInfo.setRecvId(uid);
        messageInfo.setSendId(session.getUserId());
        messageInfo.setContent(content);

        IMPrivateMessage sendMessage = new IMPrivateMessage();
        sendMessage.setSendId(session.getUserId());
        sendMessage.setSendTerminal(session.getTerminal());
        sendMessage.setSendToSelf(false);
        sendMessage.setRecvId(uid);
        sendMessage.setDatas(Arrays.asList(messageInfo));
        return sendMessage;
    }
}
