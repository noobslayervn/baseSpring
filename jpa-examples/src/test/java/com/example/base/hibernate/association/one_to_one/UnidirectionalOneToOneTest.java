package com.example.base.hibernate.association.one_to_one;

import com.example.base.BaseH2Test;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import org.junit.jupiter.api.Test;

import javax.persistence.*;

import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Created by khangld5 on Apr 22, 2021
 */
class UnidirectionalOneToOneTest extends BaseH2Test {

    @Test
    void testRelationship() {
        User kien = User.builder().userName("Kien").build();
        em.persist(kien);
        //em.find User 1L still work ?? first level cache ?
        User savedKien = em.find(User.class, 1L);
        ContactInfo info = ContactInfo.builder().address("downtown").user(savedKien).build();
        em.persist(info);

        ContactInfo saveInfo = em.createQuery("select ci from ContactInfo ci where ci.user = :user", ContactInfo.class)
                .setParameter("user", kien)
                .getSingleResult();
        assertNotNull(saveInfo.user);
    }

    @Builder
    @Entity(name = "User") //require name for createQuery hsql
    @NoArgsConstructor
    @AllArgsConstructor
    static class User {
        @Id
        @GeneratedValue(strategy = GenerationType.AUTO)
        //if id was passed then detach Entity Exception will throw
        long id;

        String userName;
    }

    @Builder
    @Entity(name = "ContactInfo")
    @NoArgsConstructor
    @AllArgsConstructor
    static class ContactInfo {
        @Id
        @GeneratedValue
        long id; //some how this goes to 2

        String address;

        @OneToOne
        @JoinColumn(name = "user_id", unique = true)
        User user; //create a foreign key linking to User @Id, name doesn't matter (generally associate class + _id)
    }
}
