package br.com.academiadev.thunderpets.controller;

import br.com.academiadev.thunderpets.dto.UsuarioDTO;
import br.com.academiadev.thunderpets.exception.FotoNaoEncontradaException;
import br.com.academiadev.thunderpets.exception.UsuarioNaoEncontradoException;
import br.com.academiadev.thunderpets.mapper.ContatoMapper;
import br.com.academiadev.thunderpets.mapper.UsuarioMapper;
import br.com.academiadev.thunderpets.model.Contato;
import br.com.academiadev.thunderpets.model.Usuario;
import br.com.academiadev.thunderpets.repository.ContatoRepository;
import br.com.academiadev.thunderpets.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/usuario")
public class UsuarioController {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private ContatoRepository contatoRepository;

    @Autowired
    private UsuarioMapper usuarioMapper;

    @Autowired
    private ContatoMapper contatoMapper;

    @GetMapping("")
    public PageImpl<UsuarioDTO> listar(@RequestParam(defaultValue = "0") int paginaAtual,
                                       @RequestParam(defaultValue = "10") int tamanho,
                                       @RequestParam(defaultValue = "ASC") Sort.Direction direcao,
                                       @RequestParam(defaultValue = "nome") String campoOrdenacao) {
        PageRequest paginacao = PageRequest.of(paginaAtual, tamanho, direcao, campoOrdenacao);
        Page<Usuario> paginaUsuarios = usuarioRepository.findAll(paginacao);
        int totalDeElementos = (int) paginaUsuarios.getTotalElements();

        return new PageImpl<UsuarioDTO>(paginaUsuarios.stream()
                .map(usuario -> usuarioMapper.converterUsuarioParaUsuarioDTO(usuario)).collect(Collectors.toList()),
                paginacao,
                totalDeElementos);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Object> buscar(@PathVariable("id") UUID id) {
        if (!usuarioRepository.existsById(id)) {
            return ResponseEntity.status(500)
                    .body(new UsuarioNaoEncontradoException("Usuario " + id + " não encontrado."));
        }

        return ResponseEntity.ok(usuarioMapper.converterUsuarioParaUsuarioDTO(usuarioRepository.findById(id).get()));
    }

    @PostMapping("")
    public ResponseEntity<Object> salvar(@RequestBody UsuarioDTO usuarioDTO) {
        usuarioDTO.setSenha(new BCryptPasswordEncoder().encode(usuarioDTO.getSenha()));
        usuarioDTO.setAtivo(true);

        if (usuarioDTO.getContatos() == null || usuarioDTO.getContatos().size() == 0) {
            return ResponseEntity
                    .status(502)
                    .body(new Exception("O usuário precisa ter pelo menos um contato cadastrado."));
        }

        final Usuario usuario = usuarioRepository
                .saveAndFlush(usuarioMapper.converterUsuarioDTOparaUsuario(usuarioDTO));

        List<Contato> contatosDoUsuario = contatoRepository.findByUsuario(usuario);
        contatosDoUsuario.forEach(contatoRepository::delete);

        usuarioDTO.getContatos().forEach(contatoDTO -> contatoRepository.save(
                contatoMapper.converterContatoDTOParaContato(contatoDTO, usuario)));

        return ResponseEntity.ok(usuarioMapper.converterUsuarioParaUsuarioDTO(usuario));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Object> deletar(@PathVariable("id") UUID id) {
        if (!usuarioRepository.existsById(id)) {
            return ResponseEntity.status(500)
                    .body(new UsuarioNaoEncontradoException("Usuario " + id + " não encontrado."));
        }

        try {
            Usuario usuario = usuarioRepository.findById(id).get();
            usuario.setAtivo(false);
            usuarioRepository.saveAndFlush(usuario);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(e.getMessage());
        }

        return ResponseEntity.ok(true);
    }

    @GetMapping("{id}/foto")
    public ResponseEntity<Object> getFoto(@PathVariable("id") UUID id) {
        if (!usuarioRepository.existsById(id)) {
            return ResponseEntity.status(500)
                    .body(new UsuarioNaoEncontradoException("Usuario " + id + " não encontrado."));
        }

        Usuario usuario = usuarioRepository.findById(id).get();
        byte[] bytes = usuario.getFoto();

        if (bytes == null) {
            return ResponseEntity.status(500)
                    .body(new FotoNaoEncontradaException("O usuário " + id + " não possui foto."));
        }

        return ResponseEntity.ok().contentType(MediaType.IMAGE_JPEG).body(bytes);
    }
}
