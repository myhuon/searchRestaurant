package ddwucom.mobile.searchrestaurant;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class FavoriteActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorite);
    }

    private void onClick(View v){
        switch(v.getId()){
            case R.id.btnRemoveSelect:
                // 선택 된 항목 찾아서 favorite 리스트에서 삭제
                // 삭제하시겠습니까 다이얼로그 띄우기

                break;
            case R.id.btnHome:
                Intent intent = new Intent(this, MainActivity.class);
                startActivity(intent);
                break;
        }
    }
}