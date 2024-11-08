package com.esprit.examen.services;

import com.esprit.examen.entities.CategorieProduit;
import com.esprit.examen.entities.DetailFacture;
import com.esprit.examen.entities.Produit;
import com.esprit.examen.entities.Stock;
import com.esprit.examen.repositories.DetailFactureRepository;
import com.esprit.examen.repositories.ProduitRepository;
import lombok.Builder;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class FactureServiceImplTest {

    @InjectMocks
    private FactureServiceImpl factureService; // Utilisez l'implémentation concrète ici

    @Mock
    private ProduitRepository produitRepository; // Mock du repository, si nécessaire
    @Mock
    private DetailFactureRepository detailFactureRepository;  // Mock the repository

   ProduitServiceImpl  produitServiceImpl;
   CategorieProduitServiceImpl categorieProduitServiceImpl;
   StockServiceImpl stockServiceImpl;




    @Test
    public void testCalculerTotalFactureAvancee() {
        // Mocking de la catégorie produit
        CategorieProduit categoriefruit = new CategorieProduit();
        categoriefruit.setLibelleCategorie("fruit");

        CategorieProduit categorielegume = new CategorieProduit();
        categorielegume.setLibelleCategorie("legume");

        // Mocking du stock
        Stock stock1 = new Stock();
        stock1.setIdStock(1L);
        stock1.setQte(10);

        Stock stock2 = new Stock();
        stock2.setIdStock(2L);
        stock2.setQte(10);

        // Mocking des produits
        Produit produit1 = new Produit();
        produit1.setIdProduit(1L);
        produit1.setStock(stock1);
        produit1.setPrix(10.0f);
        produit1.setCategorieProduit(categoriefruit);

        Produit produit2 = new Produit();
        produit2.setIdProduit(2L);
        produit2.setStock(stock2);
        produit2.setPrix(10.0f);
        produit2.setCategorieProduit(categorielegume);

        // Mocking des détails de facture
        DetailFacture detail1 = new DetailFacture();
        detail1.setIdDetailFacture(1L);
        detail1.setProduit(produit1);
        detail1.setQteCommandee(10); //10 unités
        detail1.setPourcentageRemise(0); // Pas de remise

        DetailFacture detail2 = new DetailFacture();
        detail2.setIdDetailFacture(2L);
        detail2.setProduit(produit2);
        detail2.setQteCommandee(10); // 10 unités
        detail2.setPourcentageRemise(0); // Pas de remise

        // Mocking the repository to return the details

        when(detailFactureRepository.findById(1L)).thenReturn(Optional.of(detail1));
        when(detailFactureRepository.findById(2L)).thenReturn(Optional.of(detail2));

        // Liste des détails de facture
       // List<Long> detailsFacture = Arrays.asList(detail1.getIdDetailFacture(), detail2.getIdDetailFacture());
        List<Long> detailsFacture = Arrays.asList(1L,2L);
        // Appel de la méthode à tester
        BigDecimal total = factureService.calculerTotalFactureAvancee(detailsFacture);

        // Vérifications
        // Prix pour produit1 : 10 * 10 = 100, remise 0% => 100, TVA 20% => 120
        // Prix pour produit2 : 10 * 10 = 100, remise 0% => 100, TVA 20% => 120
        // Total sans livraison ni remise globale = 100 + 100 = 200
        // frais de livraison car > 200,
        // remise globale car qte = 200 => 240*0.05=12
        // 240+15-12=243

        Assertions.assertEquals(243.0f, total.setScale(2, RoundingMode.HALF_UP).floatValue());
    }



/*
    @Test
    public void testCalculerTotalFactureAvecLivraisonEtRemise() {
        // Cas avec frais de livraison et remise globale
        // Mocking des produits et du stock similaire au premier test

        Produit produit2 = new Produit(); // Déclaration manquante de produit2
        produit2.setPrix(50.0f);
        CategorieProduit categorieStandard = new CategorieProduit();
        categorieStandard.setLibelleCategorie("standard");
        produit2.setCategorieProduit(categorieStandard);


        Stock stock2 = new Stock();
        stock2.setQte(5);
        produit2.setStock(stock2);

        Produit produit3 = new Produit(); // Déclaration de produit3
        produit3.setPrix(150.0f);
        produit3.setCategorieProduit(categorieStandard);

        Stock stock3 = new Stock();
        stock3.setQte(20);
        produit3.setStock(stock3);

        // Mocking des détails de facture
        DetailFacture detail3 = new DetailFacture();
        detail3.setProduit(produit3);
        detail3.setQteCommandee(10); // 10 unités
        detail3.setPourcentageRemise(5); // 5% de remise

        DetailFacture detail4 = new DetailFacture();
        detail4.setProduit(produit2); // produit2 bien défini ici
        detail4.setQteCommandee(2); // 2 unités
        detail4.setPourcentageRemise(0); // Pas de remise

        // Liste des détails de facture
        List<DetailFacture> detailsFacture = Arrays.asList(detail3, detail4);

        // Appel de la méthode à tester
        BigDecimal total = factureService.calculerTotalFactureAvancee(detailsFacture);

        // Vérifications
        Assertions.assertEquals(1753.5f, total.setScale(2, RoundingMode.HALF_UP).floatValue());
    }

*/


}