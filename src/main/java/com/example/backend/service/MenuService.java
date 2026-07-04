package com.example.backend.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.backend.dto.IdNameResponse;
import com.example.backend.dto.MenuAttributesRequest;
import com.example.backend.dto.MenuIdResponse;
import com.example.backend.dto.MenuResponse;
import com.example.backend.entity.Member;
import com.example.backend.entity.Menu;
import com.example.backend.entity.User;
import com.example.backend.exception.ForbiddenException;
import com.example.backend.exception.NotFoundException;
import com.example.backend.repository.MenuRepository;

@Service
public class MenuService {

    private final MenuRepository menuRepository;
    private final UserService userService;

    public MenuService(MenuRepository menuRepository, UserService userService) {
        this.menuRepository = menuRepository;
        this.userService = userService;
    }

    @Transactional(readOnly = true)
    public List<MenuResponse> index() {
        User currentUser = userService.getCurrentUser();
        if (currentUser.getFamilyId() == null) {
            return List.of();
        }

        return menuRepository.findByMemberFamilyId(currentUser.getFamilyId()).stream()
                .map(menu -> new MenuResponse(
                        menu.getId(),
                        menu.getName(),
                        new IdNameResponse(menu.getMember().getId(), menu.getMember().getName())
                ))
                .toList();
    }

    @Transactional
    public MenuIdResponse create(MenuAttributesRequest attrs) {
        User currentUser = userService.getCurrentUser();
        Member member = currentUser.getMember();
        if (member == null) {
            throw new ForbiddenException("メンバーが見つかりません");
        }

        Menu menu = new Menu();
        menu.setMember(member);
        menu.setName(attrs != null ? attrs.name() : null);
        menu = menuRepository.save(menu);

        return new MenuIdResponse(menu.getId());
    }

    @Transactional
    public void update(Long id, MenuAttributesRequest attrs) {
        Menu menu = findOwnMenu(id);
        if (attrs != null) {
            menu.setName(attrs.name());
        }
        menuRepository.save(menu);
    }

    @Transactional
    public void destroy(Long id) {
        Menu menu = findOwnMenu(id);
        menuRepository.delete(menu);
    }

    private Menu findOwnMenu(Long id) {
        User currentUser = userService.getCurrentUser();
        return menuRepository.findByIdAndMemberUserId(id, currentUser.getId())
                .orElseThrow(() -> new NotFoundException("メニューが見つかりません"));
    }
}
