package gestao.services;

import gestao.exceptions.Produto.ProdutoNaoEncontradoException;
import gestao.models.Produto.Produto;
import gestao.models.hospital.Hospital;
import gestao.repositories.ProdutoRepository;
import gestao.repositories.hospital.HospitalRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.validation.BindingResult;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProdutoService {

    @Autowired
    HospitalRepository hospitalRepository;

    @Autowired
    ProdutoRepository produtoRepository;

    public List<Produto> getProdutos(Long id) {
        Hospital hospital = hospitalRepository.findById(id).get();
        return Collections.unmodifiableList(hospital.getProdutos());
    }

    public Produto find(String nomeProduto, Long id) {
        Hospital hospital = hospitalRepository.findById(id).get();
        List<Produto> produto = hospital.getProdutos();
        produto = produto.stream()
                .filter(x -> x.getNome().toUpperCase().equals(nomeProduto.toUpperCase()))
                .collect(Collectors.toList());
        if (produto.isEmpty()) throw new ProdutoNaoEncontradoException();
        return produto.get(0);
    }


    public boolean adicionar(@Valid Produto produto, Long id, BindingResult resultado) {
        if (resultado.hasErrors()) {
            return false;
        }
        Hospital hospital = hospitalRepository.findById(id).get();
        List<Produto> novoProduto = hospital.getProdutos();
        if (novoProduto == null) novoProduto = new ArrayList<Produto>();
        produtoRepository.save(produto);
        novoProduto.add(produto);
        hospital.setProdutos(novoProduto);
        hospitalRepository.saveAndFlush(hospital);
        return true;
    }

    public boolean adicionaProdutoEstoque(Long id, @Valid Produto produto, BindingResult resultado) {
        if (resultado.hasErrors()) {
            return false;
        }
        Hospital hospital = hospitalRepository.findById(id).get();
        List<Produto> produtos = hospital.getProdutos();
        List<Produto> produtoAdd = produtos.stream()
                .filter(x -> x.getNome().toUpperCase().equals(produto.getNome().toUpperCase()))
                .collect(Collectors.toList());
        if (produtos.isEmpty()) throw new ProdutoNaoEncontradoException();

        Produto produtoAtualizado = produtoAdd.get(0);
        Integer temp = produtoAtualizado.getQuantidade();
        temp += produto.getQuantidade();
        produtoAtualizado.setQuantidade(temp);
        produtos.stream()
                .filter(x -> x != produtoAtualizado)
                .collect(Collectors.toList());
        hospital.setProdutos(produtos);
        hospitalRepository.save(hospital);
        return true;
    }

    public boolean subtrairProdutoEstoque(Long id, @Valid Produto produto, BindingResult resultado) {
        if (resultado.hasErrors()) {
            return false;
        }
        Hospital hospital = hospitalRepository.findById(id).get();
        List<Produto> produtos = hospital.getProdutos();
        List<Produto> produtoAdd = produtos.stream()
                .filter(x -> x.getNome().toUpperCase().equals(produto.getNome().toUpperCase()))
                .collect(Collectors.toList());
        if (produtos.isEmpty()) throw new ProdutoNaoEncontradoException();

        Produto produtoAtualizado = produtoAdd.get(0);
        Integer temp = produtoAtualizado.getQuantidade();
        temp -= produto.getQuantidade();
        produtoAtualizado.setQuantidade(temp);
        produtos.stream()
                .filter(x -> x != produtoAtualizado)
                .collect(Collectors.toList());
        hospital.setProdutos(produtos);
        hospitalRepository.save(hospital);
        return true;
    }

    public boolean delete(Produto produto, Long id) {
        try {
            Hospital hospital = hospitalRepository.findById(id).get();
            List<Produto> produtos = hospital.getProdutos();
            for(Produto p : produtos){
                if(p.getNome().toUpperCase().equals(produto.getNome().toUpperCase())) {
                    produtoRepository.deleteById(p.getId());
                }
            }
            produtos = produtos.stream()
                    .filter(x -> !x.getNome().toUpperCase().equals(produto.getNome().toUpperCase()))
                    .collect(Collectors.toList());
            hospital.setProdutos(produtos);
            hospitalRepository.save(hospital);
            return true;
        } catch (Exception ex) {

        }
        return false;
    }

}
