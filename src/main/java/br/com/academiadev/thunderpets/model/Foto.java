package br.com.academiadev.thunderpets.model;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import javax.validation.constraints.NotNull;

@Data
@Entity
@Builder
@NoArgsConstructor
public class Foto {

    @Id
    @GeneratedValue
    private Long id;

    @Lob
    @NotNull
    private byte[] image;

    @ManyToOne
    private Pet pet;
}
