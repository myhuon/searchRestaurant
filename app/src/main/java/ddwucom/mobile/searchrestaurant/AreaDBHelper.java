package ddwucom.mobile.searchrestaurant;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class AreaDBHelper extends SQLiteOpenHelper {

    private final String TAG = "AreaDBHelper";

    private final static String DB_NAME = "area_db";
    public final static String TABLE_NAME = "area_table";
    public final static String COL_NAME = "name";
    public final static String COL_PHONE = "phone";
    public final static String COL_ADDRESS = "address";
    public final static String COL_MEMO = "memo";
    public final static String COL_IMGPATH = "img_path"; // 사진이 있는 경로
    public final static String COL_LATLNG = "latlng";
    public final static String COL_WEBSITEURI = "websiteUri";

    // db 파일명, 버전 설정
    public AreaDBHelper(Context context) {
        super(context, DB_NAME, null, 1);
    }

    // db 테이블 생성
    @Override
    public void onCreate(SQLiteDatabase db) {
        String creatSql = "create table " + TABLE_NAME + " (_id integer primary key autoincrement, "
                + COL_NAME + " TEXT, " + COL_PHONE + " TEXT, " + COL_ADDRESS + " TEXT, " + COL_MEMO + " TEXT, "
                + COL_IMGPATH + " TEXT, " + COL_LATLNG + " TEXT, " + COL_WEBSITEURI + " TEXT);";
        Log.d(TAG, creatSql);
        db.execSQL(creatSql);

        db.execSQL("insert into " + TABLE_NAME + " values (null, '토리돈가스', '02-919-9617', '서울 성북구 화랑로13길 24', '돈가스맛집', 'null', 'null', 'null');");
        db.execSQL("insert into " + TABLE_NAME + " values (null, '청년고기장수', '02-942-8090', '서울 성북구 화랑로13길 8', '고기맛집', 'null', 'null', 'null');");
        db.execSQL("insert into " + TABLE_NAME + " values (null, '백소정', '050713653437', '서울 성북구 화랑로11길 11-3', '일식당', 'null', 'null', 'null');");
    }

    // db 업그레이드 시 자동 호출 ( 버전 달라지면 )
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }
}
