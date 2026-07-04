package com.example.backend.service;

import org.apache.coyote.BadRequestException;
import org.springframework.data.crossstore.ChangeSetPersister.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.backend.dto.AssignCookResponse;
import com.example.backend.dto.FamilyResponse;
import com.example.backend.entity.Family;
import com.example.backend.entity.Member;
import com.example.backend.entity.User;
import com.example.backend.repository.FamilyRepository;
import com.example.backend.repository.MemberRepository;

@Service
public class FamilyService {

    private final FamilyRepository familyRepository;
    private final MemberRepository memberRepository;
    private final UserService userService;

    public FamilyService(FamilyRepository familyRepository, MemberRepository memberRepository, UserService userService) {
        this.familyRepository = familyRepository;
        this.memberRepository = memberRepository;
        this.userService = userService;
    }

    @Transactional(readOnly = true)
    public FamilyResponse getMyFamily() {
        Family family = currentFamily();
        return new FamilyResponse(family.getTodayCookId());
    }

    @Transactional
    public AssignCookResponse assignCook(Long memberId) {
        Family family = currentFamily();

        Member member = memberRepository.findByIdAndFamilyId(memberId, family.getId())
                .orElseThrow(() -> new NotFoundException("メンバーが見つかりません"));

        family.setTodayCook(member);
        familyRepository.save(family);

        return new AssignCookResponse(family.getTodayCookId(), member.getName());
    }

    private Family currentFamily() {
        User currentUser = userService.getCurrentUser();
        Family family = currentUser.getFamily();
        if (family == null) {
            throw new BadRequestException("家族が見つかりません");
        }
        return family;
    }
}
