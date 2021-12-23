package ddwucom.mobile.searchrestaurant;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.PhotoMetadata;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.FetchPlaceRequest;
import com.google.android.libraries.places.api.net.FetchPlaceResponse;
import com.google.android.libraries.places.api.net.PlacesClient;

import java.util.Arrays;
import java.util.List;

import noman.googleplaces.NRPlaces;
import noman.googleplaces.PlaceType;
import noman.googleplaces.PlacesException;
import noman.googleplaces.PlacesListener;

public class SearchRestaurantActivity extends AppCompatActivity {

    private final String TAG = "Search";
    final static int PERMISSION_REQ_CODE = 100;

    /*UI*/
    private EditText etKeyword;
    private GoogleMap mGoogleMap;
    private MarkerOptions markerOptions;

    /*DATA*/
    LatLng latLng;
    private PlacesClient placesClient;

    private Intent intent;

    private ListView lvAreas = null;
    private final static String DETAIL_CODE = "updateItem";

    private MyCursorAdapter adapter;
    private AreaDBHelper helper;

    Cursor cursor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_restaurant);

        etKeyword = findViewById(R.id.etSearch);

        // Places 객체 api 키로 초기화
        Places.initialize(getApplicationContext(), getString(R.string.api_key));
        // 초기화 한 Places 객체로 클라이언트를 만든다.
        placesClient = Places.createClient(this);

        //constraintLayout = getLayoutInflater(R.layout.favorite_list_layout);
        lvAreas = (ListView) findViewById(R.id.lvMain);

        helper = new AreaDBHelper(this);
        //areaList = new ArrayList<AreaDto>();

        // cursor null로 초기화
        adapter = new MyCursorAdapter(this, R.layout.search_list_layout, null);
        lvAreas.setAdapter(adapter);

        // 아이템 롱클릭시 아이템 삭제 여부 다이얼로그 띄운다. 확인 -> 삭제
        /*lvAreas.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {

                android.app.AlertDialog.Builder dlg = new AlertDialog.Builder(SearchRestaurantActivity.this);
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
        });*/

        // 아이템 클릭시 내용 디테일 액티비티로 전환
        lvAreas.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                intent = new Intent(SearchRestaurantActivity.this, DetailViewActivity.class);
                intent.putExtra(DETAIL_CODE, id);
                startActivity(intent);
            }
        });

        helper.close();
    }

    private void onClick(View v){
        switch(v.getId()){
            case R.id.btnSearch:
                // PlaceType 지정
                if (etKeyword.getText().toString().equals("카페")) {
                    searchStart(PlaceType.CAFE);
                } else if (etKeyword.getText().toString().equals("식당")) {
                    searchStart(PlaceType.RESTAURANT);
                }

                break;
            case R.id.btnSaveList:
                intent = new Intent(this, FavoriteActivity.class);
                startActivity(intent);
                break;

            case R.id.btnGoMap:
                intent = new Intent(this, MapActivity.class);
                startActivity(intent);
                break;

        }
    }

    /*입력된 유형의 주변 정보를 검색*/
    private void searchStart(String type) {
        // NRPlaces.Builder()로 NRPlaces 객체 만듦 -> .listener()에 리스너 설정한다. 그 외에 키, 위도경도, 반경, 타입 설정 -> build() -> execute()
        new NRPlaces.Builder().listener(placesListener)
                .key(getString(R.string.api_key))
                .latlng(Double.parseDouble(getString(R.string.init_lat)), Double.parseDouble(getString(R.string.init_lng)))
                .radius(100)
                .type(type)
                .build()
                .execute();
    }

    public void onInfoWindowClick(final Marker marker){
        // getTag로 마커에 저장된 PlaceId 가져온다.
        String placeId = marker.getTag().toString();
        getPlaceDetail(placeId);
    }

    /*Place ID 의 장소에 대한 세부정보 획득*/
    private void getPlaceDetail(String placeId) {
        // Place.Field 타입의 세부정보 설정한다.
        List<Place.Field> placeField = Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.PHONE_NUMBER, Place.Field.ADDRESS, Place.Field.PHOTO_METADATAS
                                                Place.Field.LAT_LNG, Place.Field.WEBSITE_URI);
        // placeId와 세부정보를 설정한 placeField를 설정한 FetchPlaceRequest.builder()로 FetchPlaceRequest 객체를 생성하고 build() 한다.
        FetchPlaceRequest request = FetchPlaceRequest.builder(placeId, placeField).build();

        // 생성한 placesClient에 fetchPlace()에 매개변수로 request를 설정한다.
        // 성공하면 .addOnSuccessListener에 FetchPlaceResponse가 매개변수로 넘어온다.
        placesClient.fetchPlace(request).addOnSuccessListener(new OnSuccessListener<FetchPlaceResponse>() {
            @Override
            public void onSuccess(FetchPlaceResponse fetchPlaceResponse) {
                // fetchPlaceResponse.getPlace()로 장소 획득 -> 얻은 장소로 세부 정보 꺼낸다.
                Place place = fetchPlaceResponse.getPlace();
                callDetailActivity(place);
            }
        }).addOnFailureListener(new OnFailureListener() { // 요청 실패
            @Override
            public void onFailure(@NonNull Exception exception) {
                if(exception instanceof ApiException){
                    ApiException apiException = (ApiException) exception;
                    int statusCode = apiException.getStatusCode(); // 예외 상태 코드 보기
                    Log.e(TAG, "Place not found: " + exception.getMessage());
                }
            }
        });

    }

    private void callDetailActivity(Place place) {
        // Get the photo metadata.
        // 사진 정보 얻기
        final List<PhotoMetadata> metadata = place.getPhotoMetadatas();
        if (metadata == null || metadata.isEmpty()) {
            Log.w(TAG, "No photo metadata.");
            return;
        }
        final PhotoMetadata photoMetadata = metadata.get(0);

        Intent intent = new Intent(SearchRestaurantActivity.this, DetailViewActivity.class);
        intent.putExtra("name",place.getName());
        intent.putExtra("phone",place.getPhoneNumber());
        intent.putExtra("address",place.getAddress());
        intent.putExtra("photo", photoMetadata);
        intent.putExtra("lat_lng", place.getLatLng());
        intent.putExtra("websiteUri", place.getWebsiteUri());

        startActivity(intent);
    }

    // NRPlaces 객체에 정한 정보 받는 리스너
    PlacesListener placesListener = new PlacesListener() {

        @Override
        // Google Place API에도 Place 클래스가 있으므로 noman.googleplaces.Place 풀 패키지명을 적는다.
        // 스레드 사용 할 것이므로 final로 상수 지정해줌
        public void onPlacesSuccess(final List<noman.googleplaces.Place> places) {
            // UI 요소에 접근하는 것은 스레드로 분리한다.
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    for(noman.googleplaces.Place place : places){
                        // place에 담긴 정보 가져와서 마커 옵션으로 설정한다.
                        markerOptions.title(place.getName())
                                .position(new LatLng(place.getLatitude(), place.getLongitude()))
                                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));

                        // 설정한 마커 옵션을 넣은 마커를 추가한다.
                        Marker newMarker = mGoogleMap.addMarker(markerOptions);
                        // 생성한 마커에 PlaceId를 Tag에 설정한다. marker.setTag(place.getPlaceId());
                        // 장소 세부정보 사용하려면 필수로 설정해줌
                        // Google Place에서 marker.getTag()로 PlaceId 가져와 사용함.
                        newMarker.setTag(place.getPlaceId());

                        Log.d(TAG, "ID: " + place.getPlaceId());
                    }
                }
            });
        }

        @Override
        public void onPlacesFailure(PlacesException e) {

        }

        @Override
        public void onPlacesStart() {

        }

        @Override
        public void onPlacesFinished() {

        }
    };


}