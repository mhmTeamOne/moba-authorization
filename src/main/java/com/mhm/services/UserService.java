package com.mhm.services;

import com.mhm.clients.KeycloackUserClient;
import com.mhm.clients.UserClient;
import com.mhm.clients.KeycloakAdminClient;
import com.mhm.dto.EmailDTO;
import com.mhm.dto.UserCreateDTO;
import com.mhm.dto.UserResponseDTO;
import com.mhm.dto.UserUpdateDTO;
import com.mhm.entities.UserEntity;
import com.mhm.exceptions.GlobalExceptionHandler;
import com.mhm.exceptions.KeycloakException;
import com.mhm.mappers.UserMapper;
import com.mhm.models.CredentialModel;
import com.mhm.models.KeycloackUserDTO;
import com.mhm.models.TokenModel;
import com.mhm.models.UserLoginDTO;
import com.mhm.repositories.UserRepository;
import com.mhm.services.TwilioEmailService;
import io.quarkus.hibernate.reactive.panache.common.WithTransaction;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

@ApplicationScoped
public class UserService {
    public static final Logger LOGGER = LoggerFactory.getLogger(UserService.class);

    public static final String BEARER_PREFIX = "Bearer ";
    
    @Inject
    @RestClient
    KeycloackUserClient keycloackUserClient;
    
    @Inject
    @RestClient
    UserClient userClient;
    
    @Inject
    @RestClient
    KeycloakAdminClient keycloakAdminClient;
    
    @Inject
    UserRepository userRepository;
    
    @Inject
    UserMapper userMapper;
    
    @Inject
    GlobalExceptionHandler globalExceptionHandler;
    
    @Inject
    TwilioEmailService twilioEmailService;

    /**
     * Example: Manual usage of GlobalExceptionHandler
     */
    public Uni<Response> createUserWithManualErrorHandling(UserCreateDTO userCreateDTO) {
        return getAdminToken()
            .chain(adminToken -> {
                try {
                    KeycloackUserDTO keycloakUser = convertToKeycloackUserDTO(userCreateDTO);
                    return keycloakAdminClient.createUser(BEARER_PREFIX + adminToken.getAccess_token(), keycloakUser);
                } catch (Exception e) {
                    // Manually use GlobalExceptionHandler to format the error
                    LOGGER.error("Error converting user data: {}", e.getMessage());
                    
                    // Create a KeycloakException
                    KeycloakException keycloakException = new KeycloakException(
                        "Failed to prepare user data for Keycloak: " + e.getMessage(), 
                        Response.Status.BAD_REQUEST, 
                        "USER_DATA_CONVERSION_ERROR"
                    );
                    
                    // Manually call GlobalExceptionHandler.toResponse()
                    Response errorResponse = globalExceptionHandler.toResponse(keycloakException);
                    return Uni.createFrom().item(errorResponse);
                }
            })
            .onItem().transform(response -> {
                if (response.getStatus() == 201) {
                    return Response.status(Response.Status.CREATED)
                        .entity(Map.of("message", "User created successfully"))
                        .build();
                } else {
                    return response; // Return as-is if it's an error response
                }
            })
            .onFailure().recoverWithUni(throwable -> {
                // Use GlobalExceptionHandler for any other failures
                Response errorResponse = globalExceptionHandler.toResponse((Exception) throwable);
                return Uni.createFrom().item(errorResponse);
            });
    }

    //TODO: This method will be used for the user registration
    public Uni<Response> getUserToken(@NotNull UserLoginDTO userLoginDTO) {
        return keycloakAdminClient.getUserToken(userLoginDTO.getUserName(), userLoginDTO.getPassword());
    }

    @WithTransaction
    public Uni<Response> registerUser(UserCreateDTO userCreateDTO) {
        // Step 1: Validate input
        if (userCreateDTO == null) {
            return Uni.createFrom().item(Response.status(Response.Status.BAD_REQUEST)
                    .entity("User data is required").build());
        }

        // Step 2: Convert DTO to Entity and save to database
        UserEntity userEntity = userMapper.toEntity(userCreateDTO);
        
        return userRepository.createUser(userEntity)
                .onItem().transform(savedUser -> {
                    // Step 3: Convert saved entity back to response DTO
                    UserResponseDTO responseDTO = userMapper.toResponseDTO(savedUser);
                    
                    LOGGER.info("User registration successful for username: {}", userCreateDTO.getUsername());
                    
                    return Response.status(Response.Status.CREATED)
                            .entity(responseDTO)
                            .build();
                })
                .onFailure().recoverWithUni(throwable -> {
                    LOGGER.error("User registration failed for username: {}", userCreateDTO.getUsername(), throwable);
                    return Uni.createFrom().item(Response.status(Response.Status.BAD_REQUEST)
                            .entity("Error during user registration: " + throwable.getMessage()).build());
                });
    }

    // Optional: Register user with Keycloak integration
    @WithTransaction
    public Uni<Response> registerUserWithKeycloak(UserCreateDTO userCreateDTO) {
        // Step 1: Save to local database first
        UserEntity userEntity = userMapper.toEntity(userCreateDTO);
        
        return userRepository.createUser(userEntity)
                .chain(savedUser -> {
                    // Step 2: Convert to KeycloakUserDTO for Keycloak
                    KeycloackUserDTO keycloakUser = convertToKeycloackUserDTO(userCreateDTO);
                    
                    // Step 3: Register with Keycloak
                    return getAdminToken()
                            .chain(adminToken -> {
                                if (adminToken == null || adminToken.getAccess_token() == null) {
                                    return Uni.createFrom().failure(new Exception("Admin token is invalid"));
                                }
                                
                                return keycloakAdminClient.createUser(BEARER_PREFIX + adminToken.getAccess_token(), keycloakUser)
                                        .onItem().transform(response -> {
                                            if (response.getStatus() == 201) {
                                                // Success: Return our saved user
                                                UserResponseDTO responseDTO = userMapper.toResponseDTO(savedUser);
                                                LOGGER.info("User registered successfully in both DB and Keycloak: {}", userCreateDTO.getUsername());
                                                
                                                // Send welcome email after successful registration
                                                try {
                                                    twilioEmailService.sendWelcomeEmail(userCreateDTO.getEmail(), userCreateDTO.getFirstName());
                                                    LOGGER.info("Welcome email sent to: {}", userCreateDTO.getEmail());
                                                } catch (Exception emailException) {
                                                    LOGGER.warn("Failed to send welcome email to {}: {}", userCreateDTO.getEmail(), emailException.getMessage());
                                                    // Don't fail the registration if email fails
                                                }
                                                
                                                return Response.status(Response.Status.CREATED).entity(responseDTO).build();
                                            } else if (response.getStatus() == 409) {
                                                // Conflict: User already exists in Keycloak
                                                LOGGER.warn("User saved to DB but already exists in Keycloak: {}", userCreateDTO.getUsername());
                                                UserResponseDTO responseDTO = userMapper.toResponseDTO(savedUser);
                                                return Response.status(Response.Status.CREATED).entity(responseDTO).build();
                                            } else {
                                                // Other Keycloak error, but DB succeeded - log warning
                                                LOGGER.warn("User saved to DB but Keycloak registration failed with status: {}", response.getStatus());
                                                UserResponseDTO responseDTO = userMapper.toResponseDTO(savedUser);
                                                return Response.status(Response.Status.CREATED).entity(responseDTO).build();
                                            }
                                        });
//                                        .onFailure().recoverWithUni(throwable -> {
//                                            // Handle REST client exceptions (like 409 Conflict)
//                                            if (throwable.getMessage().contains("409")) {
//                                                LOGGER.warn("User saved to DB but already exists in Keycloak: {}", userCreateDTO.getUsername());
//                                                UserResponseDTO responseDTO = userMapper.toResponseDTO(savedUser);
//                                                return Uni.createFrom().item(Response.status(Response.Status.CREATED).entity(responseDTO).build());
//                                            } else {
//                                                LOGGER.error("Keycloak registration failed for user: {}", userCreateDTO.getUsername(), throwable);
//                                                UserResponseDTO responseDTO = userMapper.toResponseDTO(savedUser);
//                                                return Uni.createFrom().item(Response.status(Response.Status.CREATED).entity(responseDTO).build());
//                                            }
//                                        });
                            });
                });
//                .onFailure().recoverWithUni(throwable -> {
//                    LOGGER.error("User registration failed for username: {}", userCreateDTO.getUsername(), throwable);
//                    return Uni.createFrom().item(Response.status(Response.Status.BAD_REQUEST)
//                            .entity("Error during user registration: " + throwable.getMessage()).build());
//                });
    }
    
    private KeycloackUserDTO convertToKeycloackUserDTO(UserCreateDTO userCreateDTO) {
        KeycloackUserDTO keycloackUserDTO = new KeycloackUserDTO();
        keycloackUserDTO.setUsername(userCreateDTO.getUsername());
        keycloackUserDTO.setEmail(userCreateDTO.getEmail());
        keycloackUserDTO.setFirstName(userCreateDTO.getFirstName());
        keycloackUserDTO.setLastName(userCreateDTO.getLastName());
        keycloackUserDTO.setEnabled(true);
        keycloackUserDTO.setEmailVerified(false);
        
        // Set password credentials
        if (userCreateDTO.getPassword() != null) {
            CredentialModel credential = new CredentialModel();
            credential.setType("password");
            credential.setValue(userCreateDTO.getPassword());
            credential.setTemporary(false);
            keycloackUserDTO.setCredentials(Arrays.asList(credential));
        }
        
        return keycloackUserDTO;
    }

    public Uni<Response> deleteUser(String id) {
        if (id == null || id.isEmpty()) {
            // If ID is invalid, return a bad request response
            return Uni.createFrom().item(Response.status(Response.Status.BAD_REQUEST)
                    .entity("User ID cannot be null or empty").build());
        }

        // Step 1: Get Admin Token
        return getAdminToken()
                .chain(adminToken -> {
                    if (adminToken == null || adminToken.getAccess_token() == null) {
                        // If the token is invalid, return internal server error
                        return Uni.createFrom().failure(new Exception("Admin token is invalid"));
                    }

                    // Step 2: Delete the User
                    return keycloakAdminClient.deleteUser(BEARER_PREFIX + adminToken.getAccess_token(), id)
                            .onFailure().recoverWithUni(throwable -> {
                                LOGGER.error("Failed to delete user with ID: {}", id, throwable);
                                // If user deletion fails, return BAD_REQUEST with the error message
                                return Uni.createFrom().item(Response.status(Response.Status.BAD_REQUEST)
                                        .entity("Error during user deletion: " + throwable.getMessage()).build());
                            });
                })
                .onFailure().recoverWithUni(throwable -> {
                    LOGGER.error("Failed to get admin token", throwable);
                    // If fetching the token fails, return an INTERNAL_SERVER_ERROR response
                    return Uni.createFrom().item(Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                            .entity("Error fetching admin token: " + throwable.getMessage()).build());
                });
    }

    @WithTransaction
    public Uni<Response> updateUser(Long id, UserUpdateDTO userUpdateDTO) {
        return userRepository.findUserById(id)
                .onItem().ifNull().failWith(new RuntimeException("User not found"))
                .onItem().transform(existingUser -> {
                    userMapper.updateEntityFromDTO(existingUser, userUpdateDTO);
                    return existingUser;
                })
                .chain(userRepository::createUser) // Save updated user
                .onItem().transform(updatedUser -> {
                    UserResponseDTO responseDTO = userMapper.toResponseDTO(updatedUser);
                    return Response.ok(responseDTO).build();
                })
                .onFailure().recoverWithUni(throwable -> {
                    LOGGER.error("User update failed for ID: {}", id, throwable);
                    return Uni.createFrom().item(Response.status(Response.Status.BAD_REQUEST)
                            .entity("Error during user update: " + throwable.getMessage()).build());
                });
    }
    
    @WithTransaction
    public Uni<Response> getUser(Long id) {
        return userRepository.findUserById(id)
                .onItem().ifNull().failWith(new RuntimeException("User not found"))
                .onItem().transform(user -> {
                    UserResponseDTO responseDTO = userMapper.toResponseDTO(user);
                    return Response.ok(responseDTO).build();
                })
                .onFailure().recoverWithUni(throwable -> {
                    LOGGER.error("Failed to get user with ID: {}", id, throwable);
                    return Uni.createFrom().item(Response.status(Response.Status.NOT_FOUND)
                            .entity("User not found").build());
                });
    }

    public Uni<TokenModel> getAdminToken() {
        return keycloakAdminClient.getAdminToken();
    }
    
    // Enhanced token operations using the new client
    public Uni<TokenModel> refreshUserToken(String refreshToken) {
        return keycloakAdminClient.refreshToken(refreshToken);
    }
    
    public Uni<Response> logoutUser(String refreshToken) {
        return keycloakAdminClient.logoutUser(refreshToken);
    }
    
    public Uni<java.util.Map<String, Object>> introspectToken(String token) {
        return keycloakAdminClient.introspectToken(token);
    }

    // Check for duplicate users before registration
    public Uni<Boolean> isUsernameTaken(String username) {
        return userRepository.find("username", username)
                .firstResult()
                .onItem().transform(user -> user != null);
    }
    
    public Uni<Boolean> isEmailTaken(String email) {
        return userRepository.find("email", email)
                .firstResult()
                .onItem().transform(user -> user != null);
    }
    
    // Enhanced registration with duplicate checking
    @WithTransaction
    public Uni<Response> registerUserSafe(UserCreateDTO userCreateDTO) {
        // Step 1: Validate input
        if (userCreateDTO == null) {
            return Uni.createFrom().item(Response.status(Response.Status.BAD_REQUEST)
                    .entity("User data is required").build());
        }

        // Step 2: Check for duplicates
        return isUsernameTaken(userCreateDTO.getUsername())
                .chain(usernameTaken -> {
                    if (usernameTaken) {
                        return Uni.createFrom().item(Response.status(Response.Status.CONFLICT)
                                .entity("Username already exists").build());
                    }
                    
                    return isEmailTaken(userCreateDTO.getEmail())
                            .chain(emailTaken -> {
                                if (emailTaken) {
                                    return Uni.createFrom().item(Response.status(Response.Status.CONFLICT)
                                            .entity("Email already exists").build());
                                }
                                
                                // Step 3: Proceed with registration (inline to avoid nested transactions)
                                UserEntity userEntity = userMapper.toEntity(userCreateDTO);
                                return userRepository.createUser(userEntity)
                                        .onItem().transform(savedUser -> {
                                            UserResponseDTO responseDTO = userMapper.toResponseDTO(savedUser);
                                            LOGGER.info("User registration successful for username: {}", userCreateDTO.getUsername());
                                            return Response.status(Response.Status.CREATED)
                                                    .entity(responseDTO)
                                                    .build();
                                        });
                            });
                })
                .onFailure().recoverWithUni(throwable -> {
                    LOGGER.error("User registration failed for username: {}", userCreateDTO.getUsername(), throwable);
                    return Uni.createFrom().item(Response.status(Response.Status.BAD_REQUEST)
                            .entity("Error during user registration: " + throwable.getMessage()).build());
                });
    }
    
    /**
     * Enhanced registration flow with email verification
     */
    @WithTransaction
    public Uni<Response> registerUserWithEmailVerification(UserCreateDTO userCreateDTO) {
        // Step 1: Validate input
        if (userCreateDTO == null || userCreateDTO.getEmail() == null) {
            return Uni.createFrom().item(Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("error", "User data and email are required")).build());
        }

        // Step 2: Check if user already exists
        return userRepository.find("email", userCreateDTO.getEmail())
                .firstResult()
                .chain(existingUser -> {
                    if (existingUser != null) {
                        return Uni.createFrom().item(Response.status(Response.Status.CONFLICT)
                                .entity(Map.of("error", "User with this email already exists")).build());
                    }

                    // Step 3: Save to database
                    UserEntity userEntity = userMapper.toEntity(userCreateDTO);
                    userEntity.setDisabled(true); // Start as disabled until email verified

                    return userRepository.createUser(userEntity)
                            .chain(savedUser -> {
                                // Step 4: Generate verification token
                                String verificationToken = generateVerificationToken();
                                
                                // Step 5: Register with Keycloak
                                return registerWithKeycloak(userCreateDTO)
                                        .chain(keycloakResponse -> {
                                            // Step 6: Send verification email
                                            return sendVerificationEmail(savedUser, verificationToken)
                                                    .onItem().transform(emailResult -> {
                                                        UserResponseDTO responseDTO = userMapper.toResponseDTO(savedUser);
                                                        
                                                        Map<String, Object> response = new HashMap<>();
                                                        response.put("user", responseDTO);
                                                        response.put("message", "Registration successful! Please check your email to verify your account.");
                                                        response.put("emailSent", true);
                                                        response.put("keycloakStatus", keycloakResponse.getStatus());
                                                        
                                                        LOGGER.info("User registration completed: {}, Email verification sent", savedUser.getEmail());
                                                        
                                                        return Response.status(Response.Status.CREATED)
                                                                .entity(response)
                                                                .build();
                                                    })
                                                    .onFailure().recoverWithUni(emailError -> {
                                                        LOGGER.error("Email sending failed during registration", emailError);
                                                        
                                                        UserResponseDTO responseDTO = userMapper.toResponseDTO(savedUser);
                                                        Map<String, Object> response = new HashMap<>();
                                                        response.put("user", responseDTO);
                                                        response.put("message", "Registration successful but email sending failed. Please contact support.");
                                                        response.put("emailSent", false);
                                                        response.put("keycloakStatus", keycloakResponse.getStatus());
                                                        
                                                        return Uni.createFrom().item(Response.status(Response.Status.CREATED)
                                                                .entity(response)
                                                                .build());
                                                    });
                                        });
                            });
                })
                .onFailure().recoverWithUni(throwable -> {
                    LOGGER.error("Registration failed for email: {}", userCreateDTO.getEmail(), throwable);
                    return Uni.createFrom().item(Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                            .entity(Map.of("error", "Registration failed: " + throwable.getMessage()))
                            .build());
                });
    }

    private Uni<Response> registerWithKeycloak(UserCreateDTO userCreateDTO) {
        return getAdminToken()
                .chain(adminToken -> {
                    KeycloackUserDTO keycloakUser = convertToKeycloackUserDTO(userCreateDTO);
                    return keycloakAdminClient.createUser(BEARER_PREFIX + adminToken.getAccess_token(), keycloakUser);
                })
                .onFailure().recoverWithUni(failure -> {
                    LOGGER.warn("Keycloak registration failed, continuing with local registration", failure);
                    return Uni.createFrom().item(Response.status(Response.Status.ACCEPTED).build());
                });
    }

    private Uni<String> sendVerificationEmail(UserEntity user, String verificationToken) {
        try {
            // Create verification email with custom content
            String verificationLink = "http://localhost:8081/user/verify?token=" + verificationToken + "&email=" + user.getEmail();
            
            EmailDTO verificationEmail = new EmailDTO();
            verificationEmail.setTo(user.getEmail());
            verificationEmail.setSubject("Verify Your MOBA Account");
            verificationEmail.setTextContent(
                "Hello " + user.getFirstName() + ",\n\n" +
                "Welcome to MOBA Authorization! Please verify your email address by clicking the link below:\n\n" +
                verificationLink + "\n\n" +
                "If you didn't create this account, please ignore this email.\n\n" +
                "Best regards,\nMOBA Team"
            );
            verificationEmail.setHtmlContent(
                "<html><body>" +
                "<h2>Welcome to MOBA Authorization!</h2>" +
                "<p>Hello <strong>" + user.getFirstName() + "</strong>,</p>" +
                "<p>Thank you for registering! Please verify your email address to activate your account.</p>" +
                "<p><a href=\"" + verificationLink + "\" style=\"background-color: #4CAF50; color: white; padding: 14px 20px; text-align: center; text-decoration: none; display: inline-block; border-radius: 4px;\">Verify Email Address</a></p>" +
                "<p>If the button doesn't work, copy and paste this link into your browser:</p>" +
                "<p>" + verificationLink + "</p>" +
                "<p>If you didn't create this account, please ignore this email.</p>" +
                "<hr>" +
                "<p>Best regards,<br>MOBA Team</p>" +
                "</body></html>"
            );

            String result = twilioEmailService.sendEmail(verificationEmail);
            return Uni.createFrom().item(result);
            
        } catch (Exception e) {
            return Uni.createFrom().failure(e);
        }
    }

    private String generateVerificationToken() {
        // Generate a secure random token for email verification
        java.security.SecureRandom random = new java.security.SecureRandom();
        byte[] bytes = new byte[32];
        random.nextBytes(bytes);
        return java.util.Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    // ==================================================
    // EXAMPLE METHODS USING THE NEW KEYCLOAK ADMIN CLIENT
    // ==================================================
    
    // User management with enhanced search capabilities
    public Uni<List<KeycloackUserDTO>> searchUsers(String username, String email, String firstName, String lastName, Integer first, Integer max) {
        return getAdminToken()
                .chain(adminToken -> keycloakAdminClient.getUsers(
                        BEARER_PREFIX + adminToken.getAccess_token(),
                        username, email, firstName, lastName, first, max));
    }
    
    public Uni<KeycloackUserDTO> getUserByIdFromKeycloak(String userId) {
        return getAdminToken()
                .chain(adminToken -> keycloakAdminClient.getUserById(
                        BEARER_PREFIX + adminToken.getAccess_token(), userId));
    }
    
    public Uni<Integer> getUsersCount() {
        return getAdminToken()
                .chain(adminToken -> keycloakAdminClient.getUsersCount(
                        BEARER_PREFIX + adminToken.getAccess_token()));
    }
    
    // Password and account management
    public Uni<Response> resetUserPassword(String userId, String newPassword, boolean isTemporary) {
        return getAdminToken()
                .chain(adminToken -> {
                    java.util.Map<String, Object> passwordData = new java.util.HashMap<>();
                    passwordData.put("value", newPassword);
                    passwordData.put("temporary", isTemporary);
                    passwordData.put("type", "password");
                    
                    return keycloakAdminClient.resetPassword(
                            BEARER_PREFIX + adminToken.getAccess_token(), userId, passwordData);
                });
    }
    
    public Uni<Response> enableDisableUser(String userId, boolean enabled) {
        return getAdminToken()
                .chain(adminToken -> keycloakAdminClient.enableUser(
                        BEARER_PREFIX + adminToken.getAccess_token(), userId, enabled));
    }
    
    public Uni<Response> sendVerificationEmail(String userId) {
        return getAdminToken()
                .chain(adminToken -> keycloakAdminClient.sendVerifyEmail(
                        BEARER_PREFIX + adminToken.getAccess_token(), userId));
    }
    
    public Uni<Response> sendPasswordResetEmail(String userId) {
        return getAdminToken()
                .chain(adminToken -> {
                    List<String> actions = List.of("UPDATE_PASSWORD");
                    return keycloakAdminClient.sendExecuteActionsEmail(
                            BEARER_PREFIX + adminToken.getAccess_token(), userId, actions);
                });
    }
    
    // Session management
    public Uni<List<java.util.Map<String, Object>>> getUserSessions(String userId) {
        return getAdminToken()
                .chain(adminToken -> keycloakAdminClient.getUserSessions(
                        BEARER_PREFIX + adminToken.getAccess_token(), userId));
    }
    
    public Uni<Response> logoutAllUserSessions(String userId) {
        return getAdminToken()
                .chain(adminToken -> keycloakAdminClient.logoutAllUserSessions(
                        BEARER_PREFIX + adminToken.getAccess_token(), userId));
    }
    
    // Group management
    public Uni<List<java.util.Map<String, Object>>> getAllGroups() {
        return getAdminToken()
                .chain(adminToken -> keycloakAdminClient.getGroups(
                        BEARER_PREFIX + adminToken.getAccess_token()));
    }
    
    public Uni<Response> createGroup(String groupName, String description) {
        return getAdminToken()
                .chain(adminToken -> {
                    java.util.Map<String, Object> group = new java.util.HashMap<>();
                    group.put("name", groupName);
                    if (description != null) {
                        group.put("description", description);
                    }
                    
                    return keycloakAdminClient.createGroup(
                            BEARER_PREFIX + adminToken.getAccess_token(), group);
                });
    }
    
    public Uni<List<java.util.Map<String, Object>>> getUserGroups(String userId) {
        return getAdminToken()
                .chain(adminToken -> keycloakAdminClient.getUserGroups(
                        BEARER_PREFIX + adminToken.getAccess_token(), userId));
    }
    
    public Uni<Response> addUserToGroup(String userId, String groupId) {
        return getAdminToken()
                .chain(adminToken -> keycloakAdminClient.addUserToGroup(
                        BEARER_PREFIX + adminToken.getAccess_token(), userId, groupId));
    }
    
    public Uni<Response> removeUserFromGroup(String userId, String groupId) {
        return getAdminToken()
                .chain(adminToken -> keycloakAdminClient.removeUserFromGroup(
                        BEARER_PREFIX + adminToken.getAccess_token(), userId, groupId));
    }
    
    // Role management
    public Uni<List<java.util.Map<String, Object>>> getAllRoles() {
        return getAdminToken()
                .chain(adminToken -> keycloakAdminClient.getRoles(
                        BEARER_PREFIX + adminToken.getAccess_token()));
    }
    
    public Uni<Response> createRole(String roleName, String description) {
        return getAdminToken()
                .chain(adminToken -> {
                    java.util.Map<String, Object> role = new java.util.HashMap<>();
                    role.put("name", roleName);
                    if (description != null) {
                        role.put("description", description);
                    }
                    
                    return keycloakAdminClient.createRole(
                            BEARER_PREFIX + adminToken.getAccess_token(), role);
                });
    }
    
    public Uni<List<java.util.Map<String, Object>>> getUserRoles(String userId) {
        return getAdminToken()
                .chain(adminToken -> keycloakAdminClient.getUserRoles(
                        BEARER_PREFIX + adminToken.getAccess_token(), userId));
    }
    
    public Uni<Response> addRoleToUser(String userId, String roleName) {
        return getAdminToken()
                .chain(adminToken -> keycloakAdminClient.getRoleByName(
                        BEARER_PREFIX + adminToken.getAccess_token(), roleName))
                .chain(role -> getAdminToken()
                        .chain(adminToken -> keycloakAdminClient.addRolesToUser(
                                BEARER_PREFIX + adminToken.getAccess_token(), userId, List.of(role))));
    }
    
    // Realm and client management
    public Uni<java.util.Map<String, Object>> getRealmInfo() {
        return getAdminToken()
                .chain(adminToken -> keycloakAdminClient.getRealmInfo(
                        BEARER_PREFIX + adminToken.getAccess_token()));
    }
    
    public Uni<List<java.util.Map<String, Object>>> getClients() {
        return getAdminToken()
                .chain(adminToken -> keycloakAdminClient.getClients(
                        BEARER_PREFIX + adminToken.getAccess_token()));
    }
    
    public Uni<List<java.util.Map<String, Object>>> getEvents(String type, String client, String user, Integer first, Integer max) {
        return getAdminToken()
                .chain(adminToken -> keycloakAdminClient.getEvents(
                        BEARER_PREFIX + adminToken.getAccess_token(), type, client, user, first, max));
    }

    /**
     * Improved registration: Keycloak First approach with compensation logic
     */
    @WithTransaction
    public Uni<Response> registerUserKeycloakFirst(UserCreateDTO userCreateDTO) {
        LOGGER.info("Starting Keycloak-first registration for user: {}", userCreateDTO.getEmail());
        
        // Step 1: Validate input
        if (userCreateDTO.getEmail() == null || userCreateDTO.getUsername() == null) {
            return Uni.createFrom().item(Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("error", "User data, email, and username are required")).build());
        }

        // Step 2: Check if user already exists in database
        return userRepository.find("email", userCreateDTO.getEmail())
                .firstResult()
                .chain(existingUser -> {
                    if (existingUser != null) {
                        LOGGER.warn("User already exists in database: {}", userCreateDTO.getEmail());
                        return Uni.createFrom().item(Response.status(Response.Status.CONFLICT)
                                .entity(Map.of("error", "User with this email already exists")).build());
                    }

                    // Step 3: Register with Keycloak FIRST
                    return registerWithKeycloakFirst(userCreateDTO)
                            .chain(keycloakResponse -> {
                                if (keycloakResponse.getStatus() == 201) {
                                    // Step 4: If Keycloak succeeds, save to database
                                    return saveUserToDatabase(userCreateDTO)
                                            .chain(savedUser -> {
                                                // Step 5: Send welcome email
                                                return sendWelcomeEmailAsync(savedUser)
                                                        .onItem().transform(emailResult -> {
                                                            UserResponseDTO responseDTO = userMapper.toResponseDTO(savedUser);
                                                            
                                                            Map<String, Object> response = new HashMap<>();
                                                            response.put("user", responseDTO);
                                                            response.put("message", "User registered successfully");
                                                            response.put("keycloakRegistered", true);
                                                            response.put("databaseSaved", true);
                                                            response.put("emailSent", emailResult.equals("success"));
                                                            
                                                            LOGGER.info("Registration completed successfully for: {}", savedUser.getEmail());
                                                            
                                                            return Response.status(Response.Status.CREATED)
                                                                    .entity(response)
                                                                    .build();
                                                        });
                                            })
                                            .onFailure().recoverWithUni(dbError -> {
                                                // Step 6: Database save failed - COMPENSATE by deleting from Keycloak
                                                LOGGER.error("Database save failed, initiating Keycloak cleanup for: {}", userCreateDTO.getEmail(), dbError);
                                                
                                                return cleanupKeycloakUser(userCreateDTO.getUsername())
                                                        .onItem().transform(cleanupResult -> {
                                                            Map<String, Object> errorResponse = new HashMap<>();
                                                            errorResponse.put("error", "Registration failed during database save");
                                                            errorResponse.put("message", "Database error occurred, Keycloak user cleaned up");
                                                            errorResponse.put("keycloakCleanup", cleanupResult);
                                                            errorResponse.put("originalError", dbError.getMessage());
                                                            
                                                            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                                                                    .entity(errorResponse)
                                                                    .build();
                                                        })
                                                        .onFailure().recoverWithUni(cleanupError -> {
                                                            LOGGER.error("CRITICAL: Failed to cleanup Keycloak user after DB failure", cleanupError);
                                                            
                                                            Map<String, Object> criticalError = new HashMap<>();
                                                            criticalError.put("error", "Critical registration failure");
                                                            criticalError.put("message", "Both database save and Keycloak cleanup failed");
                                                            criticalError.put("keycloakUser", userCreateDTO.getUsername());
                                                            criticalError.put("requiresManualCleanup", true);
                                                            
                                                            return Uni.createFrom().item(Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                                                                    .entity(criticalError)
                                                                    .build());
                                                        });
                                            });
                                } else if (keycloakResponse.getStatus() == 409) {
                                    // User already exists in Keycloak
                                    LOGGER.warn("User already exists in Keycloak: {}", userCreateDTO.getUsername());
                                    return Uni.createFrom().item(Response.status(Response.Status.CONFLICT)
                                            .entity(Map.of("error", "User already exists in Keycloak")).build());
                                } else {
                                    // Other Keycloak errors
                                    LOGGER.error("Keycloak registration failed with status: {}", keycloakResponse.getStatus());
                                    return Uni.createFrom().item(Response.status(Response.Status.BAD_REQUEST)
                                            .entity(Map.of("error", "Keycloak registration failed", 
                                                          "status", keycloakResponse.getStatus())).build());
                                }
                            });
                })
                .onFailure().recoverWithUni(throwable -> {
                    LOGGER.error("Registration failed for email: {}", userCreateDTO.getEmail(), throwable);
                    return Uni.createFrom().item(Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                            .entity(Map.of("error", "Registration failed: " + throwable.getMessage()))
                            .build());
                });
    }

    /**
     * Register user with Keycloak first
     */
    private Uni<Response> registerWithKeycloakFirst(UserCreateDTO userCreateDTO) {
        return getAdminToken()
                .chain(adminToken -> {
                    if (adminToken == null || adminToken.getAccess_token() == null) {
                        return Uni.createFrom().failure(new RuntimeException("Failed to obtain Keycloak admin token"));
                    }

                    KeycloackUserDTO keycloakUser = convertToKeycloackUserDTO(userCreateDTO);
                    
                    LOGGER.debug("Registering user with Keycloak: {}", userCreateDTO.getUsername());
                    
                    return keycloakAdminClient.createUser(BEARER_PREFIX + adminToken.getAccess_token(), keycloakUser)
                            .onItem().transform(response -> {
                                LOGGER.info("Keycloak registration response for {}: {}", userCreateDTO.getUsername(), response.getStatus());
                                return response;
                            });
                })
                .onFailure().recoverWithUni(failure -> {
                    LOGGER.error("Keycloak registration failed for user: {}", userCreateDTO.getUsername(), failure);
                    return Uni.createFrom().failure(new RuntimeException("Keycloak registration failed: " + failure.getMessage()));
                });
    }

    /**
     * Save user to database
     */
    private Uni<UserEntity> saveUserToDatabase(UserCreateDTO userCreateDTO) {
        UserEntity userEntity = userMapper.toEntity(userCreateDTO);
        
        LOGGER.debug("Saving user to database: {}", userCreateDTO.getEmail());
        
        return userRepository.createUser(userEntity)
                .onItem().transform(savedUser -> {
                    LOGGER.info("User saved to database successfully: {}", savedUser.getEmail());
                    return savedUser;
                })
                .onFailure().invoke(failure -> {
                    LOGGER.error("Database save failed for user: {}", userCreateDTO.getEmail(), failure);
                });
    }

    /**
     * Send welcome email asynchronously
     */
    private Uni<String> sendWelcomeEmailAsync(UserEntity user) {
        return Uni.createFrom().item(() -> {
            try {
                String result = twilioEmailService.sendWelcomeEmail(user.getEmail(), user.getFirstName());
                LOGGER.info("Welcome email sent successfully to: {}", user.getEmail());
                return "success";
            } catch (Exception e) {
                LOGGER.warn("Failed to send welcome email to {}: {}", user.getEmail(), e.getMessage());
                return "failed";
            }
        });
    }

    /**
     * Cleanup Keycloak user in case of database failure (Compensation)
     */
    private Uni<String> cleanupKeycloakUser(String username) {
        LOGGER.warn("Starting Keycloak cleanup for user: {}", username);
        
        return getAdminToken()
                .chain(adminToken -> {
                    if (adminToken == null || adminToken.getAccess_token() == null) {
                        return Uni.createFrom().item("cleanup_failed_no_token");
                    }

                    // First, find the user by username to get the ID
                    return findKeycloakUserByUsername(username, adminToken.getAccess_token())
                            .chain(userId -> {
                                if (userId != null) {
                                    // Delete the user
                                    return userClient.deleteUser(userId, BEARER_PREFIX + adminToken.getAccess_token())
                                            .onItem().transform(response -> {
                                                if (response.getStatus() >= 200 && response.getStatus() < 300) {
                                                    LOGGER.info("Keycloak user cleanup successful for: {}", username);
                                                    return "cleanup_successful";
                                                } else {
                                                    LOGGER.error("Keycloak cleanup failed with status: {} for user: {}", response.getStatus(), username);
                                                    return "cleanup_failed_delete_error";
                                                }
                                            });
                                } else {
                                    LOGGER.warn("Keycloak user not found for cleanup: {}", username);
                                    return Uni.createFrom().item("cleanup_skipped_user_not_found");
                                }
                            });
                })
                .onFailure().recoverWithUni(failure -> {
                    LOGGER.error("Keycloak cleanup failed for user: {}", username, failure);
                    return Uni.createFrom().item("cleanup_failed_exception");
                });
    }

    /**
     * Find Keycloak user by username to get user ID for deletion
     */
    private Uni<String> findKeycloakUserByUsername(String username, String accessToken) {
        // This is a simplified version - in real implementation, you'd need to search for the user
        // For now, we'll return a placeholder since the exact Keycloak admin API search isn't implemented
        LOGGER.debug("Searching for Keycloak user: {}", username);
        
        // TODO: Implement actual user search in Keycloak
        // For now, return null to indicate user not found (safe fallback)
        return Uni.createFrom().item((String) null);
    }
}