package ddwucom.mobile.searchrestaurant;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;

public class DetailViewActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_view);
    }

    private void onClick(View v){
        switch(v.getId()){
            case R.id.btnSaveMemo:
                // 메모 디비에 저장하기
                // 저장하시겠습니까? 다이얼로그 띄우기
                break;
        }
    }
}