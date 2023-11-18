package com.rs.utils;

import java.io.File;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileChannel.MapMode;
import java.util.HashMap;
import com.rs.game.item.Item;
import com.rs.game.World;
import com.rs.game.WorldTile;
import com.rs.game.Region;
import com.rs.game.item.FloorItem;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.*;



public final class FloorItemsLoader {

	private FloorItemsLoader() {

	}

	private final static String PACKED_PATH = "data/items/FloorItems.ib";

	public static HashMap <Integer, Integer> floorItems; // FloorItemIndex, ItemID
	public static HashMap <Integer, Integer> floorItemsAmount; // FloorItemIndex, ItemAmount
	public static HashMap <Integer, Integer> floorItemsX; // FloorItemIndex, x
	public static HashMap <Integer, Integer> floorItemsY; // FloorItemIndex, y
	public static HashMap <Integer, Integer> floorItemsZ; // FloorItemIndex, z 
	public static HashMap <Integer, FloorItem> floorItemsStored; //These will be stored items that are tested compared to later.

	public static int[] RANDOM_ITEMS = { 995 , 989 }; //Id's only, amount are randomized on creation method-->create_RandomItem()
	public static int[] RANDOM_ITEMS_RARES = { 25065 }; //Id's only, amount are randomized on creation method-->create_RandomItem()



	public static final void init() {

		if (   new File(PACKED_PATH).exists()){ loadFloorItems();    }
		else{ throw new RuntimeException("Missing FloorItems.ib, pack a new one."); }

	}


	public  static boolean doesContainExactly(int key, Region region, WorldTile tile,Item item) {
				//returns true if the passed item is found in the exact amount that was set in the text. This can be used on unstackable items as well. False other wise.

				try{


					region.forceGetFloorItems(); //do this to prevent null container error
					if(region.getFloorItems().size()==0){ //no items were found in the region at all, so return false
						return false;
					}else{//some items were found in the regions container


						int howManyFound = 0;
						int howManyDesired = item.getAmount();

						floorItemsStored = new HashMap<Integer, FloorItem >(180);
						floorItemsStored.clear();//just cause
						for(int index = 0 ; index < region.getFloorItems().size() ;index++){//load all floor items in that region, assign an index to each.
							//Load a single flooritem that was found in the region. This floor item will contain all of its personal data, like its id, amount, and location.
							FloorItem floorItem = new FloorItem( region.getFloorItems().get(index),  new WorldTile(region.getFloorItems().get(index).getTile().getX(),region.getFloorItems().get(index).getTile().getY(),region.getFloorItems().get(index).getTile().getPlane()), null, false, false); // load in a floor item of the region with its values
							if(floorItem!=null){
								floorItemsStored.put(index, floorItem ); 
							}
						}


						//now that we have all the items in the region hashed, see if what we want is there, or not,  or count.
						for(int index = 0 ; index < floorItemsStored.size() ;index++){//load all floor items in that region, assign an index to each.
								FloorItem hashedFloorItem = get_FloorItem_Stored(index);

								if(hashedFloorItem.getId() == get_FloorItem_ItemID(key) ){
									//same item
									if( hashedFloorItem.getTile().getX() ==  get_FloorItem_X(key) && hashedFloorItem.getTile().getY() ==  get_FloorItem_Y(key) && hashedFloorItem.getTile().getPlane() ==  get_FloorItem_Z(key) ) {
										//this item is on a respawn tile that is equal to the keys
										if(   hashedFloorItem.getDefinitions().isStackable()   ){
											if(hashedFloorItem.getAmount() == get_FloorItem_Amount(key)){return true;}
										}else{
											if(hashedFloorItem.getAmount() == 1 ) {howManyFound++;}
										}

									}
								}



						}

						if(howManyFound==howManyDesired){
							return true;
						}


					


					}
				}catch(Exception e){
					//System.out.println(e );
				}

		return false;
	}

	public  static boolean doesContainRegion(int key, Region region, WorldTile tile,Item item) {
				//returns true if the same item is found within the region.

				FloorItem floorItem = new FloorItem(item.getId() );
				int floorItemID , floorItemAmount, floorItemX,floorItemY,floorItemZ; // any items xyz that isa floor item
				
				region.forceGetFloorItems(); //do this to prevent null container error
				if(region.getFloorItems().size()==0){ //no items were found in the region at all, so return false
					return false;
				}else{//some items were found in the regions container
					for(int index = 0 ; index < region.getFloorItems().size() ;index++){//load all floor items in that region, assign an index to each.
						//Using the index assigned to each flooritem in the region, creat a flooritem object to test against the keys of the hashmap.
						floorItem = new FloorItem( region.getFloorItems().get(index),  new WorldTile(region.getFloorItems().get(index).getTile().getX(),region.getFloorItems().get(index).getTile().getY(),region.getFloorItems().get(index).getTile().getPlane()), null, false, false);
						floorItemID = floorItem.getId();
						floorItemAmount = floorItem.getAmount();
						floorItemX = floorItem.getTile().getX();
						floorItemY = floorItem.getTile().getY();
						floorItemZ = floorItem.getTile().getPlane();
						//Can add more code here to check for amount if desired, can also remove the above variables assignment if not wanted. The ones not used at least.
						if(floorItemID == get_FloorItem_ItemID(key)){
							return true;
						}
					}
				}
		return false;
	}

	public  static boolean doesContainNear(int key, Region region, WorldTile tile,Item item,int nearDistance) {
				//This method can be called on to see if the same item, not the the amount, is within the nearDistance of the spawnable flooritems ID.
				//Example, contains_SameItemID_Near(etc, 995(COINS), 5 ) --> if coins are within the grid of 5 tiles, it will return true, false other wise.

				FloorItem floorItem = new FloorItem(item.getId() );
				int floorItemID , floorItemAmount, floorItemX,floorItemY,floorItemZ; // any items xyz that isa floor item
				int areaAround_tile_mod =  nearDistance;
				int tile_X = tile.getX(), tile_Y = tile.getY() , initial_tile_Plane = tile.getPlane();
				int areaAround_tile_XUP = tile.getX() + areaAround_tile_mod + 1 , areaAround_tile_YUP = tile.getY() + areaAround_tile_mod + 1; //used in grid searching
				int areaAround_tile_XDOWN = tile.getX() -  areaAround_tile_mod  , areaAround_tile_YDOWN = tile.getY() - areaAround_tile_mod ;

				region.forceGetFloorItems(); //do this to prevent null container error

				if(region.getFloorItems().size()==0){ //no items were found in the region at all, so return false
					return false;
				}else{//some items were found in the regions container
					for(int index = 0 ; index < region.getFloorItems().size() ;index++){//load all floor items in that region, assign an index to each.
						//Using the index assigned to each flooritem in the region, creat a flooritem object to test against the keys of the hashmap.
						floorItem = new FloorItem( region.getFloorItems().get(index),  new WorldTile(region.getFloorItems().get(index).getTile().getX(),region.getFloorItems().get(index).getTile().getY(),region.getFloorItems().get(index).getTile().getPlane()), null, false, false);
						floorItemID = floorItem.getId();
						floorItemAmount = floorItem.getAmount();
						floorItemX = floorItem.getTile().getX();
						floorItemY = floorItem.getTile().getY();
						floorItemZ = floorItem.getTile().getPlane();
						for(int searchThisX = areaAround_tile_XDOWN  ; 		searchThisX < areaAround_tile_XUP; 		searchThisX++){
							for(int searchThisY = areaAround_tile_YDOWN  ; 	searchThisY < areaAround_tile_YUP; 		searchThisY++){//searching within the grid of the respawnable item.
 								if(floorItemX == searchThisX && floorItemY == searchThisY){
									 if( floorItemID == item.getId() ){
									 		return true; 
									 }	
								}
							}
						}				
					}
				}


		return false;
	}

	public  static int getContainingAmount(int key, Region region, WorldTile tile,Item item) {
				//returns the amount of the item found.
				try{

					region.forceGetFloorItems(); //do this to prevent null container error
					if(region.getFloorItems().size()==0){ //no items were found in the region at all, so return false
						return 0;
					}else{//some items were found in the regions container
						int howManyFound = 0;
						int howManyDesired = item.getAmount();
						floorItemsStored = new HashMap<Integer, FloorItem >(180);
						floorItemsStored.clear();//just cause
						for(int index = 0 ; index < region.getFloorItems().size() ;index++){//load all floor items in that region, assign an index to each.
							//Load a single flooritem that was found in the region. This floor item will contain all of its personal data, like its id, amount, and location.
							FloorItem floorItem = new FloorItem( region.getFloorItems().get(index),  new WorldTile(region.getFloorItems().get(index).getTile().getX(),region.getFloorItems().get(index).getTile().getY(),region.getFloorItems().get(index).getTile().getPlane()), null, false, false); // load in a floor item of the region with its values
							if(floorItem!=null){
								floorItemsStored.put(index, floorItem ); 
							}
						}

						//now that we have all the items in the region hashed, see if what we want is there, or not,  or count.
						for(int index = 0 ; index < floorItemsStored.size() ;index++){//load all floor items in that region, assign an index to each.
								FloorItem hashedFloorItem = get_FloorItem_Stored(index);
								if(hashedFloorItem.getId() == get_FloorItem_ItemID(key) ){
									//same item
									if( hashedFloorItem.getTile().getX() ==  get_FloorItem_X(key) && hashedFloorItem.getTile().getY() ==  get_FloorItem_Y(key) && hashedFloorItem.getTile().getPlane() ==  get_FloorItem_Z(key) ) {
										//this item is on a respawn tile that is equal to the keys
										if(   hashedFloorItem.getDefinitions().isStackable()   ){
											if(hashedFloorItem.getAmount() == get_FloorItem_Amount(key)){return item.getAmount();}
										}else{
											if(hashedFloorItem.getAmount() == 1 ) {howManyFound++;}
										}
									}
								}
						}
	
						if (howManyFound != 0){ return howManyFound; }
					}
				}catch(Exception e){
					//System.out.println(e );
				}

		return 0;
	}

	public static final int get_FloorItem_ItemID(int keyValue ) {
		if (floorItems.get(keyValue) != null){
			return floorItems.get(keyValue);
		}
		return -1;
	}

	public static final int get_FloorItem_X(int keyValue ) {
		if (floorItemsX.get(keyValue) != null){
			return floorItemsX.get(keyValue);
		}
		return -1;
	}

	public static final int get_FloorItem_Y(int keyValue ) {
		if (floorItemsY.get(keyValue) != null){
			return floorItemsY.get(keyValue);
		}
		return -1;
	}

	public static final int get_FloorItem_Z(int keyValue ) {
		if (floorItemsZ.get(keyValue) != null){
			return floorItemsZ.get(keyValue);
		}
		return -1;
	}

	public static final int get_FloorItem_Amount(int keyValue ) {
		if (floorItemsAmount.get(keyValue) != null){
			return floorItemsAmount.get(keyValue);
		}
		return -1;
	}

	public static final FloorItem get_FloorItem_Stored(int keyValue ) {
		if (floorItemsStored.get(keyValue) != null){
			return floorItemsStored.get(keyValue);
		}
		return null;
	}

	public static final void loadFloorItems() {
		//Load in the packed file into hashmaps to access at sweet O(1) time.
		try {
			RandomAccessFile in = new RandomAccessFile(PACKED_PATH, "r");
			FileChannel channel = in.getChannel(); //channels for the packed memory file to come IN to.
			ByteBuffer buffer = channel.map(MapMode.READ_ONLY, 0,channel.size());
			floorItems = new HashMap<Integer, Integer >(buffer.remaining() );  // prep the hashmaps for data assignment
			floorItemsAmount = new HashMap<Integer, Integer >(buffer.remaining() );  
			floorItemsX = new HashMap<Integer, Integer >(buffer.remaining() );
			floorItemsY = new HashMap<Integer, Integer >(buffer.remaining() );
			floorItemsZ = new HashMap<Integer, Integer >(buffer.remaining() );
			while (buffer.hasRemaining()) {
				int[] data = new int[6]; //6 slots to hold the data in for each line of flooritem in the textfile --> objectfile
				for (int index = 0; index < data.length; index++){
					data[index] = buffer.getShort() & 0xffff;
					//if(index==0){System.out.println( "FloorItemsLoader: Index: "+data[index] );}     //enable if testing is needed
					//if(index==1){System.out.println( "FloorItemsLoader: ID: "+data[index] );} 
					//if(index==2){System.out.println( "FloorItemsLoader: Amount: "+data[index] );} 
					//if(index==3){System.out.println( "FloorItemsLoader: X: "+data[index] );} 
					//if(index==4){System.out.println( "FloorItemsLoader: Y: "+data[index] );} 
					//if(index==5){System.out.println( "FloorItemsLoader: Z: "+data[index] );} 
				}
				//System.out.println( " ");
				floorItems.put(data[0], data[1] ); // Index + ItemID
				floorItemsAmount.put(data[0], data[2] ); // Index + ItemID
				floorItemsX.put(data[0], data[3] ); // Index + x
				floorItemsY.put(data[0], data[4] ); // Index + y
				floorItemsZ.put(data[0], data[5] ); // Index + z
			}
			channel.close();
			in.close();
		} catch (Throwable e) {
			Logger.handle(e);
		}

	}




	public  static boolean tileDoesContainRandomItem(int key, Region region, WorldTile tile) {
				//returns true if the passed tile contains an item from the randomitems array

				try{


					region.forceGetFloorItems(); //do this to prevent null container error
					if(region.getFloorItems().size()==0){ //no items were found in the region at all, so return false
						return false;
					}else{//some items were found in the regions container


						floorItemsStored = new HashMap<Integer, FloorItem >(180);
						floorItemsStored.clear();//just cause

						for(int index = 0 ; index < region.getFloorItems().size() ;index++){//load all floor items in that region, assign an index to each.
							//Load a single flooritem that was found in the region. This floor item will contain all of its personal data, like its id, amount, and location.
							FloorItem floorItem = new FloorItem( region.getFloorItems().get(index),  new WorldTile(region.getFloorItems().get(index).getTile().getX(),region.getFloorItems().get(index).getTile().getY(),region.getFloorItems().get(index).getTile().getPlane()), null, false, false); // load in a floor item of the region with its values
							if(floorItem!=null){
								floorItemsStored.put(index, floorItem ); 
							}
						}


						//now that we have all the items in the region hashed, see if what we want is there, or not,  or count.
						for(int index = 0 ; index < floorItemsStored.size() ;index++){//load all floor items in that region, assign an index to each.
								FloorItem hashedFloorItem = get_FloorItem_Stored(index);


									//iterate through flooritems

									if( hashedFloorItem.getTile().getX() ==  get_FloorItem_X(key) && hashedFloorItem.getTile().getY() ==  get_FloorItem_Y(key) && hashedFloorItem.getTile().getPlane() ==  get_FloorItem_Z(key) ) {
										//respawn tile that is equal to the keys
										
										//is this item, a random item?

										if( is_randomItem( hashedFloorItem.getId() ) ){
											return true;
										}

									}


						}


					}
				}catch(Exception e){
					//System.out.println(e );
				}

		return false;
	}

	public  static FloorItem getRandomFlooritemOnTile(int key, Region region, WorldTile tile) {
				//returns true if the passed tile contains an item from the randomitems array

				try{


					region.forceGetFloorItems(); //do this to prevent null container error
					if(region.getFloorItems().size()==0){ //no items were found in the region at all, so return false
						return null;
					}else{//some items were found in the regions container


						floorItemsStored = new HashMap<Integer, FloorItem >(180);
						floorItemsStored.clear();//just cause

						for(int index = 0 ; index < region.getFloorItems().size() ;index++){//load all floor items in that region, assign an index to each.
							//Load a single flooritem that was found in the region. This floor item will contain all of its personal data, like its id, amount, and location.
							FloorItem floorItem = new FloorItem( region.getFloorItems().get(index),  new WorldTile(region.getFloorItems().get(index).getTile().getX(),region.getFloorItems().get(index).getTile().getY(),region.getFloorItems().get(index).getTile().getPlane()), null, false, false); // load in a floor item of the region with its values
							if(floorItem!=null){
								floorItemsStored.put(index, floorItem ); 
							}
						}


						//now that we have all the items in the region hashed, see if what we want is there, or not,  or count.
						for(int index = 0 ; index < floorItemsStored.size() ;index++){//load all floor items in that region, assign an index to each.
								FloorItem hashedFloorItem = get_FloorItem_Stored(index);

									if( hashedFloorItem.getTile().getX() ==  get_FloorItem_X(key) && hashedFloorItem.getTile().getY() ==  get_FloorItem_Y(key) && hashedFloorItem.getTile().getPlane() ==  get_FloorItem_Z(key) ) {
										if( is_randomItem(hashedFloorItem.getId()) ){
											return hashedFloorItem;
										}
									}
						}
					}

				}catch(Exception e){
					//System.out.println(e );
				}

		return null;
	}

	public static Item create_RandomItem(){

			//Standard random items
			Item item = new Item(995,1); //temp random item
			int length = RANDOM_ITEMS.length - 1;

			int index = Utils.getRandom(0 + length); //let's us default to index 0

			if(RANDOM_ITEMS[index] == 995){//if the item is coins
				item.setId( RANDOM_ITEMS[index] );
				item.setAmount(   1 +  Utils.getRandom(10000)   );
			}else{//anything else
				item.setId(RANDOM_ITEMS[index]);
				if(   item.getDefinitions().isStackable()   ){
					item.setAmount(   1 +  Utils.getRandom(3)   );					//if it's stackable, give them a few.
				}else{
					item.setAmount(1);					//other wise just 1
				}

			}



			if( Utils.getRandom(500) == 0 ){ 
				//create a rare instead

				index = Utils.getRandom(0 + RANDOM_ITEMS_RARES.length - 1);
				item.setId(RANDOM_ITEMS_RARES[index]);
				item.setAmount(1);
			
			
			}







			return  item;  
 	}

	public static boolean is_randomItem(int itemId){
			//Returns true if the sent item id is a value that is in the randomitem array

			for(int index = 0; index < (RANDOM_ITEMS.length) ; index++){
				if(RANDOM_ITEMS[index] == itemId){
					return true;
				}
			}

			for(int index = 0; index < (RANDOM_ITEMS_RARES.length) ; index++){
				if(RANDOM_ITEMS_RARES[index] == itemId){
					return true;
				}
			}


			return  false;  
 	}









}



		
