package ddwucom.mobile.searchrestaurant;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.libraries.places.api.net.PlacesClient;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class MapActivity extends AppCompatActivity {

    private static final int PERMISSION_RED_CODE = 100;

    private GoogleMap mGoogleMap;
    private LocationManager locManager;
    private Geocoder geocoder;
    private HashMap<Marker, LatLng> markerMap = new HashMap<Marker, LatLng>();

    final static int PERMISSION_REQ_CODE = 100;

    /*UI*/
    private MarkerOptions markerOptions;

    /*DATA*/
    LatLng latLng;
    private PlacesClient placesClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        // 지오코더 객체 얻기
        geocoder = new Geocoder(this, Locale.getDefault());

        // LocationManager 객체 얻기
        locManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        // MapFragment 얻기 (OnMapReadyCallback 메소드 callback으로 호출됨)
        /*MapFragment mapFragment = (MapFragment)getFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(mapReadyCallBack);*/

        checkPermission();
    }

    private void locationUpdate(){
        if(checkPermission()){
            Log.d("checkPermission", "granted");
            locManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 0, locationListener);
        }
    }

    // map 정보를 가져온다.
   /* public void onMapReady(GoogleMap googleMap) {
        mGoogleMap = googleMap;
        markerOptions = new MarkerOptions();

        // 구글맵 자체 현재 위치 표시 기능
        if(checkPermission()) {
            // 내 위치 표시 버튼 활성화
            mGoogleMap.setMyLocationEnabled(true);
        }


    }*/

    // 지도 중심 이동 시키는 버튼 눌렀을 때 동작하는 이벤트 리스너
    GoogleMap.OnMyLocationButtonClickListener locationButtonClickListener = new GoogleMap.OnMyLocationButtonClickListener() {
        @Override
        public boolean onMyLocationButtonClick() {


            return false;
        }
    };

    /*구글맵을 멤버변수로 로딩*/
    private void mapLoad() {
        SupportMapFragment mapFragment =
                (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);

        // 매배변수 this: MainActivity 가 OnMapReadyCallback 을 구현하므로
        //mapFragment.getMapAsync((OnMapReadyCallback) this);
        // implements OnMapReadyCallback 안하면 아래와 같이 OnMapReadyCallback 메소드 구현 해줘야함
        mapFragment.getMapAsync(mapReadyCallBack);
    }

    // 현재 위치 선택했을 때 동작하는 이벤트 리스너
    GoogleMap.OnMyLocationClickListener locationClickListener = new GoogleMap.OnMyLocationClickListener() {
        @Override
        public void onMyLocationClick(@NonNull Location location) {
            String mag = String.format("현재 위치: (%f, %f)", location.getLatitude(), location.getLongitude());

            // 현재 위치 위도, 경도 저장
            latLng = new LatLng(location.getLatitude(), location.getLongitude());
        }
    };

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

    LocationListener locationListener = new LocationListener() {
        @Override
        public void onLocationChanged(@NonNull Location location) {
            // 위치 정보 수신할 때마다 해당 위치로 지도 중심 변경
            double lat = location.getLatitude();
            double lon = location.getLongitude();

            LatLng currentLoc = new LatLng(lat, lon);
            mGoogleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLoc, 17));

            List<String> address = getAddress(lat, lon);

            if (address == null) {
                Toast.makeText(MapActivity.this, "No data", Toast.LENGTH_SHORT).show();
            }
            Toast.makeText(MapActivity.this, "Result: " + address.get(0), Toast.LENGTH_SHORT).show();
        }
    };

    OnMapReadyCallback mapReadyCallBack = new OnMapReadyCallback() {
        @Override
        public void onMapReady(GoogleMap googleMap) {
            mGoogleMap = googleMap;
            Marker centerMarker;

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
                        Toast.makeText(MapActivity.this, "No data", Toast.LENGTH_SHORT).show();
                    }
                    Toast.makeText(MapActivity.this, "Result: " + address.get(0), Toast.LENGTH_LONG).show();


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
}