package com.mrprez.gencross.insCustom.test;

import java.io.File;
import java.net.URL;

import org.junit.Before;
import org.junit.Test;

import com.mrprez.gencross.Personnage;
import com.mrprez.gencross.disk.PersonnageFactory;
import com.mrprez.gencross.disk.PersonnageSaver;

public class MigrateFromIns {
	private File dataDir;
	private File destDir;
	
	
	@Before
	public void setup(){
		URL dataUrl = ClassLoader.getSystemResource("data");
		dataDir = new File(dataUrl.getFile());
		destDir = new File( dataDir.getParentFile(), "dest");
		destDir.mkdir();
		File files[] = destDir.listFiles();
		for(int i=0; i<files.length; i++){
			files[i].delete();
		}
	}
	
	
	@Test
	public void test() throws Exception{
		File files[] = dataDir.listFiles();
		PersonnageFactory personnageFactory = new PersonnageFactory(true);
		for(int i=0; i<files.length; i++){
			Personnage personnage = personnageFactory.loadPersonnageFromXml(files[i]);
			PersonnageSaver.savePersonnageXml(personnage, new File(destDir, files[i].getName()));
		}
		
		
	}

}
