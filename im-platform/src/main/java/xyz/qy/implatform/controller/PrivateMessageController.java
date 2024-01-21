package xyz.qy.implatform.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import xyz.qy.implatform.dto.PrivateMessageDTO;
import xyz.qy.implatform.result.Result;
import xyz.qy.implatform.result.ResultUtils;
import xyz.qy.implatform.service.IPrivateMessageService;
import xyz.qy.implatform.vo.PrivateMessageVO;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.List;

@Api(tags = "私聊消息")
@RestController
@RequestMapping("/message/private")
public class PrivateMessageController {

    @Autowired
    private IPrivateMessageService privateMessageService;

    @PostMapping("/send")
    @ApiOperation(value = "发送消息", notes = "发送私聊消息")
    public Result<Long> sendMessage(@Valid @RequestBody PrivateMessageDTO vo) {
        return ResultUtils.success(privateMessageService.sendMessage(vo));
    }

    @DeleteMapping("/recall/{id}")
    @ApiOperation(value = "撤回消息", notes = "撤回私聊消息")
    public Result<Long> recallMessage(@NotNull(message = "消息id不能为空") @PathVariable Long id) {
        privateMessageService.recallMessage(id);
        return ResultUtils.success();
    }

    @PostMapping("/pullUnreadMessage")
    @ApiOperation(value = "拉取未读消息", notes = "拉取未读消息")
    public Result pullUnreadMessage() {
        privateMessageService.pullUnreadMessage();
        return ResultUtils.success();
    }

    @GetMapping("/loadMessage")
    @ApiOperation(value = "拉取消息",notes="拉取消息,一次最多拉取100条")
    public Result<List<PrivateMessageVO>> loadMessage(@RequestParam Long minId){
        return ResultUtils.success(privateMessageService.loadMessage(minId));
    }

    @PutMapping("/readed")
    @ApiOperation(value = "消息已读",notes="将会话中接收的消息状态置为已读")
    public Result readedMessage(@RequestParam Long friendId){
        privateMessageService.readedMessage(friendId);
        return ResultUtils.success();
    }

    @GetMapping("/history")
    @ApiOperation(value = "查询聊天记录", notes = "查询聊天记录")
    public Result<List<PrivateMessageVO>> recallMessage(@NotNull(message = "好友id不能为空") @RequestParam Long friendId,
                                                        @NotNull(message = "页码不能为空") @RequestParam Long page,
                                                        @NotNull(message = "size不能为空") @RequestParam Long size) {
        return ResultUtils.success(privateMessageService.findHistoryMessage(friendId, page, size));
    }
}

