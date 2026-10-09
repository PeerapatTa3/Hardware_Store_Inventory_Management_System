package com.hardwarestore.domain.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "suppliers")
@org.hibernate.annotations.SQLDelete(sql = "UPDATE suppliers SET is_active = false WHERE id=?")
@org.hibernate.annotations.SQLRestriction("is_active = true")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Supplier {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String name;

    @Column(length = 20)
    private String phone;

    @Column(length = 150, unique = true)
    private String email;

    @Column(length = 255)
    private String address;

    @Column(name = "is_active", nullable = false)
    private boolean isActive = true;
}
