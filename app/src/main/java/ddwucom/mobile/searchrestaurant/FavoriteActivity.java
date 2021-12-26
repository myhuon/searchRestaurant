package ddwucom.mobile.searchrestaurant;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

public class FavoriteActivity extends AppCompatActivity {

    public final String TAG = "FavoriteActivity";
    public static final String CHANNEL_ID = "200";

    private ListView lvAreas = null;
    private final static String DETAIL_CODE = "updateItem";

    private MyCursorAdapter adapter;
    private AreaDBHelper helper;

    private Intent intent;
    Cursor cursor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorite);

        //constraintLayout = getLayoutInflater(R.layout.favorite_list_layout);
        lvAreas = (ListView) findViewById(R.id.lvHeartList);

        helper = new AreaDBHelper(this);
        //areaList = new ArrayList<AreaDto>();

        // cursor null로 초기화
        adapter = new MyCursorAdapter(this, R.layout.search_list_layout, null);
            lvAreas.setAdapter(adapter);

        // 아이템 롱클릭시 아이템 삭제 여부 다이얼로그 띄운다. 확인 -> 삭제
            lvAreas.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {

                android.app.AlertDialog.Builder dlg = new AlertDialog.Builder(FavoriteActivity.this);
                dlg.setTitle("삭제")
                        .setMessage("선택항목을 관심목록에서 삭제 하시겠습니까?")
                        .setPositiveButton("확인", new DialogInterface.OnClickListener() {
                            @Override
                            // 확인 -> 알림 띄우기
                            public void onClick(DialogInterface dialog, int which) {
                                // 디비 관심목록 테이블에서 삭제하기
                                //삭제
                                SQLiteDatabase db = helper.getWritableDatabase();
                                db.execSQL("DELETE FROM " + AreaDBHelper.TABLE_NAME + " WHERE _id = " + id + ";" );
                                Log.d(TAG, "remove item success");
                                onResume();

                                //Intent intent = new Intent(this, DetailViewActivity.class);
                                //intent.setFlags(intent.FLAG_ACTIVITY_NEW_TASK | intent.FLAG_ACTIVITY_CLEAR_TASK);
                                //PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);

                                NotificationCompat.Builder builder = new NotificationCompat.Builder(FavoriteActivity.this, CHANNEL_ID)
                                        .setSmallIcon(R.mipmap.empty_heart)
                                        .setContentTitle("삭제")
                                        .setContentText("선택 항목을 관심목록에서 삭제하였습니다.")
                                        .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                                        //.setContentIntent(pendingIntent)
                                        .setAutoCancel(true);

                                NotificationManagerCompat notificationManager = NotificationManagerCompat.from(FavoriteActivity.this);

                                int notificationId = 200;
                                notificationManager.notify(notificationId, builder.build());
                            }
                        })
                        .setNegativeButton("취소", new DialogInterface.OnClickListener() {
                            @Override
                            // 취소 -> Toast 띄우기
                            public void onClick(DialogInterface dialog, int which) {
                                Toast.makeText(FavoriteActivity.this, "취소되었습니다.", Toast.LENGTH_SHORT).show();
                            }
                        })
                        .show();

                return true;
            }
        });

        // 아이템 클릭시 내용 디테일 액티비티로 전환
            lvAreas.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                intent = new Intent(FavoriteActivity.this, DetailViewActivity.class);
                intent.putExtra(DETAIL_CODE, id);
                startActivity(intent);
            }
        });

            helper.close();
    }

    @Override
    protected void onResume() {
        super.onResume();

        //DB에서 데이터를 읽어와 Adapter에 설정
        SQLiteDatabase db = helper.getReadableDatabase();

        //Cursor cursor = db.query(ContactDBHelper.TABLE_NAME, null, null, null, null, null, null);
        cursor = db.rawQuery("select * from " + AreaDBHelper.TABLE_NAME, null);
        // cursor 바꿔주기
        adapter.changeCursor(cursor);

        helper.close();
    }

    protected void onDestroy() {
        super.onDestroy();
        // cursor 사용 종료 ( 실행 중에 커서 닫으면 db 사용 할 수 없기 때문에 앱이 닫히는 onDestroy()에서 커서 close 해주는 것이다. )
        if(cursor != null) cursor.close();
    }

    public void onClick(View v){
        switch(v.getId()){
            /*case R.id.btnRemoveSelect:
                // 선택 된 항목 찾아서 favorite 리스트에서 삭제
                // 삭제하시겠습니까 다이얼로그 띄우기
                android.app.AlertDialog.Builder dlg = new AlertDialog.Builder(FavoriteActivity.this);
                dlg.setTitle("삭제")
                        .setMessage("선택항목을 관심목록에서 삭제 하시겠습니까?")
                        .setPositiveButton("확인", new DialogInterface.OnClickListener() {
                            @Override
                            // 확인 -> 알림 띄우기
                            public void onClick(DialogInterface dialog, int which) {
                                // 디비 관심목록 테이블에서 삭제하기
                                //삭제
                                SQLiteDatabase db = helper.getWritableDatabase();
                                db.execSQL("DELETE FROM " + AreaDBHelper.TABLE_NAME + " WHERE _id = " + id + ";" );
                                Log.d(TAG, "remove item success");
                                onResume();

                                //Intent intent = new Intent(this, DetailViewActivity.class);
                                //intent.setFlags(intent.FLAG_ACTIVITY_NEW_TASK | intent.FLAG_ACTIVITY_CLEAR_TASK);
                                //PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);

                                NotificationCompat.Builder builder = new NotificationCompat.Builder(FavoriteActivity.this, CHANNEL_ID)
                                        .setSmallIcon(R.mipmap.empty_heart)
                                        .setContentTitle("삭제")
                                        .setContentText("선택 항목을 관심목록에서 삭제하였습니다.")
                                        .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                                        //.setContentIntent(pendingIntent)
                                        .setAutoCancel(true);

                                NotificationManagerCompat notificationManager = NotificationManagerCompat.from(FavoriteActivity.this);

                                int notificationId = 200;
                                notificationManager.notify(notificationId, builder.build());
                            }
                        })
                        .setNegativeButton("취소", new DialogInterface.OnClickListener() {
                            @Override
                            // 취소 -> Toast 띄우기
                            public void onClick(DialogInterface dialog, int which) {
                                Toast.makeText(FavoriteActivity.this, "취소되었습니다.", Toast.LENGTH_SHORT).show();
                            }
                        })
                        .show();


                break;*/
            case R.id.btnHome:
                Intent intent = new Intent(this, MainActivity.class);
                startActivity(intent);
                break;
        }
    }
}