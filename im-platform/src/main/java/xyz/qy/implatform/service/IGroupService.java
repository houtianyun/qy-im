package xyz.qy.implatform.service;

import com.baomidou.mybatisplus.extension.service.IService;
import xyz.qy.implatform.entity.Group;
import xyz.qy.implatform.entity.GroupMember;
import xyz.qy.implatform.entity.User;
import xyz.qy.implatform.vo.CommonGroupVO;
import xyz.qy.implatform.vo.GroupInviteVO;
import xyz.qy.implatform.vo.GroupMemberVO;
import xyz.qy.implatform.vo.GroupVO;
import xyz.qy.implatform.vo.SwitchTemplateGroupVO;
import xyz.qy.implatform.vo.TemplateGroupCreateVO;

import java.util.List;


public interface IGroupService extends IService<Group> {


    GroupVO createGroup(String groupName);

    GroupVO modifyGroup(GroupVO vo);

    void deleteGroup(Long groupId);

    void quitGroup(Long groupId);

    void kickGroup(Long groupId,Long userId);

    List<GroupVO>  findGroups();

    void invite(GroupInviteVO vo);

    Group GetById(Long groupId);

    GroupVO findById(Long groupId);

    List<GroupMemberVO> findGroupMembers(Long groupId);

    GroupVO createTemplateGroup(TemplateGroupCreateVO templateGroupCreateVO);

    GroupVO switchTemplateGroup(SwitchTemplateGroupVO vo);

    GroupVO switchCommonGroup(CommonGroupVO vo);

    /**
     * 将用户加入公共群聊
     *
     * @param user 用户
     */
    GroupMember addToCommonGroup(User user);
}
