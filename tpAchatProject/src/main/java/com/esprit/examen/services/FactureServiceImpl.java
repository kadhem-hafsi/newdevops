package com.esprit.examen.services;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import javax.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.esprit.examen.entities.DetailFacture;
import com.esprit.examen.entities.Facture;
import com.esprit.examen.entities.Fournisseur;
import com.esprit.examen.entities.Operateur;
import com.esprit.examen.entities.Produit;
import com.esprit.examen.repositories.DetailFactureRepository;
import com.esprit.examen.repositories.FactureRepository;
import com.esprit.examen.repositories.FournisseurRepository;
import com.esprit.examen.repositories.OperateurRepository;
import com.esprit.examen.repositories.ProduitRepository;
import lombok.extern.slf4j.Slf4j;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.stream.Collectors;

@Service
@Slf4j
@Transactional
public class FactureServiceImpl implements IFactureService {

	@Autowired
	FactureRepository factureRepository;
	@Autowired
	OperateurRepository operateurRepository;
	@Autowired
	DetailFactureRepository detailFactureRepository;
	@Autowired
	FournisseurRepository fournisseurRepository;
	@Autowired
	ProduitRepository produitRepository;
    @Autowired
    ReglementServiceImpl reglementService;
	
	@Override
	public List<Facture> retrieveAllFactures() {
		List<Facture> factures = (List<Facture>) factureRepository.findAll();
		for (Facture facture : factures) {
			log.info(" facture : " + facture);
		}
		return factures;
	}

	
	public Facture addFacture(Facture f) {
		return factureRepository.save(f);
	}

	/*
	 * calculer les montants remise et le montant total d'un détail facture
	 * ainsi que les montants d'une facture
	 */
	private Facture addDetailsFacture(Facture f, Set<DetailFacture> detailsFacture) {
		float montantFacture = 0;
		float montantRemise = 0;
		for (DetailFacture detail : detailsFacture) {
			//Récuperer le produit 
			Produit produit = produitRepository.findById(detail.getProduit().getIdProduit()).get();
			//Calculer le montant total pour chaque détail Facture
			float prixTotalDetail = detail.getQteCommandee() * produit.getPrix();
			//Calculer le montant remise pour chaque détail Facture
			float montantRemiseDetail = (prixTotalDetail * detail.getPourcentageRemise()) / 100;
			float prixTotalDetailRemise = prixTotalDetail - montantRemiseDetail;
			detail.setMontantRemise(montantRemiseDetail);
			detail.setPrixTotalDetail(prixTotalDetailRemise);
			//Calculer le montant total pour la facture
			montantFacture = montantFacture + prixTotalDetailRemise;
			//Calculer le montant remise pour la facture
			montantRemise = montantRemise + montantRemiseDetail;
			detailFactureRepository.save(detail);
		}
		f.setMontantFacture(montantFacture);
		f.setMontantRemise(montantRemise);
		return f;
	}

	@Override
	public void cancelFacture(Long factureId) {
		// Méthode 01
		//Facture facture = factureRepository.findById(factureId).get();
		Facture facture = factureRepository.findById(factureId).orElse(new Facture());
		facture.setArchivee(true);
		factureRepository.save(facture);
		//Méthode 02 (Avec JPQL)
		factureRepository.updateFacture(factureId);
	}

	@Override
	public Facture retrieveFacture(Long factureId) {

		Facture facture = factureRepository.findById(factureId).orElse(null);
		log.info("facture :" + facture);
		return facture;
	}

	@Override
	public List<Facture> getFacturesByFournisseur(Long idFournisseur) {
		Fournisseur fournisseur = fournisseurRepository.findById(idFournisseur).orElse(null);
		return (List<Facture>) fournisseur.getFactures();
	}

	@Override
	public void assignOperateurToFacture(Long idOperateur, Long idFacture) {
		Facture facture = factureRepository.findById(idFacture).orElse(null);
		Operateur operateur = operateurRepository.findById(idOperateur).orElse(null);
		operateur.getFactures().add(facture);
		operateurRepository.save(operateur);
	}

	@Override
	public float pourcentageRecouvrement(Date startDate, Date endDate) {
		float totalFacturesEntreDeuxDates = factureRepository.getTotalFacturesEntreDeuxDates(startDate,endDate);
		float totalRecouvrementEntreDeuxDates =reglementService.getChiffreAffaireEntreDeuxDate(startDate,endDate);
		float pourcentage=(totalRecouvrementEntreDeuxDates/totalFacturesEntreDeuxDates)*100;
		return pourcentage;
	}




	@Override
	public BigDecimal calculerTotalFactureAvancee(List<Long> idsDetailFacture) {

		List<DetailFacture> detailsFacture = idsDetailFacture.stream()
				.map(id -> detailFactureRepository.findById(id))  // Assuming you have a repository for fetching DetailFacture by ID
				.filter(Optional::isPresent)  // Only keep those that exist
				.map(Optional::get)  // Extract the DetailFacture from the Optional
				.collect(Collectors.toList());


		// 1. Calculer le total des produits disponibles
		BigDecimal totalProduits = detailsFacture.stream()
				.filter(detail -> detail.getQteCommandee() > 0
						&& detail.getProduit() != null
						&& detail.getProduit().getStock() != null
						&& detail.getProduit().getStock().getQte() > 0)
				.map(detail -> {
					BigDecimal prixUnitaire = BigDecimal.valueOf(detail.getProduit().getPrix());
					BigDecimal prixTotalDetail = prixUnitaire.multiply(BigDecimal.valueOf(detail.getQteCommandee()));

					// Calcul de la remise
					BigDecimal montantRemise = prixTotalDetail.multiply(BigDecimal.valueOf(detail.getPourcentageRemise())).divide(BigDecimal.valueOf(100), RoundingMode.HALF_UP);
					BigDecimal prixApresRemise = prixTotalDetail.subtract(montantRemise);

					// Application de la TVA
					BigDecimal tva = detail.getProduit().getCategorieProduit().getLibelleCategorie().equalsIgnoreCase("promotion")
							? BigDecimal.valueOf(0.10)
							: BigDecimal.valueOf(0.20);
					BigDecimal prixAvecTVA = prixApresRemise.multiply(BigDecimal.ONE.add(tva));

					return prixAvecTVA.compareTo(BigDecimal.valueOf(50.0)) >= 0 ? prixAvecTVA : BigDecimal.ZERO;
				})
				.reduce(BigDecimal.ZERO, BigDecimal::add);

		// 2. Ajouter des frais de livraison
		BigDecimal fraisDeLivraison = totalProduits.compareTo(BigDecimal.valueOf(200.0)) >= 0 ? BigDecimal.valueOf(15.0) : BigDecimal.ZERO;

		// 3. Appliquer une remise globale
		int quantiteTotale = detailsFacture.stream()
				.filter(detail -> detail.getProduit() != null)
				.mapToInt(DetailFacture::getQteCommandee)
				.sum();
		BigDecimal remiseGlobale = quantiteTotale > 10 ? totalProduits.multiply(BigDecimal.valueOf(0.05)) : BigDecimal.ZERO;

		// 4. Calculer le total final
		return totalProduits.add(fraisDeLivraison).subtract(remiseGlobale);
	}
	

}