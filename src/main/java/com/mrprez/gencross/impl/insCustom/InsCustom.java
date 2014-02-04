package com.mrprez.gencross.impl.insCustom;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import com.mrprez.gencross.Personnage;
import com.mrprez.gencross.PropertiesList;
import com.mrprez.gencross.Property;
import com.mrprez.gencross.history.ConstantHistoryFactory;
import com.mrprez.gencross.history.FreeHistoryFactory;
import com.mrprez.gencross.history.HistoryFactory;
import com.mrprez.gencross.history.HistoryItem;
import com.mrprez.gencross.history.HistoryUtil;
import com.mrprez.gencross.history.ProportionalHistoryFactory;
import com.mrprez.gencross.util.PersonnageUtil;
import com.mrprez.gencross.value.IntValue;
import com.mrprez.gencross.value.Value;

public class InsCustom extends Personnage {
	
	@Override
	public void calculate() {
		super.calculate();
		if(phase.equals("Choix Superieur")){
			if(getProperty("Superieur").getValue().toString().equals("")){
				errors.add("Vous devez choisir un supérieur");
			}
		}else if(phase.equals("Creation")){
			calculateCaracValueError();		
			checkPAInteval("Caracteristiques",16,40);
			checkPATalent();
			checkPAPouvoirs();
			checkPAInteval("PP",2,8);
			checkLimitation();
		}
	}
	
	@Override
	public boolean phaseFinished() {
		if(phase.equals("Grade 0")){
			return pointPools.get("Pouvoirs de grade").getTotal()>=2;
		}
		if(phase.equals("Grade 1")){
			return pointPools.get("Pouvoirs de grade").getTotal()>=4;
		}
		if(phase.equals("Grade 2")){
			return pointPools.get("Pouvoirs de grade").getTotal()>=6;
		}
		if(phase.equals("Grade 3")){
			return pointPools.get("Pouvoirs de grade").getTotal()>=8;
		}
		return super.phaseFinished();
	}



	private void addPouvoirsExclusifs(){
		String superieur = getProperty("Superieur").getValue().toString();
		superieur = superieur.substring(0,superieur.indexOf(" - "));
		Map<String, String> pouvoirsExclusifs = appendix.getSubMap("pouvoirExclusif."+superieur);
		for(String pouvoirExclusif : pouvoirsExclusifs.values()){
			PropertiesList pouvoirList = getProperty("Pouvoirs").getSubProperties();
			Property newPouvoir = new Property(pouvoirExclusif,getProperty("Pouvoirs"));
			if("oui".equals(appendix.getProperty(pouvoirExclusif+".invariable", "non"))){
				int cout = Integer.parseInt(appendix.getProperty(pouvoirExclusif+".cout","3"));
				newPouvoir.setHistoryFactory(new ConstantHistoryFactory("PA", cout));
			}else{
				newPouvoir.setValue(new IntValue(2));
				newPouvoir.setMin();
				newPouvoir.setMax(new IntValue(4));
				int cout = Integer.parseInt(appendix.getProperty(pouvoirExclusif+".cout","3"));
				newPouvoir.setHistoryFactory(new ProportionalHistoryFactory("PA", cout));
			}
			pouvoirList.getOptions().put(newPouvoir.getFullName(), newPouvoir);
		}
	}
	
	
	private void changePouvoirsCosts(){
		String superieur = getProperty("Superieur").getValue().toString();
		superieur = superieur.substring(0,superieur.indexOf(" - "));
		Map<String, String> coutsReduits = appendix.getSubMap("coutreduit."+superieur);
		for(String key : coutsReduits.keySet()){
			String pouvoirName = key.substring(key.lastIndexOf('.')+1).replaceAll("_", " ");
			Property pouvoir = getProperty("Pouvoirs").getSubProperties().getOptions().get(pouvoirName);
			HistoryFactory historyFactory = pouvoir.getHistoryFactory();
			Map<String, String> args = new HashMap<String, String>();
			if(historyFactory instanceof ConstantHistoryFactory){
				args.put("cost", coutsReduits.get(key));
			}else{
				args.put("factor", coutsReduits.get(key));
			}
			historyFactory.setArgs(args);
		}
	}



	private void checkPAInteval(String source, int min, int max){
		int spendPA = HistoryUtil.sumHistory(history, source+"#[^#]*", "PA");
		spendPA = spendPA + HistoryUtil.sumHistory(history, source, "PA");
		if(spendPA<min || spendPA>max){
			errors.add("Vous devez dépenser entre "+min+" et "+max+" PA en "+source);
		}
	}
	
	private void checkPAPouvoirs(){
		int spendPA = HistoryUtil.sumHistory(history, "Pouvoirs#[^#]*", "PA");
		spendPA = spendPA + HistoryUtil.sumHistory(history, "Avantages#[^#]*", "PA");
		if(spendPA<20 || spendPA>28){
			errors.add("Vous devez dépenser entre 20 et 28 PA en pouvoirs ou avantages");
		}
	}
	
	private void checkPATalent(){
		int spendPA = HistoryUtil.sumHistoryOfSubTree(history, getProperty("Talents principaux"), "PA");
		spendPA = spendPA + HistoryUtil.sumHistoryOfSubTree(history, getProperty("Talents exotiques"), "PA");
		spendPA = spendPA + HistoryUtil.sumHistory(history, "PA en talents secondaires", "PA");
		if(spendPA<20 || spendPA>40){
			errors.add("Vous devez dépenser entre 20 et 40 PA en Talents");
		}
	}
	
	private void checkLimitation(){
		if(getProperty("Limitations").getSubProperties().size()>1){
			errors.add("Vous ne pouvez avoir plus d'une limitation");
		}
	}
	
	private void calculateCaracValueError(){
		Iterator<Property> it = this.getProperty("Caracteristiques").iterator();
		int compte = 0;
		while(it.hasNext()){
			Property carac = it.next();
			if(carac.getValue().getInt()==3){
				compte++;
			}
		}
		if(compte>1){
			errors.add("Vous ne pouvez avoir qu'une caractéristique à 3");
		}
	}
	
	public Boolean checkCaracChangement(Property carac, Value newValue){
		Iterator<Property> it = getProperty("Talents principaux").iterator();
		while(it.hasNext()){
			Property talent = it.next();
			if(appendix.getProperty("carac."+talent.getFullName(),"").equals(carac.getFullName())){
				if(!checkBaseValue(talent, newValue.getInt(), carac.getValue().getInt())){
					return false;
				}
			}
		}
		it = getProperty("Talents secondaires").iterator();
		while(it.hasNext()){
			Property talent = it.next();
			if(appendix.getProperty("carac."+talent.getFullName(),"").equals(carac.getFullName())){
				if(!checkBaseValue(talent, newValue.getInt(), carac.getValue().getInt())){
					return false;
				}
			}
		}
		it = getProperty("Talents exotiques").iterator();
		while(it.hasNext()){
			Property talent = it.next();
			if(appendix.getProperty("carac."+talent.getFullName(),"").equals(carac.getFullName())){
				if(!checkBaseValue(talent, newValue.getInt(), carac.getValue().getInt())){
					return false;
				}
			}
		}
		if(carac.getAbsoluteName().equals("Caracteristiques#Foi/Chance") || carac.getAbsoluteName().equals("Caracteristiques#Volonté")){
			int currentPP = getProperty("PP").getValue().getInt();
			int oldPPstartValue = (getProperty("Caracteristiques#Foi/Chance").getValue().getInt()
					+getProperty("Caracteristiques#Volonté").getValue().getInt())
					/ 2;
			int newPPstartValue = (getProperty("Caracteristiques#Foi/Chance").getValue().getInt()
					+getProperty("Caracteristiques#Volonté").getValue().getInt()
					-carac.getValue().getInt()
					+newValue.getInt())
					/ 2;
			if(currentPP - oldPPstartValue + newPPstartValue > getProperty("PP").getMax().getInt()){
				actionMessage = "Vous ne pouvez avoir plus de "+getProperty("PP").getMax().getInt()+" PP";
				return false;
			}
		}
		return true;
	}
	
	private boolean checkBaseValue(Property talent, int newCaracValue, int oldCaracValue){
		int oldStart = oldCaracValue / 2;
		int newStart = newCaracValue / 2;
		if(talent.getValue()!=null){
			if(talent.getValue().getInt()-oldStart+newStart > 14){
				actionMessage = talent.getFullName()+" ne peut dépasser 14";
				return false;
			}
		}
		if(talent.getSubProperties()!=null){
			Iterator<Property> it = talent.getSubProperties().iterator();
			while(it.hasNext()){
				if(!checkBaseValue(it.next(), newCaracValue, oldCaracValue)){
					return false;
				}
			}
		}
		return true;
	}
	
	public void changeCarac(Property carac, Value oldValue){
		Iterator<Property> it = getProperty("Talents principaux").iterator();
		while(it.hasNext()){
			Property talent = it.next();
			if(appendix.getProperty("carac."+talent.getFullName(),"").equals(carac.getFullName())){
				changeBaseValue(talent,(IntValue)oldValue, (IntValue)carac.getValue());
			}
		}
		it = getProperty("Talents exotiques").iterator();
		while(it.hasNext()){
			Property talent = it.next();
			if(appendix.getProperty("carac."+talent.getFullName(),"").equals(carac.getFullName())){
				changeBaseValue(talent,(IntValue)oldValue, (IntValue)carac.getValue());
			}
		}
		it = getProperty("Talents exotiques").getSubProperties().getOptions().values().iterator();
		while(it.hasNext()){
			Property talent = it.next();
			if(appendix.getProperty("carac."+talent.getFullName(),"").equals(carac.getFullName())){
				changeBaseValue(talent,(IntValue)oldValue, (IntValue)carac.getValue());
				Map<String, String> args = ((ProportionalHistoryFactory)talent.getActualHistoryFactory()).getArgs();
				args.put("startValue", talent.getValue().getString());
				((ProportionalHistoryFactory)talent.getHistoryFactory()).setArgs(args);
			}
		}
		it = getProperty("Talents secondaires").iterator();
		while(it.hasNext()){
			Property talent = it.next();
			if(appendix.getProperty("carac."+talent.getFullName(),"").equals(carac.getFullName())){
				changeBaseValue(talent,(IntValue)oldValue, (IntValue)carac.getValue());
			}
		}
	}
	
	public void changePPImpliedCarac(Property carac, Value oldValue){
		int oldPP = ((IntValue)getProperty("PP").getValue()).getValue();
		int newPPstartValue = (getProperty("Caracteristiques#Foi/Chance").getValue().getInt()
				+getProperty("Caracteristiques#Volonté").getValue().getInt())
				/ 2;
		int oldPPstartValue = (getProperty("Caracteristiques#Foi/Chance").getValue().getInt()
				+getProperty("Caracteristiques#Volonté").getValue().getInt()
				-carac.getValue().getInt()
				+oldValue.getInt())
				/ 2;
		getProperty("PP").setValue(Value.createValue(oldPP-oldPPstartValue+newPPstartValue));
		((IntValue)getProperty("PP").getMin()).setValue(newPPstartValue);
	}
	
	public void changeTalent(Property talent, Value oldValue){
		HistoryItem historyItem = history.get(history.size()-1);
		if(historyItem.getPointPool().equals("PA")){
			int cost = historyItem.getCost();
			pointPools.get("Points de talents secondaires").add(cost);
		}
	}
	
	protected void changeBaseValue(Property talent, IntValue oldCaracValue, IntValue newCaracValue){
		if(talent.getValue()!=null){
			int oldStart = oldCaracValue.getValue() / 2;
			int newStart = newCaracValue.getValue() / 2;
			talent.setValue(new IntValue(((IntValue)talent.getValue()).getValue()-oldStart+newStart));
			talent.setMin(new IntValue(newStart));
		}
		if(talent.getSubProperties()!=null && talent.getSubProperties().size()>0){
			changeBaseValue(talent.getSubProperties().get(0),oldCaracValue,newCaracValue);
		}
	}

	public void addSpecialite(Property newProperty){
		Property owner = (Property)newProperty.getOwner();
		if(owner.getValue()!=null){
			owner.getSubProperties().setFixe(true);
		}
		if(appendix.getProperty("carac."+owner.getFullName())!=null){
			String caracName = appendix.getProperty("carac."+owner.getFullName());
			Property carac = getProperty("Caracteristiques#"+caracName);
			newProperty.setValue(new IntValue(carac.getValue().getInt()/2));
			newProperty.setMin();
			ProportionalHistoryFactory historyFactory = (ProportionalHistoryFactory)newProperty.getHistoryFactory();
			Map<String, String> args = historyFactory.getArgs();
			args.put("startValue", newProperty.getValue().getString());
		}
	}
	
	public Boolean removeSpecialite(Property specialite){
		Property talent = (Property) specialite.getOwner();
		if(!talent.getName().equals("Langues")){
			if(specialite.getValue().getInt()>talent.getMin().getInt()){
				actionMessage = "Impossible de supprimer une spécialité supérieur à la valeur de base du talent";
				return false;
			}
			specialite.setValue(Value.createValue(0));
			talent.getSubProperties().setFixe(false);
		}
		return true;
	}
	
	public boolean changeTalentWithSpe(Property property, Value newValue){
		if(property.getSubProperties()==null){
			return true;
		}
		if(property.getSubProperties().size()==0){
			actionMessage = "Vous devez d'abord ajouter une spécialité";
			return false;
		}
		Property specialite = property.getSubProperties().get(0);
		if(newValue.getInt()>specialite.getValue().getInt()){
			actionMessage = "Le talent ne peut dépasser sa spécialité";
			return false;
		}
		return true;
	}
	
	public boolean changeSpe(Property specialite, Value newValue){
		Property talent = (Property) specialite.getOwner();
		if(talent.getValue()!=null && talent.getValue().getInt()>newValue.getInt()){
			actionMessage = "Le talent ne peut dépasser sa spécialité";
			return false;
		}
		return true;
	}
	
	public void passToGrade0(){
		PersonnageUtil.setMinRecursivly(this, "Caracteristiques");
		PersonnageUtil.setMinRecursivly(this, "Talents principaux");
		PersonnageUtil.setMinRecursivly(this, "PA en talents secondaires");
		PersonnageUtil.setMinRecursivly(this, "Talents secondaires");
		PersonnageUtil.setMinRecursivly(this, "Pouvoirs");
		PersonnageUtil.setMinRecursivly(this, "PP");
		PersonnageUtil.setMaxRecursivly(this, "Caracteristiques", new IntValue(11));
		PersonnageUtil.setMaxRecursivly(this, "Talents principaux", new IntValue(15));
		PersonnageUtil.removeMaxRecursivly(this, "PA en talents secondaires");
		PersonnageUtil.setMaxRecursivly(this, "Talents secondaires", new IntValue(15));
		PersonnageUtil.setMaxRecursivly(this, "Talents exotiques", new IntValue(15));
		PersonnageUtil.setMaxRecursivly(this, "Pouvoirs", new IntValue(5));
		getProperty("Limitations").setHistoryFactory(new FreeHistoryFactory("PA"));
		this.getPointPools().get("PA").setToEmpty(false);
	}
	
	public void passToGrade1(){
		getPointPools().get("Pouvoirs de grade").add(2);
		getProperty("Grade").setValue(new IntValue(1));
		PersonnageUtil.setMaxRecursivly(this, "Talents principaux", new IntValue(17));
		PersonnageUtil.setMaxRecursivly(this, "Talents secondaires", new IntValue(17));
		PersonnageUtil.setMaxRecursivly(this, "Talents exotiques", new IntValue(17));
		PersonnageUtil.setMaxRecursivly(this, "Pouvoirs", new IntValue(9));
		PersonnageUtil.setMaxRecursivly(this, "PP", new IntValue(25));
	}
	
	public void passToGrade2(){
		getPointPools().get("Pouvoirs de grade").add(2);
		getProperty("Grade").setValue(new IntValue(2));
		PersonnageUtil.setMaxRecursivly(this, "Caracteristiques", new IntValue(13));
		PersonnageUtil.setMaxRecursivly(this, "Talents principaux", new IntValue(19));
		PersonnageUtil.setMaxRecursivly(this, "Talents secondaires", new IntValue(19));
		PersonnageUtil.setMaxRecursivly(this, "Talents exotiques", new IntValue(19));
		PersonnageUtil.setMaxRecursivly(this, "Pouvoirs", new IntValue(13));
		PersonnageUtil.setMaxRecursivly(this, "PP", new IntValue(40));
	}
	
	public void passToGrade3(){
		getPointPools().get("Pouvoirs de grade").add(2);
		getProperty("Grade").setValue(new IntValue(3));
		PersonnageUtil.setMaxRecursivly(this, "Caracteristiques", new IntValue(15));
		PersonnageUtil.setMaxRecursivly(this, "Talents principaux", new IntValue(21));
		PersonnageUtil.setMaxRecursivly(this, "Talents secondaires", new IntValue(21));
		PersonnageUtil.setMaxRecursivly(this, "Talents exotiques", new IntValue(21));
		PersonnageUtil.setMaxRecursivly(this, "Pouvoirs", new IntValue(17));
		PersonnageUtil.setMaxRecursivly(this, "PP", new IntValue(60));
	}
	
	public void passToCreationPhase(){
		getProperty("Caracteristiques").setEditableRecursivly(true);
		getProperty("Talents principaux").setEditableRecursivly(true);
		getProperty("PA en talents secondaires").setEditable(true);
		getProperty("Talents secondaires").setEditableRecursivly(true);
		getProperty("Talents exotiques").getSubProperties().setFixe(false);
		getProperty("Pouvoirs").getSubProperties().setFixe(false);
		getProperty("PP").setEditable(true);
		getProperty("Avantages").getSubProperties().setFixe(false);
		getProperty("Limitations").getSubProperties().setFixe(false);
		getProperty("Superieur").setEditable(false);
		addPouvoirsExclusifs();
		changePouvoirsCosts();
		this.getPointPools().get("PA").setToEmpty(true);
		
		// Ajout des pouvoirs de grade
		String superieur = getProperty("Superieur").getValue().toString();
		superieur = superieur.substring(0,superieur.indexOf(" - "));
		Collection<String> pouvoirsGrade = appendix.getSubMap("pouvoirGrade."+superieur+".").values();
		for(String domaine : pouvoirsGrade){
			Property pouvoir = new Property(domaine, getProperty("Pouvoirs de grade"));
			pouvoir.setValue(new IntValue(0));
			pouvoir.setMax(new IntValue(6));
			pouvoir.setMin();
			getProperty("Pouvoirs de grade").getSubProperties().add(pouvoir);
		}
		
		// Talents exotiques spécifiques au prince démon
		for(String talentName : appendix.getSubMap("talent_exotique."+superieur+".").values()){
			Property talent = getProperty("Talents exotiques").getSubProperties().getOptions().get(talentName);
			talent.getHistoryFactory().setPointPool("Points de talents secondaires");
		}
	}


	
}
