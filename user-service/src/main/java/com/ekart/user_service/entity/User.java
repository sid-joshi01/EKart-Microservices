package com.ekart.user_service.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "ekart_users",
        indexes = { @Index(name = "idX_email", columnList = "email") }
)
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String firstName;
    private String lastName;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String password;
    private String phone;
    private String address;
    private String role;
    private String provider;
    private Instant createdAt;
    private Instant updatedAt;

    @PrePersist
    public void prePersist() {
        createdAt = Instant.now();
        updatedAt = createdAt;
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = Instant.now();
    }


}

//      docker exec -it ekart-user-db mysql -u root -p
//      use ekart_usersdb
//      select * from ekart_users;

//      UPDATE ekart_users SET role='ADMIN' WHERE email='sam@123.gmail.com';
//      SELECT email, role FROM ekart_users WHERE email = 'sam@123.gmail.com';