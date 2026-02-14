package com.devsuperior.dscatalog.entities;

import lombok.*;
import org.springframework.stereotype.Repository;

@Repository
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@EqualsAndHashCode()
public class Category {

    private Long id;
    private String name;
}
