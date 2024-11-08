/**/
package com.esprit.examen.services;

import com.esprit.examen.entities.CategorieProduit;
import com.esprit.examen.entities.DetailFacture;
import com.esprit.examen.entities.Produit;
import com.esprit.examen.entities.Stock;
import com.esprit.examen.repositories.DetailFactureRepository;
import com.esprit.examen.repositories.ProduitRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.junit4.SpringRunner;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Arrays;
import java.util.List;

@ExtendWith(SpringExtension.class)
@SpringBootTest

public class FactureServiceImplJunitTest {

    @Autowired
    private ProduitServiceImpl produitServiceImpl;

    @Autowired
    private CategorieProduitServiceImpl categorieProduitServiceImpl;

    @Autowired
    private StockServiceImpl stockServiceImpl;

    @Autowired
    private FactureServiceImpl factureService;

    @Autowired
    private ProduitRepository produitRepository;

    @Autowired
    private DetailFactureRepository detailFactureRepository;

    @Test
    public void testCalculerTotalFactureAvancee_WithBuilderPattern() {

        CategorieProduit categorieProduitPromotion = CategorieProduit.builder().libelleCategorie("promotion").build();
        CategorieProduit categorieProduitStandard = CategorieProduit.builder().libelleCategorie("standard").build();
        CategorieProduit categorieProduitsave1= categorieProduitServiceImpl.addCategorieProduit(categorieProduitPromotion);
        CategorieProduit categorieProduitsave2= categorieProduitServiceImpl.addCategorieProduit(categorieProduitStandard);

        Stock stock1 = Stock.builder().qte(10).build();
        Stock stock2 = Stock.builder().qte(10).build(); // Available quantity
        Stock stocksave1 = stockServiceImpl.addStock(stock1);
        Stock stocksave2 = stockServiceImpl.addStock(stock2);

        Produit produit1 = Produit.builder().categorieProduit(categorieProduitStandard)
                .libelleProduit("lait").prix(10).stock(stock1).build();
        Produit produit2 = Produit.builder().categorieProduit(categorieProduitStandard)
                .libelleProduit("sucre").prix(10).stock(stock2).build();
        Produit produitsave1 = produitServiceImpl.addProduit(produit1);
        Produit produitsave2 = produitServiceImpl.addProduit(produit2);

        DetailFacture detailFacture1 = DetailFacture.builder().produit(produit1).qteCommandee(10)
                .pourcentageRemise(0).build();
        DetailFacture detailFacture2 = DetailFacture.builder().produit(produit1).qteCommandee(10)
                .pourcentageRemise(0).build();
        DetailFacture detailFacturesave1 = detailFactureRepository.save(detailFacture1);
        DetailFacture detailFacturesave2 = detailFactureRepository.save(detailFacture2);

        // verfifier l'ajout
        //verffier que la
        List<Long> detailsFacture = Arrays.asList(detailFacturesave1.getIdDetailFacture(),detailFacturesave2.getIdDetailFacture());
        BigDecimal total = factureService.calculerTotalFactureAvancee(detailsFacture);
        Assertions.assertEquals(243.0f, total.setScale(2, RoundingMode.HALF_UP).floatValue());
        // netoyer la base

        detailFactureRepository.deleteById(detailFacturesave1.getIdDetailFacture());
        detailFactureRepository.deleteById(detailFacturesave2.getIdDetailFacture());
        produitServiceImpl.deleteProduit(produitsave1.getIdProduit());
        produitServiceImpl.deleteProduit(produitsave2.getIdProduit());
        categorieProduitServiceImpl.deleteCategorieProduit(categorieProduitsave1.getIdCategorieProduit());
        categorieProduitServiceImpl.deleteCategorieProduit(categorieProduitsave2.getIdCategorieProduit());
        stockServiceImpl.deleteStock(stocksave1.getIdStock());
        stockServiceImpl.deleteStock(stocksave2.getIdStock());


    }

}
