package com.example.hawkerfinder;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "zNotAgain.db";

    // Stall Table
    private static final String STALL_TABLE_NAME = "stall_table";
    private static final String STALL_COL_1 = "S_ID";
    private static final String STALL_COL_2 = "S_NAME";
    private static final String STALL_COL_3 = "S_ADDRESS";
    private static final String STALL_COL_4 = "S_POSTAL_CODE";

    DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, 1);
        SQLiteDatabase sqLiteDatabase = this.getWritableDatabase(); //create database and table
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        String SQL_String = "CREATE TABLE " + STALL_TABLE_NAME + "(" + STALL_COL_1 + " INTEGER PRIMARY KEY AUTOINCREMENT," + STALL_COL_2 + " TEXT," + STALL_COL_3 + " TEXT," + STALL_COL_4 +" TEXT UNIQUE" + ")";
        sqLiteDatabase.execSQL(SQL_String);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + STALL_TABLE_NAME);
        onCreate(sqLiteDatabase);
    }

    @Override
    public void onDowngrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + STALL_TABLE_NAME);
        onCreate(sqLiteDatabase);
    }

    ///////////////////////////////////////////////////// DATABASE METHODS /////////////////////////////////////////////////////

    public String getStallName(String postal_code){
        SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();
        Cursor res = sqLiteDatabase.rawQuery("SELECT " + STALL_COL_2 + " FROM " + STALL_TABLE_NAME + " WHERE " + STALL_COL_4 + " = '" + postal_code + "'",null);
        res.moveToNext();
        String stall_name = res.getString(0);
        res.close();
        return stall_name;
    }

    public String getStallAddress(String stall_name){
        SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();
        Cursor res = sqLiteDatabase.rawQuery("SELECT " + STALL_COL_3 + " FROM " + STALL_TABLE_NAME + " WHERE " + STALL_COL_2 + " = " + '"' + stall_name + '"',null);
        res.moveToNext();
        String stall_address = res.getString(0);
        res.close();
        return stall_address;
    }

    public String getStallPostalCode(String stall_name){
        SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();
        Cursor res = sqLiteDatabase.rawQuery("SELECT " + STALL_COL_4 + " FROM " + STALL_TABLE_NAME + " WHERE " + STALL_COL_2 + " = " + '"' + stall_name + '"',null);
        res.moveToNext();
        String stall_postal_code = res.getString(0);
        res.close();
        return stall_postal_code;
    }

    public ArrayList<String> getAllStallPostalCode(){
        SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();
        Cursor res = sqLiteDatabase.rawQuery("SELECT " + STALL_COL_4 + " FROM " + STALL_TABLE_NAME,null);
        ArrayList<String> postalCodeArrayList = new ArrayList<>();
        while(res.moveToNext()){
            postalCodeArrayList.add("Singapore " + res.getString(0));
        }
        res.close();
        return postalCodeArrayList;
    }

    public ArrayList<String> getAllStallName(){
        SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();
        Cursor res = sqLiteDatabase.rawQuery("SELECT " + STALL_COL_2 + " FROM " + STALL_TABLE_NAME,null);
        ArrayList<String> stallNameArrayList = new ArrayList<>();
        while(res.moveToNext()){
            stallNameArrayList.add(res.getString(0));
        }
        res.close();
        return stallNameArrayList;
    }

    public ArrayList<String> getAllStallAddress(){
        SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();
        Cursor res = sqLiteDatabase.rawQuery("SELECT " + STALL_COL_3 + " FROM " + STALL_TABLE_NAME,null);
        ArrayList<String> addressArrayList = new ArrayList<>();
        while(res.moveToNext()){
            addressArrayList.add(res.getString(0));
        }
        res.close();
        return addressArrayList;
    }

    public void preLoadData(){
        SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();
        Cursor res = sqLiteDatabase.rawQuery("SELECT * FROM " + STALL_TABLE_NAME,null);
        if(res.getCount() > 0) {
            Log.d("preLoadData","Data Already Exists");
            return;
        }else{
            Log.d("preLoadData","No Data Yet");
            for(HawkerCentre hawkerCentre : hawkerCentresData){
                addHawkerData(hawkerCentre.stallName,hawkerCentre.stallAddress,hawkerCentre.stallPostalCode);
            }
        }
        res.close();
    }

    private void addHawkerData(String stall_name,String stall_address,String stall_postal_code){
        SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(STALL_COL_2,stall_name);
        contentValues.put(STALL_COL_3,stall_address);
        contentValues.put(STALL_COL_4,stall_postal_code);
        sqLiteDatabase.insert(STALL_TABLE_NAME,null,contentValues);
    }

    ///////////////////////////////////////////////////// DEFAULT DATA /////////////////////////////////////////////////////
    private HawkerCentre[] hawkerCentresData = new HawkerCentre[]{
            new HawkerCentre("Adam Road Food Centre","2, Adam Road, S(289876)","289876"),
            new HawkerCentre("Amoy Street Food Centre","National Development Building, Annex B, Telok Ayer Street, S(069111)","069111"),
            new HawkerCentre("Bedok Food Centre","1, Bedok Road, S(469572)","469572"),
            new HawkerCentre("Beo Crescent Market"	,"38A, Beo Crescent, S(169982)",	"169982"),
            new HawkerCentre("Berseh Food Centre","166, Jalan Besar, S(208877)",	"208877"),
            new HawkerCentre("Bukit Timah Market","51, Upper Bukit Timah Road, S(588215)"	,"588215"),
            new HawkerCentre("Chomp Chomp Food Centre"	,"20, Kensington Park Road, S(557269)",	"557269"),
            new HawkerCentre("Commonwealth Crescent Market","31, Commonwealth Crescent, S(149644)",	"149644"),
            new HawkerCentre("Dunman Food Centre",	"271, Onan Road, S(424768)"	,"424768"),
            new HawkerCentre("East Coast Lagoon Food Village","1220, East Coast Parkway, S(468960)"	,"468960"),
            new HawkerCentre("Geylang Serai Market","Geylang Serai, S(402001)",	"402001"),
            new HawkerCentre("Golden Mile Food Centre"	,"505, Beach Road, S(199583)",	"199583"),
            new HawkerCentre("Holland Village Market & Food Centre","1, Lorong Mambong, S(277700)",	"277700"),
            new HawkerCentre("Kallang Estate Market","17, Old Airport Road, S(397972)"	,"397972"),
            new HawkerCentre("Market Street Food Centre","50, Market Street, Golden Shoe Multi-Storey Car Park, 2nd/3rd Storey, S(048940)",	"048940"),
            new HawkerCentre("Maxwell Food Centre","1, Kadayanallur Street, S(069184)",	"069184"),
            new HawkerCentre("Newton Food Centre","500, Clemenceau Ave North, S(229495)"	,"229495"),
            new HawkerCentre("North Bridge Road Market & Food Centre","861, North Bridge Road, S(198783)",	"198783"),
            new HawkerCentre("Pasir Panjang Food Centre","121, Pasir Panjang Road, S(118543)"	,"118543"),
            new HawkerCentre("Serangoon Garden Market","49A, Serangoon Garden Way, S(555945)"	,"555945"),
            new HawkerCentre("Sembawang Hill Food Centre","590, Upper Thomson Road, S(574419)",	"574419"),
            new HawkerCentre("Taman Jurong Market & Food Centre","3, Yung Sheng Road, S(618499)"	,"618499"),
            new HawkerCentre("Tanglin Halt Market","48A, Tanglin Halt Road, S(148813)",	"148813"),
            new HawkerCentre("Tiong Bahru Market",	"30, Seng Poh Road, S(168898)"	,"168898"),
            new HawkerCentre("Zion Riverside Food Centre","70, Zion Road, S(247792)"	,"247792"),
            new HawkerCentre("ABC Brickworks Market & Food Centre","Blk 6, Jalan Bukit Merah, S(150006)"	,"150006"),
            new HawkerCentre("Albert Centre Market & Food Centre","Blk 270, Queen Street, S(180270)",	"180270"),
            new HawkerCentre("Alexandra Village Food Centre","Blk 120, Bukit Merah Lane, S(150120)"	,"150120"),
            new HawkerCentre("Chinatown Market","Blk 335, Smith Street, S(050335)"	,"050335"),
            new HawkerCentre("Chong Pang Market & Food Centre"	,"Blk 104/105, Yishun Ring Road, S(760104/760105)"	,"760104"),
            new HawkerCentre("Hong Lim Market & Food Centre","Blk 531A, Upper Cross Street, S(051531)",	"051531"),
            new HawkerCentre("Kovan Market & Food Centre","Blk 209, Hougang Street 21, S(530209)",	"530209"),
            new HawkerCentre("Pek Kio Market & Food Centre","Blk 41A, Cambridge Road, S(211041)"	,"211041"),
            new HawkerCentre("People's Park Food Centre","Blk 32, New Market Road, S(050032)",	"050032"),
            new HawkerCentre("Tekka Market","Blk 665, Buffalo Road, S(210665)"	,"210665"),
            new HawkerCentre("Boon Lay Place Market","Blk 221A/B, Boon Lay Place, S(641221/642221)"	,"641221"),
            new HawkerCentre("Jurong West Street 52 Market","Blk 505, Jurong West Street 52, S(640505)"	,"640505"),
            new HawkerCentre("Jalan Kukoh Market","Blk 1, Jalan Kukoh, S(161001)"	,"161001"),
            new HawkerCentre("Hougang Ave 1 Market","Blk 105, Hougang Ave 1, S(530105)"	,"530105"),
            new HawkerCentre("Telok Blangah Crescent Market","Blk 11, Telok Blangah Crescent, S(090011)",	"090011"),
            new HawkerCentre("Jalan Bukit Merah Market","Blk 112, Jalan Bukit Merah, S(160112)"	,"160112"),
            new HawkerCentre("Bukit Merah View Market","Blk 115, Bukit Merah View, S(151115)"	,"151115"),
            new HawkerCentre("Aljunied Ave 2 Market","Blk 117, Aljunied Ave 2, S(380117)"	,"380117"),
            new HawkerCentre("Toa Payoh Lorong 1 Market","Blk 127, Lorong 1 Toa Payoh, S(310127)"	,"310127"),
            new HawkerCentre("Tampines Street 11 Market","Blk 137, Tampines Street 11, S(521137)",	"521137"),
            new HawkerCentre("Mei Chin Road Market","Blk 159, Mei Chin Road, S(140159)"	,"140159"),
            new HawkerCentre("Bedok South Road Market","Blk 16, Bedok South Road, S(460016)",	"460016"),
            new HawkerCentre("Bukit Merah Central Market",	"Blk 163, Bukit Merah Central, S(150163)",	"150163"),
            new HawkerCentre("Upper Boon Keng Road Market"	,"Blk 17, Upper Boon Keng Road, S(380017)"	,"380017"),
            new HawkerCentre("Ghim Moh Road Market","Blk 20, Ghim Moh Road, S(270020)"	,"270020"),
            new HawkerCentre("New Upper Changi Road Market","Blk 208B, New Upper Changi Road, S(462208)"	,"462208"),
            new HawkerCentre("Toa Payoh Lorong 8 Market","Blk 210, Lorong 8 Toa Payoh, S(310210)",	"310210"),
            new HawkerCentre("Bedok North Street 1 Market"	,"Blk 216, Bedok North Street 1, S(460216)"	,"460216"),
            new HawkerCentre("Toa Payoh Lorong 7 Market","Blk 22, Lorong 7 Toa Payoh, S(310022)"	,"310022"),
            new HawkerCentre("Ang Mo Kio Ave 1 Market","Blk 226D, Ang Mo Kio Ave 1, S(564226)",	"564226"),
            new HawkerCentre("Ang Mo Kio Street 22 Market"	,"Blk 226H, Ang Mo Kio Street 22, S(568226)",	"568226"),
            new HawkerCentre("Jurong East Street 24 Market","Blk 254, Jurong East Street 24, S(600254)"	,"600254"),
            new HawkerCentre("Bendemeer Road Market","Blk 29, Bendemeer Road, S(330029)"	,"330029"),
            new HawkerCentre("Shunfu Road Market","Blk 320, Shunfu Road, S(570320)"	,"570320"),
            new HawkerCentre("Ang Mo Kio Ave 1 Market"	,"Blk 341, Ang Mo Kio Ave 1, S(560341)",	"560341"),
            new HawkerCentre("Jurong East Ave 1 Market","Blk 347, Jurong East Ave 1, S(600347)",	"600347"),
            new HawkerCentre("Clementi Ave 2 Market","Blk 353, Clementi Ave 2, S(120353)",	"120353"),
            new HawkerCentre("Telok Blangah Rise Market","Blk 36, Telok Blangah Rise, S(090036)",	"090036"),
            new HawkerCentre("Teban Gardens Road Market","Blk 37A, Teban Garden Road, S(601037)",	"601037"),
            new HawkerCentre("Ang Mo Kio Ave 10 Market","Blk 409, Ang Mo Kio Ave 10, S(560409)",	"560409"),
            new HawkerCentre("Holland Drive Market","Blk 44, Holland Drive, S(270044)",	"270044"),
            new HawkerCentre("Clementi Ave 3 Market","Blk 448, Clementi Ave 3, S(120448)",	"120448"),
            new HawkerCentre("Ang Mo Kio Ave 10 Market","Blk 453A, Ang Mo Kio Ave 10, S(561453)",	"561453"),
            new HawkerCentre("Sims Place Market","Blk 49, Sims Place, S(380049)",	"380049"),
            new HawkerCentre("Eunos Crescent Market","Blk 4A, Eunos Crescent, S(402004)"	,"402004"),
            new HawkerCentre("Jalan Batu Market","Blk 4A, Jalan Batu, S(432004)"	,"432004"),
            new HawkerCentre("Woodlands Centre Road Market","Blk 4A, Woodlands Centre Road, S(731004)"	,"731004"),
            new HawkerCentre("West Cost Drive Market","Blk 502, West Coast Drive, S(120502)",	"120502"),
            new HawkerCentre("Marine Terrace Market","Blk 50A, Marine Terrace, S(441050)",	"441050"),
            new HawkerCentre("Old Airport Road Market","Blk 51, Old Airport Road, S(390051)",	"390051"),
            new HawkerCentre("Bedok North Street 3 Market","Blk 511, Bedok North Street 3, S(460511)",	"460511"),
            new HawkerCentre("Ang Mo Kio Ave 10 Market","Blk 527, Ang Mo Kio Ave 10, S(560527)",	"560527"),
            new HawkerCentre("Bedok North Street 3 Market"	,"Blk 538, Bedok North Street 3, S(460538)",	"460538"),
            new HawkerCentre("New Upper Changi Road Market","Blk 58, New Upper Changi Road, S(461058)",	"461058"),
            new HawkerCentre("Tanjong Pagar Plaza Market","Blk 6, Tanjong Pagar Plaza, S(081006)",	"081006"),
            new HawkerCentre("Ang Mo Kio Ave 4 Market"	,"Blk 628, Ang Mo Kio Ave 4, S(560628)"	,"560628"),
            new HawkerCentre("Bedok Reservoir Road Market"	,"Blk 630, Bedok Reservoir Road, S(470630)"	,"470630"),
            new HawkerCentre("Geylang Bahru Market","Blk 69, Geylang Bahru, S(330069)"	,"330069"),
            new HawkerCentre("Empress Road Market"	,"Blk 7, Empress Road, S(260007)"	,"260007"),
            new HawkerCentre("Ang Mo Kio Ave 6 Market"	,"Blk 724, Ang Mo Kio Ave 6, S(560724)"	,"560724"),
            new HawkerCentre("Clementi West Street 2 Market","Blk 726, Clementi West Street 2, S(120726)"	,"120726"),
            new HawkerCentre("Toa Payoh Lorong 4 Market","Blk 74, Lorong 4 Toa Payoh, S(310074)"	,"310074"),
            new HawkerCentre("Redhill Lane Market","Blk 79, Redhill Lane, S(150079)"	,"150079"),
            new HawkerCentre("Telok Blangah Drive Market","Blk 79, Telok Blangah Drive, S(100079)",	"100079"),
            new HawkerCentre("Circuit Road Market","Blk 80, Circuit Road, S(370080)"	,"370080"),
            new HawkerCentre("Telok Blangah Drive Market","Blk 82, Telok Blangah Drive, S(100082)"	,"100082"),
            new HawkerCentre("Marine Parade Central Market","Blk 84, Marine Parade Central, S(440084)"	,"440084"),
            new HawkerCentre("Bedok North Street 4 Market"	,"Blk 85, Bedok North Street 4, S(460085)"	,"460085"),
            new HawkerCentre("Redhill Lane Market","Blk 85, Redhill Lane, S(150085)"	,"150085"),
            new HawkerCentre("Circuit Road Market"	,"Blk 89, Circuit Road, S(370089)"	,"370089"),
            new HawkerCentre("Whampoa Drive Market","Blk 90, Whampoa Drive, S(320090)"	,"320090"),
            new HawkerCentre("Toa Payoh Lorong 4 Market","Blk 93, Lorong 4 Toa Payoh, S(310093)"	,"310093"),
            new HawkerCentre("Haig Road Market","Blk 13/14, Haig Road, S(430013/430014)"	,"430013"),
            new HawkerCentre("Ang Mo Kio Ave 4 Market","Blk 160/162, Ang Mo Kio Ave 4, S(560160/560162)"	,"560160"),
            new HawkerCentre("Commonwealth Drive Market","Blk 1A/ 2A/ 3A, Commonwealth Drive, S(141001/141002/141003)"	,"141001"),
            new HawkerCentre("Changi Village Road Market","Blk 2/3, Changi Village Road, S(500002/500003)"	,"500002"),
            new HawkerCentre("Marsiling Lane Market","Blk 20/21, Marsiling Lane, S(730020/730021)"	,"730020"),
            new HawkerCentre("Havelock Road Market","Blk 22A/B, Havelock Road, S(161022/162022)"	,"161022"),
            new HawkerCentre("Circuit Road Market"	,"Blk 79/79A, Circuit Road, S(370079/371079)"	,"370079"),
            new HawkerCentre("Whampoa Drive Market","Blk 91/92, Whampoa Drive, S(320091/320092)"	,"320091")};

}
