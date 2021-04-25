package com.example.base.hibernate.association.many_to_many;

import com.example.base.BaseH2Test;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.NaturalId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.persistence.*;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class BidirectionalManyToManyLinkEntityTest extends BaseH2Test {

    private Author xMen;

    @BeforeEach
    void setup() {
        //this required persisted child first for ID. Some mess up with the mapping book and author_book.
        Book b1 = em.persistFlushFind(new Book("some book 1"));
        Book b2 = em.persistFlushFind(new Book("some book 2"));
        Author xMen = new Author("XMEN");
        xMen.addNewBook(b1);
        xMen.addNewBook(b2);
        this.xMen = em.persistFlushFind(xMen);
    }

    @Test
    void testAdd() {
        assertNotNull(xMen);
    }

    @Entity
    @NoArgsConstructor
    @EqualsAndHashCode(onlyExplicitlyIncluded = true)
    static class Author {
        @Id
        @GeneratedValue
        @EqualsAndHashCode.Include
        Long id;

        String name;

        @OneToMany(mappedBy = "author", cascade = CascadeType.ALL, orphanRemoval = true)
        private final Set<AuthorBookMap> authorBooks = new HashSet<>();

        public Author(String name) {
            this.name = name;
        }

        //make sense of this 
        public void addNewBook(Book book) {
            AuthorBookMap map = new AuthorBookMap(this, book);
            authorBooks.add(map);
            book.addNewAuthorMap(map);
        }

        public void removeBook(Book book) {
            for (Iterator<AuthorBookMap> iterator = authorBooks.iterator(); iterator.hasNext(); ) {
                AuthorBookMap map = iterator.next();
                if (map.author.equals(this) && map.book.equals(book)) {
                    iterator.remove(); //author remove book
                    map.book.authorBooks.remove(map); //book remove author
                    map.author = null;
                    map.book = null;
                }
            }
        }
    }

    @Entity
    @NoArgsConstructor
    @EqualsAndHashCode(onlyExplicitlyIncluded = true)
    static class AuthorBookMap {

        @EmbeddedId
        AuthorBookId id;

        @ManyToOne
        @MapsId("authorId")
        @EqualsAndHashCode.Include
        Author author;

        @ManyToOne
        @MapsId("bookId")
        @EqualsAndHashCode.Include
        Book book;

        public AuthorBookMap(Author author, Book book) {
            this.author = author;
            this.book = book;
            this.id = new AuthorBookId(author.id, book.id);
        }
    }

    @Embeddable
    @AllArgsConstructor
    @NoArgsConstructor
    static class AuthorBookId implements Serializable {
        Long authorId;
        Long bookId;
    }

    @Entity
    @NoArgsConstructor
    @EqualsAndHashCode(onlyExplicitlyIncluded = true)
    static class Book {

        @Id
        @GeneratedValue
        @EqualsAndHashCode.Include
        Long id;

        @NaturalId
        @EqualsAndHashCode.Include
        String name;

        @OneToMany(mappedBy = "book", cascade = CascadeType.ALL, orphanRemoval = true)
        private final Set<AuthorBookMap> authorBooks = new HashSet<>();

        public Book(String name) {
            this.name = name;
        }

        public void addNewAuthorMap(AuthorBookMap map) {
            authorBooks.add(map);
        }
    }
}
