package xyz.qy.implatform.service;

import com.baomidou.mybatisplus.extension.service.IService;
import xyz.qy.implatform.entity.TemplateCharacter;
import xyz.qy.implatform.vo.SelectableTemplateCharacterVO;
import xyz.qy.implatform.vo.TemplateCharacterVO;
import xyz.qy.implatform.vo.TemplateGroupVO;

import java.util.List;

/**
 * @description:
 * @author: Polaris
 * @create: 2023-03-12 16:34
 **/
public interface ITemplateCharacterService extends IService<TemplateCharacter> {

    List<TemplateCharacterVO> findTemplateCharactersByGroupId(Long templateGroupId);

    /**
     * 查询模板群聊可选择的模板人物
     *
     * @param vo 入参
     * @return 模板群聊下可选择的模板人物
     */
    List<TemplateCharacterVO> findSelectableTemplateCharacter(SelectableTemplateCharacterVO vo);

    /**
     * 新增或修改模板人物信息
     *
     * @param templateGroupVO 模板群聊VO
     */
    void addOrModifyTemplateCharacters(TemplateGroupVO templateGroupVO);

    /**
     * 删除模板人物
     *
     * @param id 模板人物id
     */
    void delete(Long id);

    /**
     * 查询用户创建的模板群聊的模板人物数量
     *
     * @return 数量
     */
    int countUserTemplateCharacter(Long templateGroupId);
}
