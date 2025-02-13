package com.aggregator.controller;

import com.aggregator.model.UserDto;
import com.aggregator.model.UserRequest;
import com.aggregator.repository.UserRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("api/v1/users")
@Tag(name = "User API", description = "API for managing users across multiple databases")
public class UserController {

    private final UserRepository userRepository;

    @Autowired
    public UserController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Operation(summary = "Retrieve all users", description = "Fetches all users from all configured databases")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Users successfully retrieved",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = UserDto.class))),
            @ApiResponse(responseCode = "500", description = "Internal server error",
                    content = @Content(mediaType = "application/json"))
    })
    @GetMapping
    public List<UserDto> getAllUsers() {
        log.info("Received request to fetch all users");
        return userRepository.getAllUsers();
    }

    @Operation(summary = "Add a new user", description = "Adds a new user to all configured databases")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "User successfully added"),
            @ApiResponse(responseCode = "400", description = "Invalid request body",
                    content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "500", description = "Internal server error",
                    content = @Content(mediaType = "application/json"))
    })
    @PostMapping("/add")
    public ResponseEntity<String> addUser(@RequestBody UserRequest userRequest) {
        log.info("Received request to add user: {}", userRequest);
        try {
            userRepository.addUserToAllDatabases(userRequest);
            log.info("User added successfully: {}", userRequest);
            return ResponseEntity.ok("User added successfully to all databases");
        } catch (Exception e) {
            log.error("Failed to add user: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to add user: " + e.getMessage());
        }
    }
}

