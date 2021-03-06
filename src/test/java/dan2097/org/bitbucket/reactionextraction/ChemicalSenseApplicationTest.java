package dan2097.org.bitbucket.reactionextraction;

import static junit.framework.Assert.*;

import org.junit.Test;

public class ChemicalSenseApplicationTest {

	@Test
	public void mergeProductsByInchiTest1(){
		Reaction reaction = new Reaction();
		Chemical product1 = new Chemical("foo");
		product1.setChemicalIdentifierPair(new ChemicalIdentifierPair("O[C@@](C(O)=O)([H])[C@](O)([H])C(O)=O", "InChI=1/C4H6O6/c5-1(3(7)8)2(6)4(9)10/h1-2,5-6H,(H,7,8)(H,9,10)/t1-,2+"));
		product1.setAmountValue("5");
		product1.setAmountUnits("mols");
		Chemical product2 = new Chemical("foo");
		product2.setChemicalIdentifierPair(new ChemicalIdentifierPair("O[C@@](C(O)=O)([H])[C@](O)([H])C(O)=O", "InChI=1/C4H6O6/c5-1(3(7)8)2(6)4(9)10/h1-2,5-6H,(H,7,8)(H,9,10)/t1-,2+"));
		product2.setAmountValue("5");
		product2.setAmountUnits("mols");
		reaction.addProduct(product1);
		reaction.addProduct(product2);
		new ChemicalSenseApplication(reaction).mergeProductsByInChI();
		assertEquals(1, reaction.getProducts().size());
		assertEquals(product2, reaction.getProducts().get(0));
	}

	@Test
	public void mergeProductsByInchiTest2(){
		//last product is preferred unless a previous one has quantities and it does not
		Reaction reaction = new Reaction();
		Chemical product1 = new Chemical("foo");
		product1.setChemicalIdentifierPair(new ChemicalIdentifierPair("O[C@@](C(O)=O)([H])[C@](O)([H])C(O)=O", "InChI=1/C4H6O6/c5-1(3(7)8)2(6)4(9)10/h1-2,5-6H,(H,7,8)(H,9,10)/t1-,2+"));
		product1.setAmountValue("5");
		product1.setAmountUnits("mols");
		Chemical product2 = new Chemical("foo");
		product2.setChemicalIdentifierPair(new ChemicalIdentifierPair("O[C@@](C(O)=O)([H])[C@](O)([H])C(O)=O", "InChI=1/C4H6O6/c5-1(3(7)8)2(6)4(9)10/h1-2,5-6H,(H,7,8)(H,9,10)/t1-,2+"));
		reaction.addProduct(product1);
		reaction.addProduct(product2);
		new ChemicalSenseApplication(reaction).mergeProductsByInChI();
		assertEquals(1, reaction.getProducts().size());
		assertEquals(product1, reaction.getProducts().get(0));
	}

	@Test
	public void transitionMetalCatalystTest() {
		Reaction reaction = new Reaction();
		Chemical catalyst = new Chemical("platinum");
		catalyst.setChemicalIdentifierPair(new ChemicalIdentifierPair("[Pt]", "InChI=1/Pt"));
		reaction.addReactant(catalyst);
		catalyst.setRole(ChemicalRole.reactant);
		new ChemicalSenseApplication(reaction).correctReactantsThatAreCatalysts();
		assertEquals(ChemicalRole.catalyst, catalyst.getRole());
	}
	
	@Test
	public void transitionMetalNotCatalystTest1() {
		Reaction reaction = new Reaction();
		Chemical catalyst = new Chemical("Potassium dichromate");
		catalyst.setChemicalIdentifierPair(new ChemicalIdentifierPair("[K+].[K+].[O-][Cr](=O)(=O)O[Cr]([O-])(=O)=O", "InChI=1/2Cr.2K.7O/q;;2*+1;;;;;;2*-1"));
		reaction.addReactant(catalyst);
		catalyst.setRole(ChemicalRole.reactant);
		new ChemicalSenseApplication(reaction).correctReactantsThatAreCatalysts();
		assertEquals(ChemicalRole.reactant, catalyst.getRole());
	}
	
	@Test
	public void transitionMetalNotCatalystTest2() {
		Reaction reaction = new Reaction();
		Chemical catalyst = new Chemical("Copper(I) cyanide");
		catalyst.setChemicalIdentifierPair(new ChemicalIdentifierPair("[Cu]C#N", "InChI=1/CN.Cu/c1-2;"));
		reaction.addReactant(catalyst);
		catalyst.setRole(ChemicalRole.reactant);
		new ChemicalSenseApplication(reaction).correctReactantsThatAreCatalysts();
		assertEquals(ChemicalRole.reactant, catalyst.getRole());
	}
	
	@Test
	public void inorganicCompoundTest() {
		Reaction reaction = new Reaction();
		Chemical reactant = new Chemical("sodium hydroxide");
		reactant.setChemicalIdentifierPair(new ChemicalIdentifierPair("[Na+].[OH-]", "InChI=1/Na.H2O/h;1H2/q+1;/p-1"));
		reaction.addReactant(reactant);
		reactant.setRole(ChemicalRole.reactant);
		new ChemicalSenseApplication(reaction).correctReactantsThatAreCatalysts();
		assertEquals(ChemicalRole.reactant, reactant.getRole());
	}
	
	@Test
	public void transitionMetalCompoundFormationTest() {
		Reaction reaction = new Reaction();
		Chemical reactant = new Chemical("platinum");
		reactant.setChemicalIdentifierPair(new ChemicalIdentifierPair("[Pt]", "InChI=1/Pt"));
		reactant.setRole(ChemicalRole.reactant);
		Chemical product = new Chemical("Cisplatin");
		product.setChemicalIdentifierPair(new ChemicalIdentifierPair("[NH3][Pt](Cl)(Cl)[NH3]", "InChI=1/2ClH.2H3N.Pt/h2*1H;2*1H3;/q;;;;+2/p-2"));
		product.setRole(ChemicalRole.product);
		reaction.addReactant(reactant);
		reaction.addProduct(product);
		new ChemicalSenseApplication(reaction).correctReactantsThatAreCatalysts();
		assertEquals(ChemicalRole.reactant, reactant.getRole());
	}
	
	@Test
	public void reactantIsKnownToBeASolventInSameReactionTest() {
		Reaction reaction = new Reaction();
		Chemical misclassifiedReactant = new Chemical("THF");
		misclassifiedReactant.setChemicalIdentifierPair(new ChemicalIdentifierPair("C1CCOC1", "InChI=1/C4H8O/c1-2-4-5-3-1/h1-4H2"));
		misclassifiedReactant.setRole(ChemicalRole.reactant);
		Chemical solvent = new Chemical("THF");
		solvent.setChemicalIdentifierPair(new ChemicalIdentifierPair("C1CCOC1", "InChI=1/C4H8O/c1-2-4-5-3-1/h1-4H2"));
		solvent.setRole(ChemicalRole.solvent);
		reaction.addReactant(misclassifiedReactant);
		reaction.addSpectator(solvent);
		new ChemicalSenseApplication(reaction).correctReactantsThatAreSolvents();
		assertEquals(ChemicalRole.solvent, misclassifiedReactant.getRole());
	}
	
	@Test
	public void correctlyClassifiedTest() {
		Reaction reaction = new Reaction();
		Chemical reactant = new Chemical("DMF");
		reactant.setChemicalIdentifierPair(new ChemicalIdentifierPair("COC(/C([H])=C([H])/C(OC)=O)=O", "InChI=1/C6H8O4/c1-9-5(7)3-4-6(8)10-2/h3-4H,1-2H3/b4-3+"));
		reactant.setRole(ChemicalRole.reactant);
		Chemical solvent = new Chemical("DMF");
		solvent.setChemicalIdentifierPair(new ChemicalIdentifierPair("CN(C=O)C", "InChI=1/C3H7NO/c1-4(2)3-5/h3H,1-2H3"));
		solvent.setRole(ChemicalRole.solvent);
		reaction.addReactant(reactant);
		reaction.addSpectator(solvent);
		new ChemicalSenseApplication(reaction).correctReactantsThatAreSolvents();
		assertEquals(ChemicalRole.reactant, reactant.getRole());
	}
	
	@Test
	public void correctReactantToSolventUsingKnowledge() {
		Reaction reaction = new Reaction();
		Chemical reactant = new Chemical("THF");
		reactant.setChemicalIdentifierPair(new ChemicalIdentifierPair("C1CCOC1", "InChI=1/C4H8O/c1-2-4-5-3-1/h1-4H2"));
		reactant.setRole(ChemicalRole.reactant);
		reaction.addReactant(reactant);
		new ChemicalSenseApplication(reaction).correctReactantsThatAreSolvents();
		assertEquals(ChemicalRole.solvent, reactant.getRole());
	}
	
	@Test
	public void correctReactantToSolventUsingKnowledge2() {
		Reaction reaction = new Reaction();
		Chemical reactant = new Chemical("THF");
		reactant.setChemicalIdentifierPair(new ChemicalIdentifierPair("C1CCOC1", "InChI=1/C4H8O/c1-2-4-5-3-1/h1-4H2"));
		reactant.setAmountValue("0.5");
		reactant.setAmountUnits("mM");
		reactant.setRole(ChemicalRole.reactant);
		reaction.addReactant(reactant);
		new ChemicalSenseApplication(reaction).correctReactantsThatAreSolvents();
		assertEquals(ChemicalRole.reactant, reactant.getRole());
	}
	
	@Test
	public void correctReactantToSolventUsingKnowledge3() {
		Reaction reaction = new Reaction();
		Chemical reactant = new Chemical("acetic acid");//can be a solvent
		reactant.setChemicalIdentifierPair(new ChemicalIdentifierPair("CC(O)=O", "InChI=1/C2H4O2/c1-2(3)4/h1H3,(H,3,4)"));
		reactant.setAmountValue("0.5");
		reactant.setAmountUnits("mM");
		reactant.setRole(ChemicalRole.reactant);
		reaction.addReactant(reactant);
		Chemical misclassifiedSolvent = new Chemical("THF");
		misclassifiedSolvent.setChemicalIdentifierPair(new ChemicalIdentifierPair("C1CCOC1", "InChI=1/C4H8O/c1-2-4-5-3-1/h1-4H2"));
		misclassifiedSolvent.setRole(ChemicalRole.reactant);
		reaction.addReactant(misclassifiedSolvent);
		new ChemicalSenseApplication(reaction).correctReactantsThatAreSolvents();
		assertEquals(ChemicalRole.reactant, reactant.getRole());
		assertEquals(ChemicalRole.solvent, misclassifiedSolvent.getRole());
	}

	@Test
	public void assignedAsSolventDueToInpreciseVolumeTest(){
		Reaction reaction = new Reaction();
		Chemical reactant = new Chemical("foo");
		reactant.setVolumeValue("500");
		reactant.setVolumeUnits("ml");
		reactant.setRole(ChemicalRole.reactant);
		reaction.addReactant(reactant);
		new ChemicalSenseApplication(reaction).correctReactantsThatAreSolvents();
		assertEquals(ChemicalRole.solvent, reactant.getRole());
	}
	
	@Test
	public void correctReactantToSolventUsingKnowledge4() {
		Reaction reaction = new Reaction();
		Chemical misclassifiedSolvent = new Chemical("foo");//can be a solvent
		misclassifiedSolvent.setVolumeValue("500");
		misclassifiedSolvent.setVolumeUnits("ml");
		misclassifiedSolvent.setRole(ChemicalRole.reactant);
		reaction.addReactant(misclassifiedSolvent);
		Chemical reactant = new Chemical("foo");
		reactant.setRole(ChemicalRole.reactant);
		reaction.addReactant(reactant);
		new ChemicalSenseApplication(reaction).correctReactantsThatAreSolvents();
		assertEquals(ChemicalRole.solvent, misclassifiedSolvent.getRole());
		assertEquals(ChemicalRole.reactant, reactant.getRole());
	}

	@Test
	public void onlyEmployHeuristicWhenNoSolventPresentTest(){
		Reaction reaction = new Reaction();
		Chemical reactant = new Chemical("foo");
		reactant.setVolumeValue("500");
		reactant.setVolumeUnits("ml");
		reactant.setRole(ChemicalRole.reactant);
		reaction.addReactant(reactant);
		Chemical solvent = new Chemical("THF");
		solvent.setChemicalIdentifierPair(new ChemicalIdentifierPair("C1CCOC1", "InChI=1/C4H8O/c1-2-4-5-3-1/h1-4H2"));
		solvent.setRole(ChemicalRole.solvent);
		reaction.addReactant(solvent);
		new ChemicalSenseApplication(reaction).correctReactantsThatAreSolvents();
		assertEquals(ChemicalRole.reactant, reactant.getRole());
	}
}
