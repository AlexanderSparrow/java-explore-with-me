package ru.practicum.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "categories", uniqueConstraints = @UniqueConstraint(name = "uq_category_name", columnNames = "name"))
@Data
public class Category {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;
}
