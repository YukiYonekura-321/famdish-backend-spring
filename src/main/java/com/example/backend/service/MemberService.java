package com.example.backend.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.backend.dto.FamilyNameRequest;
import com.example.backend.dto.FamilySummaryResponse;
import com.example.backend.dto.IdNameResponse;
import com.example.backend.dto.LikeDislikeFullResponse;
import com.example.backend.dto.MemberAttributesRequest;
import com.example.backend.dto.MemberCreateRequest;
import com.example.backend.dto.MemberCreateResponse;
import com.example.backend.dto.MemberMeResponse;
import com.example.backend.dto.MemberIdResponse;
import com.example.backend.dto.MemberSummaryResponse;
import com.example.backend.dto.MemberUserResponse;
import com.example.backend.dto.NestedAttributeRequest;
import com.example.backend.entity.Dislike;
import com.example.backend.entity.Family;
import com.example.backend.entity.Like;
import com.example.backend.entity.Member;
import com.example.backend.entity.User;
import com.example.backend.exception.BadRequestException;
import com.example.backend.exception.ForbiddenException;
import com.example.backend.repository.FamilyRepository;
import com.example.backend.repository.MemberRepository;

@Service
public class MemberService {

    private final MemberRepository memberRepository;
    private final FamilyRepository familyRepository;
    private final UserService userService;

    public MemberService(MemberRepository memberRepository, FamilyRepository familyRepository, UserService userService) {
        this.memberRepository = memberRepository;
        this.familyRepository = familyRepository;
        this.userService = userService;
    }

    @Transactional(readOnly = true)
    public List<MemberSummaryResponse> index() {
        User currentUser = userService.getCurrentUser();
        Long familyId = currentUser.getFamilyId();
        if (familyId == null) {
            return List.of();
        }

        return memberRepository.findByFamilyId(familyId).stream()
                .map(this::toSummary)
                .toList();
    }

    @Transactional
    public MemberCreateResponse create(MemberCreateRequest request) {
        User currentUser = userService.getCurrentUser();
        boolean linkUser = request.linkUser() == null || request.linkUser();

        Family family = resolveFamily(request);

        Member member = new Member();
        member.setFamily(family);
        MemberAttributesRequest attrs = request.member();
        if (attrs != null) {
            member.setName(attrs.name());
        }
        if (linkUser) {
            member.setUser(currentUser);
        }
        applyLikes(member, attrs != null ? attrs.likesAttributes() : null);
        applyDislikes(member, attrs != null ? attrs.dislikesAttributes() : null);

        member = memberRepository.save(member);

        if (linkUser) {
            currentUser.setFamily(family);
            currentUser.setMember(member);
        }

        return toCreateResponse(member);
    }

    @Transactional
    public void update(Long id, MemberAttributesRequest attrs) {
        Member member = findAuthorizedMember(id);

        if (attrs != null) {
            if (attrs.name() != null) {
                member.setName(attrs.name());
            }
            applyLikes(member, attrs.likesAttributes());
            applyDislikes(member, attrs.dislikesAttributes());
        }

        memberRepository.save(member);
    }

    @Transactional
    public void destroy(Long id) {
        Member member = findAuthorizedMember(id);
        memberRepository.delete(member);
    }

    @Transactional(readOnly = true)
    public MemberMeResponse me() {
        User currentUser = userService.getCurrentUser();
        Family family = currentUser.getFamily();
        Member currentMember = memberRepository.findByUserId(currentUser.getId()).orElse(null);

        return new MemberMeResponse(
                family != null ? family.getId() : null,
                family != null ? family.getName() : null,
                currentMember != null ? currentMember.getName() : null,
                currentMember != null ? new MemberIdResponse(currentMember.getId()) : null
        );
    }

    @Transactional(readOnly = true)
    public List<IdNameResponse> all() {
        return memberRepository.findAll().stream()
                .map(m -> new IdNameResponse(m.getId(), m.getName()))
                .toList();
    }

    private Family resolveFamily(MemberCreateRequest request) {
        if (request.familyId() != null) {
            return familyRepository.findById(request.familyId())
                    .orElseThrow(() -> new BadRequestException("ファミリーが見つかりません"));
        }
        FamilyNameRequest familyName = request.family();
        Family family = new Family(familyName != null ? familyName.name() : null);
        return familyRepository.save(family);
    }

    private Member findAuthorizedMember(Long id) {
        User currentUser = userService.getCurrentUser();
        Member member = memberRepository.findByIdAndFamilyId(id, currentUser.getFamilyId())
                .orElseThrow(() -> new ForbiddenException("権限がありません"));

        if (member.getUser() != null && !member.getUser().getId().equals(currentUser.getId())) {
            throw new ForbiddenException("権限がありません");
        }
        return member;
    }

    private void applyLikes(Member member, List<NestedAttributeRequest> attrsList) {
        if (attrsList == null) {
            return;
        }
        for (NestedAttributeRequest attr : attrsList) {
            if (attr.id() != null) {
                Like like = member.getLikes().stream()
                        .filter(l -> attr.id().equals(l.getId()))
                        .findFirst()
                        .orElse(null);
                if (like == null) {
                    continue;
                }
                if (Boolean.TRUE.equals(attr.destroy())) {
                    member.getLikes().remove(like);
                } else {
                    like.setName(attr.name());
                }
            } else if (!Boolean.TRUE.equals(attr.destroy())) {
                Like like = new Like();
                like.setName(attr.name());
                like.setMember(member);
                member.getLikes().add(like);
            }
        }
    }

    private void applyDislikes(Member member, List<NestedAttributeRequest> attrsList) {
        if (attrsList == null) {
            return;
        }
        for (NestedAttributeRequest attr : attrsList) {
            if (attr.id() != null) {
                Dislike dislike = member.getDislikes().stream()
                        .filter(d -> attr.id().equals(d.getId()))
                        .findFirst()
                        .orElse(null);
                if (dislike == null) {
                    continue;
                }
                if (Boolean.TRUE.equals(attr.destroy())) {
                    member.getDislikes().remove(dislike);
                } else {
                    dislike.setName(attr.name());
                }
            } else if (!Boolean.TRUE.equals(attr.destroy())) {
                Dislike dislike = new Dislike();
                dislike.setName(attr.name());
                dislike.setMember(member);
                member.getDislikes().add(dislike);
            }
        }
    }

    private MemberSummaryResponse toSummary(Member member) {
        List<IdNameResponse> likes = member.getLikes().stream()
                .map(l -> new IdNameResponse(l.getId(), l.getName()))
                .toList();
        List<IdNameResponse> dislikes = member.getDislikes().stream()
                .map(d -> new IdNameResponse(d.getId(), d.getName()))
                .toList();
        List<IdNameResponse> menus = member.getMenus().stream()
                .map(m -> new IdNameResponse(m.getId(), m.getName()))
                .toList();
        MemberUserResponse user = member.getUser() != null
                ? new MemberUserResponse(member.getUser().getFirebaseUid())
                : null;

        return new MemberSummaryResponse(member.getId(), member.getName(), likes, dislikes, user, menus);
    }

    private MemberCreateResponse toCreateResponse(Member member) {
        List<LikeDislikeFullResponse> likes = member.getLikes().stream()
                .map(l -> new LikeDislikeFullResponse(l.getId(), l.getName(), member.getId(), l.getCreatedAt(), l.getUpdatedAt()))
                .toList();
        List<LikeDislikeFullResponse> dislikes = member.getDislikes().stream()
                .map(d -> new LikeDislikeFullResponse(d.getId(), d.getName(), member.getId(), d.getCreatedAt(), d.getUpdatedAt()))
                .toList();
        Family family = member.getFamily();
        FamilySummaryResponse familySummary = family != null
                ? new FamilySummaryResponse(family.getId(), family.getName())
                : null;

        return new MemberCreateResponse(
                member.getId(),
                member.getName(),
                member.getFamily() != null ? member.getFamily().getId() : null,
                member.getUser() != null ? member.getUser().getId() : null,
                member.getCreatedAt(),
                member.getUpdatedAt(),
                likes,
                dislikes,
                familySummary
        );
    }
}
