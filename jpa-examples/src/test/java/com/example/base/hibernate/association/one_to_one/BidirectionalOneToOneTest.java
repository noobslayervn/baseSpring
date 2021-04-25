package com.example.base.hibernate.association.one_to_one;

import com.example.base.BaseH2Test;
import lombok.*;
import org.junit.jupiter.api.Test;

import javax.persistence.*;

import static org.junit.jupiter.api.Assertions.assertThrows;

class BidirectionalOneToOneTest extends BaseH2Test {

    @Test
    void joinColumnUniqueTrue() {
        ContactInfo info1 = ContactInfo.builder().address("downtown").build();
        User kien = User.builder().userName("Kien").build();
        kien.setInfo(info1);
        User savedKien = em.persistFlushFind(kien);

        ContactInfo info2 = ContactInfo.builder().address("chinaTown").user(savedKien).build();
        assertThrows(PersistenceException.class, () -> em.persistAndFlush(info2));
    }

    @Test
    void optionalFalseOneToOne() {
        //non null relationship is required
        User kien = User.builder().userName("Kien").build();
        assertThrows(PersistenceException.class, () -> em.persistFlushFind(kien));
    }

    @Builder
    @Entity
    @NoArgsConstructor
    @AllArgsConstructor
    @ToString
    private static class User {
        @Id
        @GeneratedValue
        long id;

        String userName;

        @ToString.Exclude
        @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, optional = false)
        //mappedBy marked which one is Parent class, this fetch Eager even those specify LAZY
        ContactInfo info;

        public void setInfo(ContactInfo info) {
            if (info != null) {
                info.user = this;
            }
            this.info = info;
        }
    }

    @Builder
    @Entity(name = "BidirectionalOneToOneTest$ContactInfo")
    @NoArgsConstructor
    @AllArgsConstructor
    private static class ContactInfo {
        @Id
        @GeneratedValue
        long id;

        String address;

        @OneToOne
        @JoinColumn(name = "user_id", unique = true)
        //this will specify foreign key
        User user;
    }
}
