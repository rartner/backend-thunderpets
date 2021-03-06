package br.com.academiadev.thunderpets.dto;

import br.com.academiadev.thunderpets.enums.TipoContato;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ContatoDTO {

    private UUID id;
    private TipoContato tipo;
    private String descricao;
}
