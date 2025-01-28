package org.gym.basic.controller;

import org.gym.basic.dto.UserPasswordDto;
import org.gym.basic.exception.InvalidDataException;
import org.gym.basic.service.ServiceException;
import org.gym.basic.service.UserService;
import io.micrometer.core.instrument.MeterRegistry;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import static org.gym.basic.utility.Validation.validateLogin;
import static org.gym.basic.utility.Validation.validatePassword;

@Controller
@RequestMapping("/user")
public class UserController {
    private UserService userService;
    private MeterRegistry meterRegistry;

    public UserController(UserService userService, MeterRegistry meterRegistry) {
        this.userService = userService;
        this.meterRegistry = meterRegistry;
    }

//    @GetMapping("/login")
//    @ResponseBody
//    @ResponseStatus(HttpStatus.OK)
//    @Operation(summary = "Login in App")
//    public void login(@RequestParam("username") String username, @RequestParam("password") String password) throws NoResourcePresentException, InvalidDataException {
//
//        meterRegistry.counter("login_counter", "version", "v1.0").increment();
//
//        if (validateLogin(username) && validatePassword(password)) {
//
//            meterRegistry.counter("Login_counter_by_username", "username", username).increment();
//
//            Optional<User> user = userService.findByUsernameAndPassword(username, password);
//
//            if (user.isPresent()) {
//
//                authBean.setUser(user.get());
//
//            } else {
//                throw new NoResourcePresentException("No such user present in DB");
//            }
//        }
//    }

    @PutMapping("/password")
    @ResponseBody
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Change password")
    public void changePassword(@RequestBody UserPasswordDto userPasswordDto) throws InvalidDataException, ServiceException {
        if (validateLogin(userPasswordDto.getUsername()) && validatePassword(userPasswordDto.getOldPassword()) && validatePassword(userPasswordDto.getNewPassword())) {

            userService.changePassword(userPasswordDto);

        }
    }
}
