package org.example.workhub.repository;


import jakarta.transaction.Transactional;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.example.workhub.constant.ErrorMessage;

import org.example.workhub.domain.entity.User;
import org.example.workhub.exception.NotFoundException;
import org.example.workhub.security.UserPrincipal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, String>, JpaSpecificationExecutor<User> {

  @Query("SELECT u FROM User u WHERE u.id = ?1")
  Optional<User> findById(String id);

  @Query("SELECT u FROM User u WHERE u.username = ?1")
  Optional<User> findByUsername(String username);

  @Query("SELECT u FROM User u WHERE u.username = ?1 AND u.deleted = false")
  Optional<User> findByUsernameAndDeletedFalse(String username);

  @Query("SELECT u FROM User u WHERE u.email = ?1 AND u.deleted = false")
  Optional<User> findByEmailAndDeletedFalse(String email);

  default User getUser(UserPrincipal currentUser) {
    return findByUsername(currentUser.getUsername())
        .orElseThrow(() -> new NotFoundException(ErrorMessage.User.ERR_NOT_FOUND_USERNAME,
            new String[]{currentUser.getUsername()}));
  }

  boolean existsByEmail(@NotBlank(message = "email khong duoc de trong") String email);

  boolean existsByEmailAndIdNot(String email, String id);

  boolean existsByUsernameAndIdNot(String username, String id);

  Optional<User> findByEmail(String email);

  @Transactional
  @Modifying
  @Query("update User u set u.password= ?2 where  u.email = ?1 ")
  void updatePassword(String email, String password);

    boolean existsByUsername(@NotBlank(message = "Username is required") @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters") String username);
}