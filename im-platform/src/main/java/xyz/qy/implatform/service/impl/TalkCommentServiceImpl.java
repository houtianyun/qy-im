package xyz.qy.implatform.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import xyz.qy.implatform.entity.TalkComment;
import xyz.qy.implatform.mapper.TalkCommentMapper;
import xyz.qy.implatform.service.ITalkCommentService;

/**
 * @description: 动态评论
 * @author: HouTianYun
 * @create: 2023-12-24 15:42
 **/
@Service
public class TalkCommentServiceImpl extends ServiceImpl<TalkCommentMapper, TalkComment> implements ITalkCommentService {
}
