package ddwucom.mobile.searchrestaurant;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import java.util.Map;

public class SearchRestaurantActivity extends AppCompatActivity {

    private Intent intent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_restaurant);
    }

    private void onClick(View v){
        switch(v.getId()){
            case R.id.btnFavoriteList:
                intent = new Intent(this, FavoriteActivity.class);
                startActivity(intent);
                break;
            case R.id.btnCurrentLocation:
                // 현재 위치 찾아서 기준 현재 위치로 바꾸기
                break;
            case R.id.btnGoMap:
                intent = new Intent(this, MapActivity.class);
                startActivity(intent);
                break;

        }
    }
}