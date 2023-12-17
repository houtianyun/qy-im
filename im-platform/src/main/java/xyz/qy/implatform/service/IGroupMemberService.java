package xyz.qy.implatform.service;

import com.baomidou.mybatisplus.extension.service.IService;
import xyz.qy.implatform.entity.GroupMember;
import xyz.qy.implatform.vo.GroupMemberVO;
import xyz.qy.implatform.vo.SwitchCharacterAvatarVO;

import java.util.List;


public interface IGroupMemberService extends IService<GroupMember> {



    GroupMember findByGroupAndUserId(Long groupId,Long userId);

    List<GroupMember>  findByUserId(Long userId);

    List<GroupMember>  findByGroupId(Long groupId);

    List<GroupMember> findNoQuitGroupMembers(Long groupId);

    List<Long> findUserIdsByGroupId(Long groupId);

    List<Long> getAllGroupIdsByUserId(Long userId);

    List<Long> getAllGroupMemberIdsByUserId(Long userId);

    boolean save(GroupMember member);

    boolean saveOrUpdateBatch(Long groupId,List<GroupMember> members);

    void removeByGroupId(Long groupId);

    void removeByGroupAndUserId(Long groupId,Long userId);

    void switchTemplateCharacter(GroupMemberVO groupMemberVO);

    void switchCharacterAvatar(SwitchCharacterAvatarVO avatarVO);
}
