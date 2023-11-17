package com.rs.tools;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
import com.rs.game.Region;
import com.rs.game.item.FloorItem;

import com.rs.cache.Cache;
import com.rs.utils.Utils;

public class FloorItemPacker {

	
	public static  int floorItemsCount = 0;

	
	public static  final void main(String[] args) throws IOException {
			Cache.init();
			BufferedReader in = new BufferedReader(new FileReader("./data/items/UnpackedFloorItems.txt"));
			DataOutputStream out = new DataOutputStream(new FileOutputStream("./data/items/FloorItems.ib"));
			System.out.println("FloorItem Count: " + countLines() );
		
			while (true) {

						String line = in.readLine();

						if (line == null){ break; }

						if (line.startsWith("//")){  continue;  }

						String[] splitedLine = line.split(" - ");

						String[] splitedLine2 = splitedLine[2].split(" ");

						out.writeShort( Integer.valueOf(splitedLine[0])  ); //index
						out.writeShort( Integer.valueOf(splitedLine[1])  ); //ID

						out.writeShort( Integer.valueOf(splitedLine2[0])  ); //amount
						out.writeShort( Integer.valueOf(splitedLine2[1])  ); //x
						out.writeShort( Integer.valueOf(splitedLine2[2])  ); //y
						out.writeShort( Integer.valueOf(splitedLine2[3])  ); //z

						//System.out.println(" " + splitedLine[0] );
						//System.out.println(" " + splitedLine[1] );
						//System.out.println(" " + splitedLine2[0] );
						//System.out.println(" " + splitedLine2[1] );
						//System.out.println(" " + splitedLine2[2] );
						//System.out.println(" " + splitedLine2[3] );
					
			}


			out.close();
			in.close();

	}//end method




	public  static int countLines() throws IOException {
	    Cache.init();
		BufferedReader in = new BufferedReader(new FileReader("./data/items/UnpackedFloorItems.txt"));
	    LineNumberReader reader = null;
	    try {
	        reader = new LineNumberReader(new FileReader("./data/items/UnpackedFloorItems.txt"));
	        while ((reader.readLine()) != null){
	        	floorItemsCount++;
	        }
	        return floorItemsCount -1 ; //-1 to account for the  // comment at the top
	    } catch (Exception ex) {
	        return -1;
	    } finally { 
	        if(reader != null) 
	            reader.close();
	    }
	}


	public static int getMax()  {
		//max amount of created flooritems, used for settings.
			try {
				return countLines();
		   	} catch (Exception ex) {

		   	}
		   return -1;
	}

}//end class

