package ddwucom.mobile.searchrestaurant;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.maps.CameraUpdateFactory;
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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

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
    private LocationManager locManager;
    private Geocoder geocoder;
    private HashMap<Marker, LatLng> markerMap = new HashMap<Marker, LatLng>();

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

        // 지오코더 객체 얻기
        geocoder = new Geocoder(this, Locale.getDefault());

        // LocationManager 객체 얻기
        locManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        // MapFragment 얻기 (OnMapReadyCallback 메소드 callback으로 호출됨)
        /*MapFragment mapFragment = (MapFragment)getFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(mapReadyCallBack);*/

        checkPermission();

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

    public void onClick(View v){
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

            case R.id.btnNearRestaurant:
                intent = new Intent(this, DetailViewActivity.class);
                startActivity(intent);
                break;
      /*      case R.id.btnGoMap:
                intent = new Intent(this, MapActivity.class);
                startActivity(intent);
                break;
*/
        }
    }

    /*입력된 유형의 주변 정보를 검색*/
    private void searchStart(String type) {
        // NRPlaces.Builder()로 NRPlaces 객체 만듦 -> .listener()에 리스너 설정한다. 그 외에 키, 위도경도, 반경, 타입 설정 -> build() -> execute()
        new NRPlaces.Builder().listener(placesListener)
                .key(getString(R.string.api_key))
                .latlng(Double.parseDouble(getString(R.string.init_lat)), Double.parseDouble(getString(R.string.init_lng)))
                .radius(500)
                .type(type)
                .build()
                .execute();
    }

    /*Place ID 의 장소에 대한 세부정보 획득*/
    private void getPlaceDetail(String placeId) {
        // Place.Field 타입의 세부정보 설정한다.
        List<Place.Field> placeField = Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.PHONE_NUMBER, Place.Field.ADDRESS, Place.Field.PHOTO_METADATAS,
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

    // map 정보를 가져온다.
    OnMapReadyCallback mapReadyCallBack = new OnMapReadyCallback() {
        @Override
        public void onMapReady(GoogleMap googleMap) {
            mGoogleMap = googleMap;
            Marker centerMarker;

            Toast.makeText(SearchRestaurantActivity.this, "callback", Toast.LENGTH_SHORT).show();


            // 초기 위치 설정 - (onMapReady에 설정 했으니까)
            LatLng currentLoc = new LatLng(37.606320, 127.041808);
            mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLoc, 17)); // 카메라 배율 = 17
            //     mGoogleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLoc, 17));

            MarkerOptions options = new MarkerOptions();
            options.position(currentLoc)
                    .title(String.format("현재위치"))
                    .icon(BitmapDescriptorFactory.defaultMarker());
            // .icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_launcher));
            // .icon(BitmapDescriptorFactory.HUE_AZURE));

            // 지도에 옵션 설정한 마커 추가하기 (마커 반환)
            centerMarker = mGoogleMap.addMarker(options);
            // 각 마커 이용하기 위해 (마커, 위치) HashMap<Marker, LatLng>에 추가
            markerMap.put(centerMarker, currentLoc);
            // 마커 화면에 표시
            centerMarker.showInfoWindow();

            // 지도 롱클릭 이벤트
            mGoogleMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener(){
                public void onMapLongClick(LatLng latLng){
                    MarkerOptions options = new MarkerOptions();
                    // 롱클릭 한 위치로 position(latLng) 설정
                    options.position(latLng)
                            .title(String.format("Latitude: %.6f\nLongitude: %.6f", latLng.latitude, latLng.longitude))
                            //.snippet("이동중")
                            .icon(BitmapDescriptorFactory.defaultMarker());

                    Marker newMarker = mGoogleMap.addMarker(options);
                    markerMap.put(newMarker, latLng);
                    newMarker.showInfoWindow();
                }
            });

            // 마커 클릭 이벤트
            mGoogleMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
                @Override
                public void onInfoWindowClick(Marker marker) {
                    //locationUpdate();

                    // HashMap<key = 마커, value = LatLng>이므로 .get(marker)로 현재 마커의 LatLng을 알아낸다.
                    LatLng markerLat = markerMap.get(marker);

                    // 지오 코딩으로 주소 알아내기
                    List<String> address = getAddress(markerLat.latitude, markerLat.longitude);

                    if (address == null) {
                        Toast.makeText(SearchRestaurantActivity.this, "No data", Toast.LENGTH_SHORT).show();
                    }
                    Toast.makeText(SearchRestaurantActivity.this, "Result: " + address.get(0), Toast.LENGTH_LONG).show();

                    String placeId = marker.getTag().toString();
                    getPlaceDetail(placeId);
                }
            });

            // 지도 클릭 이벤트
//            mGoogleMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
//                @Override
//                public void onMapClick(LatLng latLng) {
//                    Toast.makeText(MainActivity.this, "지도 클릭 이벤트 실행!!", Toast.LENGTH_SHORT).show();
//                }
//            });

            // 구글맵 자체 현재 위치 표시 기능
            if(checkPermission()) {
                // 내 위치 표시 버튼 활성화
                mGoogleMap.setMyLocationEnabled(true);
            }

            // 내 위치 확인 버튼 클릭 처리 리스너
            mGoogleMap.setOnMyLocationButtonClickListener(locationButtonClickListener);
            // 지도 상의 현재 위치 아이콘 클릭 처리 리스너
            mGoogleMap.setOnMyLocationClickListener(locationClickListener);
        }
    };

    //    Geocoding
    private List<String> getAddress(double latitude, double longitude) {

        List<Address> addresses = null;
        ArrayList<String> addressFragments = null;

//        위도/경도에 해당하는 주소 정보를 Geocoder 에게 요청
        try {
            // 위치로 지오코더 주소 값 알아내기
            addresses = geocoder.getFromLocation(latitude, longitude, 1);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }

        if (addresses == null || addresses.size()  == 0) {
            return null;
        } else {
            Address addressList = addresses.get(0);
            addressFragments = new ArrayList<String>();

            for(int i = 0; i <= addressList.getMaxAddressLineIndex(); i++) {
                addressFragments.add(addressList.getAddressLine(i));
            }
        }

        return addressFragments;
    }


    //    Reverse geocoding (주소 -> 위도, 경도)
    private List<LatLng> getLatLng(String targetLocation) {

        List<Address> addresses = null;
        ArrayList<LatLng> addressFragments = null;

//        주소에 해당하는 위도/경도 정보를 Geocoder 에게 요청
        try {
            // 주소로 위치 알아내기
            addresses = geocoder.getFromLocationName(targetLocation, 1);
        } catch (IOException e) { // Catch network or other I/O problems.
            e.printStackTrace();
        } catch (IllegalArgumentException e) { // Catch invalid address values.
            e.printStackTrace();
        }

        if (addresses == null || addresses.size()  == 0) {
            return null;
        } else {
            Address addressList = addresses.get(0);
            addressFragments = new ArrayList<LatLng>();

            for(int i = 0; i <= addressList.getMaxAddressLineIndex(); i++) {
                // addressList에서 위도, 경도 알아내서 LatLng에 저장 -> LatLng을 addressFragments에 추가
                LatLng latLng = new LatLng(addressList.getLatitude(), addressList.getLongitude());
                addressFragments.add(latLng);
            }
        }

        return addressFragments;
    }

    // 지도 중심 이동 시키는 버튼 눌렀을 때 동작하는 이벤트 리스너
    GoogleMap.OnMyLocationButtonClickListener locationButtonClickListener = new GoogleMap.OnMyLocationButtonClickListener() {
        @Override
        public boolean onMyLocationButtonClick() {


            return false;
        }
    };

    // 현재 위치 선택했을 때 동작하는 이벤트 리스너
    GoogleMap.OnMyLocationClickListener locationClickListener = new GoogleMap.OnMyLocationClickListener() {
        @Override
        public void onMyLocationClick(@NonNull Location location) {
            String mag = String.format("현재 위치: (%f, %f)", location.getLatitude(), location.getLongitude());

            // 현재 위치 위도, 경도 저장
            latLng = new LatLng(location.getLatitude(), location.getLongitude());
        }
    };


    /*구글맵을 멤버변수로 로딩*/
    private void mapLoad() {
        SupportMapFragment mapFragment =
                (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);

        // 매배변수 this: MainActivity 가 OnMapReadyCallback 을 구현하므로
        //mapFragment.getMapAsync((OnMapReadyCallback) this);
        // implements OnMapReadyCallback 안하면 아래와 같이 OnMapReadyCallback 메소드 구현 해줘야함
        Toast.makeText(SearchRestaurantActivity.this, "mapLoad", Toast.LENGTH_SHORT).show();

        mapFragment.getMapAsync(mapReadyCallBack);
    }



    /* 필요 permission 요청 */
    private boolean checkPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[] {Manifest.permission.ACCESS_FINE_LOCATION},
                        PERMISSION_REQ_CODE);
                return false;
            }
        }
        return true;
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQ_CODE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // 퍼미션을 획득하였을 경우 맵 로딩 실행
                mapLoad();
            } else {
                // 퍼미션 미획득 시 액티비티 종료
                Toast.makeText(this, "앱 실행을 위해 권한 허용이 필요함", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }
}