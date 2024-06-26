package boot.security.demo.controllers;

import boot.security.demo.model.Role;
import boot.security.demo.model.User;
import boot.security.demo.service.RoleService;
import boot.security.demo.service.UserService;
import org.hibernate.Hibernate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import javax.persistence.EntityNotFoundException;
import javax.validation.Valid;
import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin/")
public class AdminController {

    @Autowired
    private UserService userService;

    @Autowired
    private RoleService roleService;


    @GetMapping("/")
    public ModelAndView getAllUsers(@AuthenticationPrincipal User authUser) {
        Collection<Role> roles = authUser.getRoles();
        List<User> users = userService.getAllWithRoles();
        ModelAndView modelAndView = new ModelAndView("admin/allUsers");
        modelAndView.addObject("user", authUser);
        modelAndView.addObject("roles", roles);
        modelAndView.addObject("usersList", users);
        return modelAndView;
    }


    @RequestMapping(value = "/edit", method = RequestMethod.GET)
    public ModelAndView editPage(@RequestParam("id") int id) {
        User user = userService.getById(id)
                .orElseThrow(() -> new EntityNotFoundException("User with id " + id + " not found"));
        ModelAndView modelAndView = new ModelAndView("admin/editPage");
        modelAndView.addObject("user", user);
        modelAndView.addObject("allRoles", roleService.getAll());
        return modelAndView;
    }

    @RequestMapping(value = "/edit", method = RequestMethod.POST)
    public ModelAndView editUser(@ModelAttribute("user") @Valid User user, BindingResult bindingResult,
                                 @RequestParam(required = false) List<Integer> selectedRoles) {

        ModelAndView modelAndView = new ModelAndView();
        if (bindingResult.hasErrors()) {
            modelAndView.addObject("allRoles", roleService.getAll());
            modelAndView.setViewName("admin/editPage");
            return modelAndView;
        }

        if ( !userService.isUsernameUniqueForEdit(user.getName(), user.getId())) {
            bindingResult.rejectValue("name", "error.user", "Имя пользователя уже существует.");
            modelAndView.addObject("allRoles", roleService.getAll());
            modelAndView.setViewName("admin/editPage");
            return modelAndView;
        }
        if (selectedRoles != null) {
            Set<Role> roles = selectedRoles.stream()
                    .map(roleService::getById)
                    .collect(Collectors.toSet());
            user.setRoles(roles);
        } else {
            user.setRoles(new HashSet<>());
        }
        userService.edit(user);
        modelAndView.setViewName("redirect:/admin/");
        return modelAndView;
    }

    @RequestMapping(value = "/add", method = RequestMethod.GET)
    public ModelAndView addPage() {
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.addObject("user", new User());
        modelAndView.addObject("allRoles", roleService.getAll());
        modelAndView.setViewName("admin/addPage");
        return modelAndView;
    }

    @RequestMapping(value = "/add", method = RequestMethod.POST)
    public ModelAndView addUser(@ModelAttribute("user") @Valid User user, BindingResult bindingResult,
                                @RequestParam(required = false) List<Integer> selectedRoles) {

        ModelAndView modelAndView = new ModelAndView();

        if (bindingResult.hasErrors()) {
            modelAndView.addObject("allRoles", roleService.getAll());
            modelAndView.setViewName("admin/editPage");
            return modelAndView;
        }

        if (!userService.isUsernameUnique(user.getUsername())) {
            bindingResult.rejectValue("name", "error.user", "Имя пользователя уже существует.");
            modelAndView.addObject("allRoles", roleService.getAll());
            modelAndView.setViewName("admin/addPage");
            return modelAndView;
        }
        if (selectedRoles != null) {
            Set<Role> roles = selectedRoles.stream()
                    .map(roleService::getById)
                    .collect(Collectors.toSet());
            user.setRoles(roles);
        }
        userService.add(user);
        modelAndView.setViewName("redirect:/admin/");
        return modelAndView;
    }

    @RequestMapping(value="/delete", method = RequestMethod.GET)
    public ModelAndView deleteUser(@RequestParam("id") int id) {
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("redirect:/admin/");
        Optional<User> user = userService.getById(id);
        userService.delete(user.orElse(null));
        return modelAndView;
    }

}
