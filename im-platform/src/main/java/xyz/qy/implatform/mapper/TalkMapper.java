package xyz.qy.implatform.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Param;
import xyz.qy.implatform.dto.TalkQueryDTO;
import xyz.qy.implatform.entity.Talk;

/**
 * @description:
 * @author: HouTianYun
 * @create: 2023-11-19 21:37
 **/
public interface TalkMapper extends BaseMapper<Talk> {
    /**
     * 分页查询所有动态
     *
     * @param page     分页对象
     * @param queryDTO 查询DTO
     * @return 动态列表
     */
    Page<Talk> pageQueryAllTalkList(@Param("page") Page<Object> page, @Param("queryDTO") TalkQueryDTO queryDTO);
}
