package com.dlkw.android.logger.sample;

import android.os.Bundle;
import android.os.Environment;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.dlkw.android.logger.Logger;

import java.io.File;
import java.util.Map;
import java.util.WeakHashMap;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Logger.init(new File(getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), "logs"), getApplication().getPackageName() + ".fileprovider");
        Logger.get().d("测试");
        Logger.get().e("测试", new Throwable("测试异常"));

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        findViewById(R.id.send_logs).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Map<String, Object[]> extras = new WeakHashMap<>();
                extras.put("test", new Object[]{"test"});
                extras.put("test2", new Object[]{"test2"});
                Logger.sendLogs(MainActivity.this, extras);
            }
        });
    }
}