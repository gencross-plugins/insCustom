package com.mrprez.gencross.impl.insCustom;

import java.util.List;

import com.mrprez.gencross.disk.PersonnageFactory;
import com.mrprez.gencross.history.HistoryItem;
import com.mrprez.gencross.history.HistoryPlayer;
import com.mrprez.gencross.migration.MigrationPersonnage;
import com.mrprez.gencross.migration.Migrator;
import com.mrprez.gencross.value.DoubleValue;
import com.mrprez.gencross.value.IntValue;

public class MigrationFrom1_9 implements Migrator {

	@Override
	public MigrationPersonnage migrate(MigrationPersonnage migrationPersonnage) throws Exception {
		treatHistory( migrationPersonnage );
		
		InsCustom newPersonnage = (InsCustom) new PersonnageFactory().buildNewPersonnage("INS Custom");
		
		HistoryPlayer historyPlayer = new HistoryPlayer(migrationPersonnage.getHistory());
		
		historyPlayer.playHistory(newPersonnage);
		
		return new MigrationPersonnage(newPersonnage.getXML().getRootElement(), newPersonnage.getPluginDescriptor() );
	}
	
	
	private void treatHistory( MigrationPersonnage migrationPersonnage ){
		List<HistoryItem> history = migrationPersonnage.getHistory();
		for( HistoryItem historyItem : history){
			if( isConcerned(historyItem.getAbsoluteName()) ){
				if( historyItem.getOldValue()!=null && historyItem.getOldValue() instanceof DoubleValue ){
					historyItem.setOldValue(new IntValue((int) (historyItem.getOldValue().getDouble()*2.0)));
				}
				if( historyItem.getNewValue()!=null && historyItem.getNewValue() instanceof DoubleValue){
					historyItem.setNewValue(new IntValue((int) (historyItem.getNewValue().getDouble()*2.0)));
				}
			}
		}
		
	}
	
//	private void treatProperty( Property property ){
//		if(isConcerned(property.getAbsoluteName())){
//			if( property.getValue()!=null && property.getValue() instanceof DoubleValue ){
//				property.setValue(new IntValue((int) (property.getValue().getDouble()*2.0)));
//			}
//			if(property.getMax()!=null && property.getMax() instanceof DoubleValue){
//				property.setMax(new IntValue((int) (property.getMax().getDouble()*2.0)));
//			}
//			if(property.getMin()!=null && property.getMin() instanceof DoubleValue){
//				property.setMin(new IntValue((int) (property.getMin().getDouble()*2.0)));
//			}
//			if(property.getSubProperties()!=null){
//				for(Property subProperty : property.getSubProperties()){
//					treatProperty(subProperty);
//				}
//			}
//		}
//		
//	}
	
	private boolean isConcerned( String absoluteName ){
		if( absoluteName.startsWith("Caracteristiques") ){
			return true;
		}
		if( absoluteName.startsWith("Talents principaux" ) ){
			return true;
		}
		if( absoluteName.startsWith( "Talents exotiques" ) ){
			return true;
		}
		if( absoluteName.startsWith( "Talents secondaires" ) ){
			return true;
		}
		if( absoluteName.startsWith( "Pouvoirs" ) ){
			return true;
		}
		return false;
	}

}
