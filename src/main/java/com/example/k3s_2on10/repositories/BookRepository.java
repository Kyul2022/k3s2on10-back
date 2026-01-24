package com.example.k3s_2on10.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import com.example.k3s_2on10.model.Book;

public interface BookRepository extends JpaRepository<Book, Long> {
}