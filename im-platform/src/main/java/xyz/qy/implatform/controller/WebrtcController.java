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
import xyz.qy.imcommon.model.PrivateMessageInfo;
import xyz.qy.implatform.config.ICEServerConfig;
import xyz.qy.implatform.enums.MessageType;
import xyz.qy.implatform.result.Result;
import xyz.qy.implatform.result.ResultUtils;
import xyz.qy.implatform.session.SessionContext;

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
        Long userId = SessionContext.getSession().getId();

        PrivateMessageInfo message = new PrivateMessageInfo();
        message.setType(MessageType.RTC_CALL.code());
        message.setRecvId(uid);
        message.setSendId(userId);
        message.setContent(offer);
        imClient.sendPrivateMessage(uid, message);
        return ResultUtils.success();
    }

    @ApiOperation(httpMethod = "POST", value = "接受视频通话")
    @PostMapping("/accept")
    public Result accept(@RequestParam Long uid, @RequestBody String answer) {
        Long userId = SessionContext.getSession().getId();

        PrivateMessageInfo message = new PrivateMessageInfo();
        message.setType(MessageType.RTC_ACCEPT.code());
        message.setRecvId(uid);
        message.setSendId(userId);
        message.setContent(answer);
        imClient.sendPrivateMessage(uid, message);
        return ResultUtils.success();
    }


    @ApiOperation(httpMethod = "POST", value = "拒绝视频通话")
    @PostMapping("/reject")
    public Result reject(@RequestParam Long uid) {
        Long userId = SessionContext.getSession().getId();
        PrivateMessageInfo message = new PrivateMessageInfo();
        message.setType(MessageType.RTC_REJECT.code());
        message.setRecvId(uid);
        message.setSendId(userId);
        imClient.sendPrivateMessage(uid, message);
        return ResultUtils.success();
    }

    @ApiOperation(httpMethod = "POST", value = "取消呼叫")
    @PostMapping("/cancel")
    public Result cancel(@RequestParam Long uid) {
        Long userId = SessionContext.getSession().getId();
        PrivateMessageInfo message = new PrivateMessageInfo();
        message.setType(MessageType.RTC_CANCEL.code());
        message.setRecvId(uid);
        message.setSendId(userId);
        imClient.sendPrivateMessage(uid, message);
        return ResultUtils.success();
    }

    @ApiOperation(httpMethod = "POST", value = "呼叫失败")
    @PostMapping("/failed")
    public Result failed(@RequestParam Long uid, @RequestParam String reason) {
        Long userId = SessionContext.getSession().getId();

        PrivateMessageInfo message = new PrivateMessageInfo();
        message.setType(MessageType.RTC_FAILED.code());
        message.setRecvId(uid);
        message.setSendId(userId);
        message.setContent(reason);
        imClient.sendPrivateMessage(uid, message);
        return ResultUtils.success();
    }

    @ApiOperation(httpMethod = "POST", value = "挂断")
    @PostMapping("/handup")
    public Result leave(@RequestParam Long uid) {
        Long userId = SessionContext.getSession().getId();

        PrivateMessageInfo message = new PrivateMessageInfo();
        message.setType(MessageType.RTC_HANDUP.code());
        message.setRecvId(uid);
        message.setSendId(userId);
        imClient.sendPrivateMessage(uid, message);
        return ResultUtils.success();
    }

    @PostMapping("/candidate")
    @ApiOperation(httpMethod = "POST", value = "同步candidate")
    public Result candidate(@RequestParam Long uid, @RequestBody String candidate) {
        Long userId = SessionContext.getSession().getId();
        PrivateMessageInfo message = new PrivateMessageInfo();
        message.setType(MessageType.RTC_CANDIDATE.code());
        message.setRecvId(uid);
        message.setSendId(userId);
        message.setContent(candidate);
        imClient.sendPrivateMessage(uid, message);
        return ResultUtils.success();
    }

    @GetMapping("/iceservers")
    @ApiOperation(httpMethod = "GET", value = "获取iceservers")
    public Result iceservers() {
        return ResultUtils.success(iceServerConfig.getIceServers());
    }
}
