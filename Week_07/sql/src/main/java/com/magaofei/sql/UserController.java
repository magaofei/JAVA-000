package com.magaofei.sql;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author magaofei
 * @date 2020/12/2
 */
@RestController
@RequestMapping(value = "/api/user")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping()
    public ResponseEntity<Object> getHello(
            @RequestParam String dataSource
    ) {
        return ResponseEntity.ok().body(this.userService.getUser(dataSource));
    }
}
