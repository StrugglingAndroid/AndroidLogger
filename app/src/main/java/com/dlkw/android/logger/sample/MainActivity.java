package com.dlkw.android.logger.sample;

import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.dlkw.android.logger.FileReadyListener;
import com.dlkw.android.logger.Logger;

import java.io.File;
import java.util.Map;
import java.util.WeakHashMap;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Logger.init(new File(getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), "logs"));
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
                Logger.sendLogs(MainActivity.this, extras, new FileReadyListener() {
                    @Override
                    public void onReady(File zipFile) {
                        Intent intent = new Intent(Intent.ACTION_SEND);
                        intent.setType("application/zip");
                        intent.putExtra(Intent.EXTRA_TITLE, "日志");
                        intent.putExtra(Intent.EXTRA_SUBJECT, "日志");
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_GRANT_READ_URI_PERMISSION);
                        intent.putExtra(Intent.EXTRA_STREAM, FileProvider.getUriForFile(getApplicationContext(), getApplication().getPackageName() + ".fileprovider", zipFile));
                        startActivity(Intent.createChooser(intent, "发送日志"));
                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace(System.err);
                        Toast.makeText(MainActivity.this, "日志发送出错", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }
}