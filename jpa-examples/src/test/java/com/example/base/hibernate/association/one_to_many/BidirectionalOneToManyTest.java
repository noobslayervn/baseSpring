package com.example.base.hibernate.association.one_to_many;

import com.example.base.BaseH2Test;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;

class BidirectionalOneToManyTest extends BaseH2Test {

    private Employee admin;

    @BeforeEach
    void setup() {
        admin = Employee.builder().name("kien").build();
        Role adminRole = Role.builder().name("admin").employee(admin).build();
        Role presidentRole = Role.builder().name("president").employee(admin).build();
        admin.addRole(adminRole);
        admin.addRole(presidentRole);
    }

    @Test
    void testAdd() {
        Employee saved = em.persistAndFlush(admin);
        assertFalse(saved.getRoles().isEmpty());
    }

    @Builder
    @Entity
    @NoArgsConstructor
    @AllArgsConstructor
    private static class Employee {
        @Id
        @GeneratedValue
        long id;

        String name;

        //if the collection is huge then consider only keep @ManyToOne at child then using query to get fetch
        //This collection cannot limit size like query-level pagination
        //this might loose nice function like cascade or orphan but gain performance
        @OneToMany(mappedBy = "employee", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
        private final List<Role> roles = new ArrayList<>();

        public List<Role> getRoles() {
            return Collections.unmodifiableList(roles);
        }

        public void addRole(Role newRole) {
            roles.add(newRole);
        }
    }

    @Builder
    @Entity
    @NoArgsConstructor
    @AllArgsConstructor
    @EqualsAndHashCode(onlyExplicitlyIncluded = true)
    private static class Role {
        @Id
        @GeneratedValue
        @EqualsAndHashCode.Include
        long id;

        String name;

        @ManyToOne(fetch = FetchType.LAZY) //this properly all you need
        Employee employee;
    }
}